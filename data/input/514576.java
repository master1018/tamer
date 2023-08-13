public final class RenderView extends GLSurfaceView implements GLSurfaceView.Renderer, SensorEventListener {
    private static final String TAG = "RenderView";
    private static final int NUM_TEXTURE_LOAD_THREADS = 4;
    private static final int MAX_LOADING_COUNT = 8;
    private static final int EVENT_NONE = 0;
    private static final int EVENT_KEY = 2;
    private static final int EVENT_FOCUS = 3;
    private SensorManager mSensorManager;
    private GL11 mGL = null;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private RootLayer mRootLayer = null;
    private boolean mListsDirty = false;
    private static final Lists sLists = new Lists();
    private Layer mTouchEventTarget = null;
    private int mCurrentEventType = EVENT_NONE;
    private KeyEvent mCurrentKeyEvent = null;
    private boolean mCurrentKeyEventResult = false;
    private volatile boolean mPendingSensorEvent = false;
    private int mLoadingCount = 0;
    private static final Deque<Texture> sLoadInputQueue = new Deque<Texture>();
    private static final Deque<Texture> sLoadInputQueueCached = new Deque<Texture>();
    private static final Deque<Texture> sLoadInputQueueVideo = new Deque<Texture>();
    private static final Deque<Texture> sLoadOutputQueue = new Deque<Texture>();
    private static TextureLoadThread sCachedTextureLoadThread = null;
    private static TextureLoadThread sVideoTextureLoadThread = null;
    private static final TextureLoadThread[] sTextureLoadThreads = new TextureLoadThread[NUM_TEXTURE_LOAD_THREADS];
    private final Deque<MotionEvent> mTouchEventQueue = new Deque<MotionEvent>();
    private final DirectLinkedList<TextureReference> mActiveTextureList = new DirectLinkedList<TextureReference>();
    @SuppressWarnings("unchecked")
    private final ReferenceQueue mUnreferencedTextureQueue = new ReferenceQueue();
    private long mFrameTime = 0;
    private float mFrameInterval = 0.0f;
    private float mAlpha;
    private long mLoadingExpensiveTexturesStartTime = 0;
    private final SparseArray<ResourceTexture> sCacheScaled = new SparseArray<ResourceTexture>();
    private final SparseArray<ResourceTexture> sCacheUnscaled = new SparseArray<ResourceTexture>();
    private boolean mFirstDraw;
    private Texture mBoundTexture;
    private static final class TextureReference extends WeakReference<Texture> {
        @SuppressWarnings("unchecked")
        public TextureReference(Texture texture, GL11 gl, ReferenceQueue referenceQueue, int textureId) {
            super(texture, referenceQueue);
            this.textureId = textureId;
            this.gl = gl;
        }
        public final int textureId;
        public final GL11 gl;
        public final DirectLinkedList.Entry<TextureReference> activeListEntry = new DirectLinkedList.Entry<TextureReference>(this);
    }
    public static final class Lists {
        public final ArrayList<Layer> updateList = new ArrayList<Layer>();
        public final ArrayList<Layer> opaqueList = new ArrayList<Layer>();
        public final ArrayList<Layer> blendedList = new ArrayList<Layer>();
        public final ArrayList<Layer> hitTestList = new ArrayList<Layer>();
        public final ArrayList<Layer> systemList = new ArrayList<Layer>();
        void clear() {
            updateList.clear();
            opaqueList.clear();
            blendedList.clear();
            hitTestList.clear();
            systemList.clear();
        }
    }
    public RenderView(final Context context) {
        super(context);
        setBackgroundDrawable(null);
        setFocusable(true);
        setRenderer(this);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sCachedTextureLoadThread == null) {
            for (int i = 0; i != NUM_TEXTURE_LOAD_THREADS; ++i) {
                TextureLoadThread thread = new TextureLoadThread();
                if (i == 0) {
                    sCachedTextureLoadThread = thread;
                }
                if (i == 1) {
                    sVideoTextureLoadThread = thread;
                }
                sTextureLoadThreads[i] = thread;
                thread.start();
            }
        }
    }
    public void setRootLayer(RootLayer layer) {
        if (mRootLayer != layer) {
            mRootLayer = layer;
            mListsDirty = true;
            if (layer != null) {
                mRootLayer.setSize(mViewWidth, mViewHeight);
            }
        }
    }
    public ResourceTexture getResource(int resourceId) {
        return getResourceInternal(resourceId, true);
    }
    public ResourceTexture getResource(int resourceId, boolean scaled) {
        return getResourceInternal(resourceId, scaled);
    }
    private ResourceTexture getResourceInternal(int resourceId, boolean scaled) {
        final SparseArray<ResourceTexture> cache = (scaled) ? sCacheScaled : sCacheUnscaled;
        ResourceTexture texture = cache.get(resourceId);
        if (texture == null && resourceId != 0) {
            texture = new ResourceTexture(resourceId, scaled);
            cache.put(resourceId, texture);
        }
        return texture;
    }
    public void clearCache() {
        clearTextureArray(sCacheScaled);
        clearTextureArray(sCacheUnscaled);
    }
    private void clearTextureArray(SparseArray<ResourceTexture> array) {
        array.clear();
    }
    public long getFrameTime() {
        return mFrameTime;
    }
    public float getFrameInterval() {
        return mFrameInterval;
    }
    public void prime(Texture texture, boolean highPriority) {
        if (texture != null && texture.mState == Texture.STATE_UNLOADED && (highPriority || mLoadingCount < MAX_LOADING_COUNT)) {
            queueLoad(texture, highPriority);
        }
    }
    public void loadTexture(Texture texture) {
        if (texture != null) {
            switch (texture.mState) {
            case Texture.STATE_UNLOADED:
            case Texture.STATE_QUEUED:
                int[] textureId = new int[1];
                texture.mState = Texture.STATE_LOADING;
                loadTextureAsync(texture);
                uploadTexture(texture, textureId);
                break;
            }
        }
    }
    private void loadTextureAsync(Texture texture) {
        try {
            Bitmap bitmap = texture.load(this);
            if (bitmap != null) {
                bitmap = Utils.resizeBitmap(bitmap, 1024);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                texture.mWidth = width;
                texture.mHeight = height;
                if (!Shared.isPowerOf2(width) || !Shared.isPowerOf2(height)) {
                    int paddedWidth = Shared.nextPowerOf2(width);
                    int paddedHeight = Shared.nextPowerOf2(height);
                    Bitmap.Config config = bitmap.getConfig();
                    if (config == null)
                        config = Bitmap.Config.RGB_565;
                    if (width * height >= 512 * 512)
                        config = Bitmap.Config.RGB_565;
                    Bitmap padded = Bitmap.createBitmap(paddedWidth, paddedHeight, config);
                    Canvas canvas = new Canvas(padded);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    bitmap.recycle();
                    bitmap = padded;
                    texture.mNormalizedWidth = (float) width / (float) paddedWidth;
                    texture.mNormalizedHeight = (float) height / (float) paddedHeight;
                } else {
                    texture.mNormalizedWidth = 1.0f;
                    texture.mNormalizedHeight = 1.0f;
                }
            }
            texture.mBitmap = bitmap;
        } catch (Exception e) {
            texture.mBitmap = null;
        } catch (OutOfMemoryError eMem) {
            Log.i(TAG, "Bitmap power of 2 creation fail, outofmemory");
            handleLowMemory();
        }
    }
    public boolean bind(Texture texture) {
        if (texture != null) {
            if (texture == mBoundTexture)
                return true;
            switch (texture.mState) {
            case Texture.STATE_UNLOADED:
                if (texture.getClass().equals(ResourceTexture.class)) {
                    loadTexture(texture);
                    return false;
                }
                if (mLoadingCount < MAX_LOADING_COUNT) {
                    queueLoad(texture, false);
                }
                break;
            case Texture.STATE_LOADED:
                mGL.glBindTexture(GL11.GL_TEXTURE_2D, texture.mId);
                mBoundTexture = texture;
                return true;
            default:
                break;
            }
        }
        return false;
    }
    public void setAlpha(float alpha) {
        GL11 gl = mGL;
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        gl.glColor4f(alpha, alpha, alpha, alpha);
        mAlpha = alpha;
    }
    public float getAlpha() {
        return mAlpha;
    }
    public void setColor(float red, float green, float blue, float alpha) {
        GL11 gl = mGL;
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        gl.glColor4f(red, green, blue, alpha);
        mAlpha = alpha;
    }
    public void resetColor() {
        GL11 gl = mGL;
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        gl.glColor4f(1, 1, 1, 1);
    }
    public boolean isLoadingExpensiveTextures() {
        return mLoadingExpensiveTexturesStartTime != 0;
    }
    public long elapsedLoadingExpensiveTextures() {
        long startTime = mLoadingExpensiveTexturesStartTime;
        if (startTime != 0) {
            return SystemClock.uptimeMillis() - startTime;
        } else {
            return -1;
        }
    }
    private void queueLoad(final Texture texture, boolean highPriority) {
        if (!texture.shouldQueue()) {
            return;
        }       
        texture.mState = Texture.STATE_LOADING;
        Deque<Texture> inputQueue = (texture.isUncachedVideo()) ? sLoadInputQueueVideo
                : (texture.isCached()) ? sLoadInputQueueCached : sLoadInputQueue;
        ;
        synchronized (inputQueue) {
            if (highPriority) {
                inputQueue.addFirst(texture);
                if (mLoadingCount >= MAX_LOADING_COUNT) {
                	Texture unloadTexture = inputQueue.pollLast();
                	unloadTexture.mState = Texture.STATE_UNLOADED;
                	--mLoadingCount;
                }
            } else {
                inputQueue.addLast(texture);
            }
            inputQueue.notify();
        }
        ++mLoadingCount;
    }
    public void draw2D(Texture texture, float x, float y) {
        if (bind(texture)) {
            final float width = texture.getWidth();
            final float height = texture.getHeight();
            ((GL11Ext) mGL).glDrawTexfOES(x, mViewHeight - y - height, 0f, width, height);
        }
    }
    public void draw2D(Texture texture, float x, float y, float width, float height) {
        if (bind(texture)) {
            ((GL11Ext) mGL).glDrawTexfOES(x, mViewHeight - y - height, 0f, width, height);
        }
    }
    public void draw2D(Texture texture, int x, int y, int width, int height) {
        if (bind(texture)) {
            ((GL11Ext) mGL).glDrawTexiOES(x, (int) (mViewHeight - y - height), 0, width, height);
        }
    }
    public void draw2D(float x, float y, float z, float width, float height) {
        ((GL11Ext) mGL).glDrawTexfOES(x, mViewHeight - y - height, z, width, height);
    }
    public boolean bindMixed(Texture from, Texture to, float ratio) {
        final GL11 gl = mGL;
        boolean bind = true;
        bind &= bind(from);
        gl.glActiveTexture(GL11.GL_TEXTURE1);
        mBoundTexture = null;
        bind &= bind(to);
        if (!bind) {
            return false;
        }
        gl.glEnable(GL11.GL_TEXTURE_2D);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_INTERPOLATE);
        final float[] color = { 1f, 1f, 1f, ratio };
        gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, color, 0);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_CONSTANT);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
        return true;
    }
    public void unbindMixed() {
        final GL11 gl = mGL;
        gl.glDisable(GL11.GL_TEXTURE_2D);
        gl.glActiveTexture(GL11.GL_TEXTURE0);
        mBoundTexture = null;
    }
    public void drawMixed2D(Texture from, Texture to, float ratio, float x, float y, float z, float width, float height) {
        final GL11 gl = mGL;
        if (bind(from)) {
            gl.glActiveTexture(GL11.GL_TEXTURE1);
            mBoundTexture = null;
            if (bind(to)) {
                gl.glEnable(GL11.GL_TEXTURE_2D);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_INTERPOLATE);
                final float[] color = { 1f, 1f, 1f, ratio };
                gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, color, 0);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_CONSTANT);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
                ((GL11Ext) gl).glDrawTexfOES(x, mViewHeight - y - height, z, width, height);
                gl.glDisable(GL11.GL_TEXTURE_2D);
            }
            gl.glActiveTexture(GL11.GL_TEXTURE0);
            mBoundTexture = null;
        }
    }
    public void processAllTextures() {
        processTextures(true);
    }
    final static int[] textureId = new int[1];
    private void processTextures(boolean processAll) {
        GL11 gl = mGL;
        TextureReference textureReference;
        while ((textureReference = (TextureReference) mUnreferencedTextureQueue.poll()) != null) {
            textureId[0] = textureReference.textureId;
            GL11 glOld = textureReference.gl;
            if (glOld == gl) {
                gl.glDeleteTextures(1, textureId, 0);
            }
            mActiveTextureList.remove(textureReference.activeListEntry);
        }
        Deque<Texture> outputQueue = sLoadOutputQueue;
        Texture texture;
        do {
            synchronized (outputQueue) {
                texture = outputQueue.pollFirst();
            }
            if (texture != null) {
                uploadTexture(texture, textureId);
                --mLoadingCount;
            } else {
                break;
            }
        } while (processAll);
    }
    private void uploadTexture(Texture texture, int[] textureId) {
        Bitmap bitmap = texture.mBitmap;
        GL11 gl = mGL;
        int glError = GL11.GL_NO_ERROR;
        if (bitmap != null) {
            final int width = texture.mWidth;
            final int height = texture.mHeight;
            int[] cropRect = { 0, height, width, -height };
            gl.glGenTextures(1, textureId, 0);
            gl.glBindTexture(GL11.GL_TEXTURE_2D, textureId[0]);
            gl.glTexParameteriv(GL11.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropRect, 0);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
            glError = gl.glGetError();
            bitmap.recycle();
            if (glError == GL11.GL_OUT_OF_MEMORY) {
                handleLowMemory();
            }
            if (glError != GL11.GL_NO_ERROR) {
                Log.i(TAG, "Texture creation fail, glError " + glError);
                texture.mId = 0;
                texture.mBitmap = null;
                texture.mState = Texture.STATE_UNLOADED;
            } else {
                texture.mBitmap = null;
                texture.mId = textureId[0];
                texture.mState = Texture.STATE_LOADED;
                final TextureReference textureRef = new TextureReference(texture, gl, mUnreferencedTextureQueue, textureId[0]);
                mActiveTextureList.add(textureRef.activeListEntry);
                requestRender();
            }
        } else {
            texture.mState = Texture.STATE_ERROR;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Sensor sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensorAccelerometer != null) {
            mSensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (mRootLayer != null) {
            mRootLayer.onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause RenderView " + this);
        mSensorManager.unregisterListener(this);
        if (mRootLayer != null) {
            mRootLayer.onPause();
        }
    }
    public void onDrawFrame(GL10 gl1) {
        GL11 gl = (GL11) gl1;
        if (!mFirstDraw) {
            Log.i(TAG, "First Draw");
        }
        mFirstDraw = true;
        if (mListsDirty) {
            updateLists();
        }
        boolean wasLoadingExpensiveTextures = isLoadingExpensiveTextures();
        boolean loadingExpensiveTextures = false;
        int numTextureThreads = sTextureLoadThreads.length;
        for (int i = 2; i < numTextureThreads; ++i) {
            if (sTextureLoadThreads[i].mIsLoading) {
                loadingExpensiveTextures = true;
                break;
            }
        }
        if (loadingExpensiveTextures != wasLoadingExpensiveTextures) {
            mLoadingExpensiveTexturesStartTime = loadingExpensiveTextures ? SystemClock.uptimeMillis() : 0;
        }
        processTextures(false);
        long now = SystemClock.uptimeMillis();
        final float dt = 0.001f * Math.min(50, now - mFrameTime);
        mFrameInterval = dt;
        mFrameTime = now;
        processCurrentEvent();
        processTouchEvent();
        final Lists lists = sLists;
        synchronized (lists) {
            final ArrayList<Layer> updateList = lists.updateList;
            boolean isDirty = false;
            for (int i = 0, size = updateList.size(); i != size; ++i) {
                boolean retVal = updateList.get(i).update(this, mFrameInterval);
                isDirty |= retVal;
            }
            if (isDirty) {
                requestRender();
            }
            gl.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL11.GL_SCISSOR_TEST);
            gl.glScissor(0, 0, getWidth(), getHeight());
            gl.glDisable(GL11.GL_BLEND);
            final ArrayList<Layer> opaqueList = lists.opaqueList;
            for (int i = opaqueList.size() - 1; i >= 0; --i) {
                final Layer layer = opaqueList.get(i);
                if (!layer.mHidden) {
                    layer.renderOpaque(this, gl);
                }
            }
            gl.glEnable(GL11.GL_BLEND);
            final ArrayList<Layer> blendedList = lists.blendedList;
            for (int i = 0, size = blendedList.size(); i != size; ++i) {
                final Layer layer = blendedList.get(i);
                if (!layer.mHidden) {
                    layer.renderBlended(this, gl);
                }
            }
            gl.glDisable(GL11.GL_BLEND);
        }
    }
    private void processCurrentEvent() {
        final int type = mCurrentEventType;
        switch (type) {
        case EVENT_KEY:
            processKeyEvent();
            break;
        case EVENT_FOCUS:
            processFocusEvent();
            break;
        default:
            break;
        }
        synchronized (this) {
            mCurrentEventType = EVENT_NONE;
            this.notify();
        }
    }
    private void processTouchEvent() {
        MotionEvent event = null;
        int numEvents = mTouchEventQueue.size();
        int i = 0;
        do {
            synchronized (mTouchEventQueue) {
                event = mTouchEventQueue.pollFirst();
            }
            if (event == null)
                return;
            final int action = event.getAction();
            Layer target;
            if (action == MotionEvent.ACTION_DOWN) {
                target = hitTest(event.getX(), event.getY());
                mTouchEventTarget = target;
            } else {
                target = mTouchEventTarget;
            }
            if (target != null) {
                target.onTouchEvent(event);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mTouchEventTarget = null;
            }
            event.recycle();
            ++i;
        } while (event != null && i < numEvents);
        synchronized (this) {
            this.notify();
        }
    }
    private void processKeyEvent() {
        final KeyEvent event = mCurrentKeyEvent;
        boolean result = false;
        mCurrentKeyEvent = null;
        if (mRootLayer != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                result = mRootLayer.onKeyDown(event.getKeyCode(), event);
            } else {
                result = mRootLayer.onKeyUp(event.getKeyCode(), event);
            }
        }
        mCurrentKeyEventResult = result;
    }
    private void processFocusEvent() {
        if (mRootLayer != null) {
        }
    }
    private Layer hitTest(float x, float y) {
        final ArrayList<Layer> hitTestList = sLists.hitTestList;
        for (int i = hitTestList.size() - 1; i >= 0; --i) {
            final Layer layer = hitTestList.get(i);
            if (layer != null && !layer.mHidden) {
                final float layerX = layer.mX;
                final float layerY = layer.mY;
                if (x >= layerX && y >= layerY && x < layerX + layer.mWidth && y < layerY + layer.mHeight
                        && layer.containsPoint(x, y)) {
                    return layer;
                }
            }
        }
        return null;
    }
    private void updateLists() {
        if (mRootLayer != null) {
            synchronized (sLists) {
                sLists.clear();
                mRootLayer.generate(this, sLists);
            }
        }
    }
    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        GL11 gl = (GL11) gl1;
        mFirstDraw = false;
        mViewWidth = width;
        mViewHeight = height;
        if (mRootLayer != null) {
            mRootLayer.setSize(width, height);
        }
        final float zNear = 0.1f;
        final float zFar = 100.0f;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / height, zNear, zFar);
        if (mRootLayer != null) {
            mRootLayer.onSurfaceChanged(this, width, height);
        }
        gl.glMatrixMode(GL11.GL_MODELVIEW);
    }
    public void setFov(float fov) {
        GL11 gl = mGL;
        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();
        final float zNear = 0.1f;
        final float zFar = 100.0f;
        GLU.gluPerspective(gl, fov, (float) getWidth() / getHeight(), zNear, zFar);
        gl.glMatrixMode(GL11.GL_MODELVIEW);
    }
    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        clearCache();
        GL11 gl = (GL11) gl1;
        if (mGL == null) {
            mGL = gl;
        } else {
            Log.i(TAG, "GLObject has changed from " + mGL + " to " + gl);
            mGL = gl;
        }
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        gl.glEnable(GL11.GL_DITHER);
        gl.glDisable(GL11.GL_LIGHTING);
        gl.glEnable(GL11.GL_TEXTURE_2D);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glClientActiveTexture(GL11.GL_TEXTURE1);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glClientActiveTexture(GL11.GL_TEXTURE0);
        gl.glEnable(GL11.GL_DEPTH_TEST);
        gl.glDepthFunc(GL11.GL_LEQUAL);
        gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        if (!mActiveTextureList.isEmpty()) {
            DirectLinkedList.Entry<TextureReference> iter = mActiveTextureList.getHead();
            while (iter != null) {
                final Texture texture = iter.value.get();
                if (texture != null) {
                    texture.mState = Texture.STATE_UNLOADED;
                }
                iter = iter.next;
            }
        }
        mActiveTextureList.clear();
        if (mRootLayer != null) {
            mRootLayer.onSurfaceCreated(this, gl);
        }
        synchronized (sLists) {
            ArrayList<Layer> systemList = sLists.systemList;
            for (int i = systemList.size() - 1; i >= 0; --i) {
                systemList.get(i).onSurfaceCreated(this, gl);
            }
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void onSensorChanged(SensorEvent event) {
        final int type = event.sensor.getType();
        if (!mPendingSensorEvent && type == Sensor.TYPE_ACCELEROMETER) {
            final SensorEvent e = event;
            if (mRootLayer != null)
                mRootLayer.onSensorChanged(RenderView.this, e);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGL == null) {
            return false;
        }
        if (mTouchEventQueue.size() > 8 && event.getAction() == MotionEvent.ACTION_MOVE)
            return true;
        synchronized (mTouchEventQueue) {
            MotionEvent eventCopy = MotionEvent.obtain(event);
            mTouchEventQueue.addLast(eventCopy);
            requestRender();
        }
        return true;
    }
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        requestRender();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mGL == null) {
            return false;
        }
        try {
            synchronized (this) {
                mCurrentKeyEvent = event;
                mCurrentEventType = EVENT_KEY;
                requestRender();
                long timeout = SystemClock.uptimeMillis() + 50;
                do {
                    wait(50);
                } while (mCurrentEventType != EVENT_NONE && SystemClock.uptimeMillis() < timeout);
            }
        } catch (InterruptedException e) {
        }
        boolean retVal = false;
        if (!mCurrentKeyEventResult) {
            retVal = super.onKeyDown(keyCode, event);
        } else {
            retVal = true;
        }
        requestRender();
        return retVal;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    private final class TextureLoadThread extends Thread {
        public boolean mIsLoading;
        public TextureLoadThread() {
            super("TextureLoad");
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Deque<Texture> inputQueue = (sVideoTextureLoadThread == this) ? sLoadInputQueueVideo
                    : ((sCachedTextureLoadThread == this) ? sLoadInputQueueCached : sLoadInputQueue);
            Deque<Texture> outputQueue = sLoadOutputQueue;
            try {
                for (;;) {
                    Texture texture = null;
                    synchronized (inputQueue) {
                        while ((texture = inputQueue.pollFirst()) == null) {
                            inputQueue.wait();
                        }
                    }
                    if (sCachedTextureLoadThread != this)
                        mIsLoading = true;
                    load(texture);
                    mIsLoading = false;
                    synchronized (outputQueue) {
                        outputQueue.addLast(texture);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
        private void load(Texture texture) {
            RenderView view = RenderView.this;
            view.loadTextureAsync(texture);
            view.requestRender();
        }
    }
    public void shutdown() {
        mRootLayer = null;
        synchronized (sLists) {
            sLists.clear();
        }
    }
    public void handleLowMemory() {
        Log.i(TAG, "Handling low memory condition");
        if (mRootLayer != null) {
            mRootLayer.handleLowMemory();
        }
    }
    public Lists getLists() {
        return sLists;
    }
}
