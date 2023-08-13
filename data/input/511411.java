public final class ViewRoot extends Handler implements ViewParent,
        View.AttachInfo.Callbacks {
    private static final String TAG = "ViewRoot";
    private static final boolean DBG = false;
    private static final boolean SHOW_FPS = false;
    @SuppressWarnings({"ConstantConditionalExpression"})
    private static final boolean LOCAL_LOGV = false ? Config.LOGD : Config.LOGV;
    private static final boolean DEBUG_DRAW = false || LOCAL_LOGV;
    private static final boolean DEBUG_LAYOUT = false || LOCAL_LOGV;
    private static final boolean DEBUG_INPUT_RESIZE = false || LOCAL_LOGV;
    private static final boolean DEBUG_ORIENTATION = false || LOCAL_LOGV;
    private static final boolean DEBUG_TRACKBALL = false || LOCAL_LOGV;
    private static final boolean DEBUG_IMF = false || LOCAL_LOGV;
    private static final boolean DEBUG_CONFIGURATION = false || LOCAL_LOGV;
    private static final boolean WATCH_POINTER = false;
    private static final boolean MEASURE_LATENCY = false;
    private static LatencyTimer lt;
    static final int MAX_TRACKBALL_DELAY = 250;
    static long sInstanceCount = 0;
    static IWindowSession sWindowSession;
    static final Object mStaticInit = new Object();
    static boolean mInitialized = false;
    static final ThreadLocal<RunQueue> sRunQueues = new ThreadLocal<RunQueue>();
    static final ArrayList<Runnable> sFirstDrawHandlers = new ArrayList<Runnable>();
    static boolean sFirstDrawComplete = false;
    static final ArrayList<ComponentCallbacks> sConfigCallbacks
            = new ArrayList<ComponentCallbacks>();
    private static int sDrawTime;
    long mLastTrackballTime = 0;
    final TrackballAxis mTrackballAxisX = new TrackballAxis();
    final TrackballAxis mTrackballAxisY = new TrackballAxis();
    final int[] mTmpLocation = new int[2];
    final InputMethodCallback mInputMethodCallback;
    final SparseArray<Object> mPendingEvents = new SparseArray<Object>();
    int mPendingEventSeq = 0;
    final Thread mThread;
    final WindowLeaked mLocation;
    final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();
    final W mWindow;
    View mView;
    View mFocusedView;
    View mRealFocusedView;  
    int mViewVisibility;
    boolean mAppVisible = true;
    final Region mTransparentRegion;
    final Region mPreviousTransparentRegion;
    int mWidth;
    int mHeight;
    Rect mDirty; 
    boolean mIsAnimating;
    CompatibilityInfo.Translator mTranslator;
    final View.AttachInfo mAttachInfo;
    final Rect mTempRect; 
    final Rect mVisRect; 
    boolean mTraversalScheduled;
    boolean mWillDrawSoon;
    boolean mLayoutRequested;
    boolean mFirst;
    boolean mReportNextDraw;
    boolean mFullRedrawNeeded;
    boolean mNewSurfaceNeeded;
    boolean mHasHadWindowFocus;
    boolean mLastWasImTarget;
    boolean mWindowAttributesChanged = false;
    private final Surface mSurface = new Surface();
    boolean mAdded;
    boolean mAddedTouchMode;
     int mAddNesting;
    final Rect mWinFrame; 
    final Rect mPendingVisibleInsets = new Rect();
    final Rect mPendingContentInsets = new Rect();
    final ViewTreeObserver.InternalInsetsInfo mLastGivenInsets
            = new ViewTreeObserver.InternalInsetsInfo();
    final Configuration mLastConfiguration = new Configuration();
    final Configuration mPendingConfiguration = new Configuration();
    class ResizedInfo {
        Rect coveredInsets;
        Rect visibleInsets;
        Configuration newConfig;
    }
    boolean mScrollMayChange;
    int mSoftInputMode;
    View mLastScrolledFocus;
    int mScrollY;
    int mCurScrollY;
    Scroller mScroller;
    EGL10 mEgl;
    EGLDisplay mEglDisplay;
    EGLContext mEglContext;
    EGLSurface mEglSurface;
    GL11 mGL;
    Canvas mGlCanvas;
    boolean mUseGL;
    boolean mGlWanted;
    final ViewConfiguration mViewConfiguration;
    AudioManager mAudioManager;
    private final int mDensity;
    public static IWindowSession getWindowSession(Looper mainLooper) {
        synchronized (mStaticInit) {
            if (!mInitialized) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance(mainLooper);
                    sWindowSession = IWindowManager.Stub.asInterface(
                            ServiceManager.getService("window"))
                            .openSession(imm.getClient(), imm.getInputContext());
                    mInitialized = true;
                } catch (RemoteException e) {
                }
            }
            return sWindowSession;
        }
    }
    public ViewRoot(Context context) {
        super();
        if (MEASURE_LATENCY && lt == null) {
            lt = new LatencyTimer(100, 1000);
        }
        getWindowSession(context.getMainLooper());
        mThread = Thread.currentThread();
        mLocation = new WindowLeaked(null);
        mLocation.fillInStackTrace();
        mWidth = -1;
        mHeight = -1;
        mDirty = new Rect();
        mTempRect = new Rect();
        mVisRect = new Rect();
        mWinFrame = new Rect();
        mWindow = new W(this, context);
        mInputMethodCallback = new InputMethodCallback(this);
        mViewVisibility = View.GONE;
        mTransparentRegion = new Region();
        mPreviousTransparentRegion = new Region();
        mFirst = true; 
        mAdded = false;
        mAttachInfo = new View.AttachInfo(sWindowSession, mWindow, this, this);
        mViewConfiguration = ViewConfiguration.get(context);
        mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }
    public static long getInstanceCount() {
        return sInstanceCount;
    }
    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }
    public static void addConfigCallback(ComponentCallbacks callback) {
        synchronized (sConfigCallbacks) {
            sConfigCallbacks.add(callback);
        }
    }
    private boolean mProfile = false;
    public void profile() {
        mProfile = true;
    }
    static boolean isInTouchMode() {
        if (mInitialized) {
            try {
                return sWindowSession.getInTouchMode();
            } catch (RemoteException e) {
            }
        }
        return false;
    }
    private void initializeGL() {
        initializeGLInner();
        int err = mEgl.eglGetError();
        if (err != EGL10.EGL_SUCCESS) {
            destroyGL();
            mGlWanted = false;
        }
    }
    private void initializeGLInner() {
        final EGL10 egl = (EGL10) EGLContext.getEGL();
        mEgl = egl;
        final EGLDisplay eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        mEglDisplay = eglDisplay;
        int[] version = new int[2];
        egl.eglInitialize(eglDisplay, version);
        final int[] configSpec = {
                EGL10.EGL_RED_SIZE,      5,
                EGL10.EGL_GREEN_SIZE,    6,
                EGL10.EGL_BLUE_SIZE,     5,
                EGL10.EGL_DEPTH_SIZE,    0,
                EGL10.EGL_NONE
        };
        final EGLConfig[] configs = new EGLConfig[1];
        final int[] num_config = new int[1];
        egl.eglChooseConfig(eglDisplay, configSpec, configs, 1, num_config);
        final EGLConfig config = configs[0];
        final EGLContext context = egl.eglCreateContext(eglDisplay, config,
                EGL10.EGL_NO_CONTEXT, null);
        mEglContext = context;
        final EGLSurface surface = egl.eglCreateWindowSurface(eglDisplay, config, mHolder, null);
        mEglSurface = surface;
        egl.eglMakeCurrent(eglDisplay, surface, surface, context);
        final GL11 gl = (GL11) context.getGL();
        mGL = gl;
        mGlCanvas = new Canvas(gl);
        mUseGL = true;
    }
    private void destroyGL() {
        nativeAbandonGlCaches();
        mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEgl.eglDestroyContext(mEglDisplay, mEglContext);
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
        mEgl.eglTerminate(mEglDisplay);
        mEglContext = null;
        mEglSurface = null;
        mEglDisplay = null;
        mEgl = null;
        mGlCanvas = null;
        mGL = null;
        mUseGL = false;
    }
    private void checkEglErrors() {
        if (mUseGL) {
            int err = mEgl.eglGetError();
            if (err != EGL10.EGL_SUCCESS) {
                destroyGL();
                if (err != EGL11.EGL_CONTEXT_LOST) {
                    mGlWanted = false;
                }
            }
        }
    }
    public void setView(View view, WindowManager.LayoutParams attrs,
            View panelParentView) {
        synchronized (this) {
            if (mView == null) {
                mView = view;
                mWindowAttributes.copyFrom(attrs);
                attrs = mWindowAttributes;
                Resources resources = mView.getContext().getResources();
                CompatibilityInfo compatibilityInfo = resources.getCompatibilityInfo();
                mTranslator = compatibilityInfo.getTranslator();
                if (mTranslator != null || !compatibilityInfo.supportsScreen()) {
                    mSurface.setCompatibleDisplayMetrics(resources.getDisplayMetrics(),
                            mTranslator);
                }
                boolean restore = false;
                if (mTranslator != null) {
                    restore = true;
                    attrs.backup();
                    mTranslator.translateWindowLayout(attrs);
                }
                if (DEBUG_LAYOUT) Log.d(TAG, "WindowLayout in setView:" + attrs);
                if (!compatibilityInfo.supportsScreen()) {
                    attrs.flags |= WindowManager.LayoutParams.FLAG_COMPATIBLE_WINDOW;
                }
                mSoftInputMode = attrs.softInputMode;
                mWindowAttributesChanged = true;
                mAttachInfo.mRootView = view;
                mAttachInfo.mScalingRequired = mTranslator != null;
                mAttachInfo.mApplicationScale =
                        mTranslator == null ? 1.0f : mTranslator.applicationScale;
                if (panelParentView != null) {
                    mAttachInfo.mPanelParentWindowToken
                            = panelParentView.getApplicationWindowToken();
                }
                mAdded = true;
                int res; 
                requestLayout();
                try {
                    res = sWindowSession.add(mWindow, mWindowAttributes,
                            getHostVisibility(), mAttachInfo.mContentInsets);
                } catch (RemoteException e) {
                    mAdded = false;
                    mView = null;
                    mAttachInfo.mRootView = null;
                    unscheduleTraversals();
                    throw new RuntimeException("Adding window failed", e);
                } finally {
                    if (restore) {
                        attrs.restore();
                    }
                }
                if (mTranslator != null) {
                    mTranslator.translateRectInScreenToAppWindow(mAttachInfo.mContentInsets);
                }
                mPendingContentInsets.set(mAttachInfo.mContentInsets);
                mPendingVisibleInsets.set(0, 0, 0, 0);
                if (Config.LOGV) Log.v("ViewRoot", "Added window " + mWindow);
                if (res < WindowManagerImpl.ADD_OKAY) {
                    mView = null;
                    mAttachInfo.mRootView = null;
                    mAdded = false;
                    unscheduleTraversals();
                    switch (res) {
                        case WindowManagerImpl.ADD_BAD_APP_TOKEN:
                        case WindowManagerImpl.ADD_BAD_SUBWINDOW_TOKEN:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window -- token " + attrs.token
                                + " is not valid; is your activity running?");
                        case WindowManagerImpl.ADD_NOT_APP_TOKEN:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window -- token " + attrs.token
                                + " is not for an application");
                        case WindowManagerImpl.ADD_APP_EXITING:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window -- app for token " + attrs.token
                                + " is exiting");
                        case WindowManagerImpl.ADD_DUPLICATE_ADD:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window -- window " + mWindow
                                + " has already been added");
                        case WindowManagerImpl.ADD_STARTING_NOT_NEEDED:
                            return;
                        case WindowManagerImpl.ADD_MULTIPLE_SINGLETON:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window " + mWindow +
                                " -- another window of this type already exists");
                        case WindowManagerImpl.ADD_PERMISSION_DENIED:
                            throw new WindowManagerImpl.BadTokenException(
                                "Unable to add window " + mWindow +
                                " -- permission denied for this window type");
                    }
                    throw new RuntimeException(
                        "Unable to add window -- unknown error code " + res);
                }
                view.assignParent(this);
                mAddedTouchMode = (res&WindowManagerImpl.ADD_FLAG_IN_TOUCH_MODE) != 0;
                mAppVisible = (res&WindowManagerImpl.ADD_FLAG_APP_VISIBLE) != 0;
            }
        }
    }
    public View getView() {
        return mView;
    }
    final WindowLeaked getLocation() {
        return mLocation;
    }
    void setLayoutParams(WindowManager.LayoutParams attrs, boolean newView) {
        synchronized (this) {
            int oldSoftInputMode = mWindowAttributes.softInputMode;
            int compatibleWindowFlag =
                mWindowAttributes.flags & WindowManager.LayoutParams.FLAG_COMPATIBLE_WINDOW;
            mWindowAttributes.copyFrom(attrs);
            mWindowAttributes.flags |= compatibleWindowFlag;
            if (newView) {
                mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            if ((attrs.softInputMode&WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                    == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED) {
                mWindowAttributes.softInputMode = (mWindowAttributes.softInputMode
                        & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                        | (oldSoftInputMode
                                & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
            }
            mWindowAttributesChanged = true;
            scheduleTraversals();
        }
    }
    void handleAppVisibility(boolean visible) {
        if (mAppVisible != visible) {
            mAppVisible = visible;
            scheduleTraversals();
        }
    }
    void handleGetNewSurface() {
        mNewSurfaceNeeded = true;
        mFullRedrawNeeded = true;
        scheduleTraversals();
    }
    public void requestLayout() {
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
    public boolean isLayoutRequested() {
        return mLayoutRequested;
    }
    public void invalidateChild(View child, Rect dirty) {
        checkThread();
        if (DEBUG_DRAW) Log.v(TAG, "Invalidate child: " + dirty);
        if (mCurScrollY != 0 || mTranslator != null) {
            mTempRect.set(dirty);
            dirty = mTempRect;
            if (mCurScrollY != 0) {
               dirty.offset(0, -mCurScrollY);
            }
            if (mTranslator != null) {
                mTranslator.translateRectInAppWindowToScreen(dirty);
            }
            if (mAttachInfo.mScalingRequired) {
                dirty.inset(-1, -1);
            }
        }
        mDirty.union(dirty);
        if (!mWillDrawSoon) {
            scheduleTraversals();
        }
    }
    public ViewParent getParent() {
        return null;
    }
    public ViewParent invalidateChildInParent(final int[] location, final Rect dirty) {
        invalidateChild(null, dirty);
        return null;
    }
    public boolean getChildVisibleRect(View child, Rect r, android.graphics.Point offset) {
        if (child != mView) {
            throw new RuntimeException("child is not mine, honest!");
        }
        return r.intersect(0, 0, mWidth, mHeight);
    }
    public void bringChildToFront(View child) {
    }
    public void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            sendEmptyMessage(DO_TRAVERSAL);
        }
    }
    public void unscheduleTraversals() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            removeMessages(DO_TRAVERSAL);
        }
    }
    int getHostVisibility() {
        return mAppVisible ? mView.getVisibility() : View.GONE;
    }
    private void performTraversals() {
        final View host = mView;
        if (DBG) {
            System.out.println("======================================");
            System.out.println("performTraversals");
            host.debug();
        }
        if (host == null || !mAdded)
            return;
        mTraversalScheduled = false;
        mWillDrawSoon = true;
        boolean windowResizesToFitContent = false;
        boolean fullRedrawNeeded = mFullRedrawNeeded;
        boolean newSurface = false;
        WindowManager.LayoutParams lp = mWindowAttributes;
        int desiredWindowWidth;
        int desiredWindowHeight;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        final View.AttachInfo attachInfo = mAttachInfo;
        final int viewVisibility = getHostVisibility();
        boolean viewVisibilityChanged = mViewVisibility != viewVisibility
                || mNewSurfaceNeeded;
        float appScale = mAttachInfo.mApplicationScale;
        WindowManager.LayoutParams params = null;
        if (mWindowAttributesChanged) {
            mWindowAttributesChanged = false;
            params = lp;
        }
        Rect frame = mWinFrame;
        if (mFirst) {
            fullRedrawNeeded = true;
            mLayoutRequested = true;
            DisplayMetrics packageMetrics =
                mView.getContext().getResources().getDisplayMetrics();
            desiredWindowWidth = packageMetrics.widthPixels;
            desiredWindowHeight = packageMetrics.heightPixels;
            attachInfo.mSurface = mSurface;
            attachInfo.mTranslucentWindow = lp.format != PixelFormat.OPAQUE;
            attachInfo.mHasWindowFocus = false;
            attachInfo.mWindowVisibility = viewVisibility;
            attachInfo.mRecomputeGlobalAttributes = false;
            attachInfo.mKeepScreenOn = false;
            viewVisibilityChanged = false;
            mLastConfiguration.setTo(host.getResources().getConfiguration());
            host.dispatchAttachedToWindow(attachInfo, 0);
        } else {
            desiredWindowWidth = frame.width();
            desiredWindowHeight = frame.height();
            if (desiredWindowWidth != mWidth || desiredWindowHeight != mHeight) {
                if (DEBUG_ORIENTATION) Log.v("ViewRoot",
                        "View " + host + " resized to: " + frame);
                fullRedrawNeeded = true;
                mLayoutRequested = true;
                windowResizesToFitContent = true;
            }
        }
        if (viewVisibilityChanged) {
            attachInfo.mWindowVisibility = viewVisibility;
            host.dispatchWindowVisibilityChanged(viewVisibility);
            if (viewVisibility != View.VISIBLE || mNewSurfaceNeeded) {
                if (mUseGL) {
                    destroyGL();
                }
            }
            if (viewVisibility == View.GONE) {
                mHasHadWindowFocus = false;
            }
        }
        boolean insetsChanged = false;
        if (mLayoutRequested) {
            getRunQueue().executeActions(attachInfo.mHandler);
            if (mFirst) {
                host.fitSystemWindows(mAttachInfo.mContentInsets);
                mAttachInfo.mInTouchMode = !mAddedTouchMode;
                ensureTouchModeLocally(mAddedTouchMode);
            } else {
                if (!mAttachInfo.mContentInsets.equals(mPendingContentInsets)) {
                    mAttachInfo.mContentInsets.set(mPendingContentInsets);
                    host.fitSystemWindows(mAttachInfo.mContentInsets);
                    insetsChanged = true;
                    if (DEBUG_LAYOUT) Log.v(TAG, "Content insets changing to: "
                            + mAttachInfo.mContentInsets);
                }
                if (!mAttachInfo.mVisibleInsets.equals(mPendingVisibleInsets)) {
                    mAttachInfo.mVisibleInsets.set(mPendingVisibleInsets);
                    if (DEBUG_LAYOUT) Log.v(TAG, "Visible insets changing to: "
                            + mAttachInfo.mVisibleInsets);
                }
                if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT
                        || lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    windowResizesToFitContent = true;
                    DisplayMetrics packageMetrics =
                        mView.getContext().getResources().getDisplayMetrics();
                    desiredWindowWidth = packageMetrics.widthPixels;
                    desiredWindowHeight = packageMetrics.heightPixels;
                }
            }
            childWidthMeasureSpec = getRootMeasureSpec(desiredWindowWidth, lp.width);
            childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
            if (DEBUG_ORIENTATION || DEBUG_LAYOUT) Log.v("ViewRoot",
                    "Measuring " + host + " in display " + desiredWindowWidth
                    + "x" + desiredWindowHeight + "...");
            host.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (DBG) {
                System.out.println("======================================");
                System.out.println("performTraversals -- after measure");
                host.debug();
            }
        }
        if (attachInfo.mRecomputeGlobalAttributes) {
            attachInfo.mRecomputeGlobalAttributes = false;
            boolean oldVal = attachInfo.mKeepScreenOn;
            attachInfo.mKeepScreenOn = false;
            host.dispatchCollectViewAttributes(0);
            if (attachInfo.mKeepScreenOn != oldVal) {
                params = lp;
            }
        }
        if (mFirst || attachInfo.mViewVisibilityChanged) {
            attachInfo.mViewVisibilityChanged = false;
            int resizeMode = mSoftInputMode &
                    WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
            if (resizeMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED) {
                final int N = attachInfo.mScrollContainers.size();
                for (int i=0; i<N; i++) {
                    if (attachInfo.mScrollContainers.get(i).isShown()) {
                        resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
                    }
                }
                if (resizeMode == 0) {
                    resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
                }
                if ((lp.softInputMode &
                        WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) != resizeMode) {
                    lp.softInputMode = (lp.softInputMode &
                            ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) |
                            resizeMode;
                    params = lp;
                }
            }
        }
        if (params != null && (host.mPrivateFlags & View.REQUEST_TRANSPARENT_REGIONS) != 0) {
            if (!PixelFormat.formatHasAlpha(params.format)) {
                params.format = PixelFormat.TRANSLUCENT;
            }
        }
        boolean windowShouldResize = mLayoutRequested && windowResizesToFitContent
            && ((mWidth != host.mMeasuredWidth || mHeight != host.mMeasuredHeight)
                || (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
                        frame.width() < desiredWindowWidth && frame.width() != mWidth)
                || (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT &&
                        frame.height() < desiredWindowHeight && frame.height() != mHeight));
        final boolean computesInternalInsets =
                attachInfo.mTreeObserver.hasComputeInternalInsetsListeners();
        boolean insetsPending = false;
        int relayoutResult = 0;
        if (mFirst || windowShouldResize || insetsChanged
                || viewVisibilityChanged || params != null) {
            if (viewVisibility == View.VISIBLE) {
                insetsPending = computesInternalInsets
                        && (mFirst || viewVisibilityChanged);
                if (mWindowAttributes.memoryType == WindowManager.LayoutParams.MEMORY_TYPE_GPU) {
                    if (params == null) {
                        params = mWindowAttributes;
                    }
                    mGlWanted = true;
                }
            }
            boolean initialized = false;
            boolean contentInsetsChanged = false;
            boolean visibleInsetsChanged;
            try {
                boolean hadSurface = mSurface.isValid();
                int fl = 0;
                if (params != null) {
                    fl = params.flags;
                    if (attachInfo.mKeepScreenOn) {
                        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    }
                }
                if (DEBUG_LAYOUT) {
                    Log.i(TAG, "host=w:" + host.mMeasuredWidth + ", h:" +
                            host.mMeasuredHeight + ", params=" + params);
                }
                relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
                if (params != null) {
                    params.flags = fl;
                }
                if (DEBUG_LAYOUT) Log.v(TAG, "relayout: frame=" + frame.toShortString()
                        + " content=" + mPendingContentInsets.toShortString()
                        + " visible=" + mPendingVisibleInsets.toShortString()
                        + " surface=" + mSurface);
                if (mPendingConfiguration.seq != 0) {
                    if (DEBUG_CONFIGURATION) Log.v(TAG, "Visible with new config: "
                            + mPendingConfiguration);
                    updateConfiguration(mPendingConfiguration, !mFirst);
                    mPendingConfiguration.seq = 0;
                }
                contentInsetsChanged = !mPendingContentInsets.equals(
                        mAttachInfo.mContentInsets);
                visibleInsetsChanged = !mPendingVisibleInsets.equals(
                        mAttachInfo.mVisibleInsets);
                if (contentInsetsChanged) {
                    mAttachInfo.mContentInsets.set(mPendingContentInsets);
                    host.fitSystemWindows(mAttachInfo.mContentInsets);
                    if (DEBUG_LAYOUT) Log.v(TAG, "Content insets changing to: "
                            + mAttachInfo.mContentInsets);
                }
                if (visibleInsetsChanged) {
                    mAttachInfo.mVisibleInsets.set(mPendingVisibleInsets);
                    if (DEBUG_LAYOUT) Log.v(TAG, "Visible insets changing to: "
                            + mAttachInfo.mVisibleInsets);
                }
                if (!hadSurface) {
                    if (mSurface.isValid()) {
                        newSurface = true;
                        fullRedrawNeeded = true;
                        mPreviousTransparentRegion.setEmpty();
                        if (mGlWanted && !mUseGL) {
                            initializeGL();
                            initialized = mGlCanvas != null;
                        }
                    }
                } else if (!mSurface.isValid()) {
                    mLastScrolledFocus = null;
                    mScrollY = mCurScrollY = 0;
                    if (mScroller != null) {
                        mScroller.abortAnimation();
                    }
                }
            } catch (RemoteException e) {
            }
            if (DEBUG_ORIENTATION) Log.v(
                    "ViewRoot", "Relayout returned: frame=" + frame + ", surface=" + mSurface);
            attachInfo.mWindowLeft = frame.left;
            attachInfo.mWindowTop = frame.top;
            mWidth = frame.width();
            mHeight = frame.height();
            if (initialized) {
                mGlCanvas.setViewport((int) (mWidth * appScale + 0.5f),
                        (int) (mHeight * appScale + 0.5f));
            }
            boolean focusChangedDueToTouchMode = ensureTouchModeLocally(
                    (relayoutResult&WindowManagerImpl.RELAYOUT_IN_TOUCH_MODE) != 0);
            if (focusChangedDueToTouchMode || mWidth != host.mMeasuredWidth
                    || mHeight != host.mMeasuredHeight || contentInsetsChanged) {
                childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
                childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
                if (DEBUG_LAYOUT) Log.v(TAG, "Ooops, something changed!  mWidth="
                        + mWidth + " measuredWidth=" + host.mMeasuredWidth
                        + " mHeight=" + mHeight
                        + " measuredHeight" + host.mMeasuredHeight
                        + " coveredInsetsChanged=" + contentInsetsChanged);
                host.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                int width = host.mMeasuredWidth;
                int height = host.mMeasuredHeight;
                boolean measureAgain = false;
                if (lp.horizontalWeight > 0.0f) {
                    width += (int) ((mWidth - width) * lp.horizontalWeight);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                            MeasureSpec.EXACTLY);
                    measureAgain = true;
                }
                if (lp.verticalWeight > 0.0f) {
                    height += (int) ((mHeight - height) * lp.verticalWeight);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.EXACTLY);
                    measureAgain = true;
                }
                if (measureAgain) {
                    if (DEBUG_LAYOUT) Log.v(TAG,
                            "And hey let's measure once more: width=" + width
                            + " height=" + height);
                    host.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                }
                mLayoutRequested = true;
            }
        }
        final boolean didLayout = mLayoutRequested;
        boolean triggerGlobalLayoutListener = didLayout
                || attachInfo.mRecomputeGlobalAttributes;
        if (didLayout) {
            mLayoutRequested = false;
            mScrollMayChange = true;
            if (DEBUG_ORIENTATION || DEBUG_LAYOUT) Log.v(
                "ViewRoot", "Laying out " + host + " to (" +
                host.mMeasuredWidth + ", " + host.mMeasuredHeight + ")");
            long startTime = 0L;
            if (Config.DEBUG && ViewDebug.profileLayout) {
                startTime = SystemClock.elapsedRealtime();
            }
            host.layout(0, 0, host.mMeasuredWidth, host.mMeasuredHeight);
            if (Config.DEBUG && ViewDebug.consistencyCheckEnabled) {
                if (!host.dispatchConsistencyCheck(ViewDebug.CONSISTENCY_LAYOUT)) {
                    throw new IllegalStateException("The view hierarchy is an inconsistent state,"
                            + "please refer to the logs with the tag "
                            + ViewDebug.CONSISTENCY_LOG_TAG + " for more infomation.");
                }
            }
            if (Config.DEBUG && ViewDebug.profileLayout) {
                EventLog.writeEvent(60001, SystemClock.elapsedRealtime() - startTime);
            }
            if ((host.mPrivateFlags & View.REQUEST_TRANSPARENT_REGIONS) != 0) {
                host.getLocationInWindow(mTmpLocation);
                mTransparentRegion.set(mTmpLocation[0], mTmpLocation[1],
                        mTmpLocation[0] + host.mRight - host.mLeft,
                        mTmpLocation[1] + host.mBottom - host.mTop);
                host.gatherTransparentRegion(mTransparentRegion);
                if (mTranslator != null) {
                    mTranslator.translateRegionInWindowToScreen(mTransparentRegion);
                }
                if (!mTransparentRegion.equals(mPreviousTransparentRegion)) {
                    mPreviousTransparentRegion.set(mTransparentRegion);
                    try {
                        sWindowSession.setTransparentRegion(mWindow, mTransparentRegion);
                    } catch (RemoteException e) {
                    }
                }
            }
            if (DBG) {
                System.out.println("======================================");
                System.out.println("performTraversals -- after setFrame");
                host.debug();
            }
        }
        if (triggerGlobalLayoutListener) {
            attachInfo.mRecomputeGlobalAttributes = false;
            attachInfo.mTreeObserver.dispatchOnGlobalLayout();
        }
        if (computesInternalInsets) {
            ViewTreeObserver.InternalInsetsInfo insets = attachInfo.mGivenInternalInsets;
            final Rect givenContent = attachInfo.mGivenInternalInsets.contentInsets;
            final Rect givenVisible = attachInfo.mGivenInternalInsets.visibleInsets;
            givenContent.left = givenContent.top = givenContent.right
                    = givenContent.bottom = givenVisible.left = givenVisible.top
                    = givenVisible.right = givenVisible.bottom = 0;
            attachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
            Rect contentInsets = insets.contentInsets;
            Rect visibleInsets = insets.visibleInsets;
            if (mTranslator != null) {
                contentInsets = mTranslator.getTranslatedContentInsets(contentInsets);
                visibleInsets = mTranslator.getTranslatedVisbileInsets(visibleInsets);
            }
            if (insetsPending || !mLastGivenInsets.equals(insets)) {
                mLastGivenInsets.set(insets);
                try {
                    sWindowSession.setInsets(mWindow, insets.mTouchableInsets,
                            contentInsets, visibleInsets);
                } catch (RemoteException e) {
                }
            }
        }
        if (mFirst) {
            if (DEBUG_INPUT_RESIZE) Log.v(TAG, "First: mView.hasFocus()="
                    + mView.hasFocus());
            if (mView != null) {
                if (!mView.hasFocus()) {
                    mView.requestFocus(View.FOCUS_FORWARD);
                    mFocusedView = mRealFocusedView = mView.findFocus();
                    if (DEBUG_INPUT_RESIZE) Log.v(TAG, "First: requested focused view="
                            + mFocusedView);
                } else {
                    mRealFocusedView = mView.findFocus();
                    if (DEBUG_INPUT_RESIZE) Log.v(TAG, "First: existing focused view="
                            + mRealFocusedView);
                }
            }
        }
        mFirst = false;
        mWillDrawSoon = false;
        mNewSurfaceNeeded = false;
        mViewVisibility = viewVisibility;
        if (mAttachInfo.mHasWindowFocus) {
            final boolean imTarget = WindowManager.LayoutParams
                    .mayUseInputMethod(mWindowAttributes.flags);
            if (imTarget != mLastWasImTarget) {
                mLastWasImTarget = imTarget;
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null && imTarget) {
                    imm.startGettingWindowFocus(mView);
                    imm.onWindowFocus(mView, mView.findFocus(),
                            mWindowAttributes.softInputMode,
                            !mHasHadWindowFocus, mWindowAttributes.flags);
                }
            }
        }
        boolean cancelDraw = attachInfo.mTreeObserver.dispatchOnPreDraw();
        if (!cancelDraw && !newSurface) {
            mFullRedrawNeeded = false;
            draw(fullRedrawNeeded);
            if ((relayoutResult&WindowManagerImpl.RELAYOUT_FIRST_TIME) != 0
                    || mReportNextDraw) {
                if (LOCAL_LOGV) {
                    Log.v("ViewRoot", "FINISHED DRAWING: " + mWindowAttributes.getTitle());
                }
                mReportNextDraw = false;
                try {
                    sWindowSession.finishDrawing(mWindow);
                } catch (RemoteException e) {
                }
            }
        } else {
            if ((relayoutResult&WindowManagerImpl.RELAYOUT_FIRST_TIME) != 0) {
                mReportNextDraw = true;
            }
            if (fullRedrawNeeded) {
                mFullRedrawNeeded = true;
            }
            scheduleTraversals();
        }
    }
    public void requestTransparentRegion(View child) {
        checkThread();
        if (mView == child) {
            mView.mPrivateFlags |= View.REQUEST_TRANSPARENT_REGIONS;
            mWindowAttributesChanged = true;
        }
    }
    private int getRootMeasureSpec(int windowSize, int rootDimension) {
        int measureSpec;
        switch (rootDimension) {
        case ViewGroup.LayoutParams.MATCH_PARENT:
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
            break;
        case ViewGroup.LayoutParams.WRAP_CONTENT:
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
            break;
        default:
            measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
            break;
        }
        return measureSpec;
    }
    private void draw(boolean fullRedrawNeeded) {
        Surface surface = mSurface;
        if (surface == null || !surface.isValid()) {
            return;
        }
        if (!sFirstDrawComplete) {
            synchronized (sFirstDrawHandlers) {
                sFirstDrawComplete = true;
                for (int i=0; i<sFirstDrawHandlers.size(); i++) {
                    post(sFirstDrawHandlers.get(i));
                }
            }
        }
        scrollToRectOrFocus(null, false);
        if (mAttachInfo.mViewScrollChanged) {
            mAttachInfo.mViewScrollChanged = false;
            mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
        }
        int yoff;
        final boolean scrolling = mScroller != null && mScroller.computeScrollOffset();
        if (scrolling) {
            yoff = mScroller.getCurrY();
        } else {
            yoff = mScrollY;
        }
        if (mCurScrollY != yoff) {
            mCurScrollY = yoff;
            fullRedrawNeeded = true;
        }
        float appScale = mAttachInfo.mApplicationScale;
        boolean scalingRequired = mAttachInfo.mScalingRequired;
        Rect dirty = mDirty;
        if (mUseGL) {
            if (!dirty.isEmpty()) {
                Canvas canvas = mGlCanvas;
                if (mGL != null && canvas != null) {
                    mGL.glDisable(GL_SCISSOR_TEST);
                    mGL.glClearColor(0, 0, 0, 0);
                    mGL.glClear(GL_COLOR_BUFFER_BIT);
                    mGL.glEnable(GL_SCISSOR_TEST);
                    mAttachInfo.mDrawingTime = SystemClock.uptimeMillis();
                    mAttachInfo.mIgnoreDirtyState = true;
                    mView.mPrivateFlags |= View.DRAWN;
                    int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
                    try {
                        canvas.translate(0, -yoff);
                        if (mTranslator != null) {
                            mTranslator.translateCanvas(canvas);
                        }
                        canvas.setScreenDensity(scalingRequired
                                ? DisplayMetrics.DENSITY_DEVICE : 0);
                        mView.draw(canvas);
                        if (Config.DEBUG && ViewDebug.consistencyCheckEnabled) {
                            mView.dispatchConsistencyCheck(ViewDebug.CONSISTENCY_DRAWING);
                        }
                    } finally {
                        canvas.restoreToCount(saveCount);
                    }
                    mAttachInfo.mIgnoreDirtyState = false;
                    mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
                    checkEglErrors();
                    if (SHOW_FPS || Config.DEBUG && ViewDebug.showFps) {
                        int now = (int)SystemClock.elapsedRealtime();
                        if (sDrawTime != 0) {
                            nativeShowFPS(canvas, now - sDrawTime);
                        }
                        sDrawTime = now;
                    }
                }
            }
            if (scrolling) {
                mFullRedrawNeeded = true;
                scheduleTraversals();
            }
            return;
        }
        if (fullRedrawNeeded) {
            mAttachInfo.mIgnoreDirtyState = true;
            dirty.union(0, 0, (int) (mWidth * appScale + 0.5f), (int) (mHeight * appScale + 0.5f));
        }
        if (DEBUG_ORIENTATION || DEBUG_DRAW) {
            Log.v("ViewRoot", "Draw " + mView + "/"
                    + mWindowAttributes.getTitle()
                    + ": dirty={" + dirty.left + "," + dirty.top
                    + "," + dirty.right + "," + dirty.bottom + "} surface="
                    + surface + " surface.isValid()=" + surface.isValid() + ", appScale:" +
                    appScale + ", width=" + mWidth + ", height=" + mHeight);
        }
        Canvas canvas;
        try {
            int left = dirty.left;
            int top = dirty.top;
            int right = dirty.right;
            int bottom = dirty.bottom;
            canvas = surface.lockCanvas(dirty);
            if (left != dirty.left || top != dirty.top || right != dirty.right ||
                    bottom != dirty.bottom) {
                mAttachInfo.mIgnoreDirtyState = true;
            }
            canvas.setDensity(mDensity);
        } catch (Surface.OutOfResourcesException e) {
            Log.e("ViewRoot", "OutOfResourcesException locking surface", e);
            return;
        } catch (IllegalArgumentException e) {
            Log.e("ViewRoot", "IllegalArgumentException locking surface", e);
            return;
        }
        try {
            if (!dirty.isEmpty() || mIsAnimating) {
                long startTime = 0L;
                if (DEBUG_ORIENTATION || DEBUG_DRAW) {
                    Log.v("ViewRoot", "Surface " + surface + " drawing to bitmap w="
                            + canvas.getWidth() + ", h=" + canvas.getHeight());
                }
                if (Config.DEBUG && ViewDebug.profileDrawing) {
                    startTime = SystemClock.elapsedRealtime();
                }
                if (!canvas.isOpaque() || yoff != 0) {
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                }
                dirty.setEmpty();
                mIsAnimating = false;
                mAttachInfo.mDrawingTime = SystemClock.uptimeMillis();
                mView.mPrivateFlags |= View.DRAWN;
                if (DEBUG_DRAW) {
                    Context cxt = mView.getContext();
                    Log.i(TAG, "Drawing: package:" + cxt.getPackageName() +
                            ", metrics=" + cxt.getResources().getDisplayMetrics() +
                            ", compatibilityInfo=" + cxt.getResources().getCompatibilityInfo());
                }
                int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
                try {
                    canvas.translate(0, -yoff);
                    if (mTranslator != null) {
                        mTranslator.translateCanvas(canvas);
                    }
                    canvas.setScreenDensity(scalingRequired
                            ? DisplayMetrics.DENSITY_DEVICE : 0);
                    mView.draw(canvas);
                } finally {
                    mAttachInfo.mIgnoreDirtyState = false;
                    canvas.restoreToCount(saveCount);
                }
                if (Config.DEBUG && ViewDebug.consistencyCheckEnabled) {
                    mView.dispatchConsistencyCheck(ViewDebug.CONSISTENCY_DRAWING);
                }
                if (SHOW_FPS || Config.DEBUG && ViewDebug.showFps) {
                    int now = (int)SystemClock.elapsedRealtime();
                    if (sDrawTime != 0) {
                        nativeShowFPS(canvas, now - sDrawTime);
                    }
                    sDrawTime = now;
                }
                if (Config.DEBUG && ViewDebug.profileDrawing) {
                    EventLog.writeEvent(60000, SystemClock.elapsedRealtime() - startTime);
                }
            }
        } finally {
            surface.unlockCanvasAndPost(canvas);
        }
        if (LOCAL_LOGV) {
            Log.v("ViewRoot", "Surface " + surface + " unlockCanvasAndPost");
        }
        if (scrolling) {
            mFullRedrawNeeded = true;
            scheduleTraversals();
        }
    }
    boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        final View.AttachInfo attachInfo = mAttachInfo;
        final Rect ci = attachInfo.mContentInsets;
        final Rect vi = attachInfo.mVisibleInsets;
        int scrollY = 0;
        boolean handled = false;
        if (vi.left > ci.left || vi.top > ci.top
                || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY = mScrollY;
            View focus = mRealFocusedView;
            if (focus == null || focus.mAttachInfo != mAttachInfo) {
                mRealFocusedView = null;
                return false;
            }
            if (focus != mLastScrolledFocus) {
                rectangle = null;
            }
            if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Eval scroll: focus=" + focus
                    + " rectangle=" + rectangle + " ci=" + ci
                    + " vi=" + vi);
            if (focus == mLastScrolledFocus && !mScrollMayChange
                    && rectangle == null) {
                if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Keeping scroll y="
                        + mScrollY + " vi=" + vi.toShortString());
            } else if (focus != null) {
                mLastScrolledFocus = focus;
                mScrollMayChange = false;
                if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Need to scroll?");
                if (focus.getGlobalVisibleRect(mVisRect, null)) {
                    if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Root w="
                            + mView.getWidth() + " h=" + mView.getHeight()
                            + " ci=" + ci.toShortString()
                            + " vi=" + vi.toShortString());
                    if (rectangle == null) {
                        focus.getFocusedRect(mTempRect);
                        if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Focus " + focus
                                + ": focusRect=" + mTempRect.toShortString());
                        if (mView instanceof ViewGroup) {
                            ((ViewGroup) mView).offsetDescendantRectToMyCoords(
                                    focus, mTempRect);
                        }
                        if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                "Focus in window: focusRect="
                                + mTempRect.toShortString()
                                + " visRect=" + mVisRect.toShortString());
                    } else {
                        mTempRect.set(rectangle);
                        if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                "Request scroll to rect: "
                                + mTempRect.toShortString()
                                + " visRect=" + mVisRect.toShortString());
                    }
                    if (mTempRect.intersect(mVisRect)) {
                        if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                "Focus window visible rect: "
                                + mTempRect.toShortString());
                        if (mTempRect.height() >
                                (mView.getHeight()-vi.top-vi.bottom)) {
                            if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                    "Too tall; leaving scrollY=" + scrollY);
                        } else if ((mTempRect.top-scrollY) < vi.top) {
                            scrollY -= vi.top - (mTempRect.top-scrollY);
                            if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                    "Top covered; scrollY=" + scrollY);
                        } else if ((mTempRect.bottom-scrollY)
                                > (mView.getHeight()-vi.bottom)) {
                            scrollY += (mTempRect.bottom-scrollY)
                                    - (mView.getHeight()-vi.bottom);
                            if (DEBUG_INPUT_RESIZE) Log.v(TAG,
                                    "Bottom covered; scrollY=" + scrollY);
                        }
                        handled = true;
                    }
                }
            }
        }
        if (scrollY != mScrollY) {
            if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Pan scroll changed: old="
                    + mScrollY + " , new=" + scrollY);
            if (!immediate) {
                if (mScroller == null) {
                    mScroller = new Scroller(mView.getContext());
                }
                mScroller.startScroll(0, mScrollY, 0, scrollY-mScrollY);
            } else if (mScroller != null) {
                mScroller.abortAnimation();
            }
            mScrollY = scrollY;
        }
        return handled;
    }
    public void requestChildFocus(View child, View focused) {
        checkThread();
        if (mFocusedView != focused) {
            mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(mFocusedView, focused);
            scheduleTraversals();
        }
        mFocusedView = mRealFocusedView = focused;
        if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Request child focus: focus now "
                + mFocusedView);
    }
    public void clearChildFocus(View child) {
        checkThread();
        View oldFocus = mFocusedView;
        if (DEBUG_INPUT_RESIZE) Log.v(TAG, "Clearing child focus");
        mFocusedView = mRealFocusedView = null;
        if (mView != null && !mView.hasFocus()) {
            if (!mView.requestFocus(View.FOCUS_FORWARD)) {
                mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
            }
        } else if (oldFocus != null) {
            mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
        }
    }
    public void focusableViewAvailable(View v) {
        checkThread();
        if (mView != null && !mView.hasFocus()) {
            v.requestFocus();
        } else {
            mFocusedView = mView.findFocus();
            boolean descendantsHaveDibsOnFocus =
                    (mFocusedView instanceof ViewGroup) &&
                        (((ViewGroup) mFocusedView).getDescendantFocusability() ==
                                ViewGroup.FOCUS_AFTER_DESCENDANTS);
            if (descendantsHaveDibsOnFocus && isViewDescendantOf(v, mFocusedView)) {
                v.requestFocus();
            }
        }
    }
    public void recomputeViewAttributes(View child) {
        checkThread();
        if (mView == child) {
            mAttachInfo.mRecomputeGlobalAttributes = true;
            if (!mWillDrawSoon) {
                scheduleTraversals();
            }
        }
    }
    void dispatchDetachedFromWindow() {
        if (Config.LOGV) Log.v("ViewRoot", "Detaching in " + this + " of " + mSurface);
        if (mView != null) {
            mView.dispatchDetachedFromWindow();
        }
        mView = null;
        mAttachInfo.mRootView = null;
        mAttachInfo.mSurface = null;
        if (mUseGL) {
            destroyGL();
        }
        mSurface.release();
        try {
            sWindowSession.remove(mWindow);
        } catch (RemoteException e) {
        }
    }
    void updateConfiguration(Configuration config, boolean force) {
        if (DEBUG_CONFIGURATION) Log.v(TAG,
                "Applying new config to window "
                + mWindowAttributes.getTitle()
                + ": " + config);
        synchronized (sConfigCallbacks) {
            for (int i=sConfigCallbacks.size()-1; i>=0; i--) {
                sConfigCallbacks.get(i).onConfigurationChanged(config);
            }
        }
        if (mView != null) {
            if (mView != null) {
                config = mView.getResources().getConfiguration();
            }
            if (force || mLastConfiguration.diff(config) != 0) {
                mLastConfiguration.setTo(config);
                mView.dispatchConfigurationChanged(config);
            }
        }
    }
    private static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        final ViewParent theParent = child.getParent();
        return (theParent instanceof ViewGroup) && isViewDescendantOf((View) theParent, parent);
    }
    private static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                forceLayout(group.getChildAt(i));
            }
        }
    }
    public final static int DO_TRAVERSAL = 1000;
    public final static int DIE = 1001;
    public final static int RESIZED = 1002;
    public final static int RESIZED_REPORT = 1003;
    public final static int WINDOW_FOCUS_CHANGED = 1004;
    public final static int DISPATCH_KEY = 1005;
    public final static int DISPATCH_POINTER = 1006;
    public final static int DISPATCH_TRACKBALL = 1007;
    public final static int DISPATCH_APP_VISIBILITY = 1008;
    public final static int DISPATCH_GET_NEW_SURFACE = 1009;
    public final static int FINISHED_EVENT = 1010;
    public final static int DISPATCH_KEY_FROM_IME = 1011;
    public final static int FINISH_INPUT_CONNECTION = 1012;
    public final static int CHECK_FOCUS = 1013;
    public final static int CLOSE_SYSTEM_DIALOGS = 1014;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case View.AttachInfo.INVALIDATE_MSG:
            ((View) msg.obj).invalidate();
            break;
        case View.AttachInfo.INVALIDATE_RECT_MSG:
            final View.AttachInfo.InvalidateInfo info = (View.AttachInfo.InvalidateInfo) msg.obj;
            info.target.invalidate(info.left, info.top, info.right, info.bottom);
            info.release();
            break;
        case DO_TRAVERSAL:
            if (mProfile) {
                Debug.startMethodTracing("ViewRoot");
            }
            performTraversals();
            if (mProfile) {
                Debug.stopMethodTracing();
                mProfile = false;
            }
            break;
        case FINISHED_EVENT:
            handleFinishedEvent(msg.arg1, msg.arg2 != 0);
            break;
        case DISPATCH_KEY:
            if (LOCAL_LOGV) Log.v(
                "ViewRoot", "Dispatching key "
                + msg.obj + " to " + mView);
            deliverKeyEvent((KeyEvent)msg.obj, true);
            break;
        case DISPATCH_POINTER: {
            MotionEvent event = (MotionEvent)msg.obj;
            boolean callWhenDone = msg.arg1 != 0;
            if (event == null) {
                try {
                    long timeBeforeGettingEvents;
                    if (MEASURE_LATENCY) {
                        timeBeforeGettingEvents = System.nanoTime();
                    }
                    event = sWindowSession.getPendingPointerMove(mWindow);
                    if (MEASURE_LATENCY && event != null) {
                        lt.sample("9 Client got events      ", System.nanoTime() - event.getEventTimeNano());
                        lt.sample("8 Client getting events  ", timeBeforeGettingEvents - event.getEventTimeNano());
                    }
                } catch (RemoteException e) {
                }
                callWhenDone = false;
            }
            if (event != null && mTranslator != null) {
                mTranslator.translateEventInScreenToAppWindow(event);
            }
            try {
                boolean handled;
                if (mView != null && mAdded && event != null) {
                    boolean isDown = event.getAction() == MotionEvent.ACTION_DOWN;
                    if (isDown) {
                        ensureTouchMode(true);
                    }
                    if(Config.LOGV) {
                        captureMotionLog("captureDispatchPointer", event);
                    }
                    if (mCurScrollY != 0) {
                        event.offsetLocation(0, mCurScrollY);
                    }
                    if (MEASURE_LATENCY) {
                        lt.sample("A Dispatching TouchEvents", System.nanoTime() - event.getEventTimeNano());
                    }
                    handled = mView.dispatchTouchEvent(event);
                    if (MEASURE_LATENCY) {
                        lt.sample("B Dispatched TouchEvents ", System.nanoTime() - event.getEventTimeNano());
                    }
                    if (!handled && isDown) {
                        int edgeSlop = mViewConfiguration.getScaledEdgeSlop();
                        final int edgeFlags = event.getEdgeFlags();
                        int direction = View.FOCUS_UP;
                        int x = (int)event.getX();
                        int y = (int)event.getY();
                        final int[] deltas = new int[2];
                        if ((edgeFlags & MotionEvent.EDGE_TOP) != 0) {
                            direction = View.FOCUS_DOWN;
                            if ((edgeFlags & MotionEvent.EDGE_LEFT) != 0) {
                                deltas[0] = edgeSlop;
                                x += edgeSlop;
                            } else if ((edgeFlags & MotionEvent.EDGE_RIGHT) != 0) {
                                deltas[0] = -edgeSlop;
                                x -= edgeSlop;
                            }
                        } else if ((edgeFlags & MotionEvent.EDGE_BOTTOM) != 0) {
                            direction = View.FOCUS_UP;
                            if ((edgeFlags & MotionEvent.EDGE_LEFT) != 0) {
                                deltas[0] = edgeSlop;
                                x += edgeSlop;
                            } else if ((edgeFlags & MotionEvent.EDGE_RIGHT) != 0) {
                                deltas[0] = -edgeSlop;
                                x -= edgeSlop;
                            }
                        } else if ((edgeFlags & MotionEvent.EDGE_LEFT) != 0) {
                            direction = View.FOCUS_RIGHT;
                        } else if ((edgeFlags & MotionEvent.EDGE_RIGHT) != 0) {
                            direction = View.FOCUS_LEFT;
                        }
                        if (edgeFlags != 0 && mView instanceof ViewGroup) {
                            View nearest = FocusFinder.getInstance().findNearestTouchable(
                                    ((ViewGroup) mView), x, y, direction, deltas);
                            if (nearest != null) {
                                event.offsetLocation(deltas[0], deltas[1]);
                                event.setEdgeFlags(0);
                                mView.dispatchTouchEvent(event);
                            }
                        }
                    }
                }
            } finally {
                if (callWhenDone) {
                    try {
                        sWindowSession.finishKey(mWindow);
                    } catch (RemoteException e) {
                    }
                }
                if (event != null) {
                    event.recycle();
                }
                if (LOCAL_LOGV || WATCH_POINTER) Log.i(TAG, "Done dispatching!");
            }
        } break;
        case DISPATCH_TRACKBALL:
            deliverTrackballEvent((MotionEvent)msg.obj, msg.arg1 != 0);
            break;
        case DISPATCH_APP_VISIBILITY:
            handleAppVisibility(msg.arg1 != 0);
            break;
        case DISPATCH_GET_NEW_SURFACE:
            handleGetNewSurface();
            break;
        case RESIZED:
            ResizedInfo ri = (ResizedInfo)msg.obj;
            if (mWinFrame.width() == msg.arg1 && mWinFrame.height() == msg.arg2
                    && mPendingContentInsets.equals(ri.coveredInsets)
                    && mPendingVisibleInsets.equals(ri.visibleInsets)
                    && ((ResizedInfo)msg.obj).newConfig == null) {
                break;
            }
        case RESIZED_REPORT:
            if (mAdded) {
                Configuration config = ((ResizedInfo)msg.obj).newConfig;
                if (config != null) {
                    updateConfiguration(config, false);
                }
                mWinFrame.left = 0;
                mWinFrame.right = msg.arg1;
                mWinFrame.top = 0;
                mWinFrame.bottom = msg.arg2;
                mPendingContentInsets.set(((ResizedInfo)msg.obj).coveredInsets);
                mPendingVisibleInsets.set(((ResizedInfo)msg.obj).visibleInsets);
                if (msg.what == RESIZED_REPORT) {
                    mReportNextDraw = true;
                }
                if (mView != null) {
                    forceLayout(mView);
                }
                requestLayout();
            }
            break;
        case WINDOW_FOCUS_CHANGED: {
            if (mAdded) {
                boolean hasWindowFocus = msg.arg1 != 0;
                mAttachInfo.mHasWindowFocus = hasWindowFocus;
                if (hasWindowFocus) {
                    boolean inTouchMode = msg.arg2 != 0;
                    ensureTouchModeLocally(inTouchMode);
                    if (mGlWanted) {
                        checkEglErrors();
                        if (mGlWanted && !mUseGL) {
                            initializeGL();
                            if (mGlCanvas != null) {
                                float appScale = mAttachInfo.mApplicationScale;
                                mGlCanvas.setViewport(
                                        (int) (mWidth * appScale + 0.5f),
                                        (int) (mHeight * appScale + 0.5f));
                            }
                        }
                    }
                }
                mLastWasImTarget = WindowManager.LayoutParams
                        .mayUseInputMethod(mWindowAttributes.flags);
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (mView != null) {
                    if (hasWindowFocus && imm != null && mLastWasImTarget) {
                        imm.startGettingWindowFocus(mView);
                    }
                    mAttachInfo.mKeyDispatchState.reset();
                    mView.dispatchWindowFocusChanged(hasWindowFocus);
                }
                if (hasWindowFocus) {
                    if (imm != null && mLastWasImTarget) {
                        imm.onWindowFocus(mView, mView.findFocus(),
                                mWindowAttributes.softInputMode,
                                !mHasHadWindowFocus, mWindowAttributes.flags);
                    }
                    mWindowAttributes.softInputMode &=
                            ~WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
                    ((WindowManager.LayoutParams)mView.getLayoutParams())
                            .softInputMode &=
                                ~WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
                    mHasHadWindowFocus = true;
                }
                if (hasWindowFocus && mView != null) {
                    sendAccessibilityEvents();
                }
            }
        } break;
        case DIE:
            doDie();
            break;
        case DISPATCH_KEY_FROM_IME: {
            if (LOCAL_LOGV) Log.v(
                "ViewRoot", "Dispatching key "
                + msg.obj + " from IME to " + mView);
            KeyEvent event = (KeyEvent)msg.obj;
            if ((event.getFlags()&KeyEvent.FLAG_FROM_SYSTEM) != 0) {
                event = KeyEvent.changeFlags(event,
                        event.getFlags()&~KeyEvent.FLAG_FROM_SYSTEM);
            }
            deliverKeyEventToViewHierarchy((KeyEvent)msg.obj, false);
        } break;
        case FINISH_INPUT_CONNECTION: {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.reportFinishInputConnection((InputConnection)msg.obj);
            }
        } break;
        case CHECK_FOCUS: {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.checkFocus();
            }
        } break;
        case CLOSE_SYSTEM_DIALOGS: {
            if (mView != null) {
                mView.onCloseSystemDialogs((String)msg.obj);
            }
        } break;
        }
    }
    boolean ensureTouchMode(boolean inTouchMode) {
        if (DBG) Log.d("touchmode", "ensureTouchMode(" + inTouchMode + "), current "
                + "touch mode is " + mAttachInfo.mInTouchMode);
        if (mAttachInfo.mInTouchMode == inTouchMode) return false;
        try {
            sWindowSession.setInTouchMode(inTouchMode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return ensureTouchModeLocally(inTouchMode);
    }
    private boolean ensureTouchModeLocally(boolean inTouchMode) {
        if (DBG) Log.d("touchmode", "ensureTouchModeLocally(" + inTouchMode + "), current "
                + "touch mode is " + mAttachInfo.mInTouchMode);
        if (mAttachInfo.mInTouchMode == inTouchMode) return false;
        mAttachInfo.mInTouchMode = inTouchMode;
        mAttachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return (inTouchMode) ? enterTouchMode() : leaveTouchMode();
    }
    private boolean enterTouchMode() {
        if (mView != null) {
            if (mView.hasFocus()) {
                final View focused = mView.findFocus();
                if (focused != null && !focused.isFocusableInTouchMode()) {
                    final ViewGroup ancestorToTakeFocus =
                            findAncestorToTakeFocusInTouchMode(focused);
                    if (ancestorToTakeFocus != null) {
                        return ancestorToTakeFocus.requestFocus();
                    } else {
                        mView.unFocus();
                        mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(focused, null);
                        mFocusedView = null;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private ViewGroup findAncestorToTakeFocusInTouchMode(View focused) {
        ViewParent parent = focused.getParent();
        while (parent instanceof ViewGroup) {
            final ViewGroup vgParent = (ViewGroup) parent;
            if (vgParent.getDescendantFocusability() == ViewGroup.FOCUS_AFTER_DESCENDANTS
                    && vgParent.isFocusableInTouchMode()) {
                return vgParent;
            }
            if (vgParent.isRootNamespace()) {
                return null;
            } else {
                parent = vgParent.getParent();
            }
        }
        return null;
    }
    private boolean leaveTouchMode() {
        if (mView != null) {
            if (mView.hasFocus()) {
                mFocusedView = mView.findFocus();
                if (!(mFocusedView instanceof ViewGroup)) {
                    return false;
                } else if (((ViewGroup)mFocusedView).getDescendantFocusability() !=
                        ViewGroup.FOCUS_AFTER_DESCENDANTS) {
                    return false;
                }
            }
            final View focused = focusSearch(null, View.FOCUS_DOWN);
            if (focused != null) {
                return focused.requestFocus(View.FOCUS_DOWN);
            }
        }
        return false;
    }
    private void deliverTrackballEvent(MotionEvent event, boolean callWhenDone) {
        if (event == null) {
            try {
                event = sWindowSession.getPendingTrackballMove(mWindow);
            } catch (RemoteException e) {
            }
            callWhenDone = false;
        }
        if (DEBUG_TRACKBALL) Log.v(TAG, "Motion event:" + event);
        boolean handled = false;
        try {
            if (event == null) {
                handled = true;
            } else if (mView != null && mAdded) {
                handled = mView.dispatchTrackballEvent(event);
                if (!handled) {
                }
            }
        } finally {
            if (handled) {
                if (callWhenDone) {
                    try {
                        sWindowSession.finishKey(mWindow);
                    } catch (RemoteException e) {
                    }
                }
                if (event != null) {
                    event.recycle();
                }
                ensureTouchMode(false);
                return;
            }
        }
        final TrackballAxis x = mTrackballAxisX;
        final TrackballAxis y = mTrackballAxisY;
        long curTime = SystemClock.uptimeMillis();
        if ((mLastTrackballTime+MAX_TRACKBALL_DELAY) < curTime) {
            x.reset(0);
            y.reset(0);
            mLastTrackballTime = curTime;
        }
        try {
            final int action = event.getAction();
            final int metastate = event.getMetaState();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    x.reset(2);
                    y.reset(2);
                    deliverKeyEvent(new KeyEvent(curTime, curTime,
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER,
                            0, metastate), false);
                    break;
                case MotionEvent.ACTION_UP:
                    x.reset(2);
                    y.reset(2);
                    deliverKeyEvent(new KeyEvent(curTime, curTime,
                            KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER,
                            0, metastate), false);
                    break;
            }
            if (DEBUG_TRACKBALL) Log.v(TAG, "TB X=" + x.position + " step="
                    + x.step + " dir=" + x.dir + " acc=" + x.acceleration
                    + " move=" + event.getX()
                    + " / Y=" + y.position + " step="
                    + y.step + " dir=" + y.dir + " acc=" + y.acceleration
                    + " move=" + event.getY());
            final float xOff = x.collect(event.getX(), event.getEventTime(), "X");
            final float yOff = y.collect(event.getY(), event.getEventTime(), "Y");
            int keycode = 0;
            int movement = 0;
            float accel = 1;
            if (xOff > yOff) {
                movement = x.generate((2/event.getXPrecision()));
                if (movement != 0) {
                    keycode = movement > 0 ? KeyEvent.KEYCODE_DPAD_RIGHT
                            : KeyEvent.KEYCODE_DPAD_LEFT;
                    accel = x.acceleration;
                    y.reset(2);
                }
            } else if (yOff > 0) {
                movement = y.generate((2/event.getYPrecision()));
                if (movement != 0) {
                    keycode = movement > 0 ? KeyEvent.KEYCODE_DPAD_DOWN
                            : KeyEvent.KEYCODE_DPAD_UP;
                    accel = y.acceleration;
                    x.reset(2);
                }
            }
            if (keycode != 0) {
                if (movement < 0) movement = -movement;
                int accelMovement = (int)(movement * accel);
                if (DEBUG_TRACKBALL) Log.v(TAG, "Move: movement=" + movement
                        + " accelMovement=" + accelMovement
                        + " accel=" + accel);
                if (accelMovement > movement) {
                    if (DEBUG_TRACKBALL) Log.v("foo", "Delivering fake DPAD: "
                            + keycode);
                    movement--;
                    deliverKeyEvent(new KeyEvent(curTime, curTime,
                            KeyEvent.ACTION_MULTIPLE, keycode,
                            accelMovement-movement, metastate), false);
                }
                while (movement > 0) {
                    if (DEBUG_TRACKBALL) Log.v("foo", "Delivering fake DPAD: "
                            + keycode);
                    movement--;
                    curTime = SystemClock.uptimeMillis();
                    deliverKeyEvent(new KeyEvent(curTime, curTime,
                            KeyEvent.ACTION_DOWN, keycode, 0, event.getMetaState()), false);
                    deliverKeyEvent(new KeyEvent(curTime, curTime,
                            KeyEvent.ACTION_UP, keycode, 0, metastate), false);
                }
                mLastTrackballTime = curTime;
            }
        } finally {
            if (callWhenDone) {
                try {
                    sWindowSession.finishKey(mWindow);
                } catch (RemoteException e) {
                }
                if (event != null) {
                    event.recycle();
                }
            }
        }
    }
    static boolean isDirectional(int keyCode) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
            return true;
        }
        return false;
    }
    private static boolean isKeyboardKey(KeyEvent keyEvent) {
      final int convertedKey = keyEvent.getUnicodeChar();
        return convertedKey > 0;
    }
    private boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
        final int action = event.getAction();
        if (action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_MULTIPLE) {
            return false;
        }
        if ((event.getFlags()&KeyEvent.FLAG_KEEP_TOUCH_MODE) != 0) {
            return false;
        }
        if (!mAttachInfo.mInTouchMode) {
            return false;
        }
        if (isKeyboardKey(event) && mView != null && mView.hasFocus()) {
            mFocusedView = mView.findFocus();
            if ((mFocusedView instanceof ViewGroup)
                    && ((ViewGroup) mFocusedView).getDescendantFocusability() ==
                    ViewGroup.FOCUS_AFTER_DESCENDANTS) {
                return false;
            }
            if (ensureTouchMode(false)) {
                throw new IllegalStateException("should not have changed focus "
                        + "when leaving touch mode while a view has focus.");
            }
            return false;
        }
        if (isDirectional(event.getKeyCode())) {
            return ensureTouchMode(false);
        }
        return false;
    }
    private static void captureMotionLog(String subTag, MotionEvent ev) {
        if (ev == null ||
                SystemProperties.getInt(ViewDebug.SYSTEM_PROPERTY_CAPTURE_EVENT, 0) == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder(subTag + ": ");
        sb.append(ev.getDownTime()).append(',');
        sb.append(ev.getEventTime()).append(',');
        sb.append(ev.getAction()).append(',');
        sb.append(ev.getX()).append(',');
        sb.append(ev.getY()).append(',');
        sb.append(ev.getPressure()).append(',');
        sb.append(ev.getSize()).append(',');
        sb.append(ev.getMetaState()).append(',');
        sb.append(ev.getXPrecision()).append(',');
        sb.append(ev.getYPrecision()).append(',');
        sb.append(ev.getDeviceId()).append(',');
        sb.append(ev.getEdgeFlags());
        Log.d(TAG, sb.toString());
    }
    private static void captureKeyLog(String subTag, KeyEvent ev) {
        if (ev == null ||
                SystemProperties.getInt(ViewDebug.SYSTEM_PROPERTY_CAPTURE_EVENT, 0) == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder(subTag + ": ");
        sb.append(ev.getDownTime()).append(',');
        sb.append(ev.getEventTime()).append(',');
        sb.append(ev.getAction()).append(',');
        sb.append(ev.getKeyCode()).append(',');
        sb.append(ev.getRepeatCount()).append(',');
        sb.append(ev.getMetaState()).append(',');
        sb.append(ev.getDeviceId()).append(',');
        sb.append(ev.getScanCode());
        Log.d(TAG, sb.toString());
    }
    int enqueuePendingEvent(Object event, boolean sendDone) {
        int seq = mPendingEventSeq+1;
        if (seq < 0) seq = 0;
        mPendingEventSeq = seq;
        mPendingEvents.put(seq, event);
        return sendDone ? seq : -seq;
    }
    Object retrievePendingEvent(int seq) {
        if (seq < 0) seq = -seq;
        Object event = mPendingEvents.get(seq);
        if (event != null) {
            mPendingEvents.remove(seq);
        }
        return event;
    }
    private void deliverKeyEvent(KeyEvent event, boolean sendDone) {
        boolean handled = mView != null
                ? mView.dispatchKeyEventPreIme(event) : true;
        if (handled) {
            if (sendDone) {
                if (LOCAL_LOGV) Log.v(
                    "ViewRoot", "Telling window manager key is finished");
                try {
                    sWindowSession.finishKey(mWindow);
                } catch (RemoteException e) {
                }
            }
            return;
        }
        if (mLastWasImTarget) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && mView != null) {
                int seq = enqueuePendingEvent(event, sendDone);
                if (DEBUG_IMF) Log.v(TAG, "Sending key event to IME: seq="
                        + seq + " event=" + event);
                imm.dispatchKeyEvent(mView.getContext(), seq, event,
                        mInputMethodCallback);
                return;
            }
        }
        deliverKeyEventToViewHierarchy(event, sendDone);
    }
    void handleFinishedEvent(int seq, boolean handled) {
        final KeyEvent event = (KeyEvent)retrievePendingEvent(seq);
        if (DEBUG_IMF) Log.v(TAG, "IME finished event: seq=" + seq
                + " handled=" + handled + " event=" + event);
        if (event != null) {
            final boolean sendDone = seq >= 0;
            if (!handled) {
                deliverKeyEventToViewHierarchy(event, sendDone);
                return;
            } else if (sendDone) {
                if (LOCAL_LOGV) Log.v(
                        "ViewRoot", "Telling window manager key is finished");
                try {
                    sWindowSession.finishKey(mWindow);
                } catch (RemoteException e) {
                }
            } else {
                Log.w("ViewRoot", "handleFinishedEvent(seq=" + seq
                        + " handled=" + handled + " ev=" + event
                        + ") neither delivering nor finishing key");
            }
        }
    }
    private void deliverKeyEventToViewHierarchy(KeyEvent event, boolean sendDone) {
        try {
            if (mView != null && mAdded) {
                final int action = event.getAction();
                boolean isDown = (action == KeyEvent.ACTION_DOWN);
                if (checkForLeavingTouchModeAndConsume(event)) {
                    return;
                }
                if (Config.LOGV) {
                    captureKeyLog("captureDispatchKeyEvent", event);
                }
                boolean keyHandled = mView.dispatchKeyEvent(event);
                if (!keyHandled && isDown) {
                    int direction = 0;
                    switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        direction = View.FOCUS_LEFT;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        direction = View.FOCUS_RIGHT;
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        direction = View.FOCUS_UP;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        direction = View.FOCUS_DOWN;
                        break;
                    }
                    if (direction != 0) {
                        View focused = mView != null ? mView.findFocus() : null;
                        if (focused != null) {
                            View v = focused.focusSearch(direction);
                            boolean focusPassed = false;
                            if (v != null && v != focused) {
                                focused.getFocusedRect(mTempRect);
                                if (mView instanceof ViewGroup) {
                                    ((ViewGroup) mView).offsetDescendantRectToMyCoords(
                                            focused, mTempRect);
                                    ((ViewGroup) mView).offsetRectIntoDescendantCoords(
                                            v, mTempRect);
                                }
                                focusPassed = v.requestFocus(direction, mTempRect);
                            }
                            if (!focusPassed) {
                                mView.dispatchUnhandledMove(focused, direction);
                            } else {
                                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                            }
                        }
                    }
                }
            }
        } finally {
            if (sendDone) {
                if (LOCAL_LOGV) Log.v(
                    "ViewRoot", "Telling window manager key is finished");
                try {
                    sWindowSession.finishKey(mWindow);
                } catch (RemoteException e) {
                }
            }
        }
    }
    private AudioManager getAudioManager() {
        if (mView == null) {
            throw new IllegalStateException("getAudioManager called when there is no mView");
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return mAudioManager;
    }
    private int relayoutWindow(WindowManager.LayoutParams params, int viewVisibility,
            boolean insetsPending) throws RemoteException {
        float appScale = mAttachInfo.mApplicationScale;
        boolean restore = false;
        if (params != null && mTranslator != null) {
            restore = true;
            params.backup();
            mTranslator.translateWindowLayout(params);
        }
        if (params != null) {
            if (DBG) Log.d(TAG, "WindowLayout in layoutWindow:" + params);
        }
        mPendingConfiguration.seq = 0;
        int relayoutResult = sWindowSession.relayout(
                mWindow, params,
                (int) (mView.mMeasuredWidth * appScale + 0.5f),
                (int) (mView.mMeasuredHeight * appScale + 0.5f),
                viewVisibility, insetsPending, mWinFrame,
                mPendingContentInsets, mPendingVisibleInsets,
                mPendingConfiguration, mSurface);
        if (restore) {
            params.restore();
        }
        if (mTranslator != null) {
            mTranslator.translateRectInScreenToAppWinFrame(mWinFrame);
            mTranslator.translateRectInScreenToAppWindow(mPendingContentInsets);
            mTranslator.translateRectInScreenToAppWindow(mPendingVisibleInsets);
        }
        return relayoutResult;
    }
    public void playSoundEffect(int effectId) {
        checkThread();
        try {
            final AudioManager audioManager = getAudioManager();
            switch (effectId) {
                case SoundEffectConstants.CLICK:
                    audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                    return;
                case SoundEffectConstants.NAVIGATION_DOWN:
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                    return;
                case SoundEffectConstants.NAVIGATION_LEFT:
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                    return;
                case SoundEffectConstants.NAVIGATION_RIGHT:
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);
                    return;
                case SoundEffectConstants.NAVIGATION_UP:
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
                    return;
                default:
                    throw new IllegalArgumentException("unknown effect id " + effectId +
                            " not defined in " + SoundEffectConstants.class.getCanonicalName());
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
    }
    public boolean performHapticFeedback(int effectId, boolean always) {
        try {
            return sWindowSession.performHapticFeedback(mWindow, effectId, always);
        } catch (RemoteException e) {
            return false;
        }
    }
    public View focusSearch(View focused, int direction) {
        checkThread();
        if (!(mView instanceof ViewGroup)) {
            return null;
        }
        return FocusFinder.getInstance().findNextFocus((ViewGroup) mView, focused, direction);
    }
    public void debug() {
        mView.debug();
    }
    public void die(boolean immediate) {
        if (immediate) {
            doDie();
        } else {
            sendEmptyMessage(DIE);
        }
    }
    void doDie() {
        checkThread();
        if (Config.LOGV) Log.v("ViewRoot", "DIE in " + this + " of " + mSurface);
        synchronized (this) {
            if (mAdded && !mFirst) {
                int viewVisibility = mView.getVisibility();
                boolean viewVisibilityChanged = mViewVisibility != viewVisibility;
                if (mWindowAttributesChanged || viewVisibilityChanged) {
                    try {
                        if ((relayoutWindow(mWindowAttributes, viewVisibility, false)
                                & WindowManagerImpl.RELAYOUT_FIRST_TIME) != 0) {
                            sWindowSession.finishDrawing(mWindow);
                        }
                    } catch (RemoteException e) {
                    }
                }
                mSurface.release();
            }
            if (mAdded) {
                mAdded = false;
                dispatchDetachedFromWindow();
            }
        }
    }
    public void dispatchFinishedEvent(int seq, boolean handled) {
        Message msg = obtainMessage(FINISHED_EVENT);
        msg.arg1 = seq;
        msg.arg2 = handled ? 1 : 0;
        sendMessage(msg);
    }
    public void dispatchResized(int w, int h, Rect coveredInsets,
            Rect visibleInsets, boolean reportDraw, Configuration newConfig) {
        if (DEBUG_LAYOUT) Log.v(TAG, "Resizing " + this + ": w=" + w
                + " h=" + h + " coveredInsets=" + coveredInsets.toShortString()
                + " visibleInsets=" + visibleInsets.toShortString()
                + " reportDraw=" + reportDraw);
        Message msg = obtainMessage(reportDraw ? RESIZED_REPORT :RESIZED);
        if (mTranslator != null) {
            mTranslator.translateRectInScreenToAppWindow(coveredInsets);
            mTranslator.translateRectInScreenToAppWindow(visibleInsets);
            w *= mTranslator.applicationInvertedScale;
            h *= mTranslator.applicationInvertedScale;
        }
        msg.arg1 = w;
        msg.arg2 = h;
        ResizedInfo ri = new ResizedInfo();
        ri.coveredInsets = new Rect(coveredInsets);
        ri.visibleInsets = new Rect(visibleInsets);
        ri.newConfig = newConfig;
        msg.obj = ri;
        sendMessage(msg);
    }
    public void dispatchKey(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (false && event.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
                if (Config.LOGD) Log.d("keydisp",
                        "===================================================");
                if (Config.LOGD) Log.d("keydisp", "Focused view Hierarchy is:");
                debug();
                if (Config.LOGD) Log.d("keydisp",
                        "===================================================");
            }
        }
        Message msg = obtainMessage(DISPATCH_KEY);
        msg.obj = event;
        if (LOCAL_LOGV) Log.v(
            "ViewRoot", "sending key " + event + " to " + mView);
        sendMessageAtTime(msg, event.getEventTime());
    }
    public void dispatchPointer(MotionEvent event, long eventTime,
            boolean callWhenDone) {
        Message msg = obtainMessage(DISPATCH_POINTER);
        msg.obj = event;
        msg.arg1 = callWhenDone ? 1 : 0;
        sendMessageAtTime(msg, eventTime);
    }
    public void dispatchTrackball(MotionEvent event, long eventTime,
            boolean callWhenDone) {
        Message msg = obtainMessage(DISPATCH_TRACKBALL);
        msg.obj = event;
        msg.arg1 = callWhenDone ? 1 : 0;
        sendMessageAtTime(msg, eventTime);
    }
    public void dispatchAppVisibility(boolean visible) {
        Message msg = obtainMessage(DISPATCH_APP_VISIBILITY);
        msg.arg1 = visible ? 1 : 0;
        sendMessage(msg);
    }
    public void dispatchGetNewSurface() {
        Message msg = obtainMessage(DISPATCH_GET_NEW_SURFACE);
        sendMessage(msg);
    }
    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        Message msg = Message.obtain();
        msg.what = WINDOW_FOCUS_CHANGED;
        msg.arg1 = hasFocus ? 1 : 0;
        msg.arg2 = inTouchMode ? 1 : 0;
        sendMessage(msg);
    }
    public void dispatchCloseSystemDialogs(String reason) {
        Message msg = Message.obtain();
        msg.what = CLOSE_SYSTEM_DIALOGS;
        msg.obj = reason;
        sendMessage(msg);
    }
    private void sendAccessibilityEvents() {
        if (!AccessibilityManager.getInstance(mView.getContext()).isEnabled()) {
            return;
        }
        mView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        View focusedView = mView.findFocus();
        if (focusedView != null && focusedView != mView) {
            focusedView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        }
    }
    public boolean showContextMenuForChild(View originalView) {
        return false;
    }
    public void createContextMenu(ContextMenu menu) {
    }
    public void childDrawableStateChanged(View child) {
    }
    protected Rect getWindowFrame() {
        return mWinFrame;
    }
    void checkThread() {
        if (mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException(
                    "Only the original thread that created a view hierarchy can touch its views.");
        }
    }
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
            boolean immediate) {
        return scrollToRectOrFocus(rectangle, immediate);
    }
    static class InputMethodCallback extends IInputMethodCallback.Stub {
        private WeakReference<ViewRoot> mViewRoot;
        public InputMethodCallback(ViewRoot viewRoot) {
            mViewRoot = new WeakReference<ViewRoot>(viewRoot);
        }
        public void finishedEvent(int seq, boolean handled) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchFinishedEvent(seq, handled);
            }
        }
        public void sessionCreated(IInputMethodSession session) throws RemoteException {
        }
    }
    static class EventCompletion extends Handler {
        final IWindow mWindow;
        final KeyEvent mKeyEvent;
        final boolean mIsPointer;
        final MotionEvent mMotionEvent;
        EventCompletion(Looper looper, IWindow window, KeyEvent key,
                boolean isPointer, MotionEvent motion) {
            super(looper);
            mWindow = window;
            mKeyEvent = key;
            mIsPointer = isPointer;
            mMotionEvent = motion;
            sendEmptyMessage(0);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mKeyEvent != null) {
                try {
                    sWindowSession.finishKey(mWindow);
                 } catch (RemoteException e) {
                 }
           } else if (mIsPointer) {
                boolean didFinish;
                MotionEvent event = mMotionEvent;
                if (event == null) {
                    try {
                        event = sWindowSession.getPendingPointerMove(mWindow);
                    } catch (RemoteException e) {
                    }
                    didFinish = true;
                } else {
                    didFinish = event.getAction() == MotionEvent.ACTION_OUTSIDE;
                }
                if (!didFinish) {
                    try {
                        sWindowSession.finishKey(mWindow);
                     } catch (RemoteException e) {
                     }
                }
            } else {
                MotionEvent event = mMotionEvent;
                if (event == null) {
                    try {
                        event = sWindowSession.getPendingTrackballMove(mWindow);
                    } catch (RemoteException e) {
                    }
                } else {
                    try {
                        sWindowSession.finishKey(mWindow);
                     } catch (RemoteException e) {
                     }
                }
            }
        }
    }
    static class W extends IWindow.Stub {
        private final WeakReference<ViewRoot> mViewRoot;
        private final Looper mMainLooper;
        public W(ViewRoot viewRoot, Context context) {
            mViewRoot = new WeakReference<ViewRoot>(viewRoot);
            mMainLooper = context.getMainLooper();
        }
        public void resized(int w, int h, Rect coveredInsets,
                Rect visibleInsets, boolean reportDraw, Configuration newConfig) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchResized(w, h, coveredInsets,
                        visibleInsets, reportDraw, newConfig);
            }
        }
        public void dispatchKey(KeyEvent event) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchKey(event);
            } else {
                Log.w("ViewRoot.W", "Key event " + event + " but no ViewRoot available!");
                new EventCompletion(mMainLooper, this, event, false, null);
            }
        }
        public void dispatchPointer(MotionEvent event, long eventTime,
                boolean callWhenDone) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {                
                if (MEASURE_LATENCY) {
                    ViewRoot.lt.sample("* ViewRoot b4 dispatchPtr", System.nanoTime() - eventTime * 1000000);
                }
                viewRoot.dispatchPointer(event, eventTime, callWhenDone);
            } else {
                new EventCompletion(mMainLooper, this, null, true, event);
            }
        }
        public void dispatchTrackball(MotionEvent event, long eventTime,
                boolean callWhenDone) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchTrackball(event, eventTime, callWhenDone);
            } else {
                new EventCompletion(mMainLooper, this, null, false, event);
            }
        }
        public void dispatchAppVisibility(boolean visible) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchAppVisibility(visible);
            }
        }
        public void dispatchGetNewSurface() {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchGetNewSurface();
            }
        }
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.windowFocusChanged(hasFocus, inTouchMode);
            }
        }
        private static int checkCallingPermission(String permission) {
            if (!Process.supportsProcesses()) {
                return PackageManager.PERMISSION_GRANTED;
            }
            try {
                return ActivityManagerNative.getDefault().checkPermission(
                        permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return PackageManager.PERMISSION_DENIED;
            }
        }
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                final View view = viewRoot.mView;
                if (view != null) {
                    if (checkCallingPermission(Manifest.permission.DUMP) !=
                            PackageManager.PERMISSION_GRANTED) {
                        throw new SecurityException("Insufficient permissions to invoke"
                                + " executeCommand() from pid=" + Binder.getCallingPid()
                                + ", uid=" + Binder.getCallingUid());
                    }
                    OutputStream clientStream = null;
                    try {
                        clientStream = new ParcelFileDescriptor.AutoCloseOutputStream(out);
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (clientStream != null) {
                            try {
                                clientStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        public void closeSystemDialogs(String reason) {
            final ViewRoot viewRoot = mViewRoot.get();
            if (viewRoot != null) {
                viewRoot.dispatchCloseSystemDialogs(reason);
            }
        }
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep,
                boolean sync) {
            if (sync) {
                try {
                    sWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                }
            }
        }
        public void dispatchWallpaperCommand(String action, int x, int y,
                int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    sWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                }
            }
        }
    }
    static final class TrackballAxis {
        static final float MAX_ACCELERATION = 20;
        static final long FAST_MOVE_TIME = 150;
        static final float ACCEL_MOVE_SCALING_FACTOR = (1.0f/40);
        float position;
        float absPosition;
        float acceleration = 1;
        long lastMoveTime = 0;
        int step;
        int dir;
        int nonAccelMovement;
        void reset(int _step) {
            position = 0;
            acceleration = 1;
            lastMoveTime = 0;
            step = _step;
            dir = 0;
        }
        float collect(float off, long time, String axis) {
            long normTime;
            if (off > 0) {
                normTime = (long)(off * FAST_MOVE_TIME);
                if (dir < 0) {
                    if (DEBUG_TRACKBALL) Log.v(TAG, axis + " reversed to positive!");
                    position = 0;
                    step = 0;
                    acceleration = 1;
                    lastMoveTime = 0;
                }
                dir = 1;
            } else if (off < 0) {
                normTime = (long)((-off) * FAST_MOVE_TIME);
                if (dir > 0) {
                    if (DEBUG_TRACKBALL) Log.v(TAG, axis + " reversed to negative!");
                    position = 0;
                    step = 0;
                    acceleration = 1;
                    lastMoveTime = 0;
                }
                dir = -1;
            } else {
                normTime = 0;
            }
            if (normTime > 0) {
                long delta = time - lastMoveTime;
                lastMoveTime = time;
                float acc = acceleration;
                if (delta < normTime) {
                    float scale = (normTime-delta) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1) acc *= scale;
                    if (DEBUG_TRACKBALL) Log.v(TAG, axis + " accelerate: off="
                            + off + " normTime=" + normTime + " delta=" + delta
                            + " scale=" + scale + " acc=" + acc);
                    acceleration = acc < MAX_ACCELERATION ? acc : MAX_ACCELERATION;
                } else {
                    float scale = (delta-normTime) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1) acc /= scale;
                    if (DEBUG_TRACKBALL) Log.v(TAG, axis + " deccelerate: off="
                            + off + " normTime=" + normTime + " delta=" + delta
                            + " scale=" + scale + " acc=" + acc);
                    acceleration = acc > 1 ? acc : 1;
                }
            }
            position += off;
            return (absPosition = Math.abs(position));
        }
        int generate(float precision) {
            int movement = 0;
            nonAccelMovement = 0;
            do {
                final int dir = position >= 0 ? 1 : -1;
                switch (step) {
                    case 0:
                        if (absPosition < precision) {
                            return movement;
                        }
                        movement += dir;
                        nonAccelMovement += dir;
                        step = 1;
                        break;
                    case 1:
                        if (absPosition < 2) {
                            return movement;
                        }
                        movement += dir;
                        nonAccelMovement += dir;
                        position += dir > 0 ? -2 : 2;
                        absPosition = Math.abs(position);
                        step = 2;
                        break;
                    default:
                        if (absPosition < 1) {
                            return movement;
                        }
                        movement += dir;
                        position += dir >= 0 ? -1 : 1;
                        absPosition = Math.abs(position);
                        float acc = acceleration;
                        acc *= 1.1f;
                        acceleration = acc < MAX_ACCELERATION ? acc : acceleration;
                        break;
                }
            } while (true);
        }
    }
    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        public CalledFromWrongThreadException(String msg) {
            super(msg);
        }
    }
    private SurfaceHolder mHolder = new SurfaceHolder() {
        public Surface getSurface() {
            return mSurface;
        }
        public boolean isCreating() {
            return false;
        }
        public void addCallback(Callback callback) {
        }
        public void removeCallback(Callback callback) {
        }
        public void setFixedSize(int width, int height) {
        }
        public void setSizeFromLayout() {
        }
        public void setFormat(int format) {
        }
        public void setType(int type) {
        }
        public void setKeepScreenOn(boolean screenOn) {
        }
        public Canvas lockCanvas() {
            return null;
        }
        public Canvas lockCanvas(Rect dirty) {
            return null;
        }
        public void unlockCanvasAndPost(Canvas canvas) {
        }
        public Rect getSurfaceFrame() {
            return null;
        }
    };
    static RunQueue getRunQueue() {
        RunQueue rq = sRunQueues.get();
        if (rq != null) {
            return rq;
        }
        rq = new RunQueue();
        sRunQueues.set(rq);
        return rq;
    }
    static final class RunQueue {
        private final ArrayList<HandlerAction> mActions = new ArrayList<HandlerAction>();
        void post(Runnable action) {
            postDelayed(action, 0);
        }
        void postDelayed(Runnable action, long delayMillis) {
            HandlerAction handlerAction = new HandlerAction();
            handlerAction.action = action;
            handlerAction.delay = delayMillis;
            synchronized (mActions) {
                mActions.add(handlerAction);
            }
        }
        void removeCallbacks(Runnable action) {
            final HandlerAction handlerAction = new HandlerAction();
            handlerAction.action = action;
            synchronized (mActions) {
                final ArrayList<HandlerAction> actions = mActions;
                while (actions.remove(handlerAction)) {
                }
            }
        }
        void executeActions(Handler handler) {
            synchronized (mActions) {
                final ArrayList<HandlerAction> actions = mActions;
                final int count = actions.size();
                for (int i = 0; i < count; i++) {
                    final HandlerAction handlerAction = actions.get(i);
                    handler.postDelayed(handlerAction.action, handlerAction.delay);
                }
                actions.clear();
            }
        }
        private static class HandlerAction {
            Runnable action;
            long delay;
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                HandlerAction that = (HandlerAction) o;
                return !(action != null ? !action.equals(that.action) : that.action != null);
            }
            @Override
            public int hashCode() {
                int result = action != null ? action.hashCode() : 0;
                result = 31 * result + (int) (delay ^ (delay >>> 32));
                return result;
            }
        }
    }
    private static native void nativeShowFPS(Canvas canvas, int durationMillis);
    private static native void nativeAbandonGlCaches();
}
