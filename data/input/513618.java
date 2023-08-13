public class SurfaceView extends View {
    static private final String TAG = "SurfaceView";
    static private final boolean DEBUG = false;
    static private final boolean localLOGV = DEBUG ? true : Config.LOGV;
    final ArrayList<SurfaceHolder.Callback> mCallbacks
            = new ArrayList<SurfaceHolder.Callback>();
    final int[] mLocation = new int[2];
    final ReentrantLock mSurfaceLock = new ReentrantLock();
    final Surface mSurface = new Surface();
    boolean mDrawingStopped = true;
    final WindowManager.LayoutParams mLayout
            = new WindowManager.LayoutParams();
    IWindowSession mSession;
    MyWindow mWindow;
    final Rect mVisibleInsets = new Rect();
    final Rect mWinFrame = new Rect();
    final Rect mContentInsets = new Rect();
    final Configuration mConfiguration = new Configuration();
    static final int KEEP_SCREEN_ON_MSG = 1;
    static final int GET_NEW_SURFACE_MSG = 2;
    static final int UPDATE_WINDOW_MSG = 3;
    int mWindowType = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA;
    boolean mIsCreating = false;
    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEEP_SCREEN_ON_MSG: {
                    setKeepScreenOn(msg.arg1 != 0);
                } break;
                case GET_NEW_SURFACE_MSG: {
                    handleGetNewSurface();
                } break;
                case UPDATE_WINDOW_MSG: {
                    updateWindow(false);
                } break;
            }
        }
    };
    final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener
            = new ViewTreeObserver.OnScrollChangedListener() {
                    public void onScrollChanged() {
                        updateWindow(false);
                    }
            };
    boolean mRequestedVisible = false;
    boolean mWindowVisibility = false;
    boolean mViewVisibility = false;
    int mRequestedWidth = -1;
    int mRequestedHeight = -1;
    int mRequestedFormat = PixelFormat.OPAQUE;
    int mRequestedType = -1;
    boolean mHaveFrame = false;
    boolean mDestroyReportNeeded = false;
    boolean mNewSurfaceNeeded = false;
    long mLastLockTime = 0;
    boolean mVisible = false;
    int mLeft = -1;
    int mTop = -1;
    int mWidth = -1;
    int mHeight = -1;
    int mFormat = -1;
    int mType = -1;
    final Rect mSurfaceFrame = new Rect();
    int mLastSurfaceWidth = -1, mLastSurfaceHeight = -1;
    boolean mUpdateWindowNeeded;
    boolean mReportDrawNeeded;
    private Translator mTranslator;
    public SurfaceView(Context context) {
        super(context);
        setWillNotDraw(true);
    }
    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(true);
    }
    public SurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(true);
    }
    public SurfaceHolder getHolder() {
        return mSurfaceHolder;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mParent.requestTransparentRegion(this);
        mSession = getWindowSession();
        mLayout.token = getWindowToken();
        mLayout.setTitle("SurfaceView");
        mViewVisibility = getVisibility() == VISIBLE;
        getViewTreeObserver().addOnScrollChangedListener(mScrollChangedListener);
    }
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisibility = visibility == VISIBLE;
        mRequestedVisible = mWindowVisibility && mViewVisibility;
        updateWindow(false);
    }
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mViewVisibility = visibility == VISIBLE;
        mRequestedVisible = mWindowVisibility && mViewVisibility;
        updateWindow(false);
    }
    protected void showSurface() {
        if (mSession != null) {
            updateWindow(true);
        }
    }
    protected void hideSurface() {
        if (mSession != null && mWindow != null) {
            mSurfaceLock.lock();
            try {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mLayout.x = metrics.widthPixels * 3;
                mSession.relayout(mWindow, mLayout, mWidth, mHeight, VISIBLE, false,
                        mWinFrame, mContentInsets, mVisibleInsets, mConfiguration, mSurface);
            } catch (RemoteException e) {
            } finally {
                mSurfaceLock.unlock();
            }
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(mScrollChangedListener);
        mRequestedVisible = false;
        updateWindow(false);
        mHaveFrame = false;
        if (mWindow != null) {
            try {
                mSession.remove(mWindow);
            } catch (RemoteException ex) {
            }
            mWindow = null;
        }
        mSession = null;
        mLayout.token = null;
        super.onDetachedFromWindow();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mRequestedWidth, widthMeasureSpec);
        int height = getDefaultSize(mRequestedHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateWindow(false);
    }
    @Override
    public boolean gatherTransparentRegion(Region region) {
        if (mWindowType == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
            return super.gatherTransparentRegion(region);
        }
        boolean opaque = true;
        if ((mPrivateFlags & SKIP_DRAW) == 0) {
            opaque = super.gatherTransparentRegion(region);
        } else if (region != null) {
            int w = getWidth();
            int h = getHeight();
            if (w>0 && h>0) {
                getLocationInWindow(mLocation);
                int l = mLocation[0];
                int t = mLocation[1];
                region.op(l, t, l+w, t+h, Region.Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(mRequestedFormat)) {
            opaque = false;
        }
        return opaque;
    }
    @Override
    public void draw(Canvas canvas) {
        if (mWindowType != WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
            if ((mPrivateFlags & SKIP_DRAW) == 0) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            }
        }
        super.draw(canvas);
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mWindowType != WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
            if ((mPrivateFlags & SKIP_DRAW) == SKIP_DRAW) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            }
        }
        mHaveFrame = true;
        updateWindow(false);
        super.dispatchDraw(canvas);
    }
    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        mWindowType = isMediaOverlay
                ? WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY
                : WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA;
    }
    public void setZOrderOnTop(boolean onTop) {
        if (onTop) {
            mWindowType = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            mLayout.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        } else {
            mWindowType = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA;
            mLayout.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
    }
    public void setWindowType(int type) {
        mWindowType = type;
    }
    private void updateWindow(boolean force) {
        if (!mHaveFrame) {
            return;
        }
        ViewRoot viewRoot = (ViewRoot) getRootView().getParent();
        if (viewRoot != null) {
            mTranslator = viewRoot.mTranslator;
        }
        Resources res = getContext().getResources();
        if (mTranslator != null || !res.getCompatibilityInfo().supportsScreen()) {
            mSurface.setCompatibleDisplayMetrics(res.getDisplayMetrics(), mTranslator);
        }
        int myWidth = mRequestedWidth;
        if (myWidth <= 0) myWidth = getWidth();
        int myHeight = mRequestedHeight;
        if (myHeight <= 0) myHeight = getHeight();
        getLocationInWindow(mLocation);
        final boolean creating = mWindow == null;
        final boolean formatChanged = mFormat != mRequestedFormat;
        final boolean sizeChanged = mWidth != myWidth || mHeight != myHeight;
        final boolean visibleChanged = mVisible != mRequestedVisible
                || mNewSurfaceNeeded;
        final boolean typeChanged = mType != mRequestedType;
        if (force || creating || formatChanged || sizeChanged || visibleChanged
            || typeChanged || mLeft != mLocation[0] || mTop != mLocation[1]
            || mUpdateWindowNeeded || mReportDrawNeeded) {
            if (localLOGV) Log.i(TAG, "Changes: creating=" + creating
                    + " format=" + formatChanged + " size=" + sizeChanged
                    + " visible=" + visibleChanged
                    + " left=" + (mLeft != mLocation[0])
                    + " top=" + (mTop != mLocation[1]));
            try {
                final boolean visible = mVisible = mRequestedVisible;
                mLeft = mLocation[0];
                mTop = mLocation[1];
                mWidth = myWidth;
                mHeight = myHeight;
                mFormat = mRequestedFormat;
                mType = mRequestedType;
                mLayout.x = mLeft;
                mLayout.y = mTop;
                mLayout.width = getWidth();
                mLayout.height = getHeight();
                if (mTranslator != null) {
                    mTranslator.translateLayoutParamsInAppWindowToScreen(mLayout);
                }
                mLayout.format = mRequestedFormat;
                mLayout.flags |=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                              | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                              | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                              | WindowManager.LayoutParams.FLAG_SCALED
                              | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                              | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                              ;
                if (!getContext().getResources().getCompatibilityInfo().supportsScreen()) {
                    mLayout.flags |= WindowManager.LayoutParams.FLAG_COMPATIBLE_WINDOW;
                }
                mLayout.memoryType = mRequestedType;
                if (mWindow == null) {
                    mWindow = new MyWindow(this);
                    mLayout.type = mWindowType;
                    mLayout.gravity = Gravity.LEFT|Gravity.TOP;
                    mSession.add(mWindow, mLayout,
                            mVisible ? VISIBLE : GONE, mContentInsets);
                }
                if (visibleChanged && (!visible || mNewSurfaceNeeded)) {
                    reportSurfaceDestroyed();
                }
                mNewSurfaceNeeded = false;
                boolean realSizeChanged;
                boolean reportDrawNeeded;
                mSurfaceLock.lock();
                try {
                    mUpdateWindowNeeded = false;
                    reportDrawNeeded = mReportDrawNeeded;
                    mReportDrawNeeded = false;
                    mDrawingStopped = !visible;
                    final int relayoutResult = mSession.relayout(
                        mWindow, mLayout, mWidth, mHeight,
                            visible ? VISIBLE : GONE, false, mWinFrame, mContentInsets,
                            mVisibleInsets, mConfiguration, mSurface);
                    if ((relayoutResult&WindowManagerImpl.RELAYOUT_FIRST_TIME) != 0) {
                        mReportDrawNeeded = true;
                    }
                    if (localLOGV) Log.i(TAG, "New surface: " + mSurface
                            + ", vis=" + visible + ", frame=" + mWinFrame);
                    mSurfaceFrame.left = 0;
                    mSurfaceFrame.top = 0;
                    if (mTranslator == null) {
                        mSurfaceFrame.right = mWinFrame.width();
                        mSurfaceFrame.bottom = mWinFrame.height();
                    } else {
                        float appInvertedScale = mTranslator.applicationInvertedScale;
                        mSurfaceFrame.right = (int) (mWinFrame.width() * appInvertedScale + 0.5f);
                        mSurfaceFrame.bottom = (int) (mWinFrame.height() * appInvertedScale + 0.5f);
                    }
                    final int surfaceWidth = mSurfaceFrame.right;
                    final int surfaceHeight = mSurfaceFrame.bottom;
                    realSizeChanged = mLastSurfaceWidth != surfaceWidth
                            || mLastSurfaceHeight != surfaceHeight;
                    mLastSurfaceWidth = surfaceWidth;
                    mLastSurfaceHeight = surfaceHeight;
                } finally {
                    mSurfaceLock.unlock();
                }
                try {
                    if (visible) {
                        mDestroyReportNeeded = true;
                        SurfaceHolder.Callback callbacks[];
                        synchronized (mCallbacks) {
                            callbacks = new SurfaceHolder.Callback[mCallbacks.size()];
                            mCallbacks.toArray(callbacks);
                        }
                        if (visibleChanged) {
                            mIsCreating = true;
                            for (SurfaceHolder.Callback c : callbacks) {
                                c.surfaceCreated(mSurfaceHolder);
                            }
                        }
                        if (creating || formatChanged || sizeChanged
                                || visibleChanged || realSizeChanged) {
                            for (SurfaceHolder.Callback c : callbacks) {
                                c.surfaceChanged(mSurfaceHolder, mFormat, myWidth, myHeight);
                            }
                        }
                    } else {
                        mSurface.release();
                    }
                } finally {
                    mIsCreating = false;
                    if (creating || reportDrawNeeded) {
                        mSession.finishDrawing(mWindow);
                    }
                }
            } catch (RemoteException ex) {
            }
            if (localLOGV) Log.v(
                TAG, "Layout: x=" + mLayout.x + " y=" + mLayout.y +
                " w=" + mLayout.width + " h=" + mLayout.height +
                ", frame=" + mSurfaceFrame);
        }
    }
    private void reportSurfaceDestroyed() {
        if (mDestroyReportNeeded) {
            mDestroyReportNeeded = false;
            SurfaceHolder.Callback callbacks[];
            synchronized (mCallbacks) {
                callbacks = new SurfaceHolder.Callback[mCallbacks.size()];
                mCallbacks.toArray(callbacks);
            }            
            for (SurfaceHolder.Callback c : callbacks) {
                c.surfaceDestroyed(mSurfaceHolder);
            }
        }
        super.onDetachedFromWindow();
    }
    void handleGetNewSurface() {
        mNewSurfaceNeeded = true;
        updateWindow(false);
    }
    public boolean isFixedSize() {
        return (mRequestedWidth != -1 || mRequestedHeight != -1);
    }
    private static class MyWindow extends BaseIWindow {
        private final WeakReference<SurfaceView> mSurfaceView;
        public MyWindow(SurfaceView surfaceView) {
            mSurfaceView = new WeakReference<SurfaceView>(surfaceView);
        }
        public void resized(int w, int h, Rect coveredInsets,
                Rect visibleInsets, boolean reportDraw, Configuration newConfig) {
            SurfaceView surfaceView = mSurfaceView.get();
            if (surfaceView != null) {
                if (localLOGV) Log.v(
                        "SurfaceView", surfaceView + " got resized: w=" +
                                w + " h=" + h + ", cur w=" + mCurWidth + " h=" + mCurHeight);
                surfaceView.mSurfaceLock.lock();
                try {
                    if (reportDraw) {
                        surfaceView.mUpdateWindowNeeded = true;
                        surfaceView.mReportDrawNeeded = true;
                        surfaceView.mHandler.sendEmptyMessage(UPDATE_WINDOW_MSG);
                    } else if (surfaceView.mWinFrame.width() != w
                            || surfaceView.mWinFrame.height() != h) {
                        surfaceView.mUpdateWindowNeeded = true;
                        surfaceView.mHandler.sendEmptyMessage(UPDATE_WINDOW_MSG);
                    }
                } finally {
                    surfaceView.mSurfaceLock.unlock();
                }
            }
        }
        public void dispatchKey(KeyEvent event) {
            SurfaceView surfaceView = mSurfaceView.get();
            if (surfaceView != null) {
                if (surfaceView.mSession != null && surfaceView.mSurface != null) {
                    try {
                        surfaceView.mSession.finishKey(surfaceView.mWindow);
                    } catch (RemoteException ex) {
                    }
                }
            }
        }
        public void dispatchPointer(MotionEvent event, long eventTime,
                boolean callWhenDone) {
            Log.w("SurfaceView", "Unexpected pointer event in surface: " + event);
        }
        public void dispatchTrackball(MotionEvent event, long eventTime,
                boolean callWhenDone) {
            Log.w("SurfaceView", "Unexpected trackball event in surface: " + event);
        }
        public void dispatchAppVisibility(boolean visible) {
        }
        public void dispatchGetNewSurface() {
            SurfaceView surfaceView = mSurfaceView.get();
            if (surfaceView != null) {
                Message msg = surfaceView.mHandler.obtainMessage(GET_NEW_SURFACE_MSG);
                surfaceView.mHandler.sendMessage(msg);
            }
        }
        public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
            Log.w("SurfaceView", "Unexpected focus in surface: focus=" + hasFocus + ", touchEnabled=" + touchEnabled);
        }
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
        }
        int mCurWidth = -1;
        int mCurHeight = -1;
    }
    private SurfaceHolder mSurfaceHolder = new SurfaceHolder() {
        private static final String LOG_TAG = "SurfaceHolder";
        private int mSaveCount;
        public boolean isCreating() {
            return mIsCreating;
        }
        public void addCallback(Callback callback) {
            synchronized (mCallbacks) {
                if (mCallbacks.contains(callback) == false) {      
                    mCallbacks.add(callback);
                }
            }
        }
        public void removeCallback(Callback callback) {
            synchronized (mCallbacks) {
                mCallbacks.remove(callback);
            }
        }
        public void setFixedSize(int width, int height) {
            if (mRequestedWidth != width || mRequestedHeight != height) {
                mRequestedWidth = width;
                mRequestedHeight = height;
                requestLayout();
            }
        }
        public void setSizeFromLayout() {
            if (mRequestedWidth != -1 || mRequestedHeight != -1) {
                mRequestedWidth = mRequestedHeight = -1;
                requestLayout();
            }
        }
        public void setFormat(int format) {
            mRequestedFormat = format;
            if (mWindow != null) {
                updateWindow(false);
            }
        }
        public void setType(int type) {
            switch (type) {
            case SURFACE_TYPE_HARDWARE:
            case SURFACE_TYPE_GPU:
                type = SURFACE_TYPE_NORMAL;
                break;
            }
            switch (type) {
            case SURFACE_TYPE_NORMAL:
            case SURFACE_TYPE_PUSH_BUFFERS:
                mRequestedType = type;
                if (mWindow != null) {
                    updateWindow(false);
                }
                break;
            }
        }
        public void setKeepScreenOn(boolean screenOn) {
            Message msg = mHandler.obtainMessage(KEEP_SCREEN_ON_MSG);
            msg.arg1 = screenOn ? 1 : 0;
            mHandler.sendMessage(msg);
        }
        public Canvas lockCanvas() {
            return internalLockCanvas(null);
        }
        public Canvas lockCanvas(Rect dirty) {
            return internalLockCanvas(dirty);
        }
        private final Canvas internalLockCanvas(Rect dirty) {
            if (mType == SURFACE_TYPE_PUSH_BUFFERS) {
                throw new BadSurfaceTypeException(
                        "Surface type is SURFACE_TYPE_PUSH_BUFFERS");
            }
            mSurfaceLock.lock();
            if (localLOGV) Log.i(TAG, "Locking canvas... stopped="
                    + mDrawingStopped + ", win=" + mWindow);
            Canvas c = null;
            if (!mDrawingStopped && mWindow != null) {
                Rect frame = dirty != null ? dirty : mSurfaceFrame;
                try {
                    c = mSurface.lockCanvas(frame);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception locking surface", e);
                }
            }
            if (localLOGV) Log.i(TAG, "Returned canvas: " + c);
            if (c != null) {
                mLastLockTime = SystemClock.uptimeMillis();
                return c;
            }
            long now = SystemClock.uptimeMillis();
            long nextTime = mLastLockTime + 100;
            if (nextTime > now) {
                try {
                    Thread.sleep(nextTime-now);
                } catch (InterruptedException e) {
                }
                now = SystemClock.uptimeMillis();
            }
            mLastLockTime = now;
            mSurfaceLock.unlock();
            return null;
        }
        public void unlockCanvasAndPost(Canvas canvas) {
            mSurface.unlockCanvasAndPost(canvas);
            mSurfaceLock.unlock();
        }
        public Surface getSurface() {
            return mSurface;
        }
        public Rect getSurfaceFrame() {
            return mSurfaceFrame;
        }
    };
}
