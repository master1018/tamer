public abstract class BaseSurfaceHolder implements SurfaceHolder {
    private static final String TAG = "BaseSurfaceHolder";
    static final boolean DEBUG = false;
    public final ArrayList<SurfaceHolder.Callback> mCallbacks
            = new ArrayList<SurfaceHolder.Callback>();
    public final ReentrantLock mSurfaceLock = new ReentrantLock();
    public final Surface mSurface = new Surface();
    int mRequestedWidth = -1;
    int mRequestedHeight = -1;
    int mRequestedFormat = PixelFormat.OPAQUE;
    int mRequestedType = -1;
    long mLastLockTime = 0;
    int mType = -1;
    final Rect mSurfaceFrame = new Rect();
    public abstract void onUpdateSurface();
    public abstract void onRelayoutContainer();
    public abstract boolean onAllowLockCanvas();
    public int getRequestedWidth() {
        return mRequestedWidth;
    }
    public int getRequestedHeight() {
        return mRequestedHeight;
    }
    public int getRequestedFormat() {
        return mRequestedFormat;
    }
    public int getRequestedType() {
        return mRequestedType;
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
            onRelayoutContainer();
        }
    }
    public void setSizeFromLayout() {
        if (mRequestedWidth != -1 || mRequestedHeight != -1) {
            mRequestedWidth = mRequestedHeight = -1;
            onRelayoutContainer();
        }
    }
    public void setFormat(int format) {
        if (mRequestedFormat != format) {
            mRequestedFormat = format;
            onUpdateSurface();
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
            if (mRequestedType != type) {
                mRequestedType = type;
                onUpdateSurface();
            }
            break;
        }
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
        if (DEBUG) Log.i(TAG, "Locking canvas..,");
        Canvas c = null;
        if (onAllowLockCanvas()) {
            Rect frame = dirty != null ? dirty : mSurfaceFrame;
            try {
                c = mSurface.lockCanvas(frame);
            } catch (Exception e) {
                Log.e(TAG, "Exception locking surface", e);
            }
        }
        if (DEBUG) Log.i(TAG, "Returned canvas: " + c);
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
