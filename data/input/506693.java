public abstract class WallpaperService extends Service {
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE =
            "android.service.wallpaper.WallpaperService";
    public static final String SERVICE_META_DATA = "android.service.wallpaper";
    static final String TAG = "WallpaperService";
    static final boolean DEBUG = false;
    private static final int DO_ATTACH = 10;
    private static final int DO_DETACH = 20;
    private static final int DO_SET_DESIRED_SIZE = 30;
    private static final int MSG_UPDATE_SURFACE = 10000;
    private static final int MSG_VISIBILITY_CHANGED = 10010;
    private static final int MSG_WALLPAPER_OFFSETS = 10020;
    private static final int MSG_WALLPAPER_COMMAND = 10025;
    private static final int MSG_WINDOW_RESIZED = 10030;
    private static final int MSG_TOUCH_EVENT = 10040;
    private Looper mCallbackLooper;
    private final ArrayList<Engine> mActiveEngines
            = new ArrayList<Engine>();
    static final class WallpaperCommand {
        String action;
        int x;
        int y;
        int z;
        Bundle extras;
        boolean sync;
    }
    public class Engine {
        IWallpaperEngineWrapper mIWallpaperEngine;
        HandlerCaller mCaller;
        IWallpaperConnection mConnection;
        IBinder mWindowToken;
        boolean mInitializing = true;
        boolean mVisible;
        boolean mScreenOn = true;
        boolean mReportedVisible;
        boolean mDestroyed;
        boolean mCreated;
        boolean mSurfaceCreated;
        boolean mIsCreating;
        boolean mDrawingAllowed;
        int mWidth;
        int mHeight;
        int mFormat;
        int mType;
        int mCurWidth;
        int mCurHeight;
        int mWindowFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        int mCurWindowFlags = mWindowFlags;
        final Rect mVisibleInsets = new Rect();
        final Rect mWinFrame = new Rect();
        final Rect mContentInsets = new Rect();
        final Configuration mConfiguration = new Configuration();
        final WindowManager.LayoutParams mLayout
                = new WindowManager.LayoutParams();
        IWindowSession mSession;
        final Object mLock = new Object();
        boolean mOffsetMessageEnqueued;
        float mPendingXOffset;
        float mPendingYOffset;
        float mPendingXOffsetStep;
        float mPendingYOffsetStep;
        boolean mPendingSync;
        MotionEvent mPendingMove;
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    mScreenOn = true;
                    reportVisibility();
                } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    mScreenOn = false;
                    reportVisibility();
                }
            }
        };
        final BaseSurfaceHolder mSurfaceHolder = new BaseSurfaceHolder() {
            @Override
            public boolean onAllowLockCanvas() {
                return mDrawingAllowed;
            }
            @Override
            public void onRelayoutContainer() {
                Message msg = mCaller.obtainMessage(MSG_UPDATE_SURFACE);
                mCaller.sendMessage(msg);
            }
            @Override
            public void onUpdateSurface() {
                Message msg = mCaller.obtainMessage(MSG_UPDATE_SURFACE);
                mCaller.sendMessage(msg);
            }
            public boolean isCreating() {
                return mIsCreating;
            }
            @Override
            public void setFixedSize(int width, int height) {
                throw new UnsupportedOperationException(
                        "Wallpapers currently only support sizing from layout");
            }
            public void setKeepScreenOn(boolean screenOn) {
                throw new UnsupportedOperationException(
                        "Wallpapers do not support keep screen on");
            }
        };
        final BaseIWindow mWindow = new BaseIWindow() {
            @Override
            public boolean onDispatchPointer(MotionEvent event, long eventTime,
                    boolean callWhenDone) {
                synchronized (mLock) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (mPendingMove != null) {
                            mCaller.removeMessages(MSG_TOUCH_EVENT, mPendingMove);
                            mPendingMove.recycle();
                        }
                        mPendingMove = event;
                    } else {
                        mPendingMove = null;
                    }
                    Message msg = mCaller.obtainMessageO(MSG_TOUCH_EVENT,
                            event);
                    mCaller.sendMessage(msg);
                }
                return false;
            }
            @Override
            public void resized(int w, int h, Rect coveredInsets,
                    Rect visibleInsets, boolean reportDraw, Configuration newConfig) {
                Message msg = mCaller.obtainMessageI(MSG_WINDOW_RESIZED,
                        reportDraw ? 1 : 0);
                mCaller.sendMessage(msg);
            }
            @Override
            public void dispatchAppVisibility(boolean visible) {
                if (!mIWallpaperEngine.mIsPreview) {
                    Message msg = mCaller.obtainMessageI(MSG_VISIBILITY_CHANGED,
                            visible ? 1 : 0);
                    mCaller.sendMessage(msg);
                }
            }
            @Override
            public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep,
                    boolean sync) {
                synchronized (mLock) {
                    if (DEBUG) Log.v(TAG, "Dispatch wallpaper offsets: " + x + ", " + y);
                    mPendingXOffset = x;
                    mPendingYOffset = y;
                    mPendingXOffsetStep = xStep;
                    mPendingYOffsetStep = yStep;
                    if (sync) {
                        mPendingSync = true;
                    }
                    if (!mOffsetMessageEnqueued) {
                        mOffsetMessageEnqueued = true;
                        Message msg = mCaller.obtainMessage(MSG_WALLPAPER_OFFSETS);
                        mCaller.sendMessage(msg);
                    }
                }
            }
            public void dispatchWallpaperCommand(String action, int x, int y,
                    int z, Bundle extras, boolean sync) {
                synchronized (mLock) {
                    if (DEBUG) Log.v(TAG, "Dispatch wallpaper command: " + x + ", " + y);
                    WallpaperCommand cmd = new WallpaperCommand();
                    cmd.action = action;
                    cmd.x = x;
                    cmd.y = y;
                    cmd.z = z;
                    cmd.extras = extras;
                    cmd.sync = sync;
                    Message msg = mCaller.obtainMessage(MSG_WALLPAPER_COMMAND);
                    msg.obj = cmd;
                    mCaller.sendMessage(msg);
                }
            }
        };
        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }
        public int getDesiredMinimumWidth() {
            return mIWallpaperEngine.mReqWidth;
        }
        public int getDesiredMinimumHeight() {
            return mIWallpaperEngine.mReqHeight;
        }
        public boolean isVisible() {
            return mReportedVisible;
        }
        public boolean isPreview() {
            return mIWallpaperEngine.mIsPreview;
        }
        public void setTouchEventsEnabled(boolean enabled) {
            mWindowFlags = enabled
                    ? (mWindowFlags&~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    : (mWindowFlags|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (mCreated) {
                updateSurface(false, false);
            }
        }
        public void onCreate(SurfaceHolder surfaceHolder) {
        }
        public void onDestroy() {
        }
        public void onVisibilityChanged(boolean visible) {
        }
        public void onTouchEvent(MotionEvent event) {
        }
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xOffsetStep, float yOffsetStep,
                int xPixelOffset, int yPixelOffset) {
        }
        public Bundle onCommand(String action, int x, int y, int z,
                Bundle extras, boolean resultRequested) {
            return null;
        }
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
        }
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
        public void onSurfaceCreated(SurfaceHolder holder) {
        }
        public void onSurfaceDestroyed(SurfaceHolder holder) {
        }
        void updateSurface(boolean forceRelayout, boolean forceReport) {
            if (mDestroyed) {
                Log.w(TAG, "Ignoring updateSurface: destroyed");
            }
            int myWidth = mSurfaceHolder.getRequestedWidth();
            if (myWidth <= 0) myWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            int myHeight = mSurfaceHolder.getRequestedHeight();
            if (myHeight <= 0) myHeight = ViewGroup.LayoutParams.MATCH_PARENT;
            final boolean creating = !mCreated;
            final boolean surfaceCreating = !mSurfaceCreated;
            final boolean formatChanged = mFormat != mSurfaceHolder.getRequestedFormat();
            boolean sizeChanged = mWidth != myWidth || mHeight != myHeight;
            final boolean typeChanged = mType != mSurfaceHolder.getRequestedType();
            final boolean flagsChanged = mCurWindowFlags != mWindowFlags;
            if (forceRelayout || creating || surfaceCreating || formatChanged || sizeChanged
                    || typeChanged || flagsChanged) {
                if (DEBUG) Log.v(TAG, "Changes: creating=" + creating
                        + " format=" + formatChanged + " size=" + sizeChanged);
                try {
                    mWidth = myWidth;
                    mHeight = myHeight;
                    mFormat = mSurfaceHolder.getRequestedFormat();
                    mType = mSurfaceHolder.getRequestedType();
                    mLayout.x = 0;
                    mLayout.y = 0;
                    mLayout.width = myWidth;
                    mLayout.height = myHeight;
                    mLayout.format = mFormat;
                    mCurWindowFlags = mWindowFlags;
                    mLayout.flags = mWindowFlags
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            ;
                    mLayout.memoryType = mType;
                    mLayout.token = mWindowToken;
                    if (!mCreated) {
                        mLayout.type = mIWallpaperEngine.mWindowType;
                        mLayout.gravity = Gravity.LEFT|Gravity.TOP;
                        mLayout.setTitle(WallpaperService.this.getClass().getName());
                        mLayout.windowAnimations =
                                com.android.internal.R.style.Animation_Wallpaper;
                        mSession.add(mWindow, mLayout, View.VISIBLE, mContentInsets);
                        mCreated = true;
                    }
                    mSurfaceHolder.mSurfaceLock.lock();
                    mDrawingAllowed = true;
                    final int relayoutResult = mSession.relayout(
                        mWindow, mLayout, mWidth, mHeight,
                            View.VISIBLE, false, mWinFrame, mContentInsets,
                            mVisibleInsets, mConfiguration, mSurfaceHolder.mSurface);
                    if (DEBUG) Log.v(TAG, "New surface: " + mSurfaceHolder.mSurface
                            + ", frame=" + mWinFrame);
                    int w = mWinFrame.width();
                    if (mCurWidth != w) {
                        sizeChanged = true;
                        mCurWidth = w;
                    }
                    int h = mWinFrame.height();
                    if (mCurHeight != h) {
                        sizeChanged = true;
                        mCurHeight = h;
                    }
                    mSurfaceHolder.mSurfaceLock.unlock();
                    if (!mSurfaceHolder.mSurface.isValid()) {
                        reportSurfaceDestroyed();
                        if (DEBUG) Log.v(TAG, "Layout: Surface destroyed");
                        return;
                    }
                    try {
                        SurfaceHolder.Callback callbacks[] = null;
                        synchronized (mSurfaceHolder.mCallbacks) {
                            final int N = mSurfaceHolder.mCallbacks.size();
                            if (N > 0) {
                                callbacks = new SurfaceHolder.Callback[N];
                                mSurfaceHolder.mCallbacks.toArray(callbacks);
                            }
                        }
                        if (surfaceCreating) {
                            mIsCreating = true;
                            if (DEBUG) Log.v(TAG, "onSurfaceCreated("
                                    + mSurfaceHolder + "): " + this);
                            onSurfaceCreated(mSurfaceHolder);
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c : callbacks) {
                                    c.surfaceCreated(mSurfaceHolder);
                                }
                            }
                        }
                        if (forceReport || creating || surfaceCreating
                                || formatChanged || sizeChanged) {
                            if (DEBUG) {
                                RuntimeException e = new RuntimeException();
                                e.fillInStackTrace();
                                Log.w(TAG, "forceReport=" + forceReport + " creating=" + creating
                                        + " formatChanged=" + formatChanged
                                        + " sizeChanged=" + sizeChanged, e);
                            }
                            if (DEBUG) Log.v(TAG, "onSurfaceChanged("
                                    + mSurfaceHolder + ", " + mFormat
                                    + ", " + mCurWidth + ", " + mCurHeight
                                    + "): " + this);
                            onSurfaceChanged(mSurfaceHolder, mFormat,
                                    mCurWidth, mCurHeight);
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c : callbacks) {
                                    c.surfaceChanged(mSurfaceHolder, mFormat,
                                            mCurWidth, mCurHeight);
                                }
                            }
                        }
                    } finally {
                        mIsCreating = false;
                        mSurfaceCreated = true;
                        if (creating || (relayoutResult&WindowManagerImpl.RELAYOUT_FIRST_TIME) != 0) {
                            mSession.finishDrawing(mWindow);
                        }
                    }
                } catch (RemoteException ex) {
                }
                if (DEBUG) Log.v(
                    TAG, "Layout: x=" + mLayout.x + " y=" + mLayout.y +
                    " w=" + mLayout.width + " h=" + mLayout.height);
            }
        }
        void attach(IWallpaperEngineWrapper wrapper) {
            if (DEBUG) Log.v(TAG, "attach: " + this + " wrapper=" + wrapper);
            if (mDestroyed) {
                return;
            }
            mIWallpaperEngine = wrapper;
            mCaller = wrapper.mCaller;
            mConnection = wrapper.mConnection;
            mWindowToken = wrapper.mWindowToken;
            mSurfaceHolder.setSizeFromLayout();
            mInitializing = true;
            mSession = ViewRoot.getWindowSession(getMainLooper());
            mWindow.setSession(mSession);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
            if (DEBUG) Log.v(TAG, "onCreate(): " + this);
            onCreate(mSurfaceHolder);
            mInitializing = false;
            updateSurface(false, false);
        }
        void doDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            if (!mDestroyed) {
                if (DEBUG) Log.v(TAG, "onDesiredSizeChanged("
                        + desiredWidth + "," + desiredHeight + "): " + this);
                onDesiredSizeChanged(desiredWidth, desiredHeight);
            }
        }
        void doVisibilityChanged(boolean visible) {
            if (!mDestroyed) {
                mVisible = visible;
                reportVisibility();
            }
        }
        void reportVisibility() {
            if (!mDestroyed) {
                boolean visible = mVisible && mScreenOn;
                if (mReportedVisible != visible) {
                    mReportedVisible = visible;
                    if (DEBUG) Log.v(TAG, "onVisibilityChanged(" + visible
                            + "): " + this);
                    if (visible) {
                        updateSurface(false, false);
                    }
                    onVisibilityChanged(visible);
                }
            }
        }
        void doOffsetsChanged() {
            if (mDestroyed) {
                return;
            }
            float xOffset;
            float yOffset;
            float xOffsetStep;
            float yOffsetStep;
            boolean sync;
            synchronized (mLock) {
                xOffset = mPendingXOffset;
                yOffset = mPendingYOffset;
                xOffsetStep = mPendingXOffsetStep;
                yOffsetStep = mPendingYOffsetStep;
                sync = mPendingSync;
                mPendingSync = false;
                mOffsetMessageEnqueued = false;
            }
            if (mSurfaceCreated) {
                if (DEBUG) Log.v(TAG, "Offsets change in " + this
                        + ": " + xOffset + "," + yOffset);
                final int availw = mIWallpaperEngine.mReqWidth-mCurWidth;
                final int xPixels = availw > 0 ? -(int)(availw*xOffset+.5f) : 0;
                final int availh = mIWallpaperEngine.mReqHeight-mCurHeight;
                final int yPixels = availh > 0 ? -(int)(availh*yOffset+.5f) : 0;
                onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixels, yPixels);
            }
            if (sync) {
                try {
                    if (DEBUG) Log.v(TAG, "Reporting offsets change complete");
                    mSession.wallpaperOffsetsComplete(mWindow.asBinder());
                } catch (RemoteException e) {
                }
            }
        }
        void doCommand(WallpaperCommand cmd) {
            Bundle result;
            if (!mDestroyed) {
                result = onCommand(cmd.action, cmd.x, cmd.y, cmd.z,
                        cmd.extras, cmd.sync);
            } else {
                result = null;
            }
            if (cmd.sync) {
                try {
                    if (DEBUG) Log.v(TAG, "Reporting command complete");
                    mSession.wallpaperCommandComplete(mWindow.asBinder(), result);
                } catch (RemoteException e) {
                }
            }
        }
        void reportSurfaceDestroyed() {
            if (mSurfaceCreated) {
                mSurfaceCreated = false;
                SurfaceHolder.Callback callbacks[];
                synchronized (mSurfaceHolder.mCallbacks) {
                    callbacks = new SurfaceHolder.Callback[
                            mSurfaceHolder.mCallbacks.size()];
                    mSurfaceHolder.mCallbacks.toArray(callbacks);
                }
                for (SurfaceHolder.Callback c : callbacks) {
                    c.surfaceDestroyed(mSurfaceHolder);
                }
                if (DEBUG) Log.v(TAG, "onSurfaceDestroyed("
                        + mSurfaceHolder + "): " + this);
                onSurfaceDestroyed(mSurfaceHolder);
            }
        }
        void detach() {
            if (mDestroyed) {
                return;
            }
            mDestroyed = true;
            if (mVisible) {
                mVisible = false;
                if (DEBUG) Log.v(TAG, "onVisibilityChanged(false): " + this);
                onVisibilityChanged(false);
            }
            reportSurfaceDestroyed();
            if (DEBUG) Log.v(TAG, "onDestroy(): " + this);
            onDestroy();
            unregisterReceiver(mReceiver);
            if (mCreated) {
                try {
                    if (DEBUG) Log.v(TAG, "Removing window and destroying surface "
                            + mSurfaceHolder.getSurface() + " of: " + this);
                    mSession.remove(mWindow);
                } catch (RemoteException e) {
                }
                mSurfaceHolder.mSurface.release();
                mCreated = false;
            }
        }
    }
    class IWallpaperEngineWrapper extends IWallpaperEngine.Stub
            implements HandlerCaller.Callback {
        private final HandlerCaller mCaller;
        final IWallpaperConnection mConnection;
        final IBinder mWindowToken;
        final int mWindowType;
        final boolean mIsPreview;
        int mReqWidth;
        int mReqHeight;
        Engine mEngine;
        IWallpaperEngineWrapper(WallpaperService context,
                IWallpaperConnection conn, IBinder windowToken,
                int windowType, boolean isPreview, int reqWidth, int reqHeight) {
            if (DEBUG && mCallbackLooper != null) {
                mCallbackLooper.setMessageLogging(new LogPrinter(Log.VERBOSE, TAG));
            }
            mCaller = new HandlerCaller(context,
                    mCallbackLooper != null
                            ? mCallbackLooper : context.getMainLooper(),
                    this);
            mConnection = conn;
            mWindowToken = windowToken;
            mWindowType = windowType;
            mIsPreview = isPreview;
            mReqWidth = reqWidth;
            mReqHeight = reqHeight;
            Message msg = mCaller.obtainMessage(DO_ATTACH);
            mCaller.sendMessage(msg);
        }
        public void setDesiredSize(int width, int height) {
            Message msg = mCaller.obtainMessageII(DO_SET_DESIRED_SIZE, width, height);
            mCaller.sendMessage(msg);
        }
        public void setVisibility(boolean visible) {
            Message msg = mCaller.obtainMessageI(MSG_VISIBILITY_CHANGED,
                    visible ? 1 : 0);
            mCaller.sendMessage(msg);
        }
        public void dispatchPointer(MotionEvent event) {
            if (mEngine != null) {
                mEngine.mWindow.onDispatchPointer(event, event.getEventTime(), false);
            }
        }
        public void destroy() {
            Message msg = mCaller.obtainMessage(DO_DETACH);
            mCaller.sendMessage(msg);
        }
        public void executeMessage(Message message) {
            switch (message.what) {
                case DO_ATTACH: {
                    try {
                        mConnection.attachEngine(this);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Wallpaper host disappeared", e);
                        return;
                    }
                    Engine engine = onCreateEngine();
                    mEngine = engine;
                    mActiveEngines.add(engine);
                    engine.attach(this);
                    return;
                }
                case DO_DETACH: {
                    mActiveEngines.remove(mEngine);
                    mEngine.detach();
                    return;
                }
                case DO_SET_DESIRED_SIZE: {
                    mEngine.doDesiredSizeChanged(message.arg1, message.arg2);
                    return;
                }
                case MSG_UPDATE_SURFACE:
                    mEngine.updateSurface(true, false);
                    break;
                case MSG_VISIBILITY_CHANGED:
                    if (DEBUG) Log.v(TAG, "Visibility change in " + mEngine
                            + ": " + message.arg1);
                    mEngine.doVisibilityChanged(message.arg1 != 0);
                    break;
                case MSG_WALLPAPER_OFFSETS: {
                    mEngine.doOffsetsChanged();
                } break;
                case MSG_WALLPAPER_COMMAND: {
                    WallpaperCommand cmd = (WallpaperCommand)message.obj;
                    mEngine.doCommand(cmd);
                } break;
                case MSG_WINDOW_RESIZED: {
                    final boolean reportDraw = message.arg1 != 0;
                    mEngine.updateSurface(true, false);
                    mEngine.doOffsetsChanged();
                    if (reportDraw) {
                        try {
                            mEngine.mSession.finishDrawing(mEngine.mWindow);
                        } catch (RemoteException e) {
                        }
                    }
                } break;
                case MSG_TOUCH_EVENT: {
                    MotionEvent ev = (MotionEvent)message.obj;
                    synchronized (mEngine.mLock) {
                        if (mEngine.mPendingMove == ev) {
                            mEngine.mPendingMove = null;
                        }
                    }
                    if (DEBUG) Log.v(TAG, "Delivering touch event: " + ev);
                    mEngine.onTouchEvent(ev);
                    ev.recycle();
                } break;
                default :
                    Log.w(TAG, "Unknown message type " + message.what);
            }
        }
    }
    class IWallpaperServiceWrapper extends IWallpaperService.Stub {
        private final WallpaperService mTarget;
        public IWallpaperServiceWrapper(WallpaperService context) {
            mTarget = context;
        }
        public void attach(IWallpaperConnection conn, IBinder windowToken,
                int windowType, boolean isPreview, int reqWidth, int reqHeight) {
            new IWallpaperEngineWrapper(mTarget, conn, windowToken,
                    windowType, isPreview, reqWidth, reqHeight);
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i=0; i<mActiveEngines.size(); i++) {
            mActiveEngines.get(i).detach();
        }
        mActiveEngines.clear();
    }
    @Override
    public final IBinder onBind(Intent intent) {
        return new IWallpaperServiceWrapper(this);
    }
    public void setCallbackLooper(Looper looper) {
        mCallbackLooper = looper;
    }
    public abstract Engine onCreateEngine();
}
