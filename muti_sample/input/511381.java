public class WindowManagerService extends IWindowManager.Stub
        implements Watchdog.Monitor, KeyInputQueue.HapticFeedbackCallback {
    static final String TAG = "WindowManager";
    static final boolean DEBUG = false;
    static final boolean DEBUG_FOCUS = false;
    static final boolean DEBUG_ANIM = false;
    static final boolean DEBUG_LAYOUT = false;
    static final boolean DEBUG_RESIZE = false;
    static final boolean DEBUG_LAYERS = false;
    static final boolean DEBUG_INPUT = false;
    static final boolean DEBUG_INPUT_METHOD = false;
    static final boolean DEBUG_VISIBILITY = false;
    static final boolean DEBUG_WINDOW_MOVEMENT = false;
    static final boolean DEBUG_ORIENTATION = false;
    static final boolean DEBUG_CONFIGURATION = false;
    static final boolean DEBUG_APP_TRANSITIONS = false;
    static final boolean DEBUG_STARTING_WINDOW = false;
    static final boolean DEBUG_REORDER = false;
    static final boolean DEBUG_WALLPAPER = false;
    static final boolean DEBUG_FREEZE = false;
    static final boolean SHOW_TRANSACTIONS = false;
    static final boolean HIDE_STACK_CRAWLS = true;
    static final boolean MEASURE_LATENCY = false;
    static private LatencyTimer lt;
    static final boolean PROFILE_ORIENTATION = false;
    static final boolean BLUR = true;
    static final boolean localLOGV = DEBUG;
    static final int KEY_REPEAT_DELAY = 50;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int MAX_ANIMATION_DURATION = 10*1000;
    static final int DEFAULT_DIM_DURATION = 200;
    static final int DEFAULT_FADE_IN_OUT_DURATION = 400;
    static final int DIM_DURATION_MULTIPLIER = 6;
    static final int INJECT_FAILED = 0;
    static final int INJECT_SUCCEEDED = 1;
    static final int INJECT_NO_PERMISSION = -1;
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    int mMinWaitTimeBetweenTouchEvents = 1000 / 35;
    long mLastTouchEventTime = 0;
    int mLastTouchEventType = OTHER_EVENT;
    static final int MIN_TIME_BETWEEN_USERACTIVITIES = 1000;
    long mLastUserActivityCallTime = 0;
    long mLastBatteryStatsCallTime = 0;
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private boolean mKeyguardDisabled = false;
    private static final int ALLOW_DISABLE_YES = 1;
    private static final int ALLOW_DISABLE_NO = 0;
    private static final int ALLOW_DISABLE_UNKNOWN = -1; 
    private int mAllowDisableKeyguard = ALLOW_DISABLE_UNKNOWN; 
    final TokenWatcher mKeyguardTokenWatcher = new TokenWatcher(
            new Handler(), "WindowManagerService.mKeyguardTokenWatcher") {
        public void acquired() {
            if (shouldAllowDisableKeyguard()) {
                mPolicy.enableKeyguard(false);
                mKeyguardDisabled = true;
            } else {
                Log.v(TAG, "Not disabling keyguard since device policy is enforced");
            }
        }
        public void released() {
            mPolicy.enableKeyguard(true);
            synchronized (mKeyguardTokenWatcher) {
                mKeyguardDisabled = false;
                mKeyguardTokenWatcher.notifyAll();
            }
        }
    };
    final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPolicy.enableKeyguard(true);
            synchronized(mKeyguardTokenWatcher) {
                mAllowDisableKeyguard = ALLOW_DISABLE_UNKNOWN;
                mKeyguardDisabled = false;
            }
        }
    };
    final Context mContext;
    final boolean mHaveInputMethods;
    final boolean mLimitedAlphaCompositing;
    final WindowManagerPolicy mPolicy = PolicyManager.makeNewWindowManager();
    final IActivityManager mActivityManager;
    final IBatteryStats mBatteryStats;
    final HashSet<Session> mSessions = new HashSet<Session>();
    final HashMap<IBinder, WindowState> mWindowMap = new HashMap<IBinder, WindowState>();
    final HashMap<IBinder, WindowToken> mTokenMap =
            new HashMap<IBinder, WindowToken>();
    final ArrayList<WindowToken> mTokenList = new ArrayList<WindowToken>();
    final ArrayList<WindowToken> mExitingTokens = new ArrayList<WindowToken>();
    final ArrayList<AppWindowToken> mAppTokens = new ArrayList<AppWindowToken>();
    final ArrayList<AppWindowToken> mExitingAppTokens = new ArrayList<AppWindowToken>();
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList<AppWindowToken>();
    AppWindowToken mLastEnterAnimToken;
    LayoutParams mLastEnterAnimParams;
    final ArrayList mWindows = new ArrayList();
    final ArrayList<WindowState> mResizingWindows = new ArrayList<WindowState>();
    final ArrayList<WindowState> mPendingRemove = new ArrayList<WindowState>();
    final ArrayList<WindowState> mDestroySurface = new ArrayList<WindowState>();
    ArrayList<WindowState> mLosingFocus = new ArrayList<WindowState>();
    ArrayList<WindowState> mForceRemoves;
    IInputMethodManager mInputMethodManager;
    SurfaceSession mFxSession;
    private DimAnimator mDimAnimator = null;
    Surface mBlurSurface;
    boolean mBlurShown;
    int mTransactionSequence = 0;
    final float[] mTmpFloats = new float[9];
    boolean mSafeMode;
    boolean mDisplayEnabled = false;
    boolean mSystemBooted = false;
    int mInitialDisplayWidth = 0;
    int mInitialDisplayHeight = 0;
    int mRotation = 0;
    int mRequestedRotation = 0;
    int mForcedAppOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    int mLastRotationFlags;
    ArrayList<IRotationWatcher> mRotationWatchers
            = new ArrayList<IRotationWatcher>();
    boolean mLayoutNeeded = true;
    boolean mAnimationPending = false;
    boolean mDisplayFrozen = false;
    boolean mWaitingForConfig = false;
    boolean mWindowsFreezingScreen = false;
    long mFreezeGcPending = 0;
    int mAppsFreezingScreen = 0;
    int mLayoutSeq = 0;
    boolean mFocusMayChange;
    Configuration mCurConfiguration = new Configuration();
    PowerManager.WakeLock mScreenFrozenLock;
    int mNextAppTransition = WindowManagerPolicy.TRANSIT_UNSET;
    String mNextAppTransitionPackage;
    int mNextAppTransitionEnter;
    int mNextAppTransitionExit;
    boolean mAppTransitionReady = false;
    boolean mAppTransitionRunning = false;
    boolean mAppTransitionTimeout = false;
    boolean mStartingIconInTransition = false;
    boolean mSkipAppTransitionAnimation = false;
    final ArrayList<AppWindowToken> mOpeningApps = new ArrayList<AppWindowToken>();
    final ArrayList<AppWindowToken> mClosingApps = new ArrayList<AppWindowToken>();
    final ArrayList<AppWindowToken> mToTopApps = new ArrayList<AppWindowToken>();
    final ArrayList<AppWindowToken> mToBottomApps = new ArrayList<AppWindowToken>();
    boolean mFatTouch = false;
    Display mDisplay;
    H mH = new H();
    WindowState mCurrentFocus = null;
    WindowState mLastFocus = null;
    WindowState mInputMethodTarget = null;
    WindowState mUpcomingInputMethodTarget = null;
    boolean mInputMethodTargetWaitingAnim;
    int mInputMethodAnimLayerAdjustment;
    WindowState mInputMethodWindow = null;
    final ArrayList<WindowState> mInputMethodDialogs = new ArrayList<WindowState>();
    final ArrayList<WindowToken> mWallpaperTokens = new ArrayList<WindowToken>();
    WindowState mWallpaperTarget = null;
    WindowState mLowerWallpaperTarget = null;
    WindowState mUpperWallpaperTarget = null;
    int mWallpaperAnimLayerAdjustment;
    float mLastWallpaperX = -1;
    float mLastWallpaperY = -1;
    float mLastWallpaperXStep = -1;
    float mLastWallpaperYStep = -1;
    boolean mSendingPointersToWallpaper = false;
    WindowState mWaitingOnWallpaper;
    long mLastWallpaperTimeoutTime;
    static final long WALLPAPER_TIMEOUT = 150;
    static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;
    AppWindowToken mFocusedApp = null;
    PowerManagerService mPowerManager;
    float mWindowAnimationScale = 1.0f;
    float mTransitionAnimationScale = 1.0f;
    final KeyWaiter mKeyWaiter = new KeyWaiter();
    final KeyQ mQueue;
    final InputDispatcherThread mInputThread;
    Session mHoldingScreenOn;
    boolean mTurnOnScreen;
    boolean mInTouchMode = false;
    private ViewServer mViewServer;
    final Rect mTempRect = new Rect();
    final Configuration mTempConfiguration = new Configuration();
    int mScreenLayout = Configuration.SCREENLAYOUT_SIZE_UNDEFINED;
    Rect mCompatibleScreenFrame = new Rect();
    Surface mBackgroundFillerSurface = null;
    boolean mBackgroundFillerShown = false;
    public static WindowManagerService main(Context context,
            PowerManagerService pm, boolean haveInputMethods) {
        WMThread thr = new WMThread(context, pm, haveInputMethods);
        thr.start();
        synchronized (thr) {
            while (thr.mService == null) {
                try {
                    thr.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return thr.mService;
    }
    static class WMThread extends Thread {
        WindowManagerService mService;
        private final Context mContext;
        private final PowerManagerService mPM;
        private final boolean mHaveInputMethods;
        public WMThread(Context context, PowerManagerService pm,
                boolean haveInputMethods) {
            super("WindowManager");
            mContext = context;
            mPM = pm;
            mHaveInputMethods = haveInputMethods;
        }
        public void run() {
            Looper.prepare();
            WindowManagerService s = new WindowManagerService(mContext, mPM,
                    mHaveInputMethods);
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_DISPLAY);
            synchronized (this) {
                mService = s;
                notifyAll();
            }
            Looper.loop();
        }
    }
    static class PolicyThread extends Thread {
        private final WindowManagerPolicy mPolicy;
        private final WindowManagerService mService;
        private final Context mContext;
        private final PowerManagerService mPM;
        boolean mRunning = false;
        public PolicyThread(WindowManagerPolicy policy,
                WindowManagerService service, Context context,
                PowerManagerService pm) {
            super("WindowManagerPolicy");
            mPolicy = policy;
            mService = service;
            mContext = context;
            mPM = pm;
        }
        public void run() {
            Looper.prepare();
            WindowManagerPolicyThread.set(this, Looper.myLooper());
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_FOREGROUND);
            mPolicy.init(mContext, mService, mPM);
            synchronized (this) {
                mRunning = true;
                notifyAll();
            }
            Looper.loop();
        }
    }
    private WindowManagerService(Context context, PowerManagerService pm,
            boolean haveInputMethods) {
        if (MEASURE_LATENCY) {
            lt = new LatencyTimer(100, 1000);
        }
        mContext = context;
        mHaveInputMethods = haveInputMethods;
        mLimitedAlphaCompositing = context.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_limitedAlpha);
        mPowerManager = pm;
        mPowerManager.setPolicy(mPolicy);
        PowerManager pmc = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mScreenFrozenLock = pmc.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "SCREEN_FROZEN");
        mScreenFrozenLock.setReferenceCounted(false);
        mActivityManager = ActivityManagerNative.getDefault();
        mBatteryStats = BatteryStatsService.getService();
        mWindowAnimationScale = Settings.System.getFloat(context.getContentResolver(),
                Settings.System.WINDOW_ANIMATION_SCALE, mWindowAnimationScale);
        mTransitionAnimationScale = Settings.System.getFloat(context.getContentResolver(),
                Settings.System.TRANSITION_ANIMATION_SCALE, mTransitionAnimationScale);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DevicePolicyManager.ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver, filter);
        int max_events_per_sec = 35;
        try {
            max_events_per_sec = Integer.parseInt(SystemProperties
                    .get("windowsmgr.max_events_per_sec"));
            if (max_events_per_sec < 1) {
                max_events_per_sec = 35;
            }
        } catch (NumberFormatException e) {
        }
        mMinWaitTimeBetweenTouchEvents = 1000 / max_events_per_sec;
        mQueue = new KeyQ();
        mInputThread = new InputDispatcherThread();
        PolicyThread thr = new PolicyThread(mPolicy, this, context, pm);
        thr.start();
        synchronized (thr) {
            while (!thr.mRunning) {
                try {
                    thr.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        mInputThread.start();
        Watchdog.getInstance().addMonitor(this);
    }
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.e(TAG, "Window Manager Crash", e);
            }
            throw e;
        }
    }
    private void placeWindowAfter(Object pos, WindowState window) {
        final int i = mWindows.indexOf(pos);
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT) Slog.v(
            TAG, "Adding window " + window + " at "
            + (i+1) + " of " + mWindows.size() + " (after " + pos + ")");
        mWindows.add(i+1, window);
    }
    private void placeWindowBefore(Object pos, WindowState window) {
        final int i = mWindows.indexOf(pos);
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT) Slog.v(
            TAG, "Adding window " + window + " at "
            + i + " of " + mWindows.size() + " (before " + pos + ")");
        mWindows.add(i, window);
    }
    private int findIdxBasedOnAppTokens(WindowState win) {
        ArrayList localmWindows = mWindows;
        int jmax = localmWindows.size();
        if(jmax == 0) {
            return -1;
        }
        for(int j = (jmax-1); j >= 0; j--) {
            WindowState wentry = (WindowState)localmWindows.get(j);
            if(wentry.mAppToken == win.mAppToken) {
                return j;
            }
        }
        return -1;
    }
    private void addWindowToListInOrderLocked(WindowState win, boolean addToToken) {
        final IWindow client = win.mClient;
        final WindowToken token = win.mToken;
        final ArrayList localmWindows = mWindows;
        final int N = localmWindows.size();
        final WindowState attached = win.mAttachedWindow;
        int i;
        if (attached == null) {
            int tokenWindowsPos = token.windows.size();
            if (token.appWindowToken != null) {
                int index = tokenWindowsPos-1;
                if (index >= 0) {
                    if (win.mAttrs.type == TYPE_BASE_APPLICATION) {
                        placeWindowBefore(token.windows.get(0), win);
                        tokenWindowsPos = 0;
                    } else {
                        AppWindowToken atoken = win.mAppToken;
                        if (atoken != null &&
                                token.windows.get(index) == atoken.startingWindow) {
                            placeWindowBefore(token.windows.get(index), win);
                            tokenWindowsPos--;
                        } else {
                            int newIdx =  findIdxBasedOnAppTokens(win);
                            if(newIdx != -1) {
                                if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT) Slog.v(
                                        TAG, "Adding window " + win + " at "
                                        + (newIdx+1) + " of " + N);
                                localmWindows.add(newIdx+1, win);
                            }
                        }
                    }
                } else {
                    if (localLOGV) Slog.v(
                        TAG, "Figuring out where to add app window "
                        + client.asBinder() + " (token=" + token + ")");
                    final int NA = mAppTokens.size();
                    Object pos = null;
                    for (i=NA-1; i>=0; i--) {
                        AppWindowToken t = mAppTokens.get(i);
                        if (t == token) {
                            i--;
                            break;
                        }
                        if (!t.sendingToBottom && t.windows.size() > 0) {
                            pos = t.windows.get(0);
                        }
                    }
                    if (pos != null) {
                        WindowToken atoken =
                            mTokenMap.get(((WindowState)pos).mClient.asBinder());
                        if (atoken != null) {
                            final int NC = atoken.windows.size();
                            if (NC > 0) {
                                WindowState bottom = atoken.windows.get(0);
                                if (bottom.mSubLayer < 0) {
                                    pos = bottom;
                                }
                            }
                        }
                        placeWindowBefore(pos, win);
                    } else {
                        while (i >= 0) {
                            AppWindowToken t = mAppTokens.get(i);
                            final int NW = t.windows.size();
                            if (NW > 0) {
                                pos = t.windows.get(NW-1);
                                break;
                            }
                            i--;
                        }
                        if (pos != null) {
                            WindowToken atoken =
                                mTokenMap.get(((WindowState)pos).mClient.asBinder());
                            if (atoken != null) {
                                final int NC = atoken.windows.size();
                                if (NC > 0) {
                                    WindowState top = atoken.windows.get(NC-1);
                                    if (top.mSubLayer >= 0) {
                                        pos = top;
                                    }
                                }
                            }
                            placeWindowAfter(pos, win);
                        } else {
                            final int myLayer = win.mBaseLayer;
                            for (i=0; i<N; i++) {
                                WindowState w = (WindowState)localmWindows.get(i);
                                if (w.mBaseLayer > myLayer) {
                                    break;
                                }
                            }
                            if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT) Slog.v(
                                    TAG, "Adding window " + win + " at "
                                    + i + " of " + N);
                            localmWindows.add(i, win);
                        }
                    }
                }
            } else {
                final int myLayer = win.mBaseLayer;
                for (i=N-1; i>=0; i--) {
                    if (((WindowState)localmWindows.get(i)).mBaseLayer <= myLayer) {
                        i++;
                        break;
                    }
                }
                if (i < 0) i = 0;
                if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT) Slog.v(
                        TAG, "Adding window " + win + " at "
                        + i + " of " + N);
                localmWindows.add(i, win);
            }
            if (addToToken) {
                token.windows.add(tokenWindowsPos, win);
            }
        } else {
            final int NA = token.windows.size();
            final int sublayer = win.mSubLayer;
            int largestSublayer = Integer.MIN_VALUE;
            WindowState windowWithLargestSublayer = null;
            for (i=0; i<NA; i++) {
                WindowState w = token.windows.get(i);
                final int wSublayer = w.mSubLayer;
                if (wSublayer >= largestSublayer) {
                    largestSublayer = wSublayer;
                    windowWithLargestSublayer = w;
                }
                if (sublayer < 0) {
                    if (wSublayer >= sublayer) {
                        if (addToToken) {
                            token.windows.add(i, win);
                        }
                        placeWindowBefore(
                            wSublayer >= 0 ? attached : w, win);
                        break;
                    }
                } else {
                    if (wSublayer > sublayer) {
                        if (addToToken) {
                            token.windows.add(i, win);
                        }
                        placeWindowBefore(w, win);
                        break;
                    }
                }
            }
            if (i >= NA) {
                if (addToToken) {
                    token.windows.add(win);
                }
                if (sublayer < 0) {
                    placeWindowBefore(attached, win);
                } else {
                    placeWindowAfter(largestSublayer >= 0
                                     ? windowWithLargestSublayer
                                     : attached,
                                     win);
                }
            }
        }
        if (win.mAppToken != null && addToToken) {
            win.mAppToken.allAppWindows.add(win);
        }
    }
    static boolean canBeImeTarget(WindowState w) {
        final int fl = w.mAttrs.flags
                & (FLAG_NOT_FOCUSABLE|FLAG_ALT_FOCUSABLE_IM);
        if (fl == 0 || fl == (FLAG_NOT_FOCUSABLE|FLAG_ALT_FOCUSABLE_IM)) {
            return w.isVisibleOrAdding();
        }
        return false;
    }
    int findDesiredInputMethodWindowIndexLocked(boolean willMove) {
        final ArrayList localmWindows = mWindows;
        final int N = localmWindows.size();
        WindowState w = null;
        int i = N;
        while (i > 0) {
            i--;
            w = (WindowState)localmWindows.get(i);
            if (canBeImeTarget(w)) {
                if (!willMove
                        && w.mAttrs.type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING
                        && i > 0) {
                    WindowState wb = (WindowState)localmWindows.get(i-1);
                    if (wb.mAppToken == w.mAppToken && canBeImeTarget(wb)) {
                        i--;
                        w = wb;
                    }
                }
                break;
            }
        }
        mUpcomingInputMethodTarget = w;
        if (DEBUG_INPUT_METHOD) Slog.v(TAG, "Desired input method target="
                + w + " willMove=" + willMove);
        if (willMove && w != null) {
            final WindowState curTarget = mInputMethodTarget;
            if (curTarget != null && curTarget.mAppToken != null) {
                AppWindowToken token = curTarget.mAppToken;
                WindowState highestTarget = null;
                int highestPos = 0;
                if (token.animating || token.animation != null) {
                    int pos = 0;
                    pos = localmWindows.indexOf(curTarget);
                    while (pos >= 0) {
                        WindowState win = (WindowState)localmWindows.get(pos);
                        if (win.mAppToken != token) {
                            break;
                        }
                        if (!win.mRemoved) {
                            if (highestTarget == null || win.mAnimLayer >
                                    highestTarget.mAnimLayer) {
                                highestTarget = win;
                                highestPos = pos;
                            }
                        }
                        pos--;
                    }
                }
                if (highestTarget != null) {
                    if (DEBUG_INPUT_METHOD) Slog.v(TAG, "mNextAppTransition="
                            + mNextAppTransition + " " + highestTarget
                            + " animating=" + highestTarget.isAnimating()
                            + " layer=" + highestTarget.mAnimLayer
                            + " new layer=" + w.mAnimLayer);
                    if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                        mInputMethodTargetWaitingAnim = true;
                        mInputMethodTarget = highestTarget;
                        return highestPos + 1;
                    } else if (highestTarget.isAnimating() &&
                            highestTarget.mAnimLayer > w.mAnimLayer) {
                        mInputMethodTarget = highestTarget;
                        return highestPos + 1;
                    }
                }
            }
        }
        if (w != null) {
            if (willMove) {
                if (DEBUG_INPUT_METHOD) {
                    RuntimeException e = null;
                    if (!HIDE_STACK_CRAWLS) {
                        e = new RuntimeException();
                        e.fillInStackTrace();
                    }
                    Slog.w(TAG, "Moving IM target from "
                            + mInputMethodTarget + " to " + w, e);
                }
                mInputMethodTarget = w;
                if (w.mAppToken != null) {
                    setInputMethodAnimLayerAdjustment(w.mAppToken.animLayerAdjustment);
                } else {
                    setInputMethodAnimLayerAdjustment(0);
                }
            }
            return i+1;
        }
        if (willMove) {
            if (DEBUG_INPUT_METHOD) {
                RuntimeException e = null;
                if (!HIDE_STACK_CRAWLS) {
                    e = new RuntimeException();
                    e.fillInStackTrace();
                }
                Slog.w(TAG, "Moving IM target from "
                        + mInputMethodTarget + " to null", e);
            }
            mInputMethodTarget = null;
            setInputMethodAnimLayerAdjustment(0);
        }
        return -1;
    }
    void addInputMethodWindowToListLocked(WindowState win) {
        int pos = findDesiredInputMethodWindowIndexLocked(true);
        if (pos >= 0) {
            win.mTargetAppToken = mInputMethodTarget.mAppToken;
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(
                    TAG, "Adding input method window " + win + " at " + pos);
            mWindows.add(pos, win);
            moveInputMethodDialogsLocked(pos+1);
            return;
        }
        win.mTargetAppToken = null;
        addWindowToListInOrderLocked(win, true);
        moveInputMethodDialogsLocked(pos);
    }
    void setInputMethodAnimLayerAdjustment(int adj) {
        if (DEBUG_LAYERS) Slog.v(TAG, "Setting im layer adj to " + adj);
        mInputMethodAnimLayerAdjustment = adj;
        WindowState imw = mInputMethodWindow;
        if (imw != null) {
            imw.mAnimLayer = imw.mLayer + adj;
            if (DEBUG_LAYERS) Slog.v(TAG, "IM win " + imw
                    + " anim layer: " + imw.mAnimLayer);
            int wi = imw.mChildWindows.size();
            while (wi > 0) {
                wi--;
                WindowState cw = (WindowState)imw.mChildWindows.get(wi);
                cw.mAnimLayer = cw.mLayer + adj;
                if (DEBUG_LAYERS) Slog.v(TAG, "IM win " + cw
                        + " anim layer: " + cw.mAnimLayer);
            }
        }
        int di = mInputMethodDialogs.size();
        while (di > 0) {
            di --;
            imw = mInputMethodDialogs.get(di);
            imw.mAnimLayer = imw.mLayer + adj;
            if (DEBUG_LAYERS) Slog.v(TAG, "IM win " + imw
                    + " anim layer: " + imw.mAnimLayer);
        }
    }
    private int tmpRemoveWindowLocked(int interestingPos, WindowState win) {
        int wpos = mWindows.indexOf(win);
        if (wpos >= 0) {
            if (wpos < interestingPos) interestingPos--;
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Temp removing at " + wpos + ": " + win);
            mWindows.remove(wpos);
            int NC = win.mChildWindows.size();
            while (NC > 0) {
                NC--;
                WindowState cw = (WindowState)win.mChildWindows.get(NC);
                int cpos = mWindows.indexOf(cw);
                if (cpos >= 0) {
                    if (cpos < interestingPos) interestingPos--;
                    if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Temp removing child at "
                            + cpos + ": " + cw);
                    mWindows.remove(cpos);
                }
            }
        }
        return interestingPos;
    }
    private void reAddWindowToListInOrderLocked(WindowState win) {
        addWindowToListInOrderLocked(win, false);
        int wpos = mWindows.indexOf(win);
        if (wpos >= 0) {
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "ReAdd removing from " + wpos
                    + ": " + win);
            mWindows.remove(wpos);
            reAddWindowLocked(wpos, win);
        }
    }
    void logWindowList(String prefix) {
        int N = mWindows.size();
        while (N > 0) {
            N--;
            Slog.v(TAG, prefix + "#" + N + ": " + mWindows.get(N));
        }
    }
    void moveInputMethodDialogsLocked(int pos) {
        ArrayList<WindowState> dialogs = mInputMethodDialogs;
        final int N = dialogs.size();
        if (DEBUG_INPUT_METHOD) Slog.v(TAG, "Removing " + N + " dialogs w/pos=" + pos);
        for (int i=0; i<N; i++) {
            pos = tmpRemoveWindowLocked(pos, dialogs.get(i));
        }
        if (DEBUG_INPUT_METHOD) {
            Slog.v(TAG, "Window list w/pos=" + pos);
            logWindowList("  ");
        }
        if (pos >= 0) {
            final AppWindowToken targetAppToken = mInputMethodTarget.mAppToken;
            if (pos < mWindows.size()) {
                WindowState wp = (WindowState)mWindows.get(pos);
                if (wp == mInputMethodWindow) {
                    pos++;
                }
            }
            if (DEBUG_INPUT_METHOD) Slog.v(TAG, "Adding " + N + " dialogs at pos=" + pos);
            for (int i=0; i<N; i++) {
                WindowState win = dialogs.get(i);
                win.mTargetAppToken = targetAppToken;
                pos = reAddWindowLocked(pos, win);
            }
            if (DEBUG_INPUT_METHOD) {
                Slog.v(TAG, "Final window list:");
                logWindowList("  ");
            }
            return;
        }
        for (int i=0; i<N; i++) {
            WindowState win = dialogs.get(i);
            win.mTargetAppToken = null;
            reAddWindowToListInOrderLocked(win);
            if (DEBUG_INPUT_METHOD) {
                Slog.v(TAG, "No IM target, final list:");
                logWindowList("  ");
            }
        }
    }
    boolean moveInputMethodWindowsIfNeededLocked(boolean needAssignLayers) {
        final WindowState imWin = mInputMethodWindow;
        final int DN = mInputMethodDialogs.size();
        if (imWin == null && DN == 0) {
            return false;
        }
        int imPos = findDesiredInputMethodWindowIndexLocked(true);
        if (imPos >= 0) {
            final int N = mWindows.size();
            WindowState firstImWin = imPos < N
                    ? (WindowState)mWindows.get(imPos) : null;
            WindowState baseImWin = imWin != null
                    ? imWin : mInputMethodDialogs.get(0);
            if (baseImWin.mChildWindows.size() > 0) {
                WindowState cw = (WindowState)baseImWin.mChildWindows.get(0);
                if (cw.mSubLayer < 0) baseImWin = cw;
            }
            if (firstImWin == baseImWin) {
                int pos = imPos+1;
                while (pos < N) {
                    if (!((WindowState)mWindows.get(pos)).mIsImWindow) {
                        break;
                    }
                    pos++;
                }
                pos++;
                while (pos < N) {
                    if (((WindowState)mWindows.get(pos)).mIsImWindow) {
                        break;
                    }
                    pos++;
                }
                if (pos >= N) {
                    return false;
                }
            }
            if (imWin != null) {
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG, "Moving IM from " + imPos);
                    logWindowList("  ");
                }
                imPos = tmpRemoveWindowLocked(imPos, imWin);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG, "List after moving with new pos " + imPos + ":");
                    logWindowList("  ");
                }
                imWin.mTargetAppToken = mInputMethodTarget.mAppToken;
                reAddWindowLocked(imPos, imWin);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG, "List after moving IM to " + imPos + ":");
                    logWindowList("  ");
                }
                if (DN > 0) moveInputMethodDialogsLocked(imPos+1);
            } else {
                moveInputMethodDialogsLocked(imPos);
            }
        } else {
            if (imWin != null) {
                if (DEBUG_INPUT_METHOD) Slog.v(TAG, "Moving IM from " + imPos);
                tmpRemoveWindowLocked(0, imWin);
                imWin.mTargetAppToken = null;
                reAddWindowToListInOrderLocked(imWin);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG, "List with no IM target:");
                    logWindowList("  ");
                }
                if (DN > 0) moveInputMethodDialogsLocked(-1);;
            } else {
                moveInputMethodDialogsLocked(-1);;
            }
        }
        if (needAssignLayers) {
            assignLayersLocked();
        }
        return true;
    }
    void adjustInputMethodDialogsLocked() {
        moveInputMethodDialogsLocked(findDesiredInputMethodWindowIndexLocked(true));
    }
    final boolean isWallpaperVisible(WindowState wallpaperTarget) {
        if (DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper vis: target obscured="
                + (wallpaperTarget != null ? Boolean.toString(wallpaperTarget.mObscured) : "??")
                + " anim=" + ((wallpaperTarget != null && wallpaperTarget.mAppToken != null)
                        ? wallpaperTarget.mAppToken.animation : null)
                + " upper=" + mUpperWallpaperTarget
                + " lower=" + mLowerWallpaperTarget);
        return (wallpaperTarget != null
                        && (!wallpaperTarget.mObscured || (wallpaperTarget.mAppToken != null
                                && wallpaperTarget.mAppToken.animation != null)))
                || mUpperWallpaperTarget != null
                || mLowerWallpaperTarget != null;
    }
    static final int ADJUST_WALLPAPER_LAYERS_CHANGED = 1<<1;
    static final int ADJUST_WALLPAPER_VISIBILITY_CHANGED = 1<<2;
    int adjustWallpaperWindowsLocked() {
        int changed = 0;
        final int dw = mDisplay.getWidth();
        final int dh = mDisplay.getHeight();
        final ArrayList localmWindows = mWindows;
        int N = localmWindows.size();
        WindowState w = null;
        WindowState foundW = null;
        int foundI = 0;
        WindowState topCurW = null;
        int topCurI = 0;
        int i = N;
        while (i > 0) {
            i--;
            w = (WindowState)localmWindows.get(i);
            if ((w.mAttrs.type == WindowManager.LayoutParams.TYPE_WALLPAPER)) {
                if (topCurW == null) {
                    topCurW = w;
                    topCurI = i;
                }
                continue;
            }
            topCurW = null;
            if (w.mAppToken != null) {
                if (w.mAppToken.hidden && w.mAppToken.animation == null) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Skipping hidden or animating token: " + w);
                    topCurW = null;
                    continue;
                }
            }
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Win " + w + ": readyfordisplay="
                    + w.isReadyForDisplay() + " drawpending=" + w.mDrawPending
                    + " commitdrawpending=" + w.mCommitDrawPending);
            if ((w.mAttrs.flags&FLAG_SHOW_WALLPAPER) != 0 && w.isReadyForDisplay()
                    && (mWallpaperTarget == w
                            || (!w.mDrawPending && !w.mCommitDrawPending))) {
                if (DEBUG_WALLPAPER) Slog.v(TAG,
                        "Found wallpaper activity: #" + i + "=" + w);
                foundW = w;
                foundI = i;
                if (w == mWallpaperTarget && ((w.mAppToken != null
                        && w.mAppToken.animation != null)
                        || w.mAnimation != null)) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG, "Win " + w
                            + ": token animating, looking behind.");
                    continue;
                }
                break;
            }
        }
        if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
            if (mWallpaperTarget != null && mWallpaperTarget.mAppToken != null) {
                if (DEBUG_WALLPAPER) Slog.v(TAG,
                        "Wallpaper not changing: waiting for app anim in current target");
                return 0;
            }
            if (foundW != null && foundW.mAppToken != null) {
                if (DEBUG_WALLPAPER) Slog.v(TAG,
                        "Wallpaper not changing: waiting for app anim in found target");
                return 0;
            }
        }
        if (mWallpaperTarget != foundW) {
            if (DEBUG_WALLPAPER) {
                Slog.v(TAG, "New wallpaper target: " + foundW
                        + " oldTarget: " + mWallpaperTarget);
            }
            mLowerWallpaperTarget = null;
            mUpperWallpaperTarget = null;
            WindowState oldW = mWallpaperTarget;
            mWallpaperTarget = foundW;
            if (foundW != null && oldW != null) {
                boolean oldAnim = oldW.mAnimation != null
                        || (oldW.mAppToken != null && oldW.mAppToken.animation != null);
                boolean foundAnim = foundW.mAnimation != null
                        || (foundW.mAppToken != null && foundW.mAppToken.animation != null);
                if (DEBUG_WALLPAPER) {
                    Slog.v(TAG, "New animation: " + foundAnim
                            + " old animation: " + oldAnim);
                }
                if (foundAnim && oldAnim) {
                    int oldI = localmWindows.indexOf(oldW);
                    if (DEBUG_WALLPAPER) {
                        Slog.v(TAG, "New i: " + foundI + " old i: " + oldI);
                    }
                    if (oldI >= 0) {
                        if (DEBUG_WALLPAPER) {
                            Slog.v(TAG, "Animating wallpapers: old#" + oldI
                                    + "=" + oldW + "; new#" + foundI
                                    + "=" + foundW);
                        }
                        if (foundW.mAppToken != null && foundW.mAppToken.hiddenRequested) {
                            if (DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Old wallpaper still the target.");
                            }
                            mWallpaperTarget = oldW;
                        }
                        if (foundI > oldI) {
                            if (DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Found target above old target.");
                            }
                            mUpperWallpaperTarget = foundW;
                            mLowerWallpaperTarget = oldW;
                            foundW = oldW;
                            foundI = oldI;
                        } else {
                            if (DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Found target below old target.");
                            }
                            mUpperWallpaperTarget = oldW;
                            mLowerWallpaperTarget = foundW;
                        }
                    }
                }
            }
        } else if (mLowerWallpaperTarget != null) {
            boolean lowerAnimating = mLowerWallpaperTarget.mAnimation != null
                    || (mLowerWallpaperTarget.mAppToken != null
                            && mLowerWallpaperTarget.mAppToken.animation != null);
            boolean upperAnimating = mUpperWallpaperTarget.mAnimation != null
                    || (mUpperWallpaperTarget.mAppToken != null
                            && mUpperWallpaperTarget.mAppToken.animation != null);
            if (!lowerAnimating || !upperAnimating) {
                if (DEBUG_WALLPAPER) {
                    Slog.v(TAG, "No longer animating wallpaper targets!");
                }
                mLowerWallpaperTarget = null;
                mUpperWallpaperTarget = null;
            }
        }
        boolean visible = foundW != null;
        if (visible) {
            visible = isWallpaperVisible(foundW);
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper visibility: " + visible);
            mWallpaperAnimLayerAdjustment =
                    (mLowerWallpaperTarget == null && foundW.mAppToken != null)
                    ? foundW.mAppToken.animLayerAdjustment : 0;
            final int maxLayer = mPolicy.getMaxWallpaperLayer()
                    * TYPE_LAYER_MULTIPLIER
                    + TYPE_LAYER_OFFSET;
            while (foundI > 0) {
                WindowState wb = (WindowState)localmWindows.get(foundI-1);
                if (wb.mBaseLayer < maxLayer &&
                        wb.mAttachedWindow != foundW &&
                        (wb.mAttrs.type != TYPE_APPLICATION_STARTING ||
                                wb.mToken != foundW.mToken)) {
                    break;
                }
                foundW = wb;
                foundI--;
            }
        } else {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "No wallpaper target");
        }
        if (foundW == null && topCurW != null) {
            foundW = topCurW;
            foundI = topCurI+1;
        } else {
            foundW = foundI > 0 ? (WindowState)localmWindows.get(foundI-1) : null;
        }
        if (visible) {
            if (mWallpaperTarget.mWallpaperX >= 0) {
                mLastWallpaperX = mWallpaperTarget.mWallpaperX;
                mLastWallpaperXStep = mWallpaperTarget.mWallpaperXStep;
            }
            if (mWallpaperTarget.mWallpaperY >= 0) {
                mLastWallpaperY = mWallpaperTarget.mWallpaperY;
                mLastWallpaperYStep = mWallpaperTarget.mWallpaperYStep;
            }
        }
        int curTokenIndex = mWallpaperTokens.size();
        while (curTokenIndex > 0) {
            curTokenIndex--;
            WindowToken token = mWallpaperTokens.get(curTokenIndex);
            if (token.hidden == visible) {
                changed |= ADJUST_WALLPAPER_VISIBILITY_CHANGED;
                token.hidden = !visible;
                mLayoutNeeded = true;
            }
            int curWallpaperIndex = token.windows.size();
            while (curWallpaperIndex > 0) {
                curWallpaperIndex--;
                WindowState wallpaper = token.windows.get(curWallpaperIndex);
                if (visible) {
                    updateWallpaperOffsetLocked(wallpaper, dw, dh, false);
                }
                if (wallpaper.mWallpaperVisible != visible) {
                    wallpaper.mWallpaperVisible = visible;
                    try {
                        if (DEBUG_VISIBILITY || DEBUG_WALLPAPER) Slog.v(TAG,
                                "Setting visibility of wallpaper " + wallpaper
                                + ": " + visible);
                        wallpaper.mClient.dispatchAppVisibility(visible);
                    } catch (RemoteException e) {
                    }
                }
                wallpaper.mAnimLayer = wallpaper.mLayer + mWallpaperAnimLayerAdjustment;
                if (DEBUG_LAYERS || DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper win "
                        + wallpaper + " anim layer: " + wallpaper.mAnimLayer);
                if (wallpaper == foundW) {
                    foundI--;
                    foundW = foundI > 0
                            ? (WindowState)localmWindows.get(foundI-1) : null;
                    continue;
                }
                int oldIndex = localmWindows.indexOf(wallpaper);
                if (oldIndex >= 0) {
                    if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Wallpaper removing at "
                            + oldIndex + ": " + wallpaper);
                    localmWindows.remove(oldIndex);
                    if (oldIndex < foundI) {
                        foundI--;
                    }
                }
                if (DEBUG_WALLPAPER || DEBUG_WINDOW_MOVEMENT) Slog.v(TAG,
                        "Moving wallpaper " + wallpaper
                        + " from " + oldIndex + " to " + foundI);
                localmWindows.add(foundI, wallpaper);
                changed |= ADJUST_WALLPAPER_LAYERS_CHANGED;
            }
        }
        return changed;
    }
    void setWallpaperAnimLayerAdjustmentLocked(int adj) {
        if (DEBUG_LAYERS || DEBUG_WALLPAPER) Slog.v(TAG,
                "Setting wallpaper layer adj to " + adj);
        mWallpaperAnimLayerAdjustment = adj;
        int curTokenIndex = mWallpaperTokens.size();
        while (curTokenIndex > 0) {
            curTokenIndex--;
            WindowToken token = mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size();
            while (curWallpaperIndex > 0) {
                curWallpaperIndex--;
                WindowState wallpaper = token.windows.get(curWallpaperIndex);
                wallpaper.mAnimLayer = wallpaper.mLayer + adj;
                if (DEBUG_LAYERS || DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper win "
                        + wallpaper + " anim layer: " + wallpaper.mAnimLayer);
            }
        }
    }
    boolean updateWallpaperOffsetLocked(WindowState wallpaperWin, int dw, int dh,
            boolean sync) {
        boolean changed = false;
        boolean rawChanged = false;
        float wpx = mLastWallpaperX >= 0 ? mLastWallpaperX : 0.5f;
        float wpxs = mLastWallpaperXStep >= 0 ? mLastWallpaperXStep : -1.0f;
        int availw = wallpaperWin.mFrame.right-wallpaperWin.mFrame.left-dw;
        int offset = availw > 0 ? -(int)(availw*wpx+.5f) : 0;
        changed = wallpaperWin.mXOffset != offset;
        if (changed) {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Update wallpaper "
                    + wallpaperWin + " x: " + offset);
            wallpaperWin.mXOffset = offset;
        }
        if (wallpaperWin.mWallpaperX != wpx || wallpaperWin.mWallpaperXStep != wpxs) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged = true;
        }
        float wpy = mLastWallpaperY >= 0 ? mLastWallpaperY : 0.5f;
        float wpys = mLastWallpaperYStep >= 0 ? mLastWallpaperYStep : -1.0f;
        int availh = wallpaperWin.mFrame.bottom-wallpaperWin.mFrame.top-dh;
        offset = availh > 0 ? -(int)(availh*wpy+.5f) : 0;
        if (wallpaperWin.mYOffset != offset) {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Update wallpaper "
                    + wallpaperWin + " y: " + offset);
            changed = true;
            wallpaperWin.mYOffset = offset;
        }
        if (wallpaperWin.mWallpaperY != wpy || wallpaperWin.mWallpaperYStep != wpys) {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }
        if (rawChanged) {
            try {
                if (DEBUG_WALLPAPER) Slog.v(TAG, "Report new wp offset "
                        + wallpaperWin + " x=" + wallpaperWin.mWallpaperX
                        + " y=" + wallpaperWin.mWallpaperY);
                if (sync) {
                    mWaitingOnWallpaper = wallpaperWin;
                }
                wallpaperWin.mClient.dispatchWallpaperOffsets(
                        wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY,
                        wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
                if (sync) {
                    if (mWaitingOnWallpaper != null) {
                        long start = SystemClock.uptimeMillis();
                        if ((mLastWallpaperTimeoutTime+WALLPAPER_TIMEOUT_RECOVERY)
                                < start) {
                            try {
                                if (DEBUG_WALLPAPER) Slog.v(TAG,
                                        "Waiting for offset complete...");
                                mWindowMap.wait(WALLPAPER_TIMEOUT);
                            } catch (InterruptedException e) {
                            }
                            if (DEBUG_WALLPAPER) Slog.v(TAG, "Offset complete!");
                            if ((start+WALLPAPER_TIMEOUT)
                                    < SystemClock.uptimeMillis()) {
                                Slog.i(TAG, "Timeout waiting for wallpaper to offset: "
                                        + wallpaperWin);
                                mLastWallpaperTimeoutTime = start;
                            }
                        }
                        mWaitingOnWallpaper = null;
                    }
                }
            } catch (RemoteException e) {
            }
        }
        return changed;
    }
    void wallpaperOffsetsComplete(IBinder window) {
        synchronized (mWindowMap) {
            if (mWaitingOnWallpaper != null &&
                    mWaitingOnWallpaper.mClient.asBinder() == window) {
                mWaitingOnWallpaper = null;
                mWindowMap.notifyAll();
            }
        }
    }
    boolean updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        final int dw = mDisplay.getWidth();
        final int dh = mDisplay.getHeight();
        boolean changed = false;
        WindowState target = mWallpaperTarget;
        if (target != null) {
            if (target.mWallpaperX >= 0) {
                mLastWallpaperX = target.mWallpaperX;
            } else if (changingTarget.mWallpaperX >= 0) {
                mLastWallpaperX = changingTarget.mWallpaperX;
            }
            if (target.mWallpaperY >= 0) {
                mLastWallpaperY = target.mWallpaperY;
            } else if (changingTarget.mWallpaperY >= 0) {
                mLastWallpaperY = changingTarget.mWallpaperY;
            }
        }
        int curTokenIndex = mWallpaperTokens.size();
        while (curTokenIndex > 0) {
            curTokenIndex--;
            WindowToken token = mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size();
            while (curWallpaperIndex > 0) {
                curWallpaperIndex--;
                WindowState wallpaper = token.windows.get(curWallpaperIndex);
                if (updateWallpaperOffsetLocked(wallpaper, dw, dh, sync)) {
                    wallpaper.computeShownFrameLocked();
                    changed = true;
                    sync = false;
                }
            }
        }
        return changed;
    }
    void updateWallpaperVisibilityLocked() {
        final boolean visible = isWallpaperVisible(mWallpaperTarget);
        final int dw = mDisplay.getWidth();
        final int dh = mDisplay.getHeight();
        int curTokenIndex = mWallpaperTokens.size();
        while (curTokenIndex > 0) {
            curTokenIndex--;
            WindowToken token = mWallpaperTokens.get(curTokenIndex);
            if (token.hidden == visible) {
                token.hidden = !visible;
                mLayoutNeeded = true;
            }
            int curWallpaperIndex = token.windows.size();
            while (curWallpaperIndex > 0) {
                curWallpaperIndex--;
                WindowState wallpaper = token.windows.get(curWallpaperIndex);
                if (visible) {
                    updateWallpaperOffsetLocked(wallpaper, dw, dh, false);
                }
                if (wallpaper.mWallpaperVisible != visible) {
                    wallpaper.mWallpaperVisible = visible;
                    try {
                        if (DEBUG_VISIBILITY || DEBUG_WALLPAPER) Slog.v(TAG,
                                "Updating visibility of wallpaper " + wallpaper
                                + ": " + visible);
                        wallpaper.mClient.dispatchAppVisibility(visible);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }
    void sendPointerToWallpaperLocked(WindowState srcWin,
            MotionEvent pointer, long eventTime) {
        int curTokenIndex = mWallpaperTokens.size();
        while (curTokenIndex > 0) {
            curTokenIndex--;
            WindowToken token = mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size();
            while (curWallpaperIndex > 0) {
                curWallpaperIndex--;
                WindowState wallpaper = token.windows.get(curWallpaperIndex);
                if ((wallpaper.mAttrs.flags &
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0) {
                    continue;
                }
                try {
                    MotionEvent ev = MotionEvent.obtainNoHistory(pointer);
                    if (srcWin != null) {
                        ev.offsetLocation(srcWin.mFrame.left-wallpaper.mFrame.left,
                                srcWin.mFrame.top-wallpaper.mFrame.top);
                    } else {
                        ev.offsetLocation(-wallpaper.mFrame.left, -wallpaper.mFrame.top);
                    }
                    switch (pointer.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mSendingPointersToWallpaper = true;
                            break;
                        case MotionEvent.ACTION_UP:
                            mSendingPointersToWallpaper = false;
                            break;
                    }
                    wallpaper.mClient.dispatchPointer(ev, eventTime, false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failure sending pointer to wallpaper", e);
                }
            }
        }
    }
    void dispatchPointerElsewhereLocked(WindowState srcWin, WindowState relWin,
            MotionEvent pointer, long eventTime, boolean skipped) {
        if (relWin != null) {
            mPolicy.dispatchedPointerEventLw(pointer, relWin.mFrame.left, relWin.mFrame.top);
        } else {
            mPolicy.dispatchedPointerEventLw(pointer, 0, 0);
        }
        if (mSendingPointersToWallpaper) {
            if (skipped) {
                Slog.i(TAG, "Sending skipped pointer to wallpaper!");
            }
            sendPointerToWallpaperLocked(relWin, pointer, eventTime);
        } else if (srcWin != null
                && pointer.getAction() == MotionEvent.ACTION_DOWN
                && mWallpaperTarget == srcWin
                && srcWin.mAttrs.type != WindowManager.LayoutParams.TYPE_KEYGUARD) {
            sendPointerToWallpaperLocked(relWin, pointer, eventTime);
        }
    }
    public int addWindow(Session session, IWindow client,
            WindowManager.LayoutParams attrs, int viewVisibility,
            Rect outContentInsets) {
        int res = mPolicy.checkAddPermission(attrs);
        if (res != WindowManagerImpl.ADD_OKAY) {
            return res;
        }
        boolean reportNewConfig = false;
        WindowState attachedWindow = null;
        WindowState win = null;
        synchronized(mWindowMap) {
            if (mDisplay == null) {
                WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
                mDisplay = wm.getDefaultDisplay();
                mInitialDisplayWidth = mDisplay.getWidth();
                mInitialDisplayHeight = mDisplay.getHeight();
                mQueue.setDisplay(mDisplay);
                reportNewConfig = true;
            }
            if (mWindowMap.containsKey(client.asBinder())) {
                Slog.w(TAG, "Window " + client + " is already added");
                return WindowManagerImpl.ADD_DUPLICATE_ADD;
            }
            if (attrs.type >= FIRST_SUB_WINDOW && attrs.type <= LAST_SUB_WINDOW) {
                attachedWindow = windowForClientLocked(null, attrs.token, false);
                if (attachedWindow == null) {
                    Slog.w(TAG, "Attempted to add window with token that is not a window: "
                          + attrs.token + ".  Aborting.");
                    return WindowManagerImpl.ADD_BAD_SUBWINDOW_TOKEN;
                }
                if (attachedWindow.mAttrs.type >= FIRST_SUB_WINDOW
                        && attachedWindow.mAttrs.type <= LAST_SUB_WINDOW) {
                    Slog.w(TAG, "Attempted to add window with token that is a sub-window: "
                            + attrs.token + ".  Aborting.");
                    return WindowManagerImpl.ADD_BAD_SUBWINDOW_TOKEN;
                }
            }
            boolean addToken = false;
            WindowToken token = mTokenMap.get(attrs.token);
            if (token == null) {
                if (attrs.type >= FIRST_APPLICATION_WINDOW
                        && attrs.type <= LAST_APPLICATION_WINDOW) {
                    Slog.w(TAG, "Attempted to add application window with unknown token "
                          + attrs.token + ".  Aborting.");
                    return WindowManagerImpl.ADD_BAD_APP_TOKEN;
                }
                if (attrs.type == TYPE_INPUT_METHOD) {
                    Slog.w(TAG, "Attempted to add input method window with unknown token "
                          + attrs.token + ".  Aborting.");
                    return WindowManagerImpl.ADD_BAD_APP_TOKEN;
                }
                if (attrs.type == TYPE_WALLPAPER) {
                    Slog.w(TAG, "Attempted to add wallpaper window with unknown token "
                          + attrs.token + ".  Aborting.");
                    return WindowManagerImpl.ADD_BAD_APP_TOKEN;
                }
                token = new WindowToken(attrs.token, -1, false);
                addToken = true;
            } else if (attrs.type >= FIRST_APPLICATION_WINDOW
                    && attrs.type <= LAST_APPLICATION_WINDOW) {
                AppWindowToken atoken = token.appWindowToken;
                if (atoken == null) {
                    Slog.w(TAG, "Attempted to add window with non-application token "
                          + token + ".  Aborting.");
                    return WindowManagerImpl.ADD_NOT_APP_TOKEN;
                } else if (atoken.removed) {
                    Slog.w(TAG, "Attempted to add window with exiting application token "
                          + token + ".  Aborting.");
                    return WindowManagerImpl.ADD_APP_EXITING;
                }
                if (attrs.type == TYPE_APPLICATION_STARTING && atoken.firstWindowDrawn) {
                    if (localLOGV) Slog.v(
                            TAG, "**** NO NEED TO START: " + attrs.getTitle());
                    return WindowManagerImpl.ADD_STARTING_NOT_NEEDED;
                }
            } else if (attrs.type == TYPE_INPUT_METHOD) {
                if (token.windowType != TYPE_INPUT_METHOD) {
                    Slog.w(TAG, "Attempted to add input method window with bad token "
                            + attrs.token + ".  Aborting.");
                      return WindowManagerImpl.ADD_BAD_APP_TOKEN;
                }
            } else if (attrs.type == TYPE_WALLPAPER) {
                if (token.windowType != TYPE_WALLPAPER) {
                    Slog.w(TAG, "Attempted to add wallpaper window with bad token "
                            + attrs.token + ".  Aborting.");
                      return WindowManagerImpl.ADD_BAD_APP_TOKEN;
                }
            }
            win = new WindowState(session, client, token,
                    attachedWindow, attrs, viewVisibility);
            if (win.mDeathRecipient == null) {
                Slog.w(TAG, "Adding window client " + client.asBinder()
                        + " that is dead, aborting.");
                return WindowManagerImpl.ADD_APP_EXITING;
            }
            mPolicy.adjustWindowParamsLw(win.mAttrs);
            res = mPolicy.prepareAddWindowLw(win, attrs);
            if (res != WindowManagerImpl.ADD_OKAY) {
                return res;
            }
            res = WindowManagerImpl.ADD_OKAY;
            final long origId = Binder.clearCallingIdentity();
            if (addToken) {
                mTokenMap.put(attrs.token, token);
                mTokenList.add(token);
            }
            win.attach();
            mWindowMap.put(client.asBinder(), win);
            if (attrs.type == TYPE_APPLICATION_STARTING &&
                    token.appWindowToken != null) {
                token.appWindowToken.startingWindow = win;
            }
            boolean imMayMove = true;
            if (attrs.type == TYPE_INPUT_METHOD) {
                mInputMethodWindow = win;
                addInputMethodWindowToListLocked(win);
                imMayMove = false;
            } else if (attrs.type == TYPE_INPUT_METHOD_DIALOG) {
                mInputMethodDialogs.add(win);
                addWindowToListInOrderLocked(win, true);
                adjustInputMethodDialogsLocked();
                imMayMove = false;
            } else {
                addWindowToListInOrderLocked(win, true);
                if (attrs.type == TYPE_WALLPAPER) {
                    mLastWallpaperTimeoutTime = 0;
                    adjustWallpaperWindowsLocked();
                } else if ((attrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
                    adjustWallpaperWindowsLocked();
                }
            }
            win.mEnterAnimationPending = true;
            mPolicy.getContentInsetHintLw(attrs, outContentInsets);
            if (mInTouchMode) {
                res |= WindowManagerImpl.ADD_FLAG_IN_TOUCH_MODE;
            }
            if (win == null || win.mAppToken == null || !win.mAppToken.clientHidden) {
                res |= WindowManagerImpl.ADD_FLAG_APP_VISIBLE;
            }
            boolean focusChanged = false;
            if (win.canReceiveKeys()) {
                if ((focusChanged=updateFocusedWindowLocked(UPDATE_FOCUS_WILL_ASSIGN_LAYERS))
                        == true) {
                    imMayMove = false;
                }
            }
            if (imMayMove) {
                moveInputMethodWindowsIfNeededLocked(false);
            }
            assignLayersLocked();
            if (focusChanged) {
                if (mCurrentFocus != null) {
                    mKeyWaiter.handleNewWindowLocked(mCurrentFocus);
                }
            }
            if (localLOGV) Slog.v(
                TAG, "New client " + client.asBinder()
                + ": window=" + win);
            if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked()) {
                reportNewConfig = true;
            }
        }
        final long origId = Binder.clearCallingIdentity();
        if (reportNewConfig) {
            sendNewConfiguration();
        }
        Binder.restoreCallingIdentity(origId);
        return res;
    }
    public void removeWindow(Session session, IWindow client) {
        synchronized(mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win == null) {
                return;
            }
            removeWindowLocked(session, win);
        }
    }
    public void removeWindowLocked(Session session, WindowState win) {
        if (localLOGV || DEBUG_FOCUS) Slog.v(
            TAG, "Remove " + win + " client="
            + Integer.toHexString(System.identityHashCode(
                win.mClient.asBinder()))
            + ", surface=" + win.mSurface);
        final long origId = Binder.clearCallingIdentity();
        if (DEBUG_APP_TRANSITIONS) Slog.v(
                TAG, "Remove " + win + ": mSurface=" + win.mSurface
                + " mExiting=" + win.mExiting
                + " isAnimating=" + win.isAnimating()
                + " app-animation="
                + (win.mAppToken != null ? win.mAppToken.animation : null)
                + " inPendingTransaction="
                + (win.mAppToken != null ? win.mAppToken.inPendingTransaction : false)
                + " mDisplayFrozen=" + mDisplayFrozen);
        boolean wasVisible = false;
        if (win.mSurface != null && !mDisplayFrozen && mPolicy.isScreenOn()) {
            if (wasVisible=win.isWinVisibleLw()) {
                int transit = WindowManagerPolicy.TRANSIT_EXIT;
                if (win.getAttrs().type == TYPE_APPLICATION_STARTING) {
                    transit = WindowManagerPolicy.TRANSIT_PREVIEW_DONE;
                }
                if (applyAnimationLocked(win, transit, false)) {
                    win.mExiting = true;
                }
            }
            if (win.mExiting || win.isAnimating()) {
                win.mExiting = true;
                win.mRemoveOnExit = true;
                mLayoutNeeded = true;
                updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
                performLayoutAndPlaceSurfacesLocked();
                if (win.mAppToken != null) {
                    win.mAppToken.updateReportedVisibilityLocked();
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
        }
        removeWindowInnerLocked(session, win);
        if (wasVisible && computeForcedAppOrientationLocked()
                != mForcedAppOrientation
                && updateOrientationFromAppTokensLocked()) {
            mH.sendEmptyMessage(H.SEND_NEW_CONFIGURATION);
        }
        updateFocusedWindowLocked(UPDATE_FOCUS_NORMAL);
        Binder.restoreCallingIdentity(origId);
    }
    private void removeWindowInnerLocked(Session session, WindowState win) {
        mKeyWaiter.finishedKey(session, win.mClient, true,
                KeyWaiter.RETURN_NOTHING);
        mKeyWaiter.releasePendingPointerLocked(win.mSession);
        mKeyWaiter.releasePendingTrackballLocked(win.mSession);
        win.mRemoved = true;
        if (mInputMethodTarget == win) {
            moveInputMethodWindowsIfNeededLocked(false);
        }
        if (false) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Slog.w(TAG, "Removing window " + win, e);
        }
        mPolicy.removeWindowLw(win);
        win.removeLocked();
        mWindowMap.remove(win.mClient.asBinder());
        mWindows.remove(win);
        if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Final remove of window: " + win);
        if (mInputMethodWindow == win) {
            mInputMethodWindow = null;
        } else if (win.mAttrs.type == TYPE_INPUT_METHOD_DIALOG) {
            mInputMethodDialogs.remove(win);
        }
        final WindowToken token = win.mToken;
        final AppWindowToken atoken = win.mAppToken;
        token.windows.remove(win);
        if (atoken != null) {
            atoken.allAppWindows.remove(win);
        }
        if (localLOGV) Slog.v(
                TAG, "**** Removing window " + win + ": count="
                + token.windows.size());
        if (token.windows.size() == 0) {
            if (!token.explicit) {
                mTokenMap.remove(token.token);
                mTokenList.remove(token);
            } else if (atoken != null) {
                atoken.firstWindowDrawn = false;
            }
        }
        if (atoken != null) {
            if (atoken.startingWindow == win) {
                atoken.startingWindow = null;
            } else if (atoken.allAppWindows.size() == 0 && atoken.startingData != null) {
                atoken.startingData = null;
            } else if (atoken.allAppWindows.size() == 1 && atoken.startingView != null) {
                if (DEBUG_STARTING_WINDOW) {
                    Slog.v(TAG, "Schedule remove starting " + token
                            + ": no more real windows");
                }
                Message m = mH.obtainMessage(H.REMOVE_STARTING, atoken);
                mH.sendMessage(m);
            }
        }
        if (win.mAttrs.type == TYPE_WALLPAPER) {
            mLastWallpaperTimeoutTime = 0;
            adjustWallpaperWindowsLocked();
        } else if ((win.mAttrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
            adjustWallpaperWindowsLocked();
        }
        if (!mInLayout) {
            assignLayersLocked();
            mLayoutNeeded = true;
            performLayoutAndPlaceSurfacesLocked();
            if (win.mAppToken != null) {
                win.mAppToken.updateReportedVisibilityLocked();
            }
        }
    }
    private static void logSurface(WindowState w, String msg, RuntimeException where) {
        String str = "  SURFACE " + Integer.toHexString(w.hashCode())
                + ": " + msg + " / " + w.mAttrs.getTitle();
        if (where != null) {
            Slog.i(TAG, str, where);
        } else {
            Slog.i(TAG, str);
        }
    }
    private void setTransparentRegionWindow(Session session, IWindow client, Region region) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (mWindowMap) {
                WindowState w = windowForClientLocked(session, client, false);
                if ((w != null) && (w.mSurface != null)) {
                    if (SHOW_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION");
                    Surface.openTransaction();
                    try {
                        if (SHOW_TRANSACTIONS) logSurface(w,
                                "transparentRegionHint=" + region, null);
                        w.mSurface.setTransparentRegionHint(region);
                    } finally {
                        if (SHOW_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION");
                        Surface.closeTransaction();
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }
    void setInsetsWindow(Session session, IWindow client,
            int touchableInsets, Rect contentInsets,
            Rect visibleInsets) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (mWindowMap) {
                WindowState w = windowForClientLocked(session, client, false);
                if (w != null) {
                    w.mGivenInsetsPending = false;
                    w.mGivenContentInsets.set(contentInsets);
                    w.mGivenVisibleInsets.set(visibleInsets);
                    w.mTouchableInsets = touchableInsets;
                    mLayoutNeeded = true;
                    performLayoutAndPlaceSurfacesLocked();
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }
    public void getWindowDisplayFrame(Session session, IWindow client,
            Rect outDisplayFrame) {
        synchronized(mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win == null) {
                outDisplayFrame.setEmpty();
                return;
            }
            outDisplayFrame.set(win.mDisplayFrame);
        }
    }
    public void setWindowWallpaperPositionLocked(WindowState window, float x, float y,
            float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y)  {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            if (updateWallpaperOffsetLocked(window, true)) {
                performLayoutAndPlaceSurfacesLocked();
            }
        }
    }
    void wallpaperCommandComplete(IBinder window, Bundle result) {
        synchronized (mWindowMap) {
            if (mWaitingOnWallpaper != null &&
                    mWaitingOnWallpaper.mClient.asBinder() == window) {
                mWaitingOnWallpaper = null;
                mWindowMap.notifyAll();
            }
        }
    }
    public Bundle sendWindowWallpaperCommandLocked(WindowState window,
            String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (window == mWallpaperTarget || window == mLowerWallpaperTarget
                || window == mUpperWallpaperTarget) {
            boolean doWait = sync;
            int curTokenIndex = mWallpaperTokens.size();
            while (curTokenIndex > 0) {
                curTokenIndex--;
                WindowToken token = mWallpaperTokens.get(curTokenIndex);
                int curWallpaperIndex = token.windows.size();
                while (curWallpaperIndex > 0) {
                    curWallpaperIndex--;
                    WindowState wallpaper = token.windows.get(curWallpaperIndex);
                    try {
                        wallpaper.mClient.dispatchWallpaperCommand(action,
                                x, y, z, extras, sync);
                        sync = false;
                    } catch (RemoteException e) {
                    }
                }
            }
            if (doWait) {
            }
        }
        return null;
    }
    public int relayoutWindow(Session session, IWindow client,
            WindowManager.LayoutParams attrs, int requestedWidth,
            int requestedHeight, int viewVisibility, boolean insetsPending,
            Rect outFrame, Rect outContentInsets, Rect outVisibleInsets,
            Configuration outConfig, Surface outSurface) {
        boolean displayed = false;
        boolean inTouchMode;
        boolean configChanged;
        long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win == null) {
                return 0;
            }
            win.mRequestedWidth = requestedWidth;
            win.mRequestedHeight = requestedHeight;
            if (attrs != null) {
                mPolicy.adjustWindowParamsLw(attrs);
            }
            int attrChanges = 0;
            int flagChanges = 0;
            if (attrs != null) {
                flagChanges = win.mAttrs.flags ^= attrs.flags;
                attrChanges = win.mAttrs.copyFrom(attrs);
            }
            if (DEBUG_LAYOUT) Slog.v(TAG, "Relayout " + win + ": " + win.mAttrs);
            if ((attrChanges & WindowManager.LayoutParams.ALPHA_CHANGED) != 0) {
                win.mAlpha = attrs.alpha;
            }
            final boolean scaledWindow =
                ((win.mAttrs.flags & WindowManager.LayoutParams.FLAG_SCALED) != 0);
            if (scaledWindow) {
                win.mHScale = (attrs.width  != requestedWidth)  ?
                        (attrs.width  / (float)requestedWidth) : 1.0f;
                win.mVScale = (attrs.height != requestedHeight) ?
                        (attrs.height / (float)requestedHeight) : 1.0f;
            } else {
                win.mHScale = win.mVScale = 1;
            }
            boolean imMayMove = (flagChanges&(
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)) != 0;
            boolean focusMayChange = win.mViewVisibility != viewVisibility
                    || ((flagChanges&WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0)
                    || (!win.mRelayoutCalled);
            boolean wallpaperMayMove = win.mViewVisibility != viewVisibility
                    && (win.mAttrs.flags & FLAG_SHOW_WALLPAPER) != 0;
            win.mRelayoutCalled = true;
            final int oldVisibility = win.mViewVisibility;
            win.mViewVisibility = viewVisibility;
            if (viewVisibility == View.VISIBLE &&
                    (win.mAppToken == null || !win.mAppToken.clientHidden)) {
                displayed = !win.isVisibleLw();
                if (win.mExiting) {
                    win.mExiting = false;
                    win.mAnimation = null;
                }
                if (win.mDestroying) {
                    win.mDestroying = false;
                    mDestroySurface.remove(win);
                }
                if (oldVisibility == View.GONE) {
                    win.mEnterAnimationPending = true;
                }
                if (displayed) {
                    if (win.mSurface != null && !win.mDrawPending
                            && !win.mCommitDrawPending && !mDisplayFrozen
                            && mPolicy.isScreenOn()) {
                        applyEnterAnimationLocked(win);
                    }
                    if ((win.mAttrs.flags
                            & WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON) != 0) {
                        if (DEBUG_VISIBILITY) Slog.v(TAG,
                                "Relayout window turning screen on: " + win);
                        win.mTurnOnScreen = true;
                    }
                    int diff = 0;
                    if (win.mConfiguration != mCurConfiguration
                            && (win.mConfiguration == null
                                    || (diff=mCurConfiguration.diff(win.mConfiguration)) != 0)) {
                        win.mConfiguration = mCurConfiguration;
                        if (DEBUG_CONFIGURATION) {
                            Slog.i(TAG, "Window " + win + " visible with new config: "
                                    + win.mConfiguration + " / 0x"
                                    + Integer.toHexString(diff));
                        }
                        outConfig.setTo(mCurConfiguration);
                    }
                }
                if ((attrChanges&WindowManager.LayoutParams.FORMAT_CHANGED) != 0) {
                    win.destroySurfaceLocked();
                    displayed = true;
                }
                try {
                    Surface surface = win.createSurfaceLocked();
                    if (surface != null) {
                        outSurface.copyFrom(surface);
                        win.mReportDestroySurface = false;
                        win.mSurfacePendingDestroy = false;
                        if (SHOW_TRANSACTIONS) Slog.i(TAG,
                                "  OUT SURFACE " + outSurface + ": copied");
                    } else {
                        outSurface.release();
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Exception thrown when creating surface for client "
                             + client + " (" + win.mAttrs.getTitle() + ")",
                             e);
                    Binder.restoreCallingIdentity(origId);
                    return 0;
                }
                if (displayed) {
                    focusMayChange = true;
                }
                if (win.mAttrs.type == TYPE_INPUT_METHOD
                        && mInputMethodWindow == null) {
                    mInputMethodWindow = win;
                    imMayMove = true;
                }
                if (win.mAttrs.type == TYPE_BASE_APPLICATION
                        && win.mAppToken != null
                        && win.mAppToken.startingWindow != null) {
                    final int mask =
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
                    WindowManager.LayoutParams sa = win.mAppToken.startingWindow.mAttrs;
                    sa.flags = (sa.flags&~mask) | (win.mAttrs.flags&mask);
                }
            } else {
                win.mEnterAnimationPending = false;
                if (win.mSurface != null) {
                    if (DEBUG_VISIBILITY) Slog.i(TAG, "Relayout invis " + win
                            + ": mExiting=" + win.mExiting
                            + " mSurfacePendingDestroy=" + win.mSurfacePendingDestroy);
                    if (!win.mExiting || win.mSurfacePendingDestroy) {
                        int transit = WindowManagerPolicy.TRANSIT_EXIT;
                        if (win.getAttrs().type == TYPE_APPLICATION_STARTING) {
                            transit = WindowManagerPolicy.TRANSIT_PREVIEW_DONE;
                        }
                        if (!win.mSurfacePendingDestroy && win.isWinVisibleLw() &&
                              applyAnimationLocked(win, transit, false)) {
                            focusMayChange = true;
                            win.mExiting = true;
                            mKeyWaiter.finishedKey(session, client, true,
                                    KeyWaiter.RETURN_NOTHING);
                        } else if (win.isAnimating()) {
                            win.mExiting = true;
                        } else if (win == mWallpaperTarget) {
                            win.mExiting = true;
                            win.mAnimating = true;
                        } else {
                            if (mInputMethodWindow == win) {
                                mInputMethodWindow = null;
                            }
                            win.destroySurfaceLocked();
                        }
                    }
                }
                if (win.mSurface == null || (win.getAttrs().flags
                        & WindowManager.LayoutParams.FLAG_KEEP_SURFACE_WHILE_ANIMATING) == 0
                        || win.mSurfacePendingDestroy) {
                    win.mSurfacePendingDestroy = false;
                    outSurface.release();
                    if (DEBUG_VISIBILITY) Slog.i(TAG, "Releasing surface in: " + win);
                } else if (win.mSurface != null) {
                    if (DEBUG_VISIBILITY) Slog.i(TAG,
                            "Keeping surface, will report destroy: " + win);
                    win.mReportDestroySurface = true;
                    outSurface.copyFrom(win.mSurface);
                }
            }
            if (focusMayChange) {
                if (updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                    imMayMove = false;
                }
            }
            boolean assignLayers = false;
            if (imMayMove) {
                if (moveInputMethodWindowsIfNeededLocked(false) || displayed) {
                    assignLayers = true;
                }
            }
            if (wallpaperMayMove) {
                if ((adjustWallpaperWindowsLocked()&ADJUST_WALLPAPER_LAYERS_CHANGED) != 0) {
                    assignLayers = true;
                }
            }
            mLayoutNeeded = true;
            win.mGivenInsetsPending = insetsPending;
            if (assignLayers) {
                assignLayersLocked();
            }
            configChanged = updateOrientationFromAppTokensLocked();
            performLayoutAndPlaceSurfacesLocked();
            if (displayed && win.mIsWallpaper) {
                updateWallpaperOffsetLocked(win, mDisplay.getWidth(),
                        mDisplay.getHeight(), false);
            }
            if (win.mAppToken != null) {
                win.mAppToken.updateReportedVisibilityLocked();
            }
            outFrame.set(win.mFrame);
            outContentInsets.set(win.mContentInsets);
            outVisibleInsets.set(win.mVisibleInsets);
            if (localLOGV) Slog.v(
                TAG, "Relayout given client " + client.asBinder()
                + ", requestedWidth=" + requestedWidth
                + ", requestedHeight=" + requestedHeight
                + ", viewVisibility=" + viewVisibility
                + "\nRelayout returning frame=" + outFrame
                + ", surface=" + outSurface);
            if (localLOGV || DEBUG_FOCUS) Slog.v(
                TAG, "Relayout of " + win + ": focusMayChange=" + focusMayChange);
            inTouchMode = mInTouchMode;
        }
        if (configChanged) {
            sendNewConfiguration();
        }
        Binder.restoreCallingIdentity(origId);
        return (inTouchMode ? WindowManagerImpl.RELAYOUT_IN_TOUCH_MODE : 0)
                | (displayed ? WindowManagerImpl.RELAYOUT_FIRST_TIME : 0);
    }
    public void finishDrawingWindow(Session session, IWindow client) {
        final long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win != null && win.finishDrawingLocked()) {
                if ((win.mAttrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
                    adjustWallpaperWindowsLocked();
                }
                mLayoutNeeded = true;
                performLayoutAndPlaceSurfacesLocked();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }
    private AttributeCache.Entry getCachedAnimations(WindowManager.LayoutParams lp) {
        if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: params package="
                + (lp != null ? lp.packageName : null)
                + " resId=0x" + (lp != null ? Integer.toHexString(lp.windowAnimations) : null));
        if (lp != null && lp.windowAnimations != 0) {
            String packageName = lp.packageName != null ? lp.packageName : "android";
            int resId = lp.windowAnimations;
            if ((resId&0xFF000000) == 0x01000000) {
                packageName = "android";
            }
            if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: picked package="
                    + packageName);
            return AttributeCache.instance().get(packageName, resId,
                    com.android.internal.R.styleable.WindowAnimation);
        }
        return null;
    }
    private AttributeCache.Entry getCachedAnimations(String packageName, int resId) {
        if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: params package="
                + packageName + " resId=0x" + Integer.toHexString(resId));
        if (packageName != null) {
            if ((resId&0xFF000000) == 0x01000000) {
                packageName = "android";
            }
            if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: picked package="
                    + packageName);
            return AttributeCache.instance().get(packageName, resId,
                    com.android.internal.R.styleable.WindowAnimation);
        }
        return null;
    }
    private void applyEnterAnimationLocked(WindowState win) {
        int transit = WindowManagerPolicy.TRANSIT_SHOW;
        if (win.mEnterAnimationPending) {
            win.mEnterAnimationPending = false;
            transit = WindowManagerPolicy.TRANSIT_ENTER;
        }
        applyAnimationLocked(win, transit, true);
    }
    private boolean applyAnimationLocked(WindowState win,
            int transit, boolean isEntrance) {
        if (win.mLocalAnimating && win.mAnimationIsEntrance == isEntrance) {
            return true;
        }
        if (!mDisplayFrozen && mPolicy.isScreenOn()) {
            int anim = mPolicy.selectAnimationLw(win, transit);
            int attr = -1;
            Animation a = null;
            if (anim != 0) {
                a = AnimationUtils.loadAnimation(mContext, anim);
            } else {
                switch (transit) {
                    case WindowManagerPolicy.TRANSIT_ENTER:
                        attr = com.android.internal.R.styleable.WindowAnimation_windowEnterAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_EXIT:
                        attr = com.android.internal.R.styleable.WindowAnimation_windowExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_SHOW:
                        attr = com.android.internal.R.styleable.WindowAnimation_windowShowAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_HIDE:
                        attr = com.android.internal.R.styleable.WindowAnimation_windowHideAnimation;
                        break;
                }
                if (attr >= 0) {
                    a = loadAnimation(win.mAttrs, attr);
                }
            }
            if (DEBUG_ANIM) Slog.v(TAG, "applyAnimation: win=" + win
                    + " anim=" + anim + " attr=0x" + Integer.toHexString(attr)
                    + " mAnimation=" + win.mAnimation
                    + " isEntrance=" + isEntrance);
            if (a != null) {
                if (DEBUG_ANIM) {
                    RuntimeException e = null;
                    if (!HIDE_STACK_CRAWLS) {
                        e = new RuntimeException();
                        e.fillInStackTrace();
                    }
                    Slog.v(TAG, "Loaded animation " + a + " for " + win, e);
                }
                win.setAnimation(a);
                win.mAnimationIsEntrance = isEntrance;
            }
        } else {
            win.clearAnimation();
        }
        return win.mAnimation != null;
    }
    private Animation loadAnimation(WindowManager.LayoutParams lp, int animAttr) {
        int anim = 0;
        Context context = mContext;
        if (animAttr >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, 0);
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }
    private Animation loadAnimation(String packageName, int resId) {
        int anim = 0;
        Context context = mContext;
        if (resId >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(packageName, resId);
            if (ent != null) {
                context = ent.context;
                anim = resId;
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }
    private boolean applyAnimationLocked(AppWindowToken wtoken,
            WindowManager.LayoutParams lp, int transit, boolean enter) {
        if (!mDisplayFrozen && mPolicy.isScreenOn()) {
            Animation a;
            if (lp != null && (lp.flags & FLAG_COMPATIBLE_WINDOW) != 0) {
                a = new FadeInOutAnimation(enter);
                if (DEBUG_ANIM) Slog.v(TAG,
                        "applying FadeInOutAnimation for a window in compatibility mode");
            } else if (mNextAppTransitionPackage != null) {
                a = loadAnimation(mNextAppTransitionPackage, enter ?
                        mNextAppTransitionEnter : mNextAppTransitionExit);
            } else {
                int animAttr = 0;
                switch (transit) {
                    case WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_activityOpenEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_activityOpenExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_activityCloseEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_activityCloseExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_TASK_OPEN:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_taskOpenEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_taskOpenExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_TASK_CLOSE:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_taskCloseEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_taskCloseExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_TASK_TO_FRONT:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_taskToFrontEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_taskToFrontExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_TASK_TO_BACK:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_taskToBackEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_taskToBackExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_WALLPAPER_OPEN:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_wallpaperOpenEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_wallpaperOpenExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_WALLPAPER_CLOSE:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_wallpaperCloseEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_wallpaperCloseExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_WALLPAPER_INTRA_OPEN:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_wallpaperIntraOpenEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_wallpaperIntraOpenExitAnimation;
                        break;
                    case WindowManagerPolicy.TRANSIT_WALLPAPER_INTRA_CLOSE:
                        animAttr = enter
                                ? com.android.internal.R.styleable.WindowAnimation_wallpaperIntraCloseEnterAnimation
                                : com.android.internal.R.styleable.WindowAnimation_wallpaperIntraCloseExitAnimation;
                        break;
                }
                a = animAttr != 0 ? loadAnimation(lp, animAttr) : null;
                if (DEBUG_ANIM) Slog.v(TAG, "applyAnimation: wtoken=" + wtoken
                        + " anim=" + a
                        + " animAttr=0x" + Integer.toHexString(animAttr)
                        + " transit=" + transit);
            }
            if (a != null) {
                if (DEBUG_ANIM) {
                    RuntimeException e = null;
                    if (!HIDE_STACK_CRAWLS) {
                        e = new RuntimeException();
                        e.fillInStackTrace();
                    }
                    Slog.v(TAG, "Loaded animation " + a + " for " + wtoken, e);
                }
                wtoken.setAnimation(a);
            }
        } else {
            wtoken.clearAnimation();
        }
        return wtoken.animation != null;
    }
    public void validateAppTokens(List tokens) {
        int v = tokens.size()-1;
        int m = mAppTokens.size()-1;
        while (v >= 0 && m >= 0) {
            AppWindowToken wtoken = mAppTokens.get(m);
            if (wtoken.removed) {
                m--;
                continue;
            }
            if (tokens.get(v) != wtoken.token) {
                Slog.w(TAG, "Tokens out of sync: external is " + tokens.get(v)
                      + " @ " + v + ", internal is " + wtoken.token + " @ " + m);
            }
            v--;
            m--;
        }
        while (v >= 0) {
            Slog.w(TAG, "External token not found: " + tokens.get(v) + " @ " + v);
            v--;
        }
        while (m >= 0) {
            AppWindowToken wtoken = mAppTokens.get(m);
            if (!wtoken.removed) {
                Slog.w(TAG, "Invalid internal token: " + wtoken.token + " @ " + m);
            }
            m--;
        }
    }
    boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid()) {
            return true;
        }
        if (mContext.checkCallingPermission(permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + ", uid=" + Binder.getCallingUid()
                + " requires " + permission;
        Slog.w(TAG, msg);
        return false;
    }
    AppWindowToken findAppWindowToken(IBinder token) {
        WindowToken wtoken = mTokenMap.get(token);
        if (wtoken == null) {
            return null;
        }
        return wtoken.appWindowToken;
    }
    public void addWindowToken(IBinder token, int type) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "addWindowToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            WindowToken wtoken = mTokenMap.get(token);
            if (wtoken != null) {
                Slog.w(TAG, "Attempted to add existing input method token: " + token);
                return;
            }
            wtoken = new WindowToken(token, type, true);
            mTokenMap.put(token, wtoken);
            mTokenList.add(wtoken);
            if (type == TYPE_WALLPAPER) {
                mWallpaperTokens.add(wtoken);
            }
        }
    }
    public void removeWindowToken(IBinder token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "removeWindowToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        final long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            WindowToken wtoken = mTokenMap.remove(token);
            mTokenList.remove(wtoken);
            if (wtoken != null) {
                boolean delayed = false;
                if (!wtoken.hidden) {
                    wtoken.hidden = true;
                    final int N = wtoken.windows.size();
                    boolean changed = false;
                    for (int i=0; i<N; i++) {
                        WindowState win = wtoken.windows.get(i);
                        if (win.isAnimating()) {
                            delayed = true;
                        }
                        if (win.isVisibleNow()) {
                            applyAnimationLocked(win,
                                    WindowManagerPolicy.TRANSIT_EXIT, false);
                            mKeyWaiter.finishedKey(win.mSession, win.mClient, true,
                                    KeyWaiter.RETURN_NOTHING);
                            changed = true;
                        }
                    }
                    if (changed) {
                        mLayoutNeeded = true;
                        performLayoutAndPlaceSurfacesLocked();
                        updateFocusedWindowLocked(UPDATE_FOCUS_NORMAL);
                    }
                    if (delayed) {
                        mExitingTokens.add(wtoken);
                    } else if (wtoken.windowType == TYPE_WALLPAPER) {
                        mWallpaperTokens.remove(wtoken);
                    }
                }
            } else {
                Slog.w(TAG, "Attempted to remove non-existing token: " + token);
            }
        }
        Binder.restoreCallingIdentity(origId);
    }
    public void addAppToken(int addPos, IApplicationToken token,
            int groupId, int requestedOrientation, boolean fullscreen) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "addAppToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token.asBinder());
            if (wtoken != null) {
                Slog.w(TAG, "Attempted to add existing app token: " + token);
                return;
            }
            wtoken = new AppWindowToken(token);
            wtoken.groupId = groupId;
            wtoken.appFullscreen = fullscreen;
            wtoken.requestedOrientation = requestedOrientation;
            mAppTokens.add(addPos, wtoken);
            if (localLOGV) Slog.v(TAG, "Adding new app token: " + wtoken);
            mTokenMap.put(token.asBinder(), wtoken);
            mTokenList.add(wtoken);
            wtoken.hidden = true;
            wtoken.hiddenRequested = true;
        }
    }
    public void setAppGroupId(IBinder token, int groupId) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppStartingIcon()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken == null) {
                Slog.w(TAG, "Attempted to set group id of non-existing app token: " + token);
                return;
            }
            wtoken.groupId = groupId;
        }
    }
    public int getOrientationFromWindowsLocked() {
        int pos = mWindows.size() - 1;
        while (pos >= 0) {
            WindowState wtoken = (WindowState) mWindows.get(pos);
            pos--;
            if (wtoken.mAppToken != null) {
                return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            }
            if (!wtoken.isVisibleLw() || !wtoken.mPolicyVisibilityAfterAnim) {
                continue;
            }
            int req = wtoken.mAttrs.screenOrientation;
            if((req == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ||
                    (req == ActivityInfo.SCREEN_ORIENTATION_BEHIND)){
                continue;
            } else {
                return req;
            }
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
    public int getOrientationFromAppTokensLocked() {
        int pos = mAppTokens.size() - 1;
        int curGroup = 0;
        int lastOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        boolean findingBehind = false;
        boolean haveGroup = false;
        boolean lastFullscreen = false;
        while (pos >= 0) {
            AppWindowToken wtoken = mAppTokens.get(pos);
            pos--;
            if (!findingBehind
                    && (!wtoken.hidden && wtoken.hiddenRequested)) {
                continue;
            }
            if (!haveGroup) {
                if (wtoken.hiddenRequested || wtoken.willBeHidden) {
                    continue;
                }
                haveGroup = true;
                curGroup = wtoken.groupId;
                lastOrientation = wtoken.requestedOrientation;
            } else if (curGroup != wtoken.groupId) {
                if (lastOrientation != ActivityInfo.SCREEN_ORIENTATION_BEHIND
                        && lastFullscreen) {
                    return lastOrientation;
                }
            }
            int or = wtoken.requestedOrientation;
            lastFullscreen = wtoken.appFullscreen;
            if (lastFullscreen
                    && or != ActivityInfo.SCREEN_ORIENTATION_BEHIND) {
                return or;
            }
            if (or == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    or == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                    or == ActivityInfo.SCREEN_ORIENTATION_SENSOR ||
                    or == ActivityInfo.SCREEN_ORIENTATION_NOSENSOR ||
                    or == ActivityInfo.SCREEN_ORIENTATION_USER) {
                return or;
            }
            findingBehind |= (or == ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
    public Configuration updateOrientationFromAppTokens(
            Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "updateOrientationFromAppTokens()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        Configuration config = null;
        long ident = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            if (updateOrientationFromAppTokensLocked()) {
                if (freezeThisOneIfNeeded != null) {
                    AppWindowToken wtoken = findAppWindowToken(
                            freezeThisOneIfNeeded);
                    if (wtoken != null) {
                        startAppFreezingScreenLocked(wtoken,
                                ActivityInfo.CONFIG_ORIENTATION);
                    }
                }
                config = computeNewConfigurationLocked();
            } else if (currentConfig != null) {
                mTempConfiguration.setToDefaults();
                if (computeNewConfigurationLocked(mTempConfiguration)) {
                    if (currentConfig.diff(mTempConfiguration) != 0) {
                        mWaitingForConfig = true;
                        mLayoutNeeded = true;
                        startFreezingDisplayLocked();
                        config = new Configuration(mTempConfiguration);
                    }
                }
            }
        }
        Binder.restoreCallingIdentity(ident);
        return config;
    }
    boolean updateOrientationFromAppTokensLocked() {
        if (mDisplayFrozen) {
            return false;
        }
        boolean changed = false;
        long ident = Binder.clearCallingIdentity();
        try {
            int req = computeForcedAppOrientationLocked();
            if (req != mForcedAppOrientation) {
                mForcedAppOrientation = req;
                mPolicy.setCurrentOrientationLw(req);
                if (setRotationUncheckedLocked(WindowManagerPolicy.USE_LAST_ROTATION,
                        mLastRotationFlags | Surface.FLAGS_ORIENTATION_ANIMATION_DISABLE)) {
                    changed = true;
                }
            }
            return changed;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    int computeForcedAppOrientationLocked() {
        int req = getOrientationFromWindowsLocked();
        if (req == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            req = getOrientationFromAppTokensLocked();
        }
        return req;
    }
    public void setNewConfiguration(Configuration config) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setNewConfiguration()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            mCurConfiguration = new Configuration(config);
            mWaitingForConfig = false;
            performLayoutAndPlaceSurfacesLocked();
        }
    }
    public void setAppOrientation(IApplicationToken token, int requestedOrientation) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppOrientation()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token.asBinder());
            if (wtoken == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + token);
                return;
            }
            wtoken.requestedOrientation = requestedOrientation;
        }
    }
    public int getAppOrientation(IApplicationToken token) {
        synchronized(mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token.asBinder());
            if (wtoken == null) {
                return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            }
            return wtoken.requestedOrientation;
        }
    }
    public void setFocusedApp(IBinder token, boolean moveFocusNow) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setFocusedApp()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            boolean changed = false;
            if (token == null) {
                if (DEBUG_FOCUS) Slog.v(TAG, "Clearing focused app, was " + mFocusedApp);
                changed = mFocusedApp != null;
                mFocusedApp = null;
                mKeyWaiter.tickle();
            } else {
                AppWindowToken newFocus = findAppWindowToken(token);
                if (newFocus == null) {
                    Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                    return;
                }
                changed = mFocusedApp != newFocus;
                mFocusedApp = newFocus;
                if (DEBUG_FOCUS) Slog.v(TAG, "Set focused app to: " + mFocusedApp);
                mKeyWaiter.tickle();
            }
            if (moveFocusNow && changed) {
                final long origId = Binder.clearCallingIdentity();
                updateFocusedWindowLocked(UPDATE_FOCUS_NORMAL);
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public void prepareAppTransition(int transit) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "prepareAppTransition()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            if (DEBUG_APP_TRANSITIONS) Slog.v(
                    TAG, "Prepare app transition: transit=" + transit
                    + " mNextAppTransition=" + mNextAppTransition);
            if (!mDisplayFrozen && mPolicy.isScreenOn()) {
                if (mNextAppTransition == WindowManagerPolicy.TRANSIT_UNSET
                        || mNextAppTransition == WindowManagerPolicy.TRANSIT_NONE) {
                    mNextAppTransition = transit;
                } else if (transit == WindowManagerPolicy.TRANSIT_TASK_OPEN
                        && mNextAppTransition == WindowManagerPolicy.TRANSIT_TASK_CLOSE) {
                    mNextAppTransition = transit;
                } else if (transit == WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN
                        && mNextAppTransition == WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE) {
                    mNextAppTransition = transit;
                }
                mAppTransitionReady = false;
                mAppTransitionTimeout = false;
                mStartingIconInTransition = false;
                mSkipAppTransitionAnimation = false;
                mH.removeMessages(H.APP_TRANSITION_TIMEOUT);
                mH.sendMessageDelayed(mH.obtainMessage(H.APP_TRANSITION_TIMEOUT),
                        5000);
            }
        }
    }
    public int getPendingAppTransition() {
        return mNextAppTransition;
    }
    public void overridePendingAppTransition(String packageName,
            int enterAnim, int exitAnim) {
        if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
            mNextAppTransitionPackage = packageName;
            mNextAppTransitionEnter = enterAnim;
            mNextAppTransitionExit = exitAnim;
        }
    }
    public void executeAppTransition() {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "executeAppTransition()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            if (DEBUG_APP_TRANSITIONS) {
                RuntimeException e = new RuntimeException("here");
                e.fillInStackTrace();
                Slog.w(TAG, "Execute app transition: mNextAppTransition="
                        + mNextAppTransition, e);
            }
            if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                mAppTransitionReady = true;
                final long origId = Binder.clearCallingIdentity();
                performLayoutAndPlaceSurfacesLocked();
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public void setAppStartingWindow(IBinder token, String pkg,
            int theme, CharSequence nonLocalizedLabel, int labelRes, int icon,
            IBinder transferFrom, boolean createIfNeeded) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppStartingIcon()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            if (DEBUG_STARTING_WINDOW) Slog.v(
                    TAG, "setAppStartingIcon: token=" + token + " pkg=" + pkg
                    + " transferFrom=" + transferFrom);
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken == null) {
                Slog.w(TAG, "Attempted to set icon of non-existing app token: " + token);
                return;
            }
            if (mDisplayFrozen || !mPolicy.isScreenOn()) {
                return;
            }
            if (wtoken.startingData != null) {
                return;
            }
            if (transferFrom != null) {
                AppWindowToken ttoken = findAppWindowToken(transferFrom);
                if (ttoken != null) {
                    WindowState startingWindow = ttoken.startingWindow;
                    if (startingWindow != null) {
                        if (mStartingIconInTransition) {
                            mSkipAppTransitionAnimation = true;
                        }
                        if (DEBUG_STARTING_WINDOW) Slog.v(TAG,
                                "Moving existing starting from " + ttoken
                                + " to " + wtoken);
                        final long origId = Binder.clearCallingIdentity();
                        wtoken.startingData = ttoken.startingData;
                        wtoken.startingView = ttoken.startingView;
                        wtoken.startingWindow = startingWindow;
                        ttoken.startingData = null;
                        ttoken.startingView = null;
                        ttoken.startingWindow = null;
                        ttoken.startingMoved = true;
                        startingWindow.mToken = wtoken;
                        startingWindow.mRootToken = wtoken;
                        startingWindow.mAppToken = wtoken;
                        if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG,
                                "Removing starting window: " + startingWindow);
                        mWindows.remove(startingWindow);
                        ttoken.windows.remove(startingWindow);
                        ttoken.allAppWindows.remove(startingWindow);
                        addWindowToListInOrderLocked(startingWindow, true);
                        if (ttoken.allDrawn) {
                            wtoken.allDrawn = true;
                        }
                        if (ttoken.firstWindowDrawn) {
                            wtoken.firstWindowDrawn = true;
                        }
                        if (!ttoken.hidden) {
                            wtoken.hidden = false;
                            wtoken.hiddenRequested = false;
                            wtoken.willBeHidden = false;
                        }
                        if (wtoken.clientHidden != ttoken.clientHidden) {
                            wtoken.clientHidden = ttoken.clientHidden;
                            wtoken.sendAppVisibilityToClients();
                        }
                        if (ttoken.animation != null) {
                            wtoken.animation = ttoken.animation;
                            wtoken.animating = ttoken.animating;
                            wtoken.animLayerAdjustment = ttoken.animLayerAdjustment;
                            ttoken.animation = null;
                            ttoken.animLayerAdjustment = 0;
                            wtoken.updateLayers();
                            ttoken.updateLayers();
                        }
                        updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
                        mLayoutNeeded = true;
                        performLayoutAndPlaceSurfacesLocked();
                        Binder.restoreCallingIdentity(origId);
                        return;
                    } else if (ttoken.startingData != null) {
                        if (DEBUG_STARTING_WINDOW) Slog.v(TAG,
                                "Moving pending starting from " + ttoken
                                + " to " + wtoken);
                        wtoken.startingData = ttoken.startingData;
                        ttoken.startingData = null;
                        ttoken.startingMoved = true;
                        Message m = mH.obtainMessage(H.ADD_STARTING, wtoken);
                        mH.sendMessageAtFrontOfQueue(m);
                        return;
                    }
                }
            }
            if (!createIfNeeded) {
                return;
            }
            if (theme != 0) {
                AttributeCache.Entry ent = AttributeCache.instance().get(pkg, theme,
                        com.android.internal.R.styleable.Window);
                if (ent.array.getBoolean(
                        com.android.internal.R.styleable.Window_windowIsTranslucent, false)) {
                    return;
                }
                if (ent.array.getBoolean(
                        com.android.internal.R.styleable.Window_windowIsFloating, false)) {
                    return;
                }
                if (ent.array.getBoolean(
                        com.android.internal.R.styleable.Window_windowShowWallpaper, false)) {
                    return;
                }
            }
            mStartingIconInTransition = true;
            wtoken.startingData = new StartingData(
                    pkg, theme, nonLocalizedLabel,
                    labelRes, icon);
            Message m = mH.obtainMessage(H.ADD_STARTING, wtoken);
            mH.sendMessageAtFrontOfQueue(m);
        }
    }
    public void setAppWillBeHidden(IBinder token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppWillBeHidden()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        AppWindowToken wtoken;
        synchronized(mWindowMap) {
            wtoken = findAppWindowToken(token);
            if (wtoken == null) {
                Slog.w(TAG, "Attempted to set will be hidden of non-existing app token: " + token);
                return;
            }
            wtoken.willBeHidden = true;
        }
    }
    boolean setTokenVisibilityLocked(AppWindowToken wtoken, WindowManager.LayoutParams lp,
            boolean visible, int transit, boolean performLayout) {
        boolean delayed = false;
        if (wtoken.clientHidden == visible) {
            wtoken.clientHidden = !visible;
            wtoken.sendAppVisibilityToClients();
        }
        wtoken.willBeHidden = false;
        if (wtoken.hidden == visible) {
            final int N = wtoken.allAppWindows.size();
            boolean changed = false;
            if (DEBUG_APP_TRANSITIONS) Slog.v(
                TAG, "Changing app " + wtoken + " hidden=" + wtoken.hidden
                + " performLayout=" + performLayout);
            boolean runningAppAnimation = false;
            if (transit != WindowManagerPolicy.TRANSIT_UNSET) {
                if (wtoken.animation == sDummyAnimation) {
                    wtoken.animation = null;
                }
                applyAnimationLocked(wtoken, lp, transit, visible);
                changed = true;
                if (wtoken.animation != null) {
                    delayed = runningAppAnimation = true;
                }
            }
            for (int i=0; i<N; i++) {
                WindowState win = wtoken.allAppWindows.get(i);
                if (win == wtoken.startingWindow) {
                    continue;
                }
                if (win.isAnimating()) {
                    delayed = true;
                }
                if (visible) {
                    if (!win.isVisibleNow()) {
                        if (!runningAppAnimation) {
                            applyAnimationLocked(win,
                                    WindowManagerPolicy.TRANSIT_ENTER, true);
                        }
                        changed = true;
                    }
                } else if (win.isVisibleNow()) {
                    if (!runningAppAnimation) {
                        applyAnimationLocked(win,
                                WindowManagerPolicy.TRANSIT_EXIT, false);
                    }
                    mKeyWaiter.finishedKey(win.mSession, win.mClient, true,
                            KeyWaiter.RETURN_NOTHING);
                    changed = true;
                }
            }
            wtoken.hidden = wtoken.hiddenRequested = !visible;
            if (!visible) {
                unsetAppFreezingScreenLocked(wtoken, true, true);
            } else {
                WindowState swin = wtoken.startingWindow;
                if (swin != null && (swin.mDrawPending
                        || swin.mCommitDrawPending)) {
                    swin.mPolicyVisibility = false;
                    swin.mPolicyVisibilityAfterAnim = false;
                 }
            }
            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "setTokenVisibilityLocked: " + wtoken
                      + ": hidden=" + wtoken.hidden + " hiddenRequested="
                      + wtoken.hiddenRequested);
            if (changed) {
                mLayoutNeeded = true;
                if (performLayout) {
                    updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
                    performLayoutAndPlaceSurfacesLocked();
                }
            }
        }
        if (wtoken.animation != null) {
            delayed = true;
        }
        return delayed;
    }
    public void setAppVisibility(IBinder token, boolean visible) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppVisibility()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        AppWindowToken wtoken;
        synchronized(mWindowMap) {
            wtoken = findAppWindowToken(token);
            if (wtoken == null) {
                Slog.w(TAG, "Attempted to set visibility of non-existing app token: " + token);
                return;
            }
            if (DEBUG_APP_TRANSITIONS || DEBUG_ORIENTATION) {
                RuntimeException e = null;
                if (!HIDE_STACK_CRAWLS) {
                    e = new RuntimeException();
                    e.fillInStackTrace();
                }
                Slog.v(TAG, "setAppVisibility(" + token + ", " + visible
                        + "): mNextAppTransition=" + mNextAppTransition
                        + " hidden=" + wtoken.hidden
                        + " hiddenRequested=" + wtoken.hiddenRequested, e);
            }
            if (!mDisplayFrozen && mPolicy.isScreenOn()
                    && mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                if (wtoken.hiddenRequested != visible) {
                    return;
                }
                wtoken.hiddenRequested = !visible;
                if (DEBUG_APP_TRANSITIONS) Slog.v(
                        TAG, "Setting dummy animation on: " + wtoken);
                wtoken.setDummyAnimation();
                mOpeningApps.remove(wtoken);
                mClosingApps.remove(wtoken);
                wtoken.waitingToShow = wtoken.waitingToHide = false;
                wtoken.inPendingTransaction = true;
                if (visible) {
                    mOpeningApps.add(wtoken);
                    wtoken.startingDisplayed = false;
                    wtoken.startingMoved = false;
                    if (wtoken.hidden) {
                        wtoken.allDrawn = false;
                        wtoken.waitingToShow = true;
                        if (wtoken.clientHidden) {
                            wtoken.clientHidden = false;
                            wtoken.sendAppVisibilityToClients();
                        }
                    }
                } else {
                    mClosingApps.add(wtoken);
                    if (!wtoken.hidden) {
                        wtoken.waitingToHide = true;
                    }
                }
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            setTokenVisibilityLocked(wtoken, null, visible, WindowManagerPolicy.TRANSIT_UNSET, true);
            wtoken.updateReportedVisibilityLocked();
            Binder.restoreCallingIdentity(origId);
        }
    }
    void unsetAppFreezingScreenLocked(AppWindowToken wtoken,
            boolean unfreezeSurfaceNow, boolean force) {
        if (wtoken.freezingScreen) {
            if (DEBUG_ORIENTATION) Slog.v(TAG, "Clear freezing of " + wtoken
                    + " force=" + force);
            final int N = wtoken.allAppWindows.size();
            boolean unfrozeWindows = false;
            for (int i=0; i<N; i++) {
                WindowState w = wtoken.allAppWindows.get(i);
                if (w.mAppFreezing) {
                    w.mAppFreezing = false;
                    if (w.mSurface != null && !w.mOrientationChanging) {
                        w.mOrientationChanging = true;
                    }
                    unfrozeWindows = true;
                }
            }
            if (force || unfrozeWindows) {
                if (DEBUG_ORIENTATION) Slog.v(TAG, "No longer freezing: " + wtoken);
                wtoken.freezingScreen = false;
                mAppsFreezingScreen--;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows) {
                    mLayoutNeeded = true;
                    performLayoutAndPlaceSurfacesLocked();
                }
                stopFreezingDisplayLocked();
            }
        }
    }
    public void startAppFreezingScreenLocked(AppWindowToken wtoken,
            int configChanges) {
        if (DEBUG_ORIENTATION) {
            RuntimeException e = null;
            if (!HIDE_STACK_CRAWLS) {
                e = new RuntimeException();
                e.fillInStackTrace();
            }
            Slog.i(TAG, "Set freezing of " + wtoken.appToken
                    + ": hidden=" + wtoken.hidden + " freezing="
                    + wtoken.freezingScreen, e);
        }
        if (!wtoken.hiddenRequested) {
            if (!wtoken.freezingScreen) {
                wtoken.freezingScreen = true;
                mAppsFreezingScreen++;
                if (mAppsFreezingScreen == 1) {
                    startFreezingDisplayLocked();
                    mH.removeMessages(H.APP_FREEZE_TIMEOUT);
                    mH.sendMessageDelayed(mH.obtainMessage(H.APP_FREEZE_TIMEOUT),
                            5000);
                }
            }
            final int N = wtoken.allAppWindows.size();
            for (int i=0; i<N; i++) {
                WindowState w = wtoken.allAppWindows.get(i);
                w.mAppFreezing = true;
            }
        }
    }
    public void startAppFreezingScreen(IBinder token, int configChanges) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppFreezingScreen()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            if (configChanges == 0 && !mDisplayFrozen && mPolicy.isScreenOn()) {
                if (DEBUG_ORIENTATION) Slog.v(TAG, "Skipping set freeze of " + token);
                return;
            }
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken == null || wtoken.appToken == null) {
                Slog.w(TAG, "Attempted to freeze screen with non-existing app token: " + wtoken);
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            startAppFreezingScreenLocked(wtoken, configChanges);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public void stopAppFreezingScreen(IBinder token, boolean force) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "setAppFreezingScreen()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken == null || wtoken.appToken == null) {
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            if (DEBUG_ORIENTATION) Slog.v(TAG, "Clear freezing of " + token
                    + ": hidden=" + wtoken.hidden + " freezing=" + wtoken.freezingScreen);
            unsetAppFreezingScreenLocked(wtoken, true, force);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public void removeAppToken(IBinder token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "removeAppToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        AppWindowToken wtoken = null;
        AppWindowToken startingToken = null;
        boolean delayed = false;
        final long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            WindowToken basewtoken = mTokenMap.remove(token);
            mTokenList.remove(basewtoken);
            if (basewtoken != null && (wtoken=basewtoken.appWindowToken) != null) {
                if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "Removing app token: " + wtoken);
                delayed = setTokenVisibilityLocked(wtoken, null, false, WindowManagerPolicy.TRANSIT_UNSET, true);
                wtoken.inPendingTransaction = false;
                mOpeningApps.remove(wtoken);
                wtoken.waitingToShow = false;
                if (mClosingApps.contains(wtoken)) {
                    delayed = true;
                } else if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                    mClosingApps.add(wtoken);
                    wtoken.waitingToHide = true;
                    delayed = true;
                }
                if (DEBUG_APP_TRANSITIONS) Slog.v(
                        TAG, "Removing app " + wtoken + " delayed=" + delayed
                        + " animation=" + wtoken.animation
                        + " animating=" + wtoken.animating);
                if (delayed) {
                    mExitingAppTokens.add(wtoken);
                } else {
                    wtoken.animation = null;
                    wtoken.animating = false;
                }
                mAppTokens.remove(wtoken);
                if (mLastEnterAnimToken == wtoken) {
                    mLastEnterAnimToken = null;
                    mLastEnterAnimParams = null;
                }
                wtoken.removed = true;
                if (wtoken.startingData != null) {
                    startingToken = wtoken;
                }
                unsetAppFreezingScreenLocked(wtoken, true, true);
                if (mFocusedApp == wtoken) {
                    if (DEBUG_FOCUS) Slog.v(TAG, "Removing focused app token:" + wtoken);
                    mFocusedApp = null;
                    updateFocusedWindowLocked(UPDATE_FOCUS_NORMAL);
                    mKeyWaiter.tickle();
                }
            } else {
                Slog.w(TAG, "Attempted to remove non-existing app token: " + token);
            }
            if (!delayed && wtoken != null) {
                wtoken.updateReportedVisibilityLocked();
            }
        }
        Binder.restoreCallingIdentity(origId);
        if (startingToken != null) {
            if (DEBUG_STARTING_WINDOW) Slog.v(TAG, "Schedule remove starting "
                    + startingToken + ": app token removed");
            Message m = mH.obtainMessage(H.REMOVE_STARTING, startingToken);
            mH.sendMessage(m);
        }
    }
    private boolean tmpRemoveAppWindowsLocked(WindowToken token) {
        final int NW = token.windows.size();
        for (int i=0; i<NW; i++) {
            WindowState win = token.windows.get(i);
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Tmp removing app window " + win);
            mWindows.remove(win);
            int j = win.mChildWindows.size();
            while (j > 0) {
                j--;
                WindowState cwin = (WindowState)win.mChildWindows.get(j);
                if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG,
                        "Tmp removing child window " + cwin);
                mWindows.remove(cwin);
            }
        }
        return NW > 0;
    }
    void dumpAppTokensLocked() {
        for (int i=mAppTokens.size()-1; i>=0; i--) {
            Slog.v(TAG, "  #" + i + ": " + mAppTokens.get(i).token);
        }
    }
    void dumpWindowsLocked() {
        for (int i=mWindows.size()-1; i>=0; i--) {
            Slog.v(TAG, "  #" + i + ": " + mWindows.get(i));
        }
    }
    private int findWindowOffsetLocked(int tokenPos) {
        final int NW = mWindows.size();
        if (tokenPos >= mAppTokens.size()) {
            int i = NW;
            while (i > 0) {
                i--;
                WindowState win = (WindowState)mWindows.get(i);
                if (win.getAppToken() != null) {
                    return i+1;
                }
            }
        }
        while (tokenPos > 0) {
            final AppWindowToken wtoken = mAppTokens.get(tokenPos-1);
            if (DEBUG_REORDER) Slog.v(TAG, "Looking for lower windows @ "
                    + tokenPos + " -- " + wtoken.token);
            if (wtoken.sendingToBottom) {
                if (DEBUG_REORDER) Slog.v(TAG,
                        "Skipping token -- currently sending to bottom");
                tokenPos--;
                continue;
            }
            int i = wtoken.windows.size();
            while (i > 0) {
                i--;
                WindowState win = wtoken.windows.get(i);
                int j = win.mChildWindows.size();
                while (j > 0) {
                    j--;
                    WindowState cwin = (WindowState)win.mChildWindows.get(j);
                    if (cwin.mSubLayer >= 0) {
                        for (int pos=NW-1; pos>=0; pos--) {
                            if (mWindows.get(pos) == cwin) {
                                if (DEBUG_REORDER) Slog.v(TAG,
                                        "Found child win @" + (pos+1));
                                return pos+1;
                            }
                        }
                    }
                }
                for (int pos=NW-1; pos>=0; pos--) {
                    if (mWindows.get(pos) == win) {
                        if (DEBUG_REORDER) Slog.v(TAG, "Found win @" + (pos+1));
                        return pos+1;
                    }
                }
            }
            tokenPos--;
        }
        return 0;
    }
    private final int reAddWindowLocked(int index, WindowState win) {
        final int NCW = win.mChildWindows.size();
        boolean added = false;
        for (int j=0; j<NCW; j++) {
            WindowState cwin = (WindowState)win.mChildWindows.get(j);
            if (!added && cwin.mSubLayer >= 0) {
                if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Re-adding child window at "
                        + index + ": " + cwin);
                mWindows.add(index, win);
                index++;
                added = true;
            }
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Re-adding window at "
                    + index + ": " + cwin);
            mWindows.add(index, cwin);
            index++;
        }
        if (!added) {
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG, "Re-adding window at "
                    + index + ": " + win);
            mWindows.add(index, win);
            index++;
        }
        return index;
    }
    private final int reAddAppWindowsLocked(int index, WindowToken token) {
        final int NW = token.windows.size();
        for (int i=0; i<NW; i++) {
            index = reAddWindowLocked(index, token.windows.get(i));
        }
        return index;
    }
    public void moveAppToken(int index, IBinder token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "moveAppToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized(mWindowMap) {
            if (DEBUG_REORDER) Slog.v(TAG, "Initial app tokens:");
            if (DEBUG_REORDER) dumpAppTokensLocked();
            final AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken == null || !mAppTokens.remove(wtoken)) {
                Slog.w(TAG, "Attempting to reorder token that doesn't exist: "
                      + token + " (" + wtoken + ")");
                return;
            }
            mAppTokens.add(index, wtoken);
            if (DEBUG_REORDER) Slog.v(TAG, "Moved " + token + " to " + index + ":");
            if (DEBUG_REORDER) dumpAppTokensLocked();
            final long origId = Binder.clearCallingIdentity();
            if (DEBUG_REORDER) Slog.v(TAG, "Removing windows in " + token + ":");
            if (DEBUG_REORDER) dumpWindowsLocked();
            if (tmpRemoveAppWindowsLocked(wtoken)) {
                if (DEBUG_REORDER) Slog.v(TAG, "Adding windows back in:");
                if (DEBUG_REORDER) dumpWindowsLocked();
                reAddAppWindowsLocked(findWindowOffsetLocked(index), wtoken);
                if (DEBUG_REORDER) Slog.v(TAG, "Final window list:");
                if (DEBUG_REORDER) dumpWindowsLocked();
                updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
                mLayoutNeeded = true;
                performLayoutAndPlaceSurfacesLocked();
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    private void removeAppTokensLocked(List<IBinder> tokens) {
        int N = tokens.size();
        for (int i=0; i<N; i++) {
            IBinder token = tokens.get(i);
            final AppWindowToken wtoken = findAppWindowToken(token);
            if (!mAppTokens.remove(wtoken)) {
                Slog.w(TAG, "Attempting to reorder token that doesn't exist: "
                      + token + " (" + wtoken + ")");
                i--;
                N--;
            }
        }
    }
    private void moveAppWindowsLocked(AppWindowToken wtoken, int tokenPos,
            boolean updateFocusAndLayout) {
        tmpRemoveAppWindowsLocked(wtoken);
        int pos = findWindowOffsetLocked(tokenPos);
        pos = reAddAppWindowsLocked(pos, wtoken);
        if (updateFocusAndLayout) {
            if (!updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                assignLayersLocked();
            }
            mLayoutNeeded = true;
            performLayoutAndPlaceSurfacesLocked();
        }
    }
    private void moveAppWindowsLocked(List<IBinder> tokens, int tokenPos) {
        final int N = tokens.size();
        int i;
        for (i=0; i<N; i++) {
            WindowToken token = mTokenMap.get(tokens.get(i));
            if (token != null) {
                tmpRemoveAppWindowsLocked(token);
            }
        }
        int pos = findWindowOffsetLocked(tokenPos);
        for (i=0; i<N; i++) {
            WindowToken token = mTokenMap.get(tokens.get(i));
            if (token != null) {
                pos = reAddAppWindowsLocked(pos, token);
            }
        }
        if (!updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
            assignLayersLocked();
        }
        mLayoutNeeded = true;
        performLayoutAndPlaceSurfacesLocked();
    }
    public void moveAppTokensToTop(List<IBinder> tokens) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "moveAppTokensToTop()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        final long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            removeAppTokensLocked(tokens);
            final int N = tokens.size();
            for (int i=0; i<N; i++) {
                AppWindowToken wt = findAppWindowToken(tokens.get(i));
                if (wt != null) {
                    mAppTokens.add(wt);
                    if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                        mToTopApps.remove(wt);
                        mToBottomApps.remove(wt);
                        mToTopApps.add(wt);
                        wt.sendingToBottom = false;
                        wt.sendingToTop = true;
                    }
                }
            }
            if (mNextAppTransition == WindowManagerPolicy.TRANSIT_UNSET) {
                moveAppWindowsLocked(tokens, mAppTokens.size());
            }
        }
        Binder.restoreCallingIdentity(origId);
    }
    public void moveAppTokensToBottom(List<IBinder> tokens) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "moveAppTokensToBottom()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        final long origId = Binder.clearCallingIdentity();
        synchronized(mWindowMap) {
            removeAppTokensLocked(tokens);
            final int N = tokens.size();
            int pos = 0;
            for (int i=0; i<N; i++) {
                AppWindowToken wt = findAppWindowToken(tokens.get(i));
                if (wt != null) {
                    mAppTokens.add(pos, wt);
                    if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                        mToTopApps.remove(wt);
                        mToBottomApps.remove(wt);
                        mToBottomApps.add(i, wt);
                        wt.sendingToTop = false;
                        wt.sendingToBottom = true;
                    }
                    pos++;
                }
            }
            if (mNextAppTransition == WindowManagerPolicy.TRANSIT_UNSET) {
                moveAppWindowsLocked(tokens, 0);
            }
        }
        Binder.restoreCallingIdentity(origId);
    }
    private boolean shouldAllowDisableKeyguard()
    {
        if (mAllowDisableKeyguard == ALLOW_DISABLE_UNKNOWN) {
            DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                mAllowDisableKeyguard = dpm.getPasswordQuality(null)
                        == DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED ?
                                ALLOW_DISABLE_YES : ALLOW_DISABLE_NO;
            }
        }
        return mAllowDisableKeyguard == ALLOW_DISABLE_YES;
    }
    public void disableKeyguard(IBinder token, String tag) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DISABLE_KEYGUARD)
            != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
        synchronized (mKeyguardTokenWatcher) {
            mKeyguardTokenWatcher.acquire(token, tag);
        }
    }
    public void reenableKeyguard(IBinder token) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DISABLE_KEYGUARD)
            != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
        synchronized (mKeyguardTokenWatcher) {
            mKeyguardTokenWatcher.release(token);
            if (!mKeyguardTokenWatcher.isAcquired()) {
                while (mKeyguardDisabled) {
                    try {
                        mKeyguardTokenWatcher.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
    public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DISABLE_KEYGUARD)
            != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
        mPolicy.exitKeyguardSecurely(new WindowManagerPolicy.OnKeyguardExitResult() {
            public void onKeyguardExitResult(boolean success) {
                try {
                    callback.onKeyguardExitResult(success);
                } catch (RemoteException e) {
                }
            }
        });
    }
    public boolean inKeyguardRestrictedInputMode() {
        return mPolicy.inKeyguardRestrictedKeyInputMode();
    }
    public void closeSystemDialogs(String reason) {
        synchronized(mWindowMap) {
            for (int i=mWindows.size()-1; i>=0; i--) {
                WindowState w = (WindowState)mWindows.get(i);
                if (w.mSurface != null) {
                    try {
                        w.mClient.closeSystemDialogs(reason);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }
    static float fixScale(float scale) {
        if (scale < 0) scale = 0;
        else if (scale > 20) scale = 20;
        return Math.abs(scale);
    }
    public void setAnimationScale(int which, float scale) {
        if (!checkCallingPermission(android.Manifest.permission.SET_ANIMATION_SCALE,
                "setAnimationScale()")) {
            throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
        }
        if (scale < 0) scale = 0;
        else if (scale > 20) scale = 20;
        scale = Math.abs(scale);
        switch (which) {
            case 0: mWindowAnimationScale = fixScale(scale); break;
            case 1: mTransitionAnimationScale = fixScale(scale); break;
        }
        mH.obtainMessage(H.PERSIST_ANIMATION_SCALE).sendToTarget();
    }
    public void setAnimationScales(float[] scales) {
        if (!checkCallingPermission(android.Manifest.permission.SET_ANIMATION_SCALE,
                "setAnimationScale()")) {
            throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
        }
        if (scales != null) {
            if (scales.length >= 1) {
                mWindowAnimationScale = fixScale(scales[0]);
            }
            if (scales.length >= 2) {
                mTransitionAnimationScale = fixScale(scales[1]);
            }
        }
        mH.obtainMessage(H.PERSIST_ANIMATION_SCALE).sendToTarget();
    }
    public float getAnimationScale(int which) {
        switch (which) {
            case 0: return mWindowAnimationScale;
            case 1: return mTransitionAnimationScale;
        }
        return 0;
    }
    public float[] getAnimationScales() {
        return new float[] { mWindowAnimationScale, mTransitionAnimationScale };
    }
    public int getSwitchState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getSwitchState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return KeyInputQueue.getSwitchState(sw);
    }
    public int getSwitchStateForDevice(int devid, int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getSwitchStateForDevice()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return KeyInputQueue.getSwitchState(devid, sw);
    }
    public int getScancodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getScancodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getScancodeState(sw);
    }
    public int getScancodeStateForDevice(int devid, int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getScancodeStateForDevice()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getScancodeState(devid, sw);
    }
    public int getTrackballScancodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getTrackballScancodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getTrackballScancodeState(sw);
    }
    public int getDPadScancodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getDPadScancodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getDPadScancodeState(sw);
    }
    public int getKeycodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getKeycodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getKeycodeState(sw);
    }
    public int getKeycodeStateForDevice(int devid, int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getKeycodeStateForDevice()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getKeycodeState(devid, sw);
    }
    public int getTrackballKeycodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getTrackballKeycodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getTrackballKeycodeState(sw);
    }
    public int getDPadKeycodeState(int sw) {
        if (!checkCallingPermission(android.Manifest.permission.READ_INPUT_STATE,
                "getDPadKeycodeState()")) {
            throw new SecurityException("Requires READ_INPUT_STATE permission");
        }
        return mQueue.getDPadKeycodeState(sw);
    }
    public boolean hasKeys(int[] keycodes, boolean[] keyExists) {
        return KeyInputQueue.hasKeys(keycodes, keyExists);
    }
    public void enableScreenAfterBoot() {
        synchronized(mWindowMap) {
            if (mSystemBooted) {
                return;
            }
            mSystemBooted = true;
        }
        performEnableScreen();
    }
    public void enableScreenIfNeededLocked() {
        if (mDisplayEnabled) {
            return;
        }
        if (!mSystemBooted) {
            return;
        }
        mH.sendMessage(mH.obtainMessage(H.ENABLE_SCREEN));
    }
    public void performEnableScreen() {
        synchronized(mWindowMap) {
            if (mDisplayEnabled) {
                return;
            }
            if (!mSystemBooted) {
                return;
            }
            final int N = mWindows.size();
            for (int i=0; i<N; i++) {
                WindowState w = (WindowState)mWindows.get(i);
                if (w.isVisibleLw() && !w.mObscured
                        && (w.mOrientationChanging || !w.isDrawnLw())) {
                    return;
                }
            }
            mDisplayEnabled = true;
            if (false) {
                Slog.i(TAG, "ENABLING SCREEN!");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                this.dump(null, pw, null);
                Slog.i(TAG, sw.toString());
            }
            try {
                IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                if (surfaceFlinger != null) {
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                    surfaceFlinger.transact(IBinder.FIRST_CALL_TRANSACTION,
                                            data, null, 0);
                    data.recycle();
                }
            } catch (RemoteException ex) {
                Slog.e(TAG, "Boot completed: SurfaceFlinger is dead!");
            }
        }
        mPolicy.enableScreenAfterBoot();
        setRotationUnchecked(WindowManagerPolicy.USE_LAST_ROTATION, false,
                mLastRotationFlags | Surface.FLAGS_ORIENTATION_ANIMATION_DISABLE);
    }
    public void setInTouchMode(boolean mode) {
        synchronized(mWindowMap) {
            mInTouchMode = mode;
        }
    }
    public void setRotation(int rotation,
            boolean alwaysSendConfiguration, int animFlags) {
        if (!checkCallingPermission(android.Manifest.permission.SET_ORIENTATION,
                "setRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        }
        setRotationUnchecked(rotation, alwaysSendConfiguration, animFlags);
    }
    public void setRotationUnchecked(int rotation,
            boolean alwaysSendConfiguration, int animFlags) {
        if(DEBUG_ORIENTATION) Slog.v(TAG,
                "alwaysSendConfiguration set to "+alwaysSendConfiguration);
        long origId = Binder.clearCallingIdentity();
        boolean changed;
        synchronized(mWindowMap) {
            changed = setRotationUncheckedLocked(rotation, animFlags);
        }
        if (changed || alwaysSendConfiguration) {
            sendNewConfiguration();
        }
        Binder.restoreCallingIdentity(origId);
    }
    public boolean setRotationUncheckedLocked(int rotation, int animFlags) {
        boolean changed;
        if (rotation == WindowManagerPolicy.USE_LAST_ROTATION) {
            rotation = mRequestedRotation;
        } else {
            mRequestedRotation = rotation;
            mLastRotationFlags = animFlags;
        }
        if (DEBUG_ORIENTATION) Slog.v(TAG, "Overwriting rotation value from " + rotation);
        rotation = mPolicy.rotationForOrientationLw(mForcedAppOrientation,
                mRotation, mDisplayEnabled);
        if (DEBUG_ORIENTATION) Slog.v(TAG, "new rotation is set to " + rotation);
        changed = mDisplayEnabled && mRotation != rotation;
        if (changed) {
            if (DEBUG_ORIENTATION) Slog.v(TAG,
                    "Rotation changed to " + rotation
                    + " from " + mRotation
                    + " (forceApp=" + mForcedAppOrientation
                    + ", req=" + mRequestedRotation + ")");
            mRotation = rotation;
            mWindowsFreezingScreen = true;
            mH.removeMessages(H.WINDOW_FREEZE_TIMEOUT);
            mH.sendMessageDelayed(mH.obtainMessage(H.WINDOW_FREEZE_TIMEOUT),
                    2000);
            mWaitingForConfig = true;
            mLayoutNeeded = true;
            startFreezingDisplayLocked();
            Slog.i(TAG, "Setting rotation to " + rotation + ", animFlags=" + animFlags);
            mQueue.setOrientation(rotation);
            if (mDisplayEnabled) {
                Surface.setOrientation(0, rotation, animFlags);
            }
            for (int i=mWindows.size()-1; i>=0; i--) {
                WindowState w = (WindowState)mWindows.get(i);
                if (w.mSurface != null) {
                    w.mOrientationChanging = true;
                }
            }
            for (int i=mRotationWatchers.size()-1; i>=0; i--) {
                try {
                    mRotationWatchers.get(i).onRotationChanged(rotation);
                } catch (RemoteException e) {
                }
            }
        } 
        return changed;
    }
    public int getRotation() {
        return mRotation;
    }
    public int watchRotation(IRotationWatcher watcher) {
        final IBinder watcherBinder = watcher.asBinder();
        IBinder.DeathRecipient dr = new IBinder.DeathRecipient() {
            public void binderDied() {
                synchronized (mWindowMap) {
                    for (int i=0; i<mRotationWatchers.size(); i++) {
                        if (watcherBinder == mRotationWatchers.get(i).asBinder()) {
                            IRotationWatcher removed = mRotationWatchers.remove(i);
                            if (removed != null) {
                                removed.asBinder().unlinkToDeath(this, 0);
                            }
                            i--;
                        }
                    }
                }
            }
        };
        synchronized (mWindowMap) {
            try {
                watcher.asBinder().linkToDeath(dr, 0);
                mRotationWatchers.add(watcher);
            } catch (RemoteException e) {
            }
            return mRotation;
        }
    }
    public boolean startViewServer(int port) {
        if (isSystemSecure()) {
            return false;
        }
        if (!checkCallingPermission(Manifest.permission.DUMP, "startViewServer")) {
            return false;
        }
        if (port < 1024) {
            return false;
        }
        if (mViewServer != null) {
            if (!mViewServer.isRunning()) {
                try {
                    return mViewServer.start();
                } catch (IOException e) {
                    Slog.w(TAG, "View server did not start");
                }
            }
            return false;
        }
        try {
            mViewServer = new ViewServer(this, port);
            return mViewServer.start();
        } catch (IOException e) {
            Slog.w(TAG, "View server did not start");
        }
        return false;
    }
    private boolean isSystemSecure() {
        return "1".equals(SystemProperties.get(SYSTEM_SECURE, "1")) &&
                "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
    }
    public boolean stopViewServer() {
        if (isSystemSecure()) {
            return false;
        }
        if (!checkCallingPermission(Manifest.permission.DUMP, "stopViewServer")) {
            return false;
        }
        if (mViewServer != null) {
            return mViewServer.stop();
        }
        return false;
    }
    public boolean isViewServerRunning() {
        if (isSystemSecure()) {
            return false;
        }
        if (!checkCallingPermission(Manifest.permission.DUMP, "isViewServerRunning")) {
            return false;
        }
        return mViewServer != null && mViewServer.isRunning();
    }
    boolean viewServerListWindows(Socket client) {
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        Object[] windows;
        synchronized (mWindowMap) {
            windows = new Object[mWindows.size()];
            windows = mWindows.toArray(windows);
        }
        BufferedWriter out = null;
        try {
            OutputStream clientStream = client.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 8 * 1024);
            final int count = windows.length;
            for (int i = 0; i < count; i++) {
                final WindowState w = (WindowState) windows[i];
                out.write(Integer.toHexString(System.identityHashCode(w)));
                out.write(' ');
                out.append(w.mAttrs.getTitle());
                out.write('\n');
            }
            out.write("DONE.\n");
            out.flush();
        } catch (Exception e) {
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    result = false;
                }
            }
        }
        return result;
    }
    boolean viewServerWindowCommand(Socket client, String command, String parameters) {
        if (isSystemSecure()) {
            return false;
        }
        boolean success = true;
        Parcel data = null;
        Parcel reply = null;
        try {
            int index = parameters.indexOf(' ');
            if (index == -1) {
                index = parameters.length();
            }
            final String code = parameters.substring(0, index);
            int hashCode = (int) Long.parseLong(code, 16);
            if (index < parameters.length()) {
                parameters = parameters.substring(index + 1);
            } else {
                parameters = "";
            }
            final WindowManagerService.WindowState window = findWindow(hashCode);
            if (window == null) {
                return false;
            }
            data = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindow");
            data.writeString(command);
            data.writeString(parameters);
            data.writeInt(1);
            ParcelFileDescriptor.fromSocket(client).writeToParcel(data, 0);
            reply = Parcel.obtain();
            final IBinder binder = window.mClient.asBinder();
            binder.transact(IBinder.FIRST_CALL_TRANSACTION, data, reply, 0);
            reply.readException();
        } catch (Exception e) {
            Slog.w(TAG, "Could not send command " + command + " with parameters " + parameters, e);
            success = false;
        } finally {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
        return success;
    }
    private WindowState findWindow(int hashCode) {
        if (hashCode == -1) {
            return getFocusedWindow();
        }
        synchronized (mWindowMap) {
            final ArrayList windows = mWindows;
            final int count = windows.size();
            for (int i = 0; i < count; i++) {
                WindowState w = (WindowState) windows.get(i);
                if (System.identityHashCode(w) == hashCode) {
                    return w;
                }
            }
        }
        return null;
    }
    void sendNewConfiguration() {
        try {
            mActivityManager.updateConfiguration(null);
        } catch (RemoteException e) {
        }
    }
    public Configuration computeNewConfiguration() {
        synchronized (mWindowMap) {
            return computeNewConfigurationLocked();
        }
    }
    Configuration computeNewConfigurationLocked() {
        Configuration config = new Configuration();
        if (!computeNewConfigurationLocked(config)) {
            return null;
        }
        return config;
    }
    boolean computeNewConfigurationLocked(Configuration config) {
        if (mDisplay == null) {
            return false;
        }
        mQueue.getInputConfiguration(config);
        final boolean rotated = (mRotation == Surface.ROTATION_90
                || mRotation == Surface.ROTATION_270);
        final int dw = rotated ? mInitialDisplayHeight : mInitialDisplayWidth;
        final int dh = rotated ? mInitialDisplayWidth : mInitialDisplayHeight;
        int orientation = Configuration.ORIENTATION_SQUARE;
        if (dw < dh) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else if (dw > dh) {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        config.orientation = orientation;
        DisplayMetrics dm = new DisplayMetrics();
        mDisplay.getMetrics(dm);
        CompatibilityInfo.updateCompatibleScreenFrame(dm, orientation, mCompatibleScreenFrame);
        if (mScreenLayout == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
            int longSize = dw;
            int shortSize = dh;
            if (longSize < shortSize) {
                int tmp = longSize;
                longSize = shortSize;
                shortSize = tmp;
            }
            longSize = (int)(longSize/dm.density);
            shortSize = (int)(shortSize/dm.density);
            if (longSize < 470) {
                mScreenLayout = Configuration.SCREENLAYOUT_SIZE_SMALL
                        | Configuration.SCREENLAYOUT_LONG_NO;
            } else {
                if (longSize > 640 && shortSize >= 480) {
                    mScreenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
                } else {
                    mScreenLayout = Configuration.SCREENLAYOUT_SIZE_NORMAL;
                    if (shortSize > 321 || longSize > 570) {
                        mScreenLayout |= Configuration.SCREENLAYOUT_COMPAT_NEEDED;
                    }
                }
                if (((longSize*3)/5) >= (shortSize-1)) {
                    mScreenLayout |= Configuration.SCREENLAYOUT_LONG_YES;
                } else {
                    mScreenLayout |= Configuration.SCREENLAYOUT_LONG_NO;
                }
            }
        }
        config.screenLayout = mScreenLayout;
        config.keyboardHidden = Configuration.KEYBOARDHIDDEN_NO;
        config.hardKeyboardHidden = Configuration.HARDKEYBOARDHIDDEN_NO;
        mPolicy.adjustConfigurationLw(config);
        return true;
    }
    private final void wakeupIfNeeded(WindowState targetWin, int eventType) {
        long curTime = SystemClock.uptimeMillis();
        if (eventType == TOUCH_EVENT || eventType == LONG_TOUCH_EVENT || eventType == CHEEK_EVENT) {
            if (mLastTouchEventType == eventType &&
                    (curTime - mLastUserActivityCallTime) < MIN_TIME_BETWEEN_USERACTIVITIES) {
                return;
            }
            mLastUserActivityCallTime = curTime;
            mLastTouchEventType = eventType;
        }
        if (targetWin == null
                || targetWin.mAttrs.type != WindowManager.LayoutParams.TYPE_KEYGUARD) {
            mPowerManager.userActivity(curTime, false, eventType, false);
        }
    }
    private static final int EVENT_NONE = 0;
    private static final int EVENT_UNKNOWN = 0;
    private static final int EVENT_CHEEK = 0;
    private static final int EVENT_IGNORE_DURATION = 300; 
    private static final float CHEEK_THRESHOLD = 0.6f;
    private int mEventState = EVENT_NONE;
    private float mEventSize;
    private int eventType(MotionEvent ev) {
        float size = ev.getSize();
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mEventSize = size;
            return (mEventSize > CHEEK_THRESHOLD) ? CHEEK_EVENT : TOUCH_EVENT;
        case MotionEvent.ACTION_UP:
            if (size > mEventSize) mEventSize = size;
            return (mEventSize > CHEEK_THRESHOLD) ? CHEEK_EVENT : TOUCH_UP_EVENT;
        case MotionEvent.ACTION_MOVE:
            final int N = ev.getHistorySize();
            if (size > mEventSize) mEventSize = size;
            if (mEventSize > CHEEK_THRESHOLD) return CHEEK_EVENT;
            for (int i=0; i<N; i++) {
                size = ev.getHistoricalSize(i);
                if (size > mEventSize) mEventSize = size;
                if (mEventSize > CHEEK_THRESHOLD) return CHEEK_EVENT;
            }
            if (ev.getEventTime() < ev.getDownTime() + EVENT_IGNORE_DURATION) {
                return TOUCH_EVENT;
            } else {
                return LONG_TOUCH_EVENT;
            }
        default:
            return OTHER_EVENT;
        }
    }
    private int dispatchPointer(QueuedEvent qev, MotionEvent ev, int pid, int uid) {
        if (DEBUG_INPUT || WindowManagerPolicy.WATCH_POINTER) Slog.v(TAG,
                "dispatchPointer " + ev);
        if (MEASURE_LATENCY) {
            lt.sample("3 Wait for last dispatch ", System.nanoTime() - qev.whenNano);
        }
        Object targetObj = mKeyWaiter.waitForNextEventTarget(null, qev,
                ev, true, false, pid, uid);
        if (MEASURE_LATENCY) {
            lt.sample("3 Last dispatch finished ", System.nanoTime() - qev.whenNano);
        }
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP) {
            mKeyWaiter.mMotionTarget = null;
            mPowerManager.logPointerUpEvent();
        } else if (action == MotionEvent.ACTION_DOWN) {
            mPowerManager.logPointerDownEvent();
        }
        if (targetObj == null) {
            if (action != MotionEvent.ACTION_MOVE) {
                Slog.w(TAG, "No window to dispatch pointer action " + ev.getAction());
            }
            synchronized (mWindowMap) {
                dispatchPointerElsewhereLocked(null, null, ev, ev.getEventTime(), true);
            }
            if (qev != null) {
                mQueue.recycleEvent(qev);
            }
            ev.recycle();
            return INJECT_FAILED;
        }
        if (targetObj == mKeyWaiter.CONSUMED_EVENT_TOKEN) {
            synchronized (mWindowMap) {
                dispatchPointerElsewhereLocked(null, null, ev, ev.getEventTime(), true);
            }
            if (qev != null) {
                mQueue.recycleEvent(qev);
            }
            ev.recycle();
            return INJECT_SUCCEEDED;
        }
        WindowState target = (WindowState)targetObj;
        final long eventTime = ev.getEventTime();
        final long eventTimeNano = ev.getEventTimeNano();
        if (uid != 0 && uid != target.mSession.mUid) {
            if (mContext.checkPermission(
                    android.Manifest.permission.INJECT_EVENTS, pid, uid)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission denied: injecting pointer event from pid "
                        + pid + " uid " + uid + " to window " + target
                        + " owned by uid " + target.mSession.mUid);
                if (qev != null) {
                    mQueue.recycleEvent(qev);
                }
                ev.recycle();
                return INJECT_NO_PERMISSION;
            }
        }
        if (MEASURE_LATENCY) {
            lt.sample("4 in dispatchPointer     ", System.nanoTime() - eventTimeNano);
        }
        if ((target.mAttrs.flags &
                        WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES) != 0) {
            boolean cheekPress = mPolicy.isCheekPressedAgainstScreen(ev);
            boolean returnFlag = false;
            if((action == MotionEvent.ACTION_DOWN)) {
                mFatTouch = false;
                if(cheekPress) {
                    mFatTouch = true;
                    returnFlag = true;
                }
            } else {
                if(action == MotionEvent.ACTION_UP) {
                    if(mFatTouch) {
                        mFatTouch = false;
                        returnFlag = true;
                    } else if(cheekPress) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        action = MotionEvent.ACTION_CANCEL;
                    }
                } else if(action == MotionEvent.ACTION_MOVE) {
                    if(mFatTouch) {
                        returnFlag = true;
                    } else if(cheekPress) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        action = MotionEvent.ACTION_CANCEL;
                        if (DEBUG_INPUT) Slog.v(TAG, "Sending cancel for invalid ACTION_MOVE");
                        mFatTouch = true;
                    }
                }
            } 
            if(returnFlag) {
                if (qev != null) {
                    mQueue.recycleEvent(qev);
                }
                ev.recycle();
                return INJECT_FAILED;
            }
        } 
        if (false && action == MotionEvent.ACTION_DOWN) {
            int max_events_per_sec = 35;
            try {
                max_events_per_sec = Integer.parseInt(SystemProperties
                        .get("windowsmgr.max_events_per_sec"));
                if (max_events_per_sec < 1) {
                    max_events_per_sec = 35;
                }
            } catch (NumberFormatException e) {
            }
            mMinWaitTimeBetweenTouchEvents = 1000 / max_events_per_sec;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            long nextEventTime = mLastTouchEventTime + mMinWaitTimeBetweenTouchEvents;
            long now = SystemClock.uptimeMillis();
            if (now < nextEventTime) {
                try {
                    Thread.sleep(nextEventTime - now);
                } catch (InterruptedException e) {
                }
                mLastTouchEventTime = nextEventTime;
            } else {
                mLastTouchEventTime = now;
            }
        }
        if (MEASURE_LATENCY) {
            lt.sample("5 in dispatchPointer     ", System.nanoTime() - eventTimeNano);
        }
        synchronized(mWindowMap) {
            if (!target.isVisibleLw()) {
                dispatchPointerElsewhereLocked(null, null, ev, ev.getEventTime(), false);
                if (qev != null) {
                    mQueue.recycleEvent(qev);
                }
                ev.recycle();
                return INJECT_SUCCEEDED;
            }
            if (qev != null && action == MotionEvent.ACTION_MOVE) {
                mKeyWaiter.bindTargetWindowLocked(target,
                        KeyWaiter.RETURN_PENDING_POINTER, qev);
                ev = null;
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    WindowState out = mKeyWaiter.mOutsideTouchTargets;
                    if (out != null) {
                        MotionEvent oev = MotionEvent.obtain(ev);
                        oev.setAction(MotionEvent.ACTION_OUTSIDE);
                        do {
                            final Rect frame = out.mFrame;
                            oev.offsetLocation(-(float)frame.left, -(float)frame.top);
                            try {
                                out.mClient.dispatchPointer(oev, eventTime, false);
                            } catch (android.os.RemoteException e) {
                                Slog.i(TAG, "WINDOW DIED during outside motion dispatch: " + out);
                            }
                            oev.offsetLocation((float)frame.left, (float)frame.top);
                            out = out.mNextOutsideTouch;
                        } while (out != null);
                        mKeyWaiter.mOutsideTouchTargets = null;
                    }
                }
                dispatchPointerElsewhereLocked(target, null, ev, ev.getEventTime(), false);
                final Rect frame = target.mFrame;
                ev.offsetLocation(-(float)frame.left, -(float)frame.top);
                mKeyWaiter.bindTargetWindowLocked(target);
            }
        }
        try {
            if (DEBUG_INPUT || DEBUG_FOCUS || WindowManagerPolicy.WATCH_POINTER) {
                Slog.v(TAG, "Delivering pointer " + qev + " to " + target);
            }
            if (MEASURE_LATENCY) {
                lt.sample("6 before svr->client ipc ", System.nanoTime() - eventTimeNano);
            }
            target.mClient.dispatchPointer(ev, eventTime, true);
            if (MEASURE_LATENCY) {
                lt.sample("7 after  svr->client ipc ", System.nanoTime() - eventTimeNano);
            }
            return INJECT_SUCCEEDED;
        } catch (android.os.RemoteException e) {
            Slog.i(TAG, "WINDOW DIED during motion dispatch: " + target);
            mKeyWaiter.mMotionTarget = null;
            try {
                removeWindow(target.mSession, target.mClient);
            } catch (java.util.NoSuchElementException ex) {
            }
        }
        return INJECT_FAILED;
    }
    private int dispatchTrackball(QueuedEvent qev, MotionEvent ev, int pid, int uid) {
        if (DEBUG_INPUT) Slog.v(
                TAG, "dispatchTrackball [" + ev.getAction() +"] <" + ev.getX() + ", " + ev.getY() + ">");
        Object focusObj = mKeyWaiter.waitForNextEventTarget(null, qev,
                ev, false, false, pid, uid);
        if (focusObj == null) {
            Slog.w(TAG, "No focus window, dropping trackball: " + ev);
            if (qev != null) {
                mQueue.recycleEvent(qev);
            }
            ev.recycle();
            return INJECT_FAILED;
        }
        if (focusObj == mKeyWaiter.CONSUMED_EVENT_TOKEN) {
            if (qev != null) {
                mQueue.recycleEvent(qev);
            }
            ev.recycle();
            return INJECT_SUCCEEDED;
        }
        WindowState focus = (WindowState)focusObj;
        if (uid != 0 && uid != focus.mSession.mUid) {
            if (mContext.checkPermission(
                    android.Manifest.permission.INJECT_EVENTS, pid, uid)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission denied: injecting key event from pid "
                        + pid + " uid " + uid + " to window " + focus
                        + " owned by uid " + focus.mSession.mUid);
                if (qev != null) {
                    mQueue.recycleEvent(qev);
                }
                ev.recycle();
                return INJECT_NO_PERMISSION;
            }
        }
        final long eventTime = ev.getEventTime();
        synchronized(mWindowMap) {
            if (qev != null && ev.getAction() == MotionEvent.ACTION_MOVE) {
                mKeyWaiter.bindTargetWindowLocked(focus,
                        KeyWaiter.RETURN_PENDING_TRACKBALL, qev);
                ev = null;
            } else {
                mKeyWaiter.bindTargetWindowLocked(focus);
            }
        }
        try {
            focus.mClient.dispatchTrackball(ev, eventTime, true);
            return INJECT_SUCCEEDED;
        } catch (android.os.RemoteException e) {
            Slog.i(TAG, "WINDOW DIED during key dispatch: " + focus);
            try {
                removeWindow(focus.mSession, focus.mClient);
            } catch (java.util.NoSuchElementException ex) {
            }
        }
        return INJECT_FAILED;
    }
    private int dispatchKey(KeyEvent event, int pid, int uid) {
        if (DEBUG_INPUT) Slog.v(TAG, "Dispatch key: " + event);
        Object focusObj = mKeyWaiter.waitForNextEventTarget(event, null,
                null, false, false, pid, uid);
        if (focusObj == null) {
            Slog.w(TAG, "No focus window, dropping: " + event);
            return INJECT_FAILED;
        }
        if (focusObj == mKeyWaiter.CONSUMED_EVENT_TOKEN) {
            return INJECT_SUCCEEDED;
        }
        if (event.getRepeatCount() > 0 && mQueue.hasKeyUpEvent(event)) {
            return INJECT_SUCCEEDED;
        }
        WindowState focus = (WindowState)focusObj;
        if (DEBUG_INPUT) Slog.v(
            TAG, "Dispatching to " + focus + ": " + event);
        if (uid != 0 && uid != focus.mSession.mUid) {
            if (mContext.checkPermission(
                    android.Manifest.permission.INJECT_EVENTS, pid, uid)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission denied: injecting key event from pid "
                        + pid + " uid " + uid + " to window " + focus
                        + " owned by uid " + focus.mSession.mUid);
                return INJECT_NO_PERMISSION;
            }
        }
        synchronized(mWindowMap) {
            mKeyWaiter.bindTargetWindowLocked(focus);
        }
        mKeyWaiter.recordDispatchState(event, focus);
        try {
            if (DEBUG_INPUT || DEBUG_FOCUS) {
                Slog.v(TAG, "Delivering key " + event.getKeyCode()
                        + " to " + focus);
            }
            focus.mClient.dispatchKey(event);
            return INJECT_SUCCEEDED;
        } catch (android.os.RemoteException e) {
            Slog.i(TAG, "WINDOW DIED during key dispatch: " + focus);
            try {
                removeWindow(focus.mSession, focus.mClient);
            } catch (java.util.NoSuchElementException ex) {
            }
        }
        return INJECT_FAILED;
    }
    public void pauseKeyDispatching(IBinder _token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "pauseKeyDispatching()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized (mWindowMap) {
            WindowToken token = mTokenMap.get(_token);
            if (token != null) {
                mKeyWaiter.pauseDispatchingLocked(token);
            }
        }
    }
    public void resumeKeyDispatching(IBinder _token) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "resumeKeyDispatching()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized (mWindowMap) {
            WindowToken token = mTokenMap.get(_token);
            if (token != null) {
                mKeyWaiter.resumeDispatchingLocked(token);
            }
        }
    }
    public void setEventDispatching(boolean enabled) {
        if (!checkCallingPermission(android.Manifest.permission.MANAGE_APP_TOKENS,
                "resumeKeyDispatching()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
        synchronized (mWindowMap) {
            mKeyWaiter.setEventDispatchingLocked(enabled);
        }
    }
    public boolean injectKeyEvent(KeyEvent ev, boolean sync) {
        long downTime = ev.getDownTime();
        long eventTime = ev.getEventTime();
        int action = ev.getAction();
        int code = ev.getKeyCode();
        int repeatCount = ev.getRepeatCount();
        int metaState = ev.getMetaState();
        int deviceId = ev.getDeviceId();
        int scancode = ev.getScanCode();
        if (eventTime == 0) eventTime = SystemClock.uptimeMillis();
        if (downTime == 0) downTime = eventTime;
        KeyEvent newEvent = new KeyEvent(downTime, eventTime, action, code, repeatCount, metaState,
                deviceId, scancode, KeyEvent.FLAG_FROM_SYSTEM);
        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        final long ident = Binder.clearCallingIdentity();
        final int result = dispatchKey(newEvent, pid, uid);
        if (sync) {
            mKeyWaiter.waitForNextEventTarget(null, null, null, false, true, pid, uid);
        }
        Binder.restoreCallingIdentity(ident);
        switch (result) {
            case INJECT_NO_PERMISSION:
                throw new SecurityException(
                        "Injecting to another application requires INJECT_EVENTS permission");
            case INJECT_SUCCEEDED:
                return true;
        }
        return false;
    }
    public boolean injectPointerEvent(MotionEvent ev, boolean sync) {
        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        final long ident = Binder.clearCallingIdentity();
        final int result = dispatchPointer(null, ev, pid, uid);
        if (sync) {
            mKeyWaiter.waitForNextEventTarget(null, null, null, false, true, pid, uid);
        }
        Binder.restoreCallingIdentity(ident);
        switch (result) {
            case INJECT_NO_PERMISSION:
                throw new SecurityException(
                        "Injecting to another application requires INJECT_EVENTS permission");
            case INJECT_SUCCEEDED:
                return true;
        }
        return false;
    }
    public boolean injectTrackballEvent(MotionEvent ev, boolean sync) {
        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        final long ident = Binder.clearCallingIdentity();
        final int result = dispatchTrackball(null, ev, pid, uid);
        if (sync) {
            mKeyWaiter.waitForNextEventTarget(null, null, null, false, true, pid, uid);
        }
        Binder.restoreCallingIdentity(ident);
        switch (result) {
            case INJECT_NO_PERMISSION:
                throw new SecurityException(
                        "Injecting to another application requires INJECT_EVENTS permission");
            case INJECT_SUCCEEDED:
                return true;
        }
        return false;
    }
    private WindowState getFocusedWindow() {
        synchronized (mWindowMap) {
            return getFocusedWindowLocked();
        }
    }
    private WindowState getFocusedWindowLocked() {
        return mCurrentFocus;
    }
    final class KeyWaiter {
        public class DispatchState {
            private KeyEvent event;
            private WindowState focus;
            private long time;
            private WindowState lastWin;
            private IBinder lastBinder;
            private boolean finished;
            private boolean gotFirstWindow;
            private boolean eventDispatching;
            private long timeToSwitch;
            private boolean wasFrozen;
            private boolean focusPaused;
            private WindowState curFocus;
            DispatchState(KeyEvent theEvent, WindowState theFocus) {
                focus = theFocus;
                event = theEvent;
                time = System.currentTimeMillis();
                lastWin = mLastWin;
                lastBinder = mLastBinder;
                finished = mFinished;
                gotFirstWindow = mGotFirstWindow;
                eventDispatching = mEventDispatching;
                timeToSwitch = mTimeToSwitch;
                wasFrozen = mWasFrozen;
                curFocus = mCurrentFocus;
                if (theFocus == null || theFocus.mToken == null) {
                    focusPaused = false;
                } else {
                    focusPaused = theFocus.mToken.paused;
                }
            }
            public String toString() {
                return "{{" + event + " to " + focus + " @ " + time
                        + " lw=" + lastWin + " lb=" + lastBinder
                        + " fin=" + finished + " gfw=" + gotFirstWindow
                        + " ed=" + eventDispatching + " tts=" + timeToSwitch
                        + " wf=" + wasFrozen + " fp=" + focusPaused
                        + " mcf=" + curFocus + "}}";
            }
        };
        private DispatchState mDispatchState = null;
        public void recordDispatchState(KeyEvent theEvent, WindowState theFocus) {
            mDispatchState = new DispatchState(theEvent, theFocus);
        }
        public static final int RETURN_NOTHING = 0;
        public static final int RETURN_PENDING_POINTER = 1;
        public static final int RETURN_PENDING_TRACKBALL = 2;
        final Object SKIP_TARGET_TOKEN = new Object();
        final Object CONSUMED_EVENT_TOKEN = new Object();
        private WindowState mLastWin = null;
        private IBinder mLastBinder = null;
        private boolean mFinished = true;
        private boolean mGotFirstWindow = false;
        private boolean mEventDispatching = true;
        private long mTimeToSwitch = 0;
         boolean mWasFrozen = false;
        WindowState mMotionTarget;
        WindowState mOutsideTouchTargets;
        Object waitForNextEventTarget(KeyEvent nextKey, QueuedEvent qev,
                MotionEvent nextMotion, boolean isPointerEvent,
                boolean failIfTimeout, int callingPid, int callingUid) {
            long startTime = SystemClock.uptimeMillis();
            long keyDispatchingTimeout = 5 * 1000;
            long waitedFor = 0;
            while (true) {
                WindowState targetWin = mLastWin;
                boolean targetIsNew = targetWin == null;
                if (DEBUG_INPUT) Slog.v(
                        TAG, "waitForLastKey: mFinished=" + mFinished +
                        ", mLastWin=" + mLastWin);
                if (targetIsNew) {
                    Object target = findTargetWindow(nextKey, qev, nextMotion,
                            isPointerEvent, callingPid, callingUid);
                    if (target == SKIP_TARGET_TOKEN) {
                        if (DEBUG_INPUT) Slog.v(TAG, "Skipping: " + nextKey
                                + " " + nextMotion);
                        return null;
                    }
                    if (target == CONSUMED_EVENT_TOKEN) {
                        if (DEBUG_INPUT) Slog.v(TAG, "Consumed: " + nextKey
                                + " " + nextMotion);
                        return target;
                    }
                    targetWin = (WindowState)target;
                }
                AppWindowToken targetApp = null;
                synchronized (this) {
                    if (!targetIsNew && mLastWin == null) {
                        continue;
                    }
                    if (mFinished && !mDisplayFrozen) {
                        if (!mEventDispatching) {
                            if (DEBUG_INPUT) Slog.v(TAG,
                                    "Skipping event; dispatching disabled: "
                                    + nextKey + " " + nextMotion);
                            return null;
                        }
                        if (targetWin != null) {
                            if (targetIsNew && !targetWin.mToken.paused) {
                                return targetWin;
                            }
                        } else if (mFocusedApp == null) {
                            if (DEBUG_INPUT) Slog.v(TAG,
                                    "Skipping event; no focused app: "
                                    + nextKey + " " + nextMotion);
                            return null;
                        }
                    }
                    if (DEBUG_INPUT) Slog.v(
                            TAG, "Waiting for last key in " + mLastBinder
                            + " target=" + targetWin
                            + " mFinished=" + mFinished
                            + " mDisplayFrozen=" + mDisplayFrozen
                            + " targetIsNew=" + targetIsNew
                            + " paused="
                            + (targetWin != null ? targetWin.mToken.paused : false)
                            + " mFocusedApp=" + mFocusedApp
                            + " mCurrentFocus=" + mCurrentFocus);
                    targetApp = targetWin != null
                            ? targetWin.mAppToken : mFocusedApp;
                    long curTimeout = keyDispatchingTimeout;
                    if (mTimeToSwitch != 0) {
                        long now = SystemClock.uptimeMillis();
                        if (mTimeToSwitch <= now) {
                            doFinishedKeyLocked(false);
                            continue;
                        }
                        long switchTimeout = mTimeToSwitch - now;
                        if (curTimeout > switchTimeout) {
                            curTimeout = switchTimeout;
                        }
                    }
                    try {
                        if (DEBUG_INPUT) Slog.v(
                                TAG, "Waiting for key dispatch: " + curTimeout);
                        wait(curTimeout);
                        if (DEBUG_INPUT) Slog.v(TAG, "Finished waiting @"
                                + SystemClock.uptimeMillis() + " startTime="
                                + startTime + " switchTime=" + mTimeToSwitch
                                + " target=" + targetWin + " mLW=" + mLastWin
                                + " mLB=" + mLastBinder + " fin=" + mFinished
                                + " mCurrentFocus=" + mCurrentFocus);
                    } catch (InterruptedException e) {
                    }
                }
                if (mWasFrozen) {
                    waitedFor = 0;
                    mWasFrozen = false;
                } else {
                    waitedFor = SystemClock.uptimeMillis() - startTime;
                }
                if (waitedFor >= keyDispatchingTimeout && mTimeToSwitch == 0) {
                    IApplicationToken at = null;
                    synchronized (this) {
                        Slog.w(TAG, "Key dispatching timed out sending to " +
                              (targetWin != null ? targetWin.mAttrs.getTitle()
                              : "<null>: no window ready for key dispatch"));
                        Slog.w(TAG, "Previous dispatch state: " + mDispatchState);
                        Slog.w(TAG, "Current dispatch state: " +
                                new DispatchState(nextKey, targetWin));
                        if (targetWin != null) {
                            at = targetWin.getAppToken();
                        } else if (targetApp != null) {
                            at = targetApp.appToken;
                        }
                    }
                    boolean abort = true;
                    if (at != null) {
                        try {
                            long timeout = at.getKeyDispatchingTimeout();
                            if (timeout > waitedFor) {
                                keyDispatchingTimeout = timeout - waitedFor;
                                continue;
                            } else {
                                abort = at.keyDispatchingTimedOut();
                            }
                        } catch (RemoteException ex) {
                        }
                    }
                    synchronized (this) {
                        if (abort && (mLastWin == targetWin || targetWin == null)) {
                            mFinished = true;
                            if (mLastWin != null) {
                                if (DEBUG_INPUT) Slog.v(TAG,
                                        "Window " + mLastWin +
                                        " timed out on key input");
                                if (mLastWin.mToken.paused) {
                                    Slog.w(TAG, "Un-pausing dispatching to this window");
                                    mLastWin.mToken.paused = false;
                                }
                            }
                            if (mMotionTarget == targetWin) {
                                mMotionTarget = null;
                            }
                            mLastWin = null;
                            mLastBinder = null;
                            if (failIfTimeout || targetWin == null) {
                                return null;
                            }
                        } else {
                            Slog.w(TAG, "Continuing to wait for key to be dispatched");
                            startTime = SystemClock.uptimeMillis();
                        }
                    }
                }
            }
        }
        Object findTargetWindow(KeyEvent nextKey, QueuedEvent qev,
                MotionEvent nextMotion, boolean isPointerEvent,
                int callingPid, int callingUid) {
            mOutsideTouchTargets = null;
            if (nextKey != null) {
                final int keycode = nextKey.getKeyCode();
                final int repeatCount = nextKey.getRepeatCount();
                final boolean down = nextKey.getAction() != KeyEvent.ACTION_UP;
                boolean dispatch = mKeyWaiter.checkShouldDispatchKey(keycode);
                if (!dispatch) {
                    if (callingUid == 0 ||
                            mContext.checkPermission(
                                    android.Manifest.permission.INJECT_EVENTS,
                                    callingPid, callingUid)
                                    == PackageManager.PERMISSION_GRANTED) {
                        mPolicy.interceptKeyTi(null, keycode,
                                nextKey.getMetaState(), down, repeatCount,
                                nextKey.getFlags());
                    }
                    Slog.w(TAG, "Event timeout during app switch: dropping "
                            + nextKey);
                    return SKIP_TARGET_TOKEN;
                }
                WindowState focus = null;
                synchronized(mWindowMap) {
                    focus = getFocusedWindowLocked();
                }
                wakeupIfNeeded(focus, LocalPowerManager.BUTTON_EVENT);
                if (callingUid == 0 ||
                        (focus != null && callingUid == focus.mSession.mUid) ||
                        mContext.checkPermission(
                                android.Manifest.permission.INJECT_EVENTS,
                                callingPid, callingUid)
                                == PackageManager.PERMISSION_GRANTED) {
                    if (mPolicy.interceptKeyTi(focus,
                            keycode, nextKey.getMetaState(), down, repeatCount,
                            nextKey.getFlags())) {
                        return CONSUMED_EVENT_TOKEN;
                    }
                }
                return focus;
            } else if (!isPointerEvent) {
                boolean dispatch = mKeyWaiter.checkShouldDispatchKey(-1);
                if (!dispatch) {
                    Slog.w(TAG, "Event timeout during app switch: dropping trackball "
                            + nextMotion);
                    return SKIP_TARGET_TOKEN;
                }
                WindowState focus = null;
                synchronized(mWindowMap) {
                    focus = getFocusedWindowLocked();
                }
                wakeupIfNeeded(focus, LocalPowerManager.BUTTON_EVENT);
                return focus;
            }
            if (nextMotion == null) {
                return SKIP_TARGET_TOKEN;
            }
            boolean dispatch = mKeyWaiter.checkShouldDispatchKey(
                    KeyEvent.KEYCODE_UNKNOWN);
            if (!dispatch) {
                Slog.w(TAG, "Event timeout during app switch: dropping pointer "
                        + nextMotion);
                return SKIP_TARGET_TOKEN;
            }
            int action = nextMotion.getAction();
            final float xf = nextMotion.getX();
            final float yf = nextMotion.getY();
            final long eventTime = nextMotion.getEventTime();
            final boolean screenWasOff = qev != null
                    && (qev.flags&WindowManagerPolicy.FLAG_BRIGHT_HERE) != 0;
            WindowState target = null;
            synchronized(mWindowMap) {
                synchronized (this) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        if (mMotionTarget != null) {
                            Slog.w(TAG, "Pointer down received while already down in: "
                                    + mMotionTarget);
                            mMotionTarget = null;
                        }
                        final int x = (int)xf;
                        final int y = (int)yf;
                        final ArrayList windows = mWindows;
                        final int N = windows.size();
                        WindowState topErrWindow = null;
                        final Rect tmpRect = mTempRect;
                        for (int i=N-1; i>=0; i--) {
                            WindowState child = (WindowState)windows.get(i);
                            final int flags = child.mAttrs.flags;
                            if ((flags & WindowManager.LayoutParams.FLAG_SYSTEM_ERROR) != 0) {
                                if (topErrWindow == null) {
                                    topErrWindow = child;
                                }
                            }
                            if (!child.isVisibleLw()) {
                                continue;
                            }
                            if ((flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0) {
                                if ((flags & WindowManager.LayoutParams
                                        .FLAG_WATCH_OUTSIDE_TOUCH) != 0) {
                                    child.mNextOutsideTouch = mOutsideTouchTargets;
                                    mOutsideTouchTargets = child;
                                }
                                continue;
                            }
                            tmpRect.set(child.mFrame);
                            if (child.mTouchableInsets == ViewTreeObserver
                                        .InternalInsetsInfo.TOUCHABLE_INSETS_CONTENT) {
                                tmpRect.left += child.mGivenContentInsets.left;
                                tmpRect.top += child.mGivenContentInsets.top;
                                tmpRect.right -= child.mGivenContentInsets.right;
                                tmpRect.bottom -= child.mGivenContentInsets.bottom;
                            } else if (child.mTouchableInsets == ViewTreeObserver
                                        .InternalInsetsInfo.TOUCHABLE_INSETS_VISIBLE) {
                                tmpRect.left += child.mGivenVisibleInsets.left;
                                tmpRect.top += child.mGivenVisibleInsets.top;
                                tmpRect.right -= child.mGivenVisibleInsets.right;
                                tmpRect.bottom -= child.mGivenVisibleInsets.bottom;
                            }
                            final int touchFlags = flags &
                                (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                            if (tmpRect.contains(x, y) || touchFlags == 0) {
                                if (!screenWasOff || (flags &
                                        WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING) != 0) {
                                    mMotionTarget = child;
                                } else {
                                    mMotionTarget = null;
                                }
                                break;
                            }
                            if ((flags & WindowManager.LayoutParams
                                    .FLAG_WATCH_OUTSIDE_TOUCH) != 0) {
                                child.mNextOutsideTouch = mOutsideTouchTargets;
                                mOutsideTouchTargets = child;
                            }
                        }
                        if (topErrWindow != null && mMotionTarget != topErrWindow) {
                            mMotionTarget = null;
                        }
                    }
                    target = mMotionTarget;
                }
            }
            wakeupIfNeeded(target, eventType(nextMotion));
            return target != null ? target : SKIP_TARGET_TOKEN;
        }
        boolean checkShouldDispatchKey(int keycode) {
            synchronized (this) {
                if (mPolicy.isAppSwitchKeyTqTiLwLi(keycode)) {
                    mTimeToSwitch = 0;
                    return true;
                }
                if (mTimeToSwitch != 0
                        && mTimeToSwitch < SystemClock.uptimeMillis()) {
                    return false;
                }
                return true;
            }
        }
        void bindTargetWindowLocked(WindowState win,
                int pendingWhat, QueuedEvent pendingMotion) {
            synchronized (this) {
                bindTargetWindowLockedLocked(win, pendingWhat, pendingMotion);
            }
        }
        void bindTargetWindowLocked(WindowState win) {
            synchronized (this) {
                bindTargetWindowLockedLocked(win, RETURN_NOTHING, null);
            }
        }
        void bindTargetWindowLockedLocked(WindowState win,
                int pendingWhat, QueuedEvent pendingMotion) {
            mLastWin = win;
            mLastBinder = win.mClient.asBinder();
            mFinished = false;
            if (pendingMotion != null) {
                final Session s = win.mSession;
                if (pendingWhat == RETURN_PENDING_POINTER) {
                    releasePendingPointerLocked(s);
                    s.mPendingPointerMove = pendingMotion;
                    s.mPendingPointerWindow = win;
                    if (DEBUG_INPUT) Slog.v(TAG,
                            "bindTargetToWindow " + s.mPendingPointerMove);
                } else if (pendingWhat == RETURN_PENDING_TRACKBALL) {
                    releasePendingTrackballLocked(s);
                    s.mPendingTrackballMove = pendingMotion;
                    s.mPendingTrackballWindow = win;
                }
            }
        }
        void releasePendingPointerLocked(Session s) {
            if (DEBUG_INPUT) Slog.v(TAG,
                    "releasePendingPointer " + s.mPendingPointerMove);
            if (s.mPendingPointerMove != null) {
                mQueue.recycleEvent(s.mPendingPointerMove);
                s.mPendingPointerMove = null;
            }
        }
        void releasePendingTrackballLocked(Session s) {
            if (s.mPendingTrackballMove != null) {
                mQueue.recycleEvent(s.mPendingTrackballMove);
                s.mPendingTrackballMove = null;
            }
        }
        MotionEvent finishedKey(Session session, IWindow client, boolean force,
                int returnWhat) {
            if (DEBUG_INPUT) Slog.v(
                TAG, "finishedKey: client=" + client + ", force=" + force);
            if (client == null) {
                return null;
            }
            MotionEvent res = null;
            QueuedEvent qev = null;
            WindowState win = null;
            synchronized (this) {
                if (DEBUG_INPUT) Slog.v(
                    TAG, "finishedKey: client=" + client.asBinder()
                    + ", force=" + force + ", last=" + mLastBinder
                    + " (token=" + (mLastWin != null ? mLastWin.mToken : null) + ")");
                if (returnWhat == RETURN_PENDING_POINTER) {
                    qev = session.mPendingPointerMove;
                    win = session.mPendingPointerWindow;
                    session.mPendingPointerMove = null;
                    session.mPendingPointerWindow = null;
                } else if (returnWhat == RETURN_PENDING_TRACKBALL) {
                    qev = session.mPendingTrackballMove;
                    win = session.mPendingTrackballWindow;
                    session.mPendingTrackballMove = null;
                    session.mPendingTrackballWindow = null;
                }
                if (mLastBinder == client.asBinder()) {
                    if (DEBUG_INPUT) Slog.v(
                        TAG, "finishedKey: last paused="
                        + ((mLastWin != null) ? mLastWin.mToken.paused : "null"));
                    if (mLastWin != null && (!mLastWin.mToken.paused || force
                            || !mEventDispatching)) {
                        doFinishedKeyLocked(true);
                    } else {
                        mFinished = true;
                        notifyAll();
                    }
                }
                if (qev != null) {
                    res = (MotionEvent)qev.event;
                    if (DEBUG_INPUT) Slog.v(TAG,
                            "Returning pending motion: " + res);
                    mQueue.recycleEvent(qev);
                    if (win != null && returnWhat == RETURN_PENDING_POINTER) {
                        res.offsetLocation(-win.mFrame.left, -win.mFrame.top);
                    }
                }
            }
            if (res != null && returnWhat == RETURN_PENDING_POINTER) {
                synchronized (mWindowMap) {
                    dispatchPointerElsewhereLocked(win, win, res, res.getEventTime(), false);
                }
            }
            return res;
        }
        void tickle() {
            synchronized (this) {
                notifyAll();
            }
        }
        void handleNewWindowLocked(WindowState newWindow) {
            if (!newWindow.canReceiveKeys()) {
                return;
            }
            synchronized (this) {
                if (DEBUG_INPUT) Slog.v(
                    TAG, "New key dispatch window: win="
                    + newWindow.mClient.asBinder()
                    + ", last=" + mLastBinder
                    + " (token=" + (mLastWin != null ? mLastWin.mToken : null)
                    + "), finished=" + mFinished + ", paused="
                    + newWindow.mToken.paused);
                newWindow.mToken.paused = false;
                mGotFirstWindow = true;
                if ((newWindow.mAttrs.flags & FLAG_SYSTEM_ERROR) != 0) {
                    if (DEBUG_INPUT) Slog.v(TAG,
                            "New SYSTEM_ERROR window; resetting state");
                    mLastWin = null;
                    mLastBinder = null;
                    mMotionTarget = null;
                    mFinished = true;
                } else if (mLastWin != null) {
                    if (DEBUG_INPUT) Slog.v(
                        TAG, "Last win layer=" + mLastWin.mLayer
                        + ", new win layer=" + newWindow.mLayer);
                    if (newWindow.mLayer >= mLastWin.mLayer) {
                        mLastWin.mToken.paused = false;
                        doFinishedKeyLocked(false);  
                        return;
                    }
                }
                notifyAll();
            }
        }
        void pauseDispatchingLocked(WindowToken token) {
            synchronized (this)
            {
                if (DEBUG_INPUT) Slog.v(TAG, "Pausing WindowToken " + token);
                token.paused = true;
            }
        }
        void resumeDispatchingLocked(WindowToken token) {
            synchronized (this) {
                if (token.paused) {
                    if (DEBUG_INPUT) Slog.v(
                        TAG, "Resuming WindowToken " + token
                        + ", last=" + mLastBinder
                        + " (token=" + (mLastWin != null ? mLastWin.mToken : null)
                        + "), finished=" + mFinished + ", paused="
                        + token.paused);
                    token.paused = false;
                    if (mLastWin != null && mLastWin.mToken == token && mFinished) {
                        doFinishedKeyLocked(false);
                    } else {
                        notifyAll();
                    }
                }
            }
        }
        void setEventDispatchingLocked(boolean enabled) {
            synchronized (this) {
                mEventDispatching = enabled;
                notifyAll();
            }
        }
        void appSwitchComing() {
            synchronized (this) {
                long now = SystemClock.uptimeMillis() + 500;
                if (DEBUG_INPUT) Slog.v(TAG, "appSwitchComing: " + now);
                if (mTimeToSwitch == 0 || now < mTimeToSwitch) {
                    mTimeToSwitch = now;
                }
                notifyAll();
            }
        }
        private final void doFinishedKeyLocked(boolean force) {
            if (mLastWin != null) {
                releasePendingPointerLocked(mLastWin.mSession);
                releasePendingTrackballLocked(mLastWin.mSession);
            }
            if (force || mLastWin == null || !mLastWin.mToken.paused
                    || !mLastWin.isVisibleLw()) {
                mLastWin = null;
                mLastBinder = null;
            }
            mFinished = true;
            notifyAll();
        }
    }
    private class KeyQ extends KeyInputQueue
            implements KeyInputQueue.FilterCallback {
        PowerManager.WakeLock mHoldingScreen;
        KeyQ() {
            super(mContext, WindowManagerService.this);
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            mHoldingScreen = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                    "KEEP_SCREEN_ON_FLAG");
            mHoldingScreen.setReferenceCounted(false);
        }
        @Override
        boolean preprocessEvent(InputDevice device, RawInputEvent event) {
            if (mPolicy.preprocessInputEventTq(event)) {
                return true;
            }
            switch (event.type) {
                case RawInputEvent.EV_KEY: {
                    if (DEBUG) {
                        if (event.keycode == KeyEvent.KEYCODE_G) {
                            if (event.value != 0) {
                                mPolicy.screenTurnedOff(WindowManagerPolicy.OFF_BECAUSE_OF_USER);
                            }
                            return false;
                        }
                        if (event.keycode == KeyEvent.KEYCODE_D) {
                            if (event.value != 0) {
                            }
                            return false;
                        }
                    }
                    boolean screenIsOff = !mPowerManager.isScreenOn();
                    boolean screenIsDim = !mPowerManager.isScreenBright();
                    int actions = mPolicy.interceptKeyTq(event, !screenIsOff);
                    if ((actions & WindowManagerPolicy.ACTION_GO_TO_SLEEP) != 0) {
                        mPowerManager.goToSleep(event.when);
                    }
                    if (screenIsOff) {
                        event.flags |= WindowManagerPolicy.FLAG_WOKE_HERE;
                    }
                    if (screenIsDim) {
                        event.flags |= WindowManagerPolicy.FLAG_BRIGHT_HERE;
                    }
                    if ((actions & WindowManagerPolicy.ACTION_POKE_USER_ACTIVITY) != 0) {
                        mPowerManager.userActivity(event.when, false,
                                LocalPowerManager.BUTTON_EVENT, false);
                    }
                    if ((actions & WindowManagerPolicy.ACTION_PASS_TO_USER) != 0) {
                        if (event.value != 0 && mPolicy.isAppSwitchKeyTqTiLwLi(event.keycode)) {
                            filterQueue(this);
                            mKeyWaiter.appSwitchComing();
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                case RawInputEvent.EV_REL: {
                    boolean screenIsOff = !mPowerManager.isScreenOn();
                    boolean screenIsDim = !mPowerManager.isScreenBright();
                    if (screenIsOff) {
                        if (!mPolicy.isWakeRelMovementTq(event.deviceId,
                                device.classes, event)) {
                            return false;
                        }
                        event.flags |= WindowManagerPolicy.FLAG_WOKE_HERE;
                    }
                    if (screenIsDim) {
                        event.flags |= WindowManagerPolicy.FLAG_BRIGHT_HERE;
                    }
                    return true;
                }
                case RawInputEvent.EV_ABS: {
                    boolean screenIsOff = !mPowerManager.isScreenOn();
                    boolean screenIsDim = !mPowerManager.isScreenBright();
                    if (screenIsOff) {
                        if (!mPolicy.isWakeAbsMovementTq(event.deviceId,
                                device.classes, event)) {
                            return false;
                        }
                        event.flags |= WindowManagerPolicy.FLAG_WOKE_HERE;
                    }
                    if (screenIsDim) {
                        event.flags |= WindowManagerPolicy.FLAG_BRIGHT_HERE;
                    }
                    return true;
                }
                default:
                    return true;
            }
        }
        public int filterEvent(QueuedEvent ev) {
            switch (ev.classType) {
                case RawInputEvent.CLASS_KEYBOARD:
                    KeyEvent ke = (KeyEvent)ev.event;
                    if (mPolicy.isMovementKeyTi(ke.getKeyCode())) {
                        Slog.w(TAG, "Dropping movement key during app switch: "
                                + ke.getKeyCode() + ", action=" + ke.getAction());
                        return FILTER_REMOVE;
                    }
                    return FILTER_ABORT;
                default:
                    return FILTER_KEEP;
            }
        }
        void setHoldScreenLocked(boolean holding) {
            boolean state = mHoldingScreen.isHeld();
            if (holding != state) {
                if (holding) {
                    mHoldingScreen.acquire();
                } else {
                    mPolicy.screenOnStoppedLw();
                    mHoldingScreen.release();
                }
            }
        }
    }
    public boolean detectSafeMode() {
        mSafeMode = mPolicy.detectSafeMode();
        return mSafeMode;
    }
    public void systemReady() {
        mPolicy.systemReady();
    }
    private final class InputDispatcherThread extends Thread {
        static final int LONG_WAIT=9999*1000;
        public InputDispatcherThread() {
            super("InputDispatcher");
        }
        @Override
        public void run() {
            while (true) {
                try {
                    process();
                } catch (Exception e) {
                    Slog.e(TAG, "Exception in input dispatcher", e);
                }
            }
        }
        private void process() {
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
            KeyEvent lastKey = null;
            long lastKeyTime = SystemClock.uptimeMillis();
            long nextKeyTime = lastKeyTime+LONG_WAIT;
            long downTime = 0;
            int keyRepeatCount = 0;
            boolean configChanged = false;
            while (true) {
                long curTime = SystemClock.uptimeMillis();
                if (DEBUG_INPUT) Slog.v(
                    TAG, "Waiting for next key: now=" + curTime
                    + ", repeat @ " + nextKeyTime);
                QueuedEvent ev = mQueue.getEvent(
                    (int)((!configChanged && curTime < nextKeyTime)
                            ? (nextKeyTime-curTime) : 0));
                if (DEBUG_INPUT && ev != null) Slog.v(
                        TAG, "Event: type=" + ev.classType + " data=" + ev.event);
                if (MEASURE_LATENCY) {
                    lt.sample("2 got event              ", System.nanoTime() - ev.whenNano);
                }
                if (lastKey != null && !mPolicy.allowKeyRepeat()) {
                    lastKey = null;
                    downTime = 0;
                    lastKeyTime = curTime;
                    nextKeyTime = curTime + LONG_WAIT;
                }
                try {
                    if (ev != null) {
                        curTime = SystemClock.uptimeMillis();
                        int eventType;
                        if (ev.classType == RawInputEvent.CLASS_TOUCHSCREEN) {
                            eventType = eventType((MotionEvent)ev.event);
                        } else if (ev.classType == RawInputEvent.CLASS_KEYBOARD ||
                                    ev.classType == RawInputEvent.CLASS_TRACKBALL) {
                            eventType = LocalPowerManager.BUTTON_EVENT;
                        } else {
                            eventType = LocalPowerManager.OTHER_EVENT;
                        }
                        try {
                            if ((curTime - mLastBatteryStatsCallTime)
                                    >= MIN_TIME_BETWEEN_USERACTIVITIES) {
                                mLastBatteryStatsCallTime = curTime;
                                mBatteryStats.noteInputEvent();
                            }
                        } catch (RemoteException e) {
                        }
                        if (ev.classType == RawInputEvent.CLASS_CONFIGURATION_CHANGED) {
                        } else if (eventType != TOUCH_EVENT
                                && eventType != LONG_TOUCH_EVENT
                                && eventType != CHEEK_EVENT) {
                            mPowerManager.userActivity(curTime, false,
                                    eventType, false);
                        } else if (mLastTouchEventType != eventType
                                || (curTime - mLastUserActivityCallTime)
                                >= MIN_TIME_BETWEEN_USERACTIVITIES) {
                            mLastUserActivityCallTime = curTime;
                            mLastTouchEventType = eventType;
                            mPowerManager.userActivity(curTime, false,
                                    eventType, false);
                        }
                        switch (ev.classType) {
                            case RawInputEvent.CLASS_KEYBOARD:
                                KeyEvent ke = (KeyEvent)ev.event;
                                if (ke.isDown()) {
                                    lastKey = ke;
                                    downTime = curTime;
                                    keyRepeatCount = 0;
                                    lastKeyTime = curTime;
                                    nextKeyTime = lastKeyTime
                                            + ViewConfiguration.getLongPressTimeout();
                                    if (DEBUG_INPUT) Slog.v(
                                        TAG, "Received key down: first repeat @ "
                                        + nextKeyTime);
                                } else {
                                    lastKey = null;
                                    downTime = 0;
                                    lastKeyTime = curTime;
                                    nextKeyTime = curTime + LONG_WAIT;
                                    if (DEBUG_INPUT) Slog.v(
                                        TAG, "Received key up: ignore repeat @ "
                                        + nextKeyTime);
                                }
                                dispatchKey((KeyEvent)ev.event, 0, 0);
                                mQueue.recycleEvent(ev);
                                break;
                            case RawInputEvent.CLASS_TOUCHSCREEN:
                                dispatchPointer(ev, (MotionEvent)ev.event, 0, 0);
                                break;
                            case RawInputEvent.CLASS_TRACKBALL:
                                dispatchTrackball(ev, (MotionEvent)ev.event, 0, 0);
                                break;
                            case RawInputEvent.CLASS_CONFIGURATION_CHANGED:
                                configChanged = true;
                                break;
                            default:
                                mQueue.recycleEvent(ev);
                            break;
                        }
                    } else if (configChanged) {
                        configChanged = false;
                        sendNewConfiguration();
                    } else if (lastKey != null) {
                        curTime = SystemClock.uptimeMillis();
                        if (DEBUG_INPUT) Slog.v(
                            TAG, "Key timeout: repeat=" + nextKeyTime
                            + ", now=" + curTime);
                        if (curTime < nextKeyTime) {
                            continue;
                        }
                        lastKeyTime = nextKeyTime;
                        nextKeyTime = nextKeyTime + KEY_REPEAT_DELAY;
                        keyRepeatCount++;
                        if (DEBUG_INPUT) Slog.v(
                            TAG, "Key repeat: count=" + keyRepeatCount
                            + ", next @ " + nextKeyTime);
                        KeyEvent newEvent;
                        if (downTime != 0 && (downTime
                                + ViewConfiguration.getLongPressTimeout())
                                <= curTime) {
                            newEvent = KeyEvent.changeTimeRepeat(lastKey,
                                    curTime, keyRepeatCount,
                                    lastKey.getFlags() | KeyEvent.FLAG_LONG_PRESS);
                            downTime = 0;
                        } else {
                            newEvent = KeyEvent.changeTimeRepeat(lastKey,
                                    curTime, keyRepeatCount);
                        }
                        dispatchKey(newEvent, 0, 0);
                    } else {
                        curTime = SystemClock.uptimeMillis();
                        lastKeyTime = curTime;
                        nextKeyTime = curTime + LONG_WAIT;
                    }
                } catch (Exception e) {
                    Slog.e(TAG,
                        "Input thread received uncaught exception: " + e, e);
                }
            }
        }
    }
    private final class Session extends IWindowSession.Stub
            implements IBinder.DeathRecipient {
        final IInputMethodClient mClient;
        final IInputContext mInputContext;
        final int mUid;
        final int mPid;
        final String mStringName;
        SurfaceSession mSurfaceSession;
        int mNumWindow = 0;
        boolean mClientDead = false;
        QueuedEvent mPendingPointerMove;
        WindowState mPendingPointerWindow;
        QueuedEvent mPendingTrackballMove;
        WindowState mPendingTrackballWindow;
        public Session(IInputMethodClient client, IInputContext inputContext) {
            mClient = client;
            mInputContext = inputContext;
            mUid = Binder.getCallingUid();
            mPid = Binder.getCallingPid();
            StringBuilder sb = new StringBuilder();
            sb.append("Session{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" uid ");
            sb.append(mUid);
            sb.append("}");
            mStringName = sb.toString();
            synchronized (mWindowMap) {
                if (mInputMethodManager == null && mHaveInputMethods) {
                    IBinder b = ServiceManager.getService(
                            Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager = IInputMethodManager.Stub.asInterface(b);
                }
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (mInputMethodManager != null) {
                    mInputMethodManager.addClient(client, inputContext,
                            mUid, mPid);
                } else {
                    client.setUsingInputMethod(false);
                }
                client.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                try {
                    if (mInputMethodManager != null) {
                        mInputMethodManager.removeClient(client);
                    }
                } catch (RemoteException ee) {
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.e(TAG, "Window Session Crash", e);
                }
                throw e;
            }
        }
        public void binderDied() {
            try {
                if (mInputMethodManager != null) {
                    mInputMethodManager.removeClient(mClient);
                }
            } catch (RemoteException e) {
            }
            synchronized(mWindowMap) {
                mClient.asBinder().unlinkToDeath(this, 0);
                mClientDead = true;
                killSessionLocked();
            }
        }
        public int add(IWindow window, WindowManager.LayoutParams attrs,
                int viewVisibility, Rect outContentInsets) {
            return addWindow(this, window, attrs, viewVisibility, outContentInsets);
        }
        public void remove(IWindow window) {
            removeWindow(this, window);
        }
        public int relayout(IWindow window, WindowManager.LayoutParams attrs,
                int requestedWidth, int requestedHeight, int viewFlags,
                boolean insetsPending, Rect outFrame, Rect outContentInsets,
                Rect outVisibleInsets, Configuration outConfig, Surface outSurface) {
            return relayoutWindow(this, window, attrs,
                    requestedWidth, requestedHeight, viewFlags, insetsPending,
                    outFrame, outContentInsets, outVisibleInsets, outConfig, outSurface);
        }
        public void setTransparentRegion(IWindow window, Region region) {
            setTransparentRegionWindow(this, window, region);
        }
        public void setInsets(IWindow window, int touchableInsets,
                Rect contentInsets, Rect visibleInsets) {
            setInsetsWindow(this, window, touchableInsets, contentInsets,
                    visibleInsets);
        }
        public void getDisplayFrame(IWindow window, Rect outDisplayFrame) {
            getWindowDisplayFrame(this, window, outDisplayFrame);
        }
        public void finishDrawing(IWindow window) {
            if (localLOGV) Slog.v(
                TAG, "IWindow finishDrawing called for " + window);
            finishDrawingWindow(this, window);
        }
        public void finishKey(IWindow window) {
            if (localLOGV) Slog.v(
                TAG, "IWindow finishKey called for " + window);
            mKeyWaiter.finishedKey(this, window, false,
                    KeyWaiter.RETURN_NOTHING);
        }
        public MotionEvent getPendingPointerMove(IWindow window) {
            if (localLOGV) Slog.v(
                    TAG, "IWindow getPendingMotionEvent called for " + window);
            return mKeyWaiter.finishedKey(this, window, false,
                    KeyWaiter.RETURN_PENDING_POINTER);
        }
        public MotionEvent getPendingTrackballMove(IWindow window) {
            if (localLOGV) Slog.v(
                    TAG, "IWindow getPendingMotionEvent called for " + window);
            return mKeyWaiter.finishedKey(this, window, false,
                    KeyWaiter.RETURN_PENDING_TRACKBALL);
        }
        public void setInTouchMode(boolean mode) {
            synchronized(mWindowMap) {
                mInTouchMode = mode;
            }
        }
        public boolean getInTouchMode() {
            synchronized(mWindowMap) {
                return mInTouchMode;
            }
        }
        public boolean performHapticFeedback(IWindow window, int effectId,
                boolean always) {
            synchronized(mWindowMap) {
                long ident = Binder.clearCallingIdentity();
                try {
                    return mPolicy.performHapticFeedbackLw(
                            windowForClientLocked(this, window, true),
                            effectId, always);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        public void setWallpaperPosition(IBinder window, float x, float y, float xStep, float yStep) {
            synchronized(mWindowMap) {
                long ident = Binder.clearCallingIdentity();
                try {
                    setWindowWallpaperPositionLocked(
                            windowForClientLocked(this, window, true),
                            x, y, xStep, yStep);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        public void wallpaperOffsetsComplete(IBinder window) {
            WindowManagerService.this.wallpaperOffsetsComplete(window);
        }
        public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y,
                int z, Bundle extras, boolean sync) {
            synchronized(mWindowMap) {
                long ident = Binder.clearCallingIdentity();
                try {
                    return sendWindowWallpaperCommandLocked(
                            windowForClientLocked(this, window, true),
                            action, x, y, z, extras, sync);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        public void wallpaperCommandComplete(IBinder window, Bundle result) {
            WindowManagerService.this.wallpaperCommandComplete(window, result);
        }
        void windowAddedLocked() {
            if (mSurfaceSession == null) {
                if (localLOGV) Slog.v(
                    TAG, "First window added to " + this + ", creating SurfaceSession");
                mSurfaceSession = new SurfaceSession();
                if (SHOW_TRANSACTIONS) Slog.i(
                        TAG, "  NEW SURFACE SESSION " + mSurfaceSession);
                mSessions.add(this);
            }
            mNumWindow++;
        }
        void windowRemovedLocked() {
            mNumWindow--;
            killSessionLocked();
        }
        void killSessionLocked() {
            if (mNumWindow <= 0 && mClientDead) {
                mSessions.remove(this);
                if (mSurfaceSession != null) {
                    if (localLOGV) Slog.v(
                        TAG, "Last window removed from " + this
                        + ", destroying " + mSurfaceSession);
                    if (SHOW_TRANSACTIONS) Slog.i(
                            TAG, "  KILL SURFACE SESSION " + mSurfaceSession);
                    try {
                        mSurfaceSession.kill();
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception thrown when killing surface session "
                            + mSurfaceSession + " in session " + this
                            + ": " + e.toString());
                    }
                    mSurfaceSession = null;
                }
            }
        }
        void dump(PrintWriter pw, String prefix) {
            pw.print(prefix); pw.print("mNumWindow="); pw.print(mNumWindow);
                    pw.print(" mClientDead="); pw.print(mClientDead);
                    pw.print(" mSurfaceSession="); pw.println(mSurfaceSession);
            if (mPendingPointerWindow != null || mPendingPointerMove != null) {
                pw.print(prefix);
                        pw.print("mPendingPointerWindow="); pw.print(mPendingPointerWindow);
                        pw.print(" mPendingPointerMove="); pw.println(mPendingPointerMove);
            }
            if (mPendingTrackballWindow != null || mPendingTrackballMove != null) {
                pw.print(prefix);
                        pw.print("mPendingTrackballWindow="); pw.print(mPendingTrackballWindow);
                        pw.print(" mPendingTrackballMove="); pw.println(mPendingTrackballMove);
            }
        }
        @Override
        public String toString() {
            return mStringName;
        }
    }
    private final class WindowState implements WindowManagerPolicy.WindowState {
        final Session mSession;
        final IWindow mClient;
        WindowToken mToken;
        WindowToken mRootToken;
        AppWindowToken mAppToken;
        AppWindowToken mTargetAppToken;
        final WindowManager.LayoutParams mAttrs = new WindowManager.LayoutParams();
        final DeathRecipient mDeathRecipient;
        final WindowState mAttachedWindow;
        final ArrayList mChildWindows = new ArrayList();
        final int mBaseLayer;
        final int mSubLayer;
        final boolean mLayoutAttached;
        final boolean mIsImWindow;
        final boolean mIsWallpaper;
        final boolean mIsFloatingLayer;
        int mViewVisibility;
        boolean mPolicyVisibility = true;
        boolean mPolicyVisibilityAfterAnim = true;
        boolean mAppFreezing;
        Surface mSurface;
        boolean mReportDestroySurface;
        boolean mSurfacePendingDestroy;
        boolean mAttachedHidden;    
        boolean mLastHidden;        
        boolean mWallpaperVisible;  
        int mRequestedWidth;
        int mRequestedHeight;
        int mLastRequestedWidth;
        int mLastRequestedHeight;
        int mLayer;
        int mAnimLayer;
        int mLastLayer;
        boolean mHaveFrame;
        boolean mObscured;
        boolean mTurnOnScreen;
        WindowState mNextOutsideTouch;
        int mLayoutSeq = -1;
        Configuration mConfiguration = null;
        final Rect mShownFrame = new Rect();
        final Rect mLastShownFrame = new Rect();
        boolean mSurfaceResized;
        final Rect mVisibleInsets = new Rect();
        final Rect mLastVisibleInsets = new Rect();
        boolean mVisibleInsetsChanged;
        final Rect mContentInsets = new Rect();
        final Rect mLastContentInsets = new Rect();
        boolean mContentInsetsChanged;
        boolean mGivenInsetsPending;
        final Rect mGivenContentInsets = new Rect();
        final Rect mGivenVisibleInsets = new Rect();
        int mTouchableInsets = ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_FRAME;
        float mDsDx=1, mDtDx=0, mDsDy=0, mDtDy=1;
        float mLastDsDx=1, mLastDtDx=0, mLastDsDy=0, mLastDtDy=1;
        float mHScale=1, mVScale=1;
        float mLastHScale=1, mLastVScale=1;
        final Matrix mTmpMatrix = new Matrix();
        final Rect mFrame = new Rect();
        final Rect mLastFrame = new Rect();
        final Rect mContainingFrame = new Rect();
        final Rect mDisplayFrame = new Rect();
        final Rect mContentFrame = new Rect();
        final Rect mVisibleFrame = new Rect();
        float mShownAlpha = 1;
        float mAlpha = 1;
        float mLastAlpha = 1;
        boolean mEnterAnimationPending;
        boolean mAnimating;
        boolean mLocalAnimating;
        Animation mAnimation;
        boolean mAnimationIsEntrance;
        boolean mHasTransformation;
        boolean mHasLocalTransformation;
        final Transformation mTransformation = new Transformation();
        float mWallpaperX = -1;
        float mWallpaperY = -1;
        float mWallpaperXStep = -1;
        float mWallpaperYStep = -1;
        int mXOffset;
        int mYOffset;
        boolean mRelayoutCalled;
        boolean mDrawPending;
        boolean mCommitDrawPending;
        boolean mReadyToShow;
        boolean mHasDrawn;
        boolean mExiting;
        boolean mDestroying;
        boolean mRemoveOnExit;
        boolean mOrientationChanging;
        boolean mRemoved;
        boolean mSurfaceShown;
        int mSurfaceX, mSurfaceY, mSurfaceW, mSurfaceH;
        int mSurfaceLayer;
        float mSurfaceAlpha;
        WindowState(Session s, IWindow c, WindowToken token,
               WindowState attachedWindow, WindowManager.LayoutParams a,
               int viewVisibility) {
            mSession = s;
            mClient = c;
            mToken = token;
            mAttrs.copyFrom(a);
            mViewVisibility = viewVisibility;
            DeathRecipient deathRecipient = new DeathRecipient();
            mAlpha = a.alpha;
            if (localLOGV) Slog.v(
                TAG, "Window " + this + " client=" + c.asBinder()
                + " token=" + token + " (" + mAttrs.token + ")");
            try {
                c.asBinder().linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                mDeathRecipient = null;
                mAttachedWindow = null;
                mLayoutAttached = false;
                mIsImWindow = false;
                mIsWallpaper = false;
                mIsFloatingLayer = false;
                mBaseLayer = 0;
                mSubLayer = 0;
                return;
            }
            mDeathRecipient = deathRecipient;
            if ((mAttrs.type >= FIRST_SUB_WINDOW &&
                    mAttrs.type <= LAST_SUB_WINDOW)) {
                mBaseLayer = mPolicy.windowTypeToLayerLw(
                        attachedWindow.mAttrs.type) * TYPE_LAYER_MULTIPLIER
                        + TYPE_LAYER_OFFSET;
                mSubLayer = mPolicy.subWindowTypeToLayerLw(a.type);
                mAttachedWindow = attachedWindow;
                mAttachedWindow.mChildWindows.add(this);
                mLayoutAttached = mAttrs.type !=
                        WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
                mIsImWindow = attachedWindow.mAttrs.type == TYPE_INPUT_METHOD
                        || attachedWindow.mAttrs.type == TYPE_INPUT_METHOD_DIALOG;
                mIsWallpaper = attachedWindow.mAttrs.type == TYPE_WALLPAPER;
                mIsFloatingLayer = mIsImWindow || mIsWallpaper;
            } else {
                mBaseLayer = mPolicy.windowTypeToLayerLw(a.type)
                        * TYPE_LAYER_MULTIPLIER
                        + TYPE_LAYER_OFFSET;
                mSubLayer = 0;
                mAttachedWindow = null;
                mLayoutAttached = false;
                mIsImWindow = mAttrs.type == TYPE_INPUT_METHOD
                        || mAttrs.type == TYPE_INPUT_METHOD_DIALOG;
                mIsWallpaper = mAttrs.type == TYPE_WALLPAPER;
                mIsFloatingLayer = mIsImWindow || mIsWallpaper;
            }
            WindowState appWin = this;
            while (appWin.mAttachedWindow != null) {
                appWin = mAttachedWindow;
            }
            WindowToken appToken = appWin.mToken;
            while (appToken.appWindowToken == null) {
                WindowToken parent = mTokenMap.get(appToken.token);
                if (parent == null || appToken == parent) {
                    break;
                }
                appToken = parent;
            }
            mRootToken = appToken;
            mAppToken = appToken.appWindowToken;
            mSurface = null;
            mRequestedWidth = 0;
            mRequestedHeight = 0;
            mLastRequestedWidth = 0;
            mLastRequestedHeight = 0;
            mXOffset = 0;
            mYOffset = 0;
            mLayer = 0;
            mAnimLayer = 0;
            mLastLayer = 0;
        }
        void attach() {
            if (localLOGV) Slog.v(
                TAG, "Attaching " + this + " token=" + mToken
                + ", list=" + mToken.windows);
            mSession.windowAddedLocked();
        }
        public void computeFrameLw(Rect pf, Rect df, Rect cf, Rect vf) {
            mHaveFrame = true;
            final Rect container = mContainingFrame;
            container.set(pf);
            final Rect display = mDisplayFrame;
            display.set(df);
            if ((mAttrs.flags & FLAG_COMPATIBLE_WINDOW) != 0) {
                container.intersect(mCompatibleScreenFrame);
                if ((mAttrs.flags & FLAG_LAYOUT_NO_LIMITS) == 0) {
                    display.intersect(mCompatibleScreenFrame);
                }
            }
            final int pw = container.right - container.left;
            final int ph = container.bottom - container.top;
            int w,h;
            if ((mAttrs.flags & mAttrs.FLAG_SCALED) != 0) {
                w = mAttrs.width < 0 ? pw : mAttrs.width;
                h = mAttrs.height< 0 ? ph : mAttrs.height;
            } else {
                w = mAttrs.width == mAttrs.MATCH_PARENT ? pw : mRequestedWidth;
                h = mAttrs.height== mAttrs.MATCH_PARENT ? ph : mRequestedHeight;
            }
            final Rect content = mContentFrame;
            content.set(cf);
            final Rect visible = mVisibleFrame;
            visible.set(vf);
            final Rect frame = mFrame;
            final int fw = frame.width();
            final int fh = frame.height();
            Gravity.apply(mAttrs.gravity, w, h, container,
                    (int) (mAttrs.x + mAttrs.horizontalMargin * pw),
                    (int) (mAttrs.y + mAttrs.verticalMargin * ph), frame);
            Gravity.applyDisplay(mAttrs.gravity, df, frame);
            if (content.left < frame.left) content.left = frame.left;
            if (content.top < frame.top) content.top = frame.top;
            if (content.right > frame.right) content.right = frame.right;
            if (content.bottom > frame.bottom) content.bottom = frame.bottom;
            if (visible.left < frame.left) visible.left = frame.left;
            if (visible.top < frame.top) visible.top = frame.top;
            if (visible.right > frame.right) visible.right = frame.right;
            if (visible.bottom > frame.bottom) visible.bottom = frame.bottom;
            final Rect contentInsets = mContentInsets;
            contentInsets.left = content.left-frame.left;
            contentInsets.top = content.top-frame.top;
            contentInsets.right = frame.right-content.right;
            contentInsets.bottom = frame.bottom-content.bottom;
            final Rect visibleInsets = mVisibleInsets;
            visibleInsets.left = visible.left-frame.left;
            visibleInsets.top = visible.top-frame.top;
            visibleInsets.right = frame.right-visible.right;
            visibleInsets.bottom = frame.bottom-visible.bottom;
            if (mIsWallpaper && (fw != frame.width() || fh != frame.height())) {
                updateWallpaperOffsetLocked(this, mDisplay.getWidth(),
                        mDisplay.getHeight(), false);
            }
            if (localLOGV) {
                    Slog.v(TAG, "Resolving (mRequestedWidth="
                            + mRequestedWidth + ", mRequestedheight="
                            + mRequestedHeight + ") to" + " (pw=" + pw + ", ph=" + ph
                            + "): frame=" + mFrame.toShortString()
                            + " ci=" + contentInsets.toShortString()
                            + " vi=" + visibleInsets.toShortString());
            }
        }
        public Rect getFrameLw() {
            return mFrame;
        }
        public Rect getShownFrameLw() {
            return mShownFrame;
        }
        public Rect getDisplayFrameLw() {
            return mDisplayFrame;
        }
        public Rect getContentFrameLw() {
            return mContentFrame;
        }
        public Rect getVisibleFrameLw() {
            return mVisibleFrame;
        }
        public boolean getGivenInsetsPendingLw() {
            return mGivenInsetsPending;
        }
        public Rect getGivenContentInsetsLw() {
            return mGivenContentInsets;
        }
        public Rect getGivenVisibleInsetsLw() {
            return mGivenVisibleInsets;
        }
        public WindowManager.LayoutParams getAttrs() {
            return mAttrs;
        }
        public int getSurfaceLayer() {
            return mLayer;
        }
        public IApplicationToken getAppToken() {
            return mAppToken != null ? mAppToken.appToken : null;
        }
        public boolean hasAppShownWindows() {
            return mAppToken != null ? mAppToken.firstWindowDrawn : false;
        }
        public void setAnimation(Animation anim) {
            if (localLOGV) Slog.v(
                TAG, "Setting animation in " + this + ": " + anim);
            mAnimating = false;
            mLocalAnimating = false;
            mAnimation = anim;
            mAnimation.restrictDuration(MAX_ANIMATION_DURATION);
            mAnimation.scaleCurrentDuration(mWindowAnimationScale);
        }
        public void clearAnimation() {
            if (mAnimation != null) {
                mAnimating = true;
                mLocalAnimating = false;
                mAnimation = null;
            }
        }
        Surface createSurfaceLocked() {
            if (mSurface == null) {
                mReportDestroySurface = false;
                mSurfacePendingDestroy = false;
                mDrawPending = true;
                mCommitDrawPending = false;
                mReadyToShow = false;
                if (mAppToken != null) {
                    mAppToken.allDrawn = false;
                }
                int flags = 0;
                if (mAttrs.memoryType == MEMORY_TYPE_PUSH_BUFFERS) {
                    flags |= Surface.PUSH_BUFFERS;
                }
                if ((mAttrs.flags&WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                    flags |= Surface.SECURE;
                }
                if (DEBUG_VISIBILITY) Slog.v(
                    TAG, "Creating surface in session "
                    + mSession.mSurfaceSession + " window " + this
                    + " w=" + mFrame.width()
                    + " h=" + mFrame.height() + " format="
                    + mAttrs.format + " flags=" + flags);
                int w = mFrame.width();
                int h = mFrame.height();
                if ((mAttrs.flags & LayoutParams.FLAG_SCALED) != 0) {
                    w = mRequestedWidth;
                    h = mRequestedHeight;
                }
                if (w <= 0) w = 1;
                if (h <= 0) h = 1;
                mSurfaceShown = false;
                mSurfaceLayer = 0;
                mSurfaceAlpha = 1;
                mSurfaceX = 0;
                mSurfaceY = 0;
                mSurfaceW = w;
                mSurfaceH = h;
                try {
                    mSurface = new Surface(
                            mSession.mSurfaceSession, mSession.mPid,
                            mAttrs.getTitle().toString(),
                            0, w, h, mAttrs.format, flags);
                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "  CREATE SURFACE "
                            + mSurface + " IN SESSION "
                            + mSession.mSurfaceSession
                            + ": pid=" + mSession.mPid + " format="
                            + mAttrs.format + " flags=0x"
                            + Integer.toHexString(flags)
                            + " / " + this);
                } catch (Surface.OutOfResourcesException e) {
                    Slog.w(TAG, "OutOfResourcesException creating surface");
                    reclaimSomeSurfaceMemoryLocked(this, "create");
                    return null;
                } catch (Exception e) {
                    Slog.e(TAG, "Exception creating surface", e);
                    return null;
                }
                if (localLOGV) Slog.v(
                    TAG, "Got surface: " + mSurface
                    + ", set left=" + mFrame.left + " top=" + mFrame.top
                    + ", animLayer=" + mAnimLayer);
                if (SHOW_TRANSACTIONS) {
                    Slog.i(TAG, ">>> OPEN TRANSACTION");
                    if (SHOW_TRANSACTIONS) logSurface(this,
                            "CREATE pos=(" + mFrame.left + "," + mFrame.top + ") (" +
                            mFrame.width() + "x" + mFrame.height() + "), layer=" +
                            mAnimLayer + " HIDE", null);
                }
                Surface.openTransaction();
                try {
                    try {
                        mSurfaceX = mFrame.left + mXOffset;
                        mSurfaceY = mFrame.top + mYOffset;
                        mSurface.setPosition(mSurfaceX, mSurfaceY);
                        mSurfaceLayer = mAnimLayer;
                        mSurface.setLayer(mAnimLayer);
                        mSurfaceShown = false;
                        mSurface.hide();
                        if ((mAttrs.flags&WindowManager.LayoutParams.FLAG_DITHER) != 0) {
                            if (SHOW_TRANSACTIONS) logSurface(this, "DITHER", null);
                            mSurface.setFlags(Surface.SURFACE_DITHER,
                                    Surface.SURFACE_DITHER);
                        }
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Error creating surface in " + w, e);
                        reclaimSomeSurfaceMemoryLocked(this, "create-init");
                    }
                    mLastHidden = true;
                } finally {
                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION");
                    Surface.closeTransaction();
                }
                if (localLOGV) Slog.v(
                        TAG, "Created surface " + this);
            }
            return mSurface;
        }
        void destroySurfaceLocked() {
            mKeyWaiter.finishedKey(mSession, mClient, true,
                    KeyWaiter.RETURN_NOTHING);
            mKeyWaiter.releasePendingPointerLocked(mSession);
            mKeyWaiter.releasePendingTrackballLocked(mSession);
            if (mAppToken != null && this == mAppToken.startingWindow) {
                mAppToken.startingDisplayed = false;
            }
            if (mSurface != null) {
                mDrawPending = false;
                mCommitDrawPending = false;
                mReadyToShow = false;
                int i = mChildWindows.size();
                while (i > 0) {
                    i--;
                    WindowState c = (WindowState)mChildWindows.get(i);
                    c.mAttachedHidden = true;
                }
                if (mReportDestroySurface) {
                    mReportDestroySurface = false;
                    mSurfacePendingDestroy = true;
                    try {
                        mClient.dispatchGetNewSurface();
                        return;
                    } catch (RemoteException e) {
                    }
                }
                try {
                    if (DEBUG_VISIBILITY) {
                        RuntimeException e = null;
                        if (!HIDE_STACK_CRAWLS) {
                            e = new RuntimeException();
                            e.fillInStackTrace();
                        }
                        Slog.w(TAG, "Window " + this + " destroying surface "
                                + mSurface + ", session " + mSession, e);
                    }
                    if (SHOW_TRANSACTIONS) {
                        RuntimeException e = null;
                        if (!HIDE_STACK_CRAWLS) {
                            e = new RuntimeException();
                            e.fillInStackTrace();
                        }
                        if (SHOW_TRANSACTIONS) logSurface(this, "DESTROY", e);
                    }
                    mSurface.destroy();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Exception thrown when destroying Window " + this
                        + " surface " + mSurface + " session " + mSession
                        + ": " + e.toString());
                }
                mSurfaceShown = false;
                mSurface = null;
            }
        }
        boolean finishDrawingLocked() {
            if (mDrawPending) {
                if (SHOW_TRANSACTIONS || DEBUG_ORIENTATION) Slog.v(
                    TAG, "finishDrawingLocked: " + mSurface);
                mCommitDrawPending = true;
                mDrawPending = false;
                return true;
            }
            return false;
        }
        boolean commitFinishDrawingLocked(long currentTime) {
            if (!mCommitDrawPending) {
                return false;
            }
            mCommitDrawPending = false;
            mReadyToShow = true;
            final boolean starting = mAttrs.type == TYPE_APPLICATION_STARTING;
            final AppWindowToken atoken = mAppToken;
            if (atoken == null || atoken.allDrawn || starting) {
                performShowLocked();
            }
            return true;
        }
        boolean performShowLocked() {
            if (DEBUG_VISIBILITY) {
                RuntimeException e = null;
                if (!HIDE_STACK_CRAWLS) {
                    e = new RuntimeException();
                    e.fillInStackTrace();
                }
                Slog.v(TAG, "performShow on " + this
                        + ": readyToShow=" + mReadyToShow + " readyForDisplay=" + isReadyForDisplay()
                        + " starting=" + (mAttrs.type == TYPE_APPLICATION_STARTING), e);
            }
            if (mReadyToShow && isReadyForDisplay()) {
                if (SHOW_TRANSACTIONS || DEBUG_ORIENTATION) logSurface(this,
                        "SHOW (performShowLocked)", null);
                if (DEBUG_VISIBILITY) Slog.v(TAG, "Showing " + this
                        + " during animation: policyVis=" + mPolicyVisibility
                        + " attHidden=" + mAttachedHidden
                        + " tok.hiddenRequested="
                        + (mAppToken != null ? mAppToken.hiddenRequested : false)
                        + " tok.hidden="
                        + (mAppToken != null ? mAppToken.hidden : false)
                        + " animating=" + mAnimating
                        + " tok animating="
                        + (mAppToken != null ? mAppToken.animating : false));
                if (!showSurfaceRobustlyLocked(this)) {
                    return false;
                }
                mLastAlpha = -1;
                mHasDrawn = true;
                mLastHidden = false;
                mReadyToShow = false;
                enableScreenIfNeededLocked();
                applyEnterAnimationLocked(this);
                int i = mChildWindows.size();
                while (i > 0) {
                    i--;
                    WindowState c = (WindowState)mChildWindows.get(i);
                    if (c.mAttachedHidden) {
                        c.mAttachedHidden = false;
                        if (c.mSurface != null) {
                            c.performShowLocked();
                            mLayoutNeeded = true;
                        }
                    }
                }
                if (mAttrs.type != TYPE_APPLICATION_STARTING
                        && mAppToken != null) {
                    mAppToken.firstWindowDrawn = true;
                    if (mAppToken.startingData != null) {
                        if (DEBUG_STARTING_WINDOW || DEBUG_ANIM) Slog.v(TAG,
                                "Finish starting " + mToken
                                + ": first real window is shown, no animation");
                        if (mAnimation != null) {
                            mAnimation = null;
                            mAnimating = true;
                        }
                        mFinishedStarting.add(mAppToken);
                        mH.sendEmptyMessage(H.FINISHED_STARTING);
                    }
                    mAppToken.updateReportedVisibilityLocked();
                }
            }
            return true;
        }
        boolean stepAnimationLocked(long currentTime, int dw, int dh) {
            if (!mDisplayFrozen && mPolicy.isScreenOn()) {
                if (!mDrawPending && !mCommitDrawPending && mAnimation != null) {
                    mHasTransformation = true;
                    mHasLocalTransformation = true;
                    if (!mLocalAnimating) {
                        if (DEBUG_ANIM) Slog.v(
                            TAG, "Starting animation in " + this +
                            " @ " + currentTime + ": ww=" + mFrame.width() + " wh=" + mFrame.height() +
                            " dw=" + dw + " dh=" + dh + " scale=" + mWindowAnimationScale);
                        mAnimation.initialize(mFrame.width(), mFrame.height(), dw, dh);
                        mAnimation.setStartTime(currentTime);
                        mLocalAnimating = true;
                        mAnimating = true;
                    }
                    mTransformation.clear();
                    final boolean more = mAnimation.getTransformation(
                        currentTime, mTransformation);
                    if (DEBUG_ANIM) Slog.v(
                        TAG, "Stepped animation in " + this +
                        ": more=" + more + ", xform=" + mTransformation);
                    if (more) {
                        return true;
                    }
                    if (DEBUG_ANIM) Slog.v(
                        TAG, "Finished animation in " + this +
                        " @ " + currentTime);
                    mAnimation = null;
                }
                mHasLocalTransformation = false;
                if ((!mLocalAnimating || mAnimationIsEntrance) && mAppToken != null
                        && mAppToken.animation != null) {
                    mAnimating = true;
                    mHasTransformation = true;
                    mTransformation.clear();
                    return false;
                } else if (mHasTransformation) {
                    mAnimating = true;
                } else if (isAnimating()) {
                    mAnimating = true;
                }
            } else if (mAnimation != null) {
                mAnimating = true;
                mLocalAnimating = true;
                mAnimation = null;
            }
            if (!mAnimating && !mLocalAnimating) {
                return false;
            }
            if (DEBUG_ANIM) Slog.v(
                TAG, "Animation done in " + this + ": exiting=" + mExiting
                + ", reportedVisible="
                + (mAppToken != null ? mAppToken.reportedVisible : false));
            mAnimating = false;
            mLocalAnimating = false;
            mAnimation = null;
            mAnimLayer = mLayer;
            if (mIsImWindow) {
                mAnimLayer += mInputMethodAnimLayerAdjustment;
            } else if (mIsWallpaper) {
                mAnimLayer += mWallpaperAnimLayerAdjustment;
            }
            if (DEBUG_LAYERS) Slog.v(TAG, "Stepping win " + this
                    + " anim layer: " + mAnimLayer);
            mHasTransformation = false;
            mHasLocalTransformation = false;
            if (mPolicyVisibility != mPolicyVisibilityAfterAnim) {
                if (DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Policy visibility changing after anim in " + this + ": "
                            + mPolicyVisibilityAfterAnim);
                }
                mPolicyVisibility = mPolicyVisibilityAfterAnim;
                if (!mPolicyVisibility) {
                    if (mCurrentFocus == this) {
                        mFocusMayChange = true;
                    }
                    enableScreenIfNeededLocked();
                }
            }
            mTransformation.clear();
            if (mHasDrawn
                    && mAttrs.type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING
                    && mAppToken != null
                    && mAppToken.firstWindowDrawn
                    && mAppToken.startingData != null) {
                if (DEBUG_STARTING_WINDOW) Slog.v(TAG, "Finish starting "
                        + mToken + ": first real window done animating");
                mFinishedStarting.add(mAppToken);
                mH.sendEmptyMessage(H.FINISHED_STARTING);
            }
            finishExit();
            if (mAppToken != null) {
                mAppToken.updateReportedVisibilityLocked();
            }
            return false;
        }
        void finishExit() {
            if (DEBUG_ANIM) Slog.v(
                    TAG, "finishExit in " + this
                    + ": exiting=" + mExiting
                    + " remove=" + mRemoveOnExit
                    + " windowAnimating=" + isWindowAnimating());
            final int N = mChildWindows.size();
            for (int i=0; i<N; i++) {
                ((WindowState)mChildWindows.get(i)).finishExit();
            }
            if (!mExiting) {
                return;
            }
            if (isWindowAnimating()) {
                return;
            }
            if (localLOGV) Slog.v(
                    TAG, "Exit animation finished in " + this
                    + ": remove=" + mRemoveOnExit);
            if (mSurface != null) {
                mDestroySurface.add(this);
                mDestroying = true;
                if (SHOW_TRANSACTIONS) logSurface(this, "HIDE (finishExit)", null);
                mSurfaceShown = false;
                try {
                    mSurface.hide();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Error hiding surface in " + this, e);
                }
                mLastHidden = true;
                mKeyWaiter.releasePendingPointerLocked(mSession);
            }
            mExiting = false;
            if (mRemoveOnExit) {
                mPendingRemove.add(this);
                mRemoveOnExit = false;
            }
        }
        boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
            if (dsdx < .99999f || dsdx > 1.00001f) return false;
            if (dtdy < .99999f || dtdy > 1.00001f) return false;
            if (dtdx < -.000001f || dtdx > .000001f) return false;
            if (dsdy < -.000001f || dsdy > .000001f) return false;
            return true;
        }
        void computeShownFrameLocked() {
            final boolean selfTransformation = mHasLocalTransformation;
            Transformation attachedTransformation =
                    (mAttachedWindow != null && mAttachedWindow.mHasLocalTransformation)
                    ? mAttachedWindow.mTransformation : null;
            Transformation appTransformation =
                    (mAppToken != null && mAppToken.hasTransformation)
                    ? mAppToken.transformation : null;
            if (mAttrs.type == TYPE_WALLPAPER && mLowerWallpaperTarget == null
                    && mWallpaperTarget != null) {
                if (mWallpaperTarget.mHasLocalTransformation &&
                        mWallpaperTarget.mAnimation != null &&
                        !mWallpaperTarget.mAnimation.getDetachWallpaper()) {
                    attachedTransformation = mWallpaperTarget.mTransformation;
                    if (DEBUG_WALLPAPER && attachedTransformation != null) {
                        Slog.v(TAG, "WP target attached xform: " + attachedTransformation);
                    }
                }
                if (mWallpaperTarget.mAppToken != null &&
                        mWallpaperTarget.mAppToken.hasTransformation &&
                        mWallpaperTarget.mAppToken.animation != null &&
                        !mWallpaperTarget.mAppToken.animation.getDetachWallpaper()) {
                    appTransformation = mWallpaperTarget.mAppToken.transformation;
                    if (DEBUG_WALLPAPER && appTransformation != null) {
                        Slog.v(TAG, "WP target app xform: " + appTransformation);
                    }
                }
            }
            if (selfTransformation || attachedTransformation != null
                    || appTransformation != null) {
                final Rect frame = mFrame;
                final float tmpFloats[] = mTmpFloats;
                final Matrix tmpMatrix = mTmpMatrix;
                tmpMatrix.setTranslate(0, 0);
                if (selfTransformation) {
                    tmpMatrix.postConcat(mTransformation.getMatrix());
                }
                tmpMatrix.postTranslate(frame.left, frame.top);
                if (attachedTransformation != null) {
                    tmpMatrix.postConcat(attachedTransformation.getMatrix());
                }
                if (appTransformation != null) {
                    tmpMatrix.postConcat(appTransformation.getMatrix());
                }
                tmpMatrix.getValues(tmpFloats);
                mDsDx = tmpFloats[Matrix.MSCALE_X];
                mDtDx = tmpFloats[Matrix.MSKEW_X];
                mDsDy = tmpFloats[Matrix.MSKEW_Y];
                mDtDy = tmpFloats[Matrix.MSCALE_Y];
                int x = (int)tmpFloats[Matrix.MTRANS_X] + mXOffset;
                int y = (int)tmpFloats[Matrix.MTRANS_Y] + mYOffset;
                int w = frame.width();
                int h = frame.height();
                mShownFrame.set(x, y, x+w, y+h);
                mShownAlpha = mAlpha;
                if (!mLimitedAlphaCompositing
                        || (!PixelFormat.formatHasAlpha(mAttrs.format)
                        || (isIdentityMatrix(mDsDx, mDtDx, mDsDy, mDtDy)
                                && x == frame.left && y == frame.top))) {
                    if (selfTransformation) {
                        mShownAlpha *= mTransformation.getAlpha();
                    }
                    if (attachedTransformation != null) {
                        mShownAlpha *= attachedTransformation.getAlpha();
                    }
                    if (appTransformation != null) {
                        mShownAlpha *= appTransformation.getAlpha();
                    }
                } else {
                }
                if (localLOGV) Slog.v(
                    TAG, "Continuing animation in " + this +
                    ": " + mShownFrame +
                    ", alpha=" + mTransformation.getAlpha());
                return;
            }
            mShownFrame.set(mFrame);
            if (mXOffset != 0 || mYOffset != 0) {
                mShownFrame.offset(mXOffset, mYOffset);
            }
            mShownAlpha = mAlpha;
            mDsDx = 1;
            mDtDx = 0;
            mDsDy = 0;
            mDtDy = 1;
        }
        public boolean isVisibleLw() {
            final AppWindowToken atoken = mAppToken;
            return mSurface != null && mPolicyVisibility && !mAttachedHidden
                    && (atoken == null || !atoken.hiddenRequested)
                    && !mExiting && !mDestroying;
        }
        public boolean isVisibleOrBehindKeyguardLw() {
            final AppWindowToken atoken = mAppToken;
            return mSurface != null && !mAttachedHidden
                    && (atoken == null ? mPolicyVisibility : !atoken.hiddenRequested)
                    && (mOrientationChanging || (!mDrawPending && !mCommitDrawPending))
                    && !mExiting && !mDestroying;
        }
        public boolean isWinVisibleLw() {
            final AppWindowToken atoken = mAppToken;
            return mSurface != null && mPolicyVisibility && !mAttachedHidden
                    && (atoken == null || !atoken.hiddenRequested || atoken.animating)
                    && !mExiting && !mDestroying;
        }
        boolean isVisibleNow() {
            return mSurface != null && mPolicyVisibility && !mAttachedHidden
                    && !mRootToken.hidden && !mExiting && !mDestroying;
        }
        boolean isVisibleOrAdding() {
            final AppWindowToken atoken = mAppToken;
            return ((mSurface != null && !mReportDestroySurface)
                            || (!mRelayoutCalled && mViewVisibility == View.VISIBLE))
                    && mPolicyVisibility && !mAttachedHidden
                    && (atoken == null || !atoken.hiddenRequested)
                    && !mExiting && !mDestroying;
        }
        boolean isOnScreen() {
            final AppWindowToken atoken = mAppToken;
            if (atoken != null) {
                return mSurface != null && mPolicyVisibility && !mDestroying
                        && ((!mAttachedHidden && !atoken.hiddenRequested)
                                || mAnimation != null || atoken.animation != null);
            } else {
                return mSurface != null && mPolicyVisibility && !mDestroying
                        && (!mAttachedHidden || mAnimation != null);
            }
        }
        boolean isReadyForDisplay() {
            if (mRootToken.waitingToShow &&
                    mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                return false;
            }
            final AppWindowToken atoken = mAppToken;
            final boolean animating = atoken != null
                    ? (atoken.animation != null) : false;
            return mSurface != null && mPolicyVisibility && !mDestroying
                    && ((!mAttachedHidden && mViewVisibility == View.VISIBLE
                                    && !mRootToken.hidden)
                            || mAnimation != null || animating);
        }
        boolean isAnimating() {
            final WindowState attached = mAttachedWindow;
            final AppWindowToken atoken = mAppToken;
            return mAnimation != null
                    || (attached != null && attached.mAnimation != null)
                    || (atoken != null &&
                            (atoken.animation != null
                                    || atoken.inPendingTransaction));
        }
        boolean isWindowAnimating() {
            return mAnimation != null;
        }
        public boolean isDisplayedLw() {
            final AppWindowToken atoken = mAppToken;
            return mSurface != null && mPolicyVisibility && !mDestroying
                && !mDrawPending && !mCommitDrawPending
                && ((!mAttachedHidden &&
                        (atoken == null || !atoken.hiddenRequested))
                        || mAnimating);
        }
        public boolean isDrawnLw() {
            final AppWindowToken atoken = mAppToken;
            return mSurface != null && !mDestroying
                && (mOrientationChanging || (!mDrawPending && !mCommitDrawPending));
        }
        public boolean fillsScreenLw(int screenWidth, int screenHeight,
                                   boolean shownFrame, boolean onlyOpaque) {
            if (mSurface == null) {
                return false;
            }
            if (mAppToken != null && !mAppToken.appFullscreen) {
                return false;
            }
            if (onlyOpaque && mAttrs.format != PixelFormat.OPAQUE) {
                return false;
            }
            final Rect frame = shownFrame ? mShownFrame : mFrame;
            if ((mAttrs.flags & FLAG_COMPATIBLE_WINDOW) != 0) {
                return frame.left <= mCompatibleScreenFrame.left &&
                        frame.top <= mCompatibleScreenFrame.top &&
                        frame.right >= mCompatibleScreenFrame.right &&
                        frame.bottom >= mCompatibleScreenFrame.bottom;
            } else {
                return frame.left <= 0 && frame.top <= 0
                        && frame.right >= screenWidth
                        && frame.bottom >= screenHeight;
            }
        }
        boolean isOpaqueDrawn() {
            return (mAttrs.format == PixelFormat.OPAQUE
                            || mAttrs.type == TYPE_WALLPAPER)
                    && mSurface != null && mAnimation == null
                    && (mAppToken == null || mAppToken.animation == null)
                    && !mDrawPending && !mCommitDrawPending;
        }
        boolean needsBackgroundFiller(int screenWidth, int screenHeight) {
            return
                 (mAttrs.flags & FLAG_COMPATIBLE_WINDOW) != 0 &&
                 mHasDrawn && mViewVisibility == View.VISIBLE &&
                 mFrame.left <= mCompatibleScreenFrame.left &&
                 mFrame.top <= mCompatibleScreenFrame.top &&
                 mFrame.right >= mCompatibleScreenFrame.right &&
                 mFrame.bottom >= mCompatibleScreenFrame.bottom &&
                 mAttrs.type != mAttrs.TYPE_APPLICATION_STARTING;
        }
        boolean isFullscreen(int screenWidth, int screenHeight) {
            return mFrame.left <= 0 && mFrame.top <= 0 &&
                    mFrame.right >= screenWidth && mFrame.bottom >= screenHeight;
        }
        void removeLocked() {
            if (mAttachedWindow != null) {
                mAttachedWindow.mChildWindows.remove(this);
            }
            destroySurfaceLocked();
            mSession.windowRemovedLocked();
            try {
                mClient.asBinder().unlinkToDeath(mDeathRecipient, 0);
            } catch (RuntimeException e) {
            }
        }
        private class DeathRecipient implements IBinder.DeathRecipient {
            public void binderDied() {
                try {
                    synchronized(mWindowMap) {
                        WindowState win = windowForClientLocked(mSession, mClient, false);
                        Slog.i(TAG, "WIN DEATH: " + win);
                        if (win != null) {
                            removeWindowLocked(mSession, win);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                }
            }
        }
        public final boolean canReceiveKeys() {
            return     isVisibleOrAdding()
                    && (mViewVisibility == View.VISIBLE)
                    && ((mAttrs.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0);
        }
        public boolean hasDrawnLw() {
            return mHasDrawn;
        }
        public boolean showLw(boolean doAnimation) {
            return showLw(doAnimation, true);
        }
        boolean showLw(boolean doAnimation, boolean requestAnim) {
            if (mPolicyVisibility && mPolicyVisibilityAfterAnim) {
                return false;
            }
            if (DEBUG_VISIBILITY) Slog.v(TAG, "Policy visibility true: " + this);
            if (doAnimation) {
                if (DEBUG_VISIBILITY) Slog.v(TAG, "doAnimation: mPolicyVisibility="
                        + mPolicyVisibility + " mAnimation=" + mAnimation);
                if (mDisplayFrozen || !mPolicy.isScreenOn()) {
                    doAnimation = false;
                } else if (mPolicyVisibility && mAnimation == null) {
                    doAnimation = false;
                }
            }
            mPolicyVisibility = true;
            mPolicyVisibilityAfterAnim = true;
            if (doAnimation) {
                applyAnimationLocked(this, WindowManagerPolicy.TRANSIT_ENTER, true);
            }
            if (requestAnim) {
                requestAnimationLocked(0);
            }
            return true;
        }
        public boolean hideLw(boolean doAnimation) {
            return hideLw(doAnimation, true);
        }
        boolean hideLw(boolean doAnimation, boolean requestAnim) {
            if (doAnimation) {
                if (mDisplayFrozen || !mPolicy.isScreenOn()) {
                    doAnimation = false;
                }
            }
            boolean current = doAnimation ? mPolicyVisibilityAfterAnim
                    : mPolicyVisibility;
            if (!current) {
                return false;
            }
            if (doAnimation) {
                applyAnimationLocked(this, WindowManagerPolicy.TRANSIT_EXIT, false);
                if (mAnimation == null) {
                    doAnimation = false;
                }
            }
            if (doAnimation) {
                mPolicyVisibilityAfterAnim = false;
            } else {
                if (DEBUG_VISIBILITY) Slog.v(TAG, "Policy visibility false: " + this);
                mPolicyVisibilityAfterAnim = false;
                mPolicyVisibility = false;
                enableScreenIfNeededLocked();
                if (mCurrentFocus == this) {
                    mFocusMayChange = true;
                }
            }
            if (requestAnim) {
                requestAnimationLocked(0);
            }
            return true;
        }
        void dump(PrintWriter pw, String prefix) {
            pw.print(prefix); pw.print("mSession="); pw.print(mSession);
                    pw.print(" mClient="); pw.println(mClient.asBinder());
            pw.print(prefix); pw.print("mAttrs="); pw.println(mAttrs);
            if (mAttachedWindow != null || mLayoutAttached) {
                pw.print(prefix); pw.print("mAttachedWindow="); pw.print(mAttachedWindow);
                        pw.print(" mLayoutAttached="); pw.println(mLayoutAttached);
            }
            if (mIsImWindow || mIsWallpaper || mIsFloatingLayer) {
                pw.print(prefix); pw.print("mIsImWindow="); pw.print(mIsImWindow);
                        pw.print(" mIsWallpaper="); pw.print(mIsWallpaper);
                        pw.print(" mIsFloatingLayer="); pw.print(mIsFloatingLayer);
                        pw.print(" mWallpaperVisible="); pw.println(mWallpaperVisible);
            }
            pw.print(prefix); pw.print("mBaseLayer="); pw.print(mBaseLayer);
                    pw.print(" mSubLayer="); pw.print(mSubLayer);
                    pw.print(" mAnimLayer="); pw.print(mLayer); pw.print("+");
                    pw.print((mTargetAppToken != null ? mTargetAppToken.animLayerAdjustment
                          : (mAppToken != null ? mAppToken.animLayerAdjustment : 0)));
                    pw.print("="); pw.print(mAnimLayer);
                    pw.print(" mLastLayer="); pw.println(mLastLayer);
            if (mSurface != null) {
                pw.print(prefix); pw.print("mSurface="); pw.println(mSurface);
                pw.print(prefix); pw.print("Surface: shown="); pw.print(mSurfaceShown);
                        pw.print(" layer="); pw.print(mSurfaceLayer);
                        pw.print(" alpha="); pw.print(mSurfaceAlpha);
                        pw.print(" rect=("); pw.print(mSurfaceX);
                        pw.print(","); pw.print(mSurfaceY);
                        pw.print(") "); pw.print(mSurfaceW);
                        pw.print(" x "); pw.println(mSurfaceH);
            }
            pw.print(prefix); pw.print("mToken="); pw.println(mToken);
            pw.print(prefix); pw.print("mRootToken="); pw.println(mRootToken);
            if (mAppToken != null) {
                pw.print(prefix); pw.print("mAppToken="); pw.println(mAppToken);
            }
            if (mTargetAppToken != null) {
                pw.print(prefix); pw.print("mTargetAppToken="); pw.println(mTargetAppToken);
            }
            pw.print(prefix); pw.print("mViewVisibility=0x");
                    pw.print(Integer.toHexString(mViewVisibility));
                    pw.print(" mLastHidden="); pw.print(mLastHidden);
                    pw.print(" mHaveFrame="); pw.print(mHaveFrame);
                    pw.print(" mObscured="); pw.println(mObscured);
            if (!mPolicyVisibility || !mPolicyVisibilityAfterAnim || mAttachedHidden) {
                pw.print(prefix); pw.print("mPolicyVisibility=");
                        pw.print(mPolicyVisibility);
                        pw.print(" mPolicyVisibilityAfterAnim=");
                        pw.print(mPolicyVisibilityAfterAnim);
                        pw.print(" mAttachedHidden="); pw.println(mAttachedHidden);
            }
            if (!mRelayoutCalled) {
                pw.print(prefix); pw.print("mRelayoutCalled="); pw.println(mRelayoutCalled);
            }
            pw.print(prefix); pw.print("Requested w="); pw.print(mRequestedWidth);
                    pw.print(" h="); pw.print(mRequestedHeight);
                    pw.print(" mLayoutSeq="); pw.println(mLayoutSeq);
            if (mXOffset != 0 || mYOffset != 0) {
                pw.print(prefix); pw.print("Offsets x="); pw.print(mXOffset);
                        pw.print(" y="); pw.println(mYOffset);
            }
            pw.print(prefix); pw.print("mGivenContentInsets=");
                    mGivenContentInsets.printShortString(pw);
                    pw.print(" mGivenVisibleInsets=");
                    mGivenVisibleInsets.printShortString(pw);
                    pw.println();
            if (mTouchableInsets != 0 || mGivenInsetsPending) {
                pw.print(prefix); pw.print("mTouchableInsets="); pw.print(mTouchableInsets);
                        pw.print(" mGivenInsetsPending="); pw.println(mGivenInsetsPending);
            }
            pw.print(prefix); pw.print("mConfiguration="); pw.println(mConfiguration);
            pw.print(prefix); pw.print("mShownFrame=");
                    mShownFrame.printShortString(pw);
                    pw.print(" last="); mLastShownFrame.printShortString(pw);
                    pw.println();
            pw.print(prefix); pw.print("mFrame="); mFrame.printShortString(pw);
                    pw.print(" last="); mLastFrame.printShortString(pw);
                    pw.println();
            pw.print(prefix); pw.print("mContainingFrame=");
                    mContainingFrame.printShortString(pw);
                    pw.print(" mDisplayFrame=");
                    mDisplayFrame.printShortString(pw);
                    pw.println();
            pw.print(prefix); pw.print("mContentFrame="); mContentFrame.printShortString(pw);
                    pw.print(" mVisibleFrame="); mVisibleFrame.printShortString(pw);
                    pw.println();
            pw.print(prefix); pw.print("mContentInsets="); mContentInsets.printShortString(pw);
                    pw.print(" last="); mLastContentInsets.printShortString(pw);
                    pw.print(" mVisibleInsets="); mVisibleInsets.printShortString(pw);
                    pw.print(" last="); mLastVisibleInsets.printShortString(pw);
                    pw.println();
            if (mShownAlpha != 1 || mAlpha != 1 || mLastAlpha != 1) {
                pw.print(prefix); pw.print("mShownAlpha="); pw.print(mShownAlpha);
                        pw.print(" mAlpha="); pw.print(mAlpha);
                        pw.print(" mLastAlpha="); pw.println(mLastAlpha);
            }
            if (mAnimating || mLocalAnimating || mAnimationIsEntrance
                    || mAnimation != null) {
                pw.print(prefix); pw.print("mAnimating="); pw.print(mAnimating);
                        pw.print(" mLocalAnimating="); pw.print(mLocalAnimating);
                        pw.print(" mAnimationIsEntrance="); pw.print(mAnimationIsEntrance);
                        pw.print(" mAnimation="); pw.println(mAnimation);
            }
            if (mHasTransformation || mHasLocalTransformation) {
                pw.print(prefix); pw.print("XForm: has=");
                        pw.print(mHasTransformation);
                        pw.print(" hasLocal="); pw.print(mHasLocalTransformation);
                        pw.print(" "); mTransformation.printShortString(pw);
                        pw.println();
            }
            pw.print(prefix); pw.print("mDrawPending="); pw.print(mDrawPending);
                    pw.print(" mCommitDrawPending="); pw.print(mCommitDrawPending);
                    pw.print(" mReadyToShow="); pw.print(mReadyToShow);
                    pw.print(" mHasDrawn="); pw.println(mHasDrawn);
            if (mExiting || mRemoveOnExit || mDestroying || mRemoved) {
                pw.print(prefix); pw.print("mExiting="); pw.print(mExiting);
                        pw.print(" mRemoveOnExit="); pw.print(mRemoveOnExit);
                        pw.print(" mDestroying="); pw.print(mDestroying);
                        pw.print(" mRemoved="); pw.println(mRemoved);
            }
            if (mOrientationChanging || mAppFreezing || mTurnOnScreen) {
                pw.print(prefix); pw.print("mOrientationChanging=");
                        pw.print(mOrientationChanging);
                        pw.print(" mAppFreezing="); pw.print(mAppFreezing);
                        pw.print(" mTurnOnScreen="); pw.println(mTurnOnScreen);
            }
            if (mHScale != 1 || mVScale != 1) {
                pw.print(prefix); pw.print("mHScale="); pw.print(mHScale);
                        pw.print(" mVScale="); pw.println(mVScale);
            }
            if (mWallpaperX != -1 || mWallpaperY != -1) {
                pw.print(prefix); pw.print("mWallpaperX="); pw.print(mWallpaperX);
                        pw.print(" mWallpaperY="); pw.println(mWallpaperY);
            }
            if (mWallpaperXStep != -1 || mWallpaperYStep != -1) {
                pw.print(prefix); pw.print("mWallpaperXStep="); pw.print(mWallpaperXStep);
                        pw.print(" mWallpaperYStep="); pw.println(mWallpaperYStep);
            }
        }
        @Override
        public String toString() {
            return "Window{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + mAttrs.getTitle() + " paused=" + mToken.paused + "}";
        }
    }
    class WindowToken {
        final IBinder token;
        final int windowType;
        final boolean explicit;
        String stringName;
        AppWindowToken appWindowToken;
        final ArrayList<WindowState> windows = new ArrayList<WindowState>();
        boolean paused = false;
        boolean hidden;
        boolean hasVisible;
        boolean waitingToShow;
        boolean waitingToHide;
        boolean sendingToBottom;
        boolean sendingToTop;
        WindowToken(IBinder _token, int type, boolean _explicit) {
            token = _token;
            windowType = type;
            explicit = _explicit;
        }
        void dump(PrintWriter pw, String prefix) {
            pw.print(prefix); pw.print("token="); pw.println(token);
            pw.print(prefix); pw.print("windows="); pw.println(windows);
            pw.print(prefix); pw.print("windowType="); pw.print(windowType);
                    pw.print(" hidden="); pw.print(hidden);
                    pw.print(" hasVisible="); pw.println(hasVisible);
            if (waitingToShow || waitingToHide || sendingToBottom || sendingToTop) {
                pw.print(prefix); pw.print("waitingToShow="); pw.print(waitingToShow);
                        pw.print(" waitingToHide="); pw.print(waitingToHide);
                        pw.print(" sendingToBottom="); pw.print(sendingToBottom);
                        pw.print(" sendingToTop="); pw.println(sendingToTop);
            }
        }
        @Override
        public String toString() {
            if (stringName == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("WindowToken{");
                sb.append(Integer.toHexString(System.identityHashCode(this)));
                sb.append(" token="); sb.append(token); sb.append('}');
                stringName = sb.toString();
            }
            return stringName;
        }
    };
    class AppWindowToken extends WindowToken {
        final IApplicationToken appToken;
        final ArrayList<WindowState> allAppWindows = new ArrayList<WindowState>();
        int groupId = -1;
        boolean appFullscreen;
        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        int lastTransactionSequence = mTransactionSequence-1;
        int numInterestingWindows;
        int numDrawnWindows;
        boolean inPendingTransaction;
        boolean allDrawn;
        boolean willBeHidden;
        boolean hiddenRequested;
        boolean clientHidden;
        boolean reportedVisible;
        boolean removed;
        boolean freezingScreen;
        boolean animating;
        Animation animation;
        boolean hasTransformation;
        final Transformation transformation = new Transformation();
        int animLayerAdjustment;
        StartingData startingData;
        WindowState startingWindow;
        View startingView;
        boolean startingDisplayed;
        boolean startingMoved;
        boolean firstWindowDrawn;
        AppWindowToken(IApplicationToken _token) {
            super(_token.asBinder(),
                    WindowManager.LayoutParams.TYPE_APPLICATION, true);
            appWindowToken = this;
            appToken = _token;
        }
        public void setAnimation(Animation anim) {
            if (localLOGV) Slog.v(
                TAG, "Setting animation in " + this + ": " + anim);
            animation = anim;
            animating = false;
            anim.restrictDuration(MAX_ANIMATION_DURATION);
            anim.scaleCurrentDuration(mTransitionAnimationScale);
            int zorder = anim.getZAdjustment();
            int adj = 0;
            if (zorder == Animation.ZORDER_TOP) {
                adj = TYPE_LAYER_OFFSET;
            } else if (zorder == Animation.ZORDER_BOTTOM) {
                adj = -TYPE_LAYER_OFFSET;
            }
            if (animLayerAdjustment != adj) {
                animLayerAdjustment = adj;
                updateLayers();
            }
        }
        public void setDummyAnimation() {
            if (animation == null) {
                if (localLOGV) Slog.v(
                    TAG, "Setting dummy animation in " + this);
                animation = sDummyAnimation;
            }
        }
        public void clearAnimation() {
            if (animation != null) {
                animation = null;
                animating = true;
            }
        }
        void updateLayers() {
            final int N = allAppWindows.size();
            final int adj = animLayerAdjustment;
            for (int i=0; i<N; i++) {
                WindowState w = allAppWindows.get(i);
                w.mAnimLayer = w.mLayer + adj;
                if (DEBUG_LAYERS) Slog.v(TAG, "Updating layer " + w + ": "
                        + w.mAnimLayer);
                if (w == mInputMethodTarget) {
                    setInputMethodAnimLayerAdjustment(adj);
                }
                if (w == mWallpaperTarget && mLowerWallpaperTarget == null) {
                    setWallpaperAnimLayerAdjustmentLocked(adj);
                }
            }
        }
        void sendAppVisibilityToClients() {
            final int N = allAppWindows.size();
            for (int i=0; i<N; i++) {
                WindowState win = allAppWindows.get(i);
                if (win == startingWindow && clientHidden) {
                    continue;
                }
                try {
                    if (DEBUG_VISIBILITY) Slog.v(TAG,
                            "Setting visibility of " + win + ": " + (!clientHidden));
                    win.mClient.dispatchAppVisibility(!clientHidden);
                } catch (RemoteException e) {
                }
            }
        }
        void showAllWindowsLocked() {
            final int NW = allAppWindows.size();
            for (int i=0; i<NW; i++) {
                WindowState w = allAppWindows.get(i);
                if (DEBUG_VISIBILITY) Slog.v(TAG,
                        "performing show on: " + w);
                w.performShowLocked();
            }
        }
        boolean stepAnimationLocked(long currentTime, int dw, int dh) {
            if (!mDisplayFrozen && mPolicy.isScreenOn()) {
                if (animation == sDummyAnimation) {
                    return false;
                }
                if ((allDrawn || animating || startingDisplayed) && animation != null) {
                    if (!animating) {
                        if (DEBUG_ANIM) Slog.v(
                            TAG, "Starting animation in " + this +
                            " @ " + currentTime + ": dw=" + dw + " dh=" + dh
                            + " scale=" + mTransitionAnimationScale
                            + " allDrawn=" + allDrawn + " animating=" + animating);
                        animation.initialize(dw, dh, dw, dh);
                        animation.setStartTime(currentTime);
                        animating = true;
                    }
                    transformation.clear();
                    final boolean more = animation.getTransformation(
                        currentTime, transformation);
                    if (DEBUG_ANIM) Slog.v(
                        TAG, "Stepped animation in " + this +
                        ": more=" + more + ", xform=" + transformation);
                    if (more) {
                        hasTransformation = true;
                        return true;
                    }
                    if (DEBUG_ANIM) Slog.v(
                        TAG, "Finished animation in " + this +
                        " @ " + currentTime);
                    animation = null;
                }
            } else if (animation != null) {
                animating = true;
                animation = null;
            }
            hasTransformation = false;
            if (!animating) {
                return false;
            }
            clearAnimation();
            animating = false;
            if (mInputMethodTarget != null && mInputMethodTarget.mAppToken == this) {
                moveInputMethodWindowsIfNeededLocked(true);
            }
            if (DEBUG_ANIM) Slog.v(
                    TAG, "Animation done in " + this
                    + ": reportedVisible=" + reportedVisible);
            transformation.clear();
            if (animLayerAdjustment != 0) {
                animLayerAdjustment = 0;
                updateLayers();
            }
            final int N = windows.size();
            for (int i=0; i<N; i++) {
                ((WindowState)windows.get(i)).finishExit();
            }
            updateReportedVisibilityLocked();
            return false;
        }
        void updateReportedVisibilityLocked() {
            if (appToken == null) {
                return;
            }
            int numInteresting = 0;
            int numVisible = 0;
            boolean nowGone = true;
            if (DEBUG_VISIBILITY) Slog.v(TAG, "Update reported visibility: " + this);
            final int N = allAppWindows.size();
            for (int i=0; i<N; i++) {
                WindowState win = allAppWindows.get(i);
                if (win == startingWindow || win.mAppFreezing
                        || win.mViewVisibility != View.VISIBLE) {
                    continue;
                }
                if (DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Win " + win + ": isDrawn="
                            + win.isDrawnLw()
                            + ", isAnimating=" + win.isAnimating());
                    if (!win.isDrawnLw()) {
                        Slog.v(TAG, "Not displayed: s=" + win.mSurface
                                + " pv=" + win.mPolicyVisibility
                                + " dp=" + win.mDrawPending
                                + " cdp=" + win.mCommitDrawPending
                                + " ah=" + win.mAttachedHidden
                                + " th="
                                + (win.mAppToken != null
                                        ? win.mAppToken.hiddenRequested : false)
                                + " a=" + win.mAnimating);
                    }
                }
                numInteresting++;
                if (win.isDrawnLw()) {
                    if (!win.isAnimating()) {
                        numVisible++;
                    }
                    nowGone = false;
                } else if (win.isAnimating()) {
                    nowGone = false;
                }
            }
            boolean nowVisible = numInteresting > 0 && numVisible >= numInteresting;
            if (DEBUG_VISIBILITY) Slog.v(TAG, "VIS " + this + ": interesting="
                    + numInteresting + " visible=" + numVisible);
            if (nowVisible != reportedVisible) {
                if (DEBUG_VISIBILITY) Slog.v(
                        TAG, "Visibility changed in " + this
                        + ": vis=" + nowVisible);
                reportedVisible = nowVisible;
                Message m = mH.obtainMessage(
                        H.REPORT_APPLICATION_TOKEN_WINDOWS,
                        nowVisible ? 1 : 0,
                        nowGone ? 1 : 0,
                        this);
                    mH.sendMessage(m);
            }
        }
        WindowState findMainWindow() {
            int j = windows.size();
            while (j > 0) {
                j--;
                WindowState win = windows.get(j);
                if (win.mAttrs.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION
                        || win.mAttrs.type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING) {
                    return win;
                }
            }
            return null;
        }
        void dump(PrintWriter pw, String prefix) {
            super.dump(pw, prefix);
            if (appToken != null) {
                pw.print(prefix); pw.println("app=true");
            }
            if (allAppWindows.size() > 0) {
                pw.print(prefix); pw.print("allAppWindows="); pw.println(allAppWindows);
            }
            pw.print(prefix); pw.print("groupId="); pw.print(groupId);
                    pw.print(" appFullscreen="); pw.print(appFullscreen);
                    pw.print(" requestedOrientation="); pw.println(requestedOrientation);
            pw.print(prefix); pw.print("hiddenRequested="); pw.print(hiddenRequested);
                    pw.print(" clientHidden="); pw.print(clientHidden);
                    pw.print(" willBeHidden="); pw.print(willBeHidden);
                    pw.print(" reportedVisible="); pw.println(reportedVisible);
            if (paused || freezingScreen) {
                pw.print(prefix); pw.print("paused="); pw.print(paused);
                        pw.print(" freezingScreen="); pw.println(freezingScreen);
            }
            if (numInterestingWindows != 0 || numDrawnWindows != 0
                    || inPendingTransaction || allDrawn) {
                pw.print(prefix); pw.print("numInterestingWindows=");
                        pw.print(numInterestingWindows);
                        pw.print(" numDrawnWindows="); pw.print(numDrawnWindows);
                        pw.print(" inPendingTransaction="); pw.print(inPendingTransaction);
                        pw.print(" allDrawn="); pw.println(allDrawn);
            }
            if (animating || animation != null) {
                pw.print(prefix); pw.print("animating="); pw.print(animating);
                        pw.print(" animation="); pw.println(animation);
            }
            if (animLayerAdjustment != 0) {
                pw.print(prefix); pw.print("animLayerAdjustment="); pw.println(animLayerAdjustment);
            }
            if (hasTransformation) {
                pw.print(prefix); pw.print("hasTransformation="); pw.print(hasTransformation);
                        pw.print(" transformation="); transformation.printShortString(pw);
                        pw.println();
            }
            if (startingData != null || removed || firstWindowDrawn) {
                pw.print(prefix); pw.print("startingData="); pw.print(startingData);
                        pw.print(" removed="); pw.print(removed);
                        pw.print(" firstWindowDrawn="); pw.println(firstWindowDrawn);
            }
            if (startingWindow != null || startingView != null
                    || startingDisplayed || startingMoved) {
                pw.print(prefix); pw.print("startingWindow="); pw.print(startingWindow);
                        pw.print(" startingView="); pw.print(startingView);
                        pw.print(" startingDisplayed="); pw.print(startingDisplayed);
                        pw.print(" startingMoved"); pw.println(startingMoved);
            }
        }
        @Override
        public String toString() {
            if (stringName == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("AppWindowToken{");
                sb.append(Integer.toHexString(System.identityHashCode(this)));
                sb.append(" token="); sb.append(token); sb.append('}');
                stringName = sb.toString();
            }
            return stringName;
        }
    }
    static final class DummyAnimation extends Animation {
        public boolean getTransformation(long currentTime, Transformation outTransformation) {
            return false;
        }
    }
    static final Animation sDummyAnimation = new DummyAnimation();
    static final class StartingData {
        final String pkg;
        final int theme;
        final CharSequence nonLocalizedLabel;
        final int labelRes;
        final int icon;
        StartingData(String _pkg, int _theme, CharSequence _nonLocalizedLabel,
                int _labelRes, int _icon) {
            pkg = _pkg;
            theme = _theme;
            nonLocalizedLabel = _nonLocalizedLabel;
            labelRes = _labelRes;
            icon = _icon;
        }
    }
    private final class H extends Handler {
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int ANIMATE = 4;
        public static final int ADD_STARTING = 5;
        public static final int REMOVE_STARTING = 6;
        public static final int FINISHED_STARTING = 7;
        public static final int REPORT_APPLICATION_TOKEN_WINDOWS = 8;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int HOLD_SCREEN_CHANGED = 12;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int FORCE_GC = 15;
        public static final int ENABLE_SCREEN = 16;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int SEND_NEW_CONFIGURATION = 18;
        private Session mLastReportedHold;
        public H() {
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_FOCUS_CHANGE: {
                    WindowState lastFocus;
                    WindowState newFocus;
                    synchronized(mWindowMap) {
                        lastFocus = mLastFocus;
                        newFocus = mCurrentFocus;
                        if (lastFocus == newFocus) {
                            return;
                        }
                        mLastFocus = newFocus;
                        if (newFocus != null && lastFocus != null
                                && !newFocus.isDisplayedLw()) {
                            mLosingFocus.add(lastFocus);
                            lastFocus = null;
                        }
                    }
                    if (lastFocus != newFocus) {
                        if (newFocus != null) {
                            try {
                                newFocus.mClient.windowFocusChanged(true, mInTouchMode);
                            } catch (RemoteException e) {
                            }
                        }
                        if (lastFocus != null) {
                            try {
                                lastFocus.mClient.windowFocusChanged(false, mInTouchMode);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                } break;
                case REPORT_LOSING_FOCUS: {
                    ArrayList<WindowState> losers;
                    synchronized(mWindowMap) {
                        losers = mLosingFocus;
                        mLosingFocus = new ArrayList<WindowState>();
                    }
                    final int N = losers.size();
                    for (int i=0; i<N; i++) {
                        try {
                            losers.get(i).mClient.windowFocusChanged(false, mInTouchMode);
                        } catch (RemoteException e) {
                        }
                    }
                } break;
                case ANIMATE: {
                    synchronized(mWindowMap) {
                        mAnimationPending = false;
                        performLayoutAndPlaceSurfacesLocked();
                    }
                } break;
                case ADD_STARTING: {
                    final AppWindowToken wtoken = (AppWindowToken)msg.obj;
                    final StartingData sd = wtoken.startingData;
                    if (sd == null) {
                        return;
                    }
                    if (DEBUG_STARTING_WINDOW) Slog.v(TAG, "Add starting "
                            + wtoken + ": pkg=" + sd.pkg);
                    View view = null;
                    try {
                        view = mPolicy.addStartingWindow(
                            wtoken.token, sd.pkg,
                            sd.theme, sd.nonLocalizedLabel, sd.labelRes,
                            sd.icon);
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception when adding starting window", e);
                    }
                    if (view != null) {
                        boolean abort = false;
                        synchronized(mWindowMap) {
                            if (wtoken.removed || wtoken.startingData == null) {
                                if (wtoken.startingWindow != null) {
                                    if (DEBUG_STARTING_WINDOW) Slog.v(TAG,
                                            "Aborted starting " + wtoken
                                            + ": removed=" + wtoken.removed
                                            + " startingData=" + wtoken.startingData);
                                    wtoken.startingWindow = null;
                                    wtoken.startingData = null;
                                    abort = true;
                                }
                            } else {
                                wtoken.startingView = view;
                            }
                            if (DEBUG_STARTING_WINDOW && !abort) Slog.v(TAG,
                                    "Added starting " + wtoken
                                    + ": startingWindow="
                                    + wtoken.startingWindow + " startingView="
                                    + wtoken.startingView);
                        }
                        if (abort) {
                            try {
                                mPolicy.removeStartingWindow(wtoken.token, view);
                            } catch (Exception e) {
                                Slog.w(TAG, "Exception when removing starting window", e);
                            }
                        }
                    }
                } break;
                case REMOVE_STARTING: {
                    final AppWindowToken wtoken = (AppWindowToken)msg.obj;
                    IBinder token = null;
                    View view = null;
                    synchronized (mWindowMap) {
                        if (DEBUG_STARTING_WINDOW) Slog.v(TAG, "Remove starting "
                                + wtoken + ": startingWindow="
                                + wtoken.startingWindow + " startingView="
                                + wtoken.startingView);
                        if (wtoken.startingWindow != null) {
                            view = wtoken.startingView;
                            token = wtoken.token;
                            wtoken.startingData = null;
                            wtoken.startingView = null;
                            wtoken.startingWindow = null;
                        }
                    }
                    if (view != null) {
                        try {
                            mPolicy.removeStartingWindow(token, view);
                        } catch (Exception e) {
                            Slog.w(TAG, "Exception when removing starting window", e);
                        }
                    }
                } break;
                case FINISHED_STARTING: {
                    IBinder token = null;
                    View view = null;
                    while (true) {
                        synchronized (mWindowMap) {
                            final int N = mFinishedStarting.size();
                            if (N <= 0) {
                                break;
                            }
                            AppWindowToken wtoken = mFinishedStarting.remove(N-1);
                            if (DEBUG_STARTING_WINDOW) Slog.v(TAG,
                                    "Finished starting " + wtoken
                                    + ": startingWindow=" + wtoken.startingWindow
                                    + " startingView=" + wtoken.startingView);
                            if (wtoken.startingWindow == null) {
                                continue;
                            }
                            view = wtoken.startingView;
                            token = wtoken.token;
                            wtoken.startingData = null;
                            wtoken.startingView = null;
                            wtoken.startingWindow = null;
                        }
                        try {
                            mPolicy.removeStartingWindow(token, view);
                        } catch (Exception e) {
                            Slog.w(TAG, "Exception when removing starting window", e);
                        }
                    }
                } break;
                case REPORT_APPLICATION_TOKEN_WINDOWS: {
                    final AppWindowToken wtoken = (AppWindowToken)msg.obj;
                    boolean nowVisible = msg.arg1 != 0;
                    boolean nowGone = msg.arg2 != 0;
                    try {
                        if (DEBUG_VISIBILITY) Slog.v(
                                TAG, "Reporting visible in " + wtoken
                                + " visible=" + nowVisible
                                + " gone=" + nowGone);
                        if (nowVisible) {
                            wtoken.appToken.windowsVisible();
                        } else {
                            wtoken.appToken.windowsGone();
                        }
                    } catch (RemoteException ex) {
                    }
                } break;
                case WINDOW_FREEZE_TIMEOUT: {
                    synchronized (mWindowMap) {
                        Slog.w(TAG, "Window freeze timeout expired.");
                        int i = mWindows.size();
                        while (i > 0) {
                            i--;
                            WindowState w = (WindowState)mWindows.get(i);
                            if (w.mOrientationChanging) {
                                w.mOrientationChanging = false;
                                Slog.w(TAG, "Force clearing orientation change: " + w);
                            }
                        }
                        performLayoutAndPlaceSurfacesLocked();
                    }
                    break;
                }
                case HOLD_SCREEN_CHANGED: {
                    Session oldHold;
                    Session newHold;
                    synchronized (mWindowMap) {
                        oldHold = mLastReportedHold;
                        newHold = (Session)msg.obj;
                        mLastReportedHold = newHold;
                    }
                    if (oldHold != newHold) {
                        try {
                            if (oldHold != null) {
                                mBatteryStats.noteStopWakelock(oldHold.mUid,
                                        "window",
                                        BatteryStats.WAKE_TYPE_WINDOW);
                            }
                            if (newHold != null) {
                                mBatteryStats.noteStartWakelock(newHold.mUid,
                                        "window",
                                        BatteryStats.WAKE_TYPE_WINDOW);
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    break;
                }
                case APP_TRANSITION_TIMEOUT: {
                    synchronized (mWindowMap) {
                        if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "*** APP TRANSITION TIMEOUT");
                            mAppTransitionReady = true;
                            mAppTransitionTimeout = true;
                            performLayoutAndPlaceSurfacesLocked();
                        }
                    }
                    break;
                }
                case PERSIST_ANIMATION_SCALE: {
                    Settings.System.putFloat(mContext.getContentResolver(),
                            Settings.System.WINDOW_ANIMATION_SCALE, mWindowAnimationScale);
                    Settings.System.putFloat(mContext.getContentResolver(),
                            Settings.System.TRANSITION_ANIMATION_SCALE, mTransitionAnimationScale);
                    break;
                }
                case FORCE_GC: {
                    synchronized(mWindowMap) {
                        if (mAnimationPending) {
                            mH.sendMessageDelayed(mH.obtainMessage(H.FORCE_GC),
                                    2000);
                            return;
                        }
                        if (mDisplayFrozen) {
                            return;
                        }
                        mFreezeGcPending = 0;
                    }
                    Runtime.getRuntime().gc();
                    break;
                }
                case ENABLE_SCREEN: {
                    performEnableScreen();
                    break;
                }
                case APP_FREEZE_TIMEOUT: {
                    synchronized (mWindowMap) {
                        Slog.w(TAG, "App freeze timeout expired.");
                        int i = mAppTokens.size();
                        while (i > 0) {
                            i--;
                            AppWindowToken tok = mAppTokens.get(i);
                            if (tok.freezingScreen) {
                                Slog.w(TAG, "Force clearing freeze: " + tok);
                                unsetAppFreezingScreenLocked(tok, true, true);
                            }
                        }
                    }
                    break;
                }
                case SEND_NEW_CONFIGURATION: {
                    removeMessages(SEND_NEW_CONFIGURATION);
                    sendNewConfiguration();
                    break;
                }
            }
        }
    }
    public IWindowSession openSession(IInputMethodClient client,
            IInputContext inputContext) {
        if (client == null) throw new IllegalArgumentException("null client");
        if (inputContext == null) throw new IllegalArgumentException("null inputContext");
        return new Session(client, inputContext);
    }
    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        synchronized (mWindowMap) {
            int idx = findDesiredInputMethodWindowIndexLocked(false);
            WindowState imFocus;
            if (idx > 0) {
                imFocus = (WindowState)mWindows.get(idx-1);
                if (imFocus != null) {
                    if (imFocus.mSession.mClient != null &&
                            imFocus.mSession.mClient.asBinder() == client.asBinder()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    final WindowState windowForClientLocked(Session session, IWindow client,
            boolean throwOnError) {
        return windowForClientLocked(session, client.asBinder(), throwOnError);
    }
    final WindowState windowForClientLocked(Session session, IBinder client,
            boolean throwOnError) {
        WindowState win = mWindowMap.get(client);
        if (localLOGV) Slog.v(
            TAG, "Looking up client " + client + ": " + win);
        if (win == null) {
            RuntimeException ex = new IllegalArgumentException(
                    "Requested window " + client + " does not exist");
            if (throwOnError) {
                throw ex;
            }
            Slog.w(TAG, "Failed looking up window", ex);
            return null;
        }
        if (session != null && win.mSession != session) {
            RuntimeException ex = new IllegalArgumentException(
                    "Requested window " + client + " is in session " +
                    win.mSession + ", not " + session);
            if (throwOnError) {
                throw ex;
            }
            Slog.w(TAG, "Failed looking up window", ex);
            return null;
        }
        return win;
    }
    final void rebuildAppWindowListLocked() {
        int NW = mWindows.size();
        int i;
        int lastWallpaper = -1;
        int numRemoved = 0;
        i=0;
        while (i < NW) {
            WindowState w = (WindowState)mWindows.get(i);
            if (w.mAppToken != null) {
                WindowState win = (WindowState)mWindows.remove(i);
                if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG,
                        "Rebuild removing window: " + win);
                NW--;
                numRemoved++;
                continue;
            } else if (w.mAttrs.type == WindowManager.LayoutParams.TYPE_WALLPAPER
                    && lastWallpaper == i-1) {
                lastWallpaper = i;
            }
            i++;
        }
        lastWallpaper++;
        i = lastWallpaper;
        int NT = mExitingAppTokens.size();
        for (int j=0; j<NT; j++) {
            i = reAddAppWindowsLocked(i, mExitingAppTokens.get(j));
        }
        NT = mAppTokens.size();
        for (int j=0; j<NT; j++) {
            i = reAddAppWindowsLocked(i, mAppTokens.get(j));
        }
        i -= lastWallpaper;
        if (i != numRemoved) {
            Slog.w(TAG, "Rebuild removed " + numRemoved
                    + " windows but added " + i);
        }
    }
    private final void assignLayersLocked() {
        int N = mWindows.size();
        int curBaseLayer = 0;
        int curLayer = 0;
        int i;
        for (i=0; i<N; i++) {
            WindowState w = (WindowState)mWindows.get(i);
            if (w.mBaseLayer == curBaseLayer || w.mIsImWindow
                    || (i > 0 && w.mIsWallpaper)) {
                curLayer += WINDOW_LAYER_MULTIPLIER;
                w.mLayer = curLayer;
            } else {
                curBaseLayer = curLayer = w.mBaseLayer;
                w.mLayer = curLayer;
            }
            if (w.mTargetAppToken != null) {
                w.mAnimLayer = w.mLayer + w.mTargetAppToken.animLayerAdjustment;
            } else if (w.mAppToken != null) {
                w.mAnimLayer = w.mLayer + w.mAppToken.animLayerAdjustment;
            } else {
                w.mAnimLayer = w.mLayer;
            }
            if (w.mIsImWindow) {
                w.mAnimLayer += mInputMethodAnimLayerAdjustment;
            } else if (w.mIsWallpaper) {
                w.mAnimLayer += mWallpaperAnimLayerAdjustment;
            }
            if (DEBUG_LAYERS) Slog.v(TAG, "Assign layer " + w + ": "
                    + w.mAnimLayer);
        }
    }
    private boolean mInLayout = false;
    private final void performLayoutAndPlaceSurfacesLocked() {
        if (mInLayout) {
            if (DEBUG) {
                throw new RuntimeException("Recursive call!");
            }
            Slog.w(TAG, "performLayoutAndPlaceSurfacesLocked called while in layout");
            return;
        }
        if (mWaitingForConfig) {
            return;
        }
        boolean recoveringMemory = false;
        if (mForceRemoves != null) {
            recoveringMemory = true;
            for (int i=0; i<mForceRemoves.size(); i++) {
                WindowState ws = mForceRemoves.get(i);
                Slog.i(TAG, "Force removing: " + ws);
                removeWindowInnerLocked(ws.mSession, ws);
            }
            mForceRemoves = null;
            Slog.w(TAG, "Due to memory failure, waiting a bit for next layout");
            Object tmp = new Object();
            synchronized (tmp) {
                try {
                    tmp.wait(250);
                } catch (InterruptedException e) {
                }
            }
        }
        mInLayout = true;
        try {
            performLayoutAndPlaceSurfacesLockedInner(recoveringMemory);
            int i = mPendingRemove.size()-1;
            if (i >= 0) {
                while (i >= 0) {
                    WindowState w = mPendingRemove.get(i);
                    removeWindowInnerLocked(w.mSession, w);
                    i--;
                }
                mPendingRemove.clear();
                mInLayout = false;
                assignLayersLocked();
                mLayoutNeeded = true;
                performLayoutAndPlaceSurfacesLocked();
            } else {
                mInLayout = false;
                if (mLayoutNeeded) {
                    requestAnimationLocked(0);
                }
            }
        } catch (RuntimeException e) {
            mInLayout = false;
            Slog.e(TAG, "Unhandled exception while layout out windows", e);
        }
    }
    private final int performLayoutLockedInner() {
        if (!mLayoutNeeded) {
            return 0;
        }
        mLayoutNeeded = false;
        final int dw = mDisplay.getWidth();
        final int dh = mDisplay.getHeight();
        final int N = mWindows.size();
        int i;
        if (DEBUG_LAYOUT) Slog.v(TAG, "performLayout: needed="
                + mLayoutNeeded + " dw=" + dw + " dh=" + dh);
        mPolicy.beginLayoutLw(dw, dh);
        int seq = mLayoutSeq+1;
        if (seq < 0) seq = 0;
        mLayoutSeq = seq;
        int topAttached = -1;
        for (i = N-1; i >= 0; i--) {
            WindowState win = (WindowState) mWindows.get(i);
            final AppWindowToken atoken = win.mAppToken;
            final boolean gone = win.mViewVisibility == View.GONE
                    || !win.mRelayoutCalled
                    || win.mRootToken.hidden
                    || (atoken != null && atoken.hiddenRequested)
                    || win.mAttachedHidden
                    || win.mExiting || win.mDestroying;
            if (!win.mLayoutAttached) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "First pass " + win
                        + ": gone=" + gone + " mHaveFrame=" + win.mHaveFrame
                        + " mLayoutAttached=" + win.mLayoutAttached);
                if (DEBUG_LAYOUT && gone) Slog.v(TAG, "  (mViewVisibility="
                        + win.mViewVisibility + " mRelayoutCalled="
                        + win.mRelayoutCalled + " hidden="
                        + win.mRootToken.hidden + " hiddenRequested="
                        + (atoken != null && atoken.hiddenRequested)
                        + " mAttachedHidden=" + win.mAttachedHidden);
            }
            if (!gone || !win.mHaveFrame) {
                if (!win.mLayoutAttached) {
                    mPolicy.layoutWindowLw(win, win.mAttrs, null);
                    win.mLayoutSeq = seq;
                    if (DEBUG_LAYOUT) Slog.v(TAG, "-> mFrame="
                            + win.mFrame + " mContainingFrame="
                            + win.mContainingFrame + " mDisplayFrame="
                            + win.mDisplayFrame);
                } else {
                    if (topAttached < 0) topAttached = i;
                }
            }
        }
        for (i = topAttached; i >= 0; i--) {
            WindowState win = (WindowState) mWindows.get(i);
            if (win.mLayoutAttached) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "Second pass " + win
                        + " mHaveFrame=" + win.mHaveFrame
                        + " mViewVisibility=" + win.mViewVisibility
                        + " mRelayoutCalled=" + win.mRelayoutCalled);
                if ((win.mViewVisibility != View.GONE && win.mRelayoutCalled)
                        || !win.mHaveFrame) {
                    mPolicy.layoutWindowLw(win, win.mAttrs, win.mAttachedWindow);
                    win.mLayoutSeq = seq;
                    if (DEBUG_LAYOUT) Slog.v(TAG, "-> mFrame="
                            + win.mFrame + " mContainingFrame="
                            + win.mContainingFrame + " mDisplayFrame="
                            + win.mDisplayFrame);
                }
            }
        }
        return mPolicy.finishLayoutLw();
    }
    private final void performLayoutAndPlaceSurfacesLockedInner(
            boolean recoveringMemory) {
        final long currentTime = SystemClock.uptimeMillis();
        final int dw = mDisplay.getWidth();
        final int dh = mDisplay.getHeight();
        int i;
        if (mFocusMayChange) {
            mFocusMayChange = false;
            updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
        }
        if (mFxSession == null) {
            mFxSession = new SurfaceSession();
        }
        if (SHOW_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION");
        for (i=mExitingTokens.size()-1; i>=0; i--) {
            mExitingTokens.get(i).hasVisible = false;
        }
        for (i=mExitingAppTokens.size()-1; i>=0; i--) {
            mExitingAppTokens.get(i).hasVisible = false;
        }
        boolean orientationChangeComplete = true;
        Session holdScreen = null;
        float screenBrightness = -1;
        float buttonBrightness = -1;
        boolean focusDisplayed = false;
        boolean animating = false;
        Surface.openTransaction();
        try {
            boolean wallpaperForceHidingChanged = false;
            int repeats = 0;
            int changes = 0;
            do {
                repeats++;
                if (repeats > 6) {
                    Slog.w(TAG, "Animation repeat aborted after too many iterations");
                    mLayoutNeeded = false;
                    break;
                }
                if ((changes&(WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER
                        | WindowManagerPolicy.FINISH_LAYOUT_REDO_CONFIG
                        | WindowManagerPolicy.FINISH_LAYOUT_REDO_LAYOUT)) != 0) {
                    if ((changes&WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER) != 0) {
                        if ((adjustWallpaperWindowsLocked()&ADJUST_WALLPAPER_LAYERS_CHANGED) != 0) {
                            assignLayersLocked();
                            mLayoutNeeded = true;
                        }
                    }
                    if ((changes&WindowManagerPolicy.FINISH_LAYOUT_REDO_CONFIG) != 0) {
                        if (DEBUG_LAYOUT) Slog.v(TAG, "Computing new config from layout");
                        if (updateOrientationFromAppTokensLocked()) {
                            mLayoutNeeded = true;
                            mH.sendEmptyMessage(H.SEND_NEW_CONFIGURATION);
                        }
                    }
                    if ((changes&WindowManagerPolicy.FINISH_LAYOUT_REDO_LAYOUT) != 0) {
                        mLayoutNeeded = true;
                    }
                }
                if (repeats < 4) {
                    changes = performLayoutLockedInner();
                    if (changes != 0) {
                        continue;
                    }
                } else {
                    Slog.w(TAG, "Layout repeat skipped after too many iterations");
                    changes = 0;
                }
                final int transactionSequence = ++mTransactionSequence;
                boolean tokensAnimating = false;
                final int NAT = mAppTokens.size();
                for (i=0; i<NAT; i++) {
                    if (mAppTokens.get(i).stepAnimationLocked(currentTime, dw, dh)) {
                        tokensAnimating = true;
                    }
                }
                final int NEAT = mExitingAppTokens.size();
                for (i=0; i<NEAT; i++) {
                    if (mExitingAppTokens.get(i).stepAnimationLocked(currentTime, dw, dh)) {
                        tokensAnimating = true;
                    }
                }
                if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "*** ANIM STEP: seq="
                        + transactionSequence + " tokensAnimating="
                        + tokensAnimating);
                animating = tokensAnimating;
                boolean tokenMayBeDrawn = false;
                boolean wallpaperMayChange = false;
                boolean forceHiding = false;
                mPolicy.beginAnimationLw(dw, dh);
                final int N = mWindows.size();
                for (i=N-1; i>=0; i--) {
                    WindowState w = (WindowState)mWindows.get(i);
                    final WindowManager.LayoutParams attrs = w.mAttrs;
                    if (w.mSurface != null) {
                        if (w.commitFinishDrawingLocked(currentTime)) {
                            if ((w.mAttrs.flags
                                    & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER) != 0) {
                                if (DEBUG_WALLPAPER) Slog.v(TAG,
                                        "First draw done in potential wallpaper target " + w);
                                wallpaperMayChange = true;
                            }
                        }
                        boolean wasAnimating = w.mAnimating;
                        if (w.stepAnimationLocked(currentTime, dw, dh)) {
                            animating = true;
                        }
                        if (wasAnimating && !w.mAnimating && mWallpaperTarget == w) {
                            wallpaperMayChange = true;
                        }
                        if (mPolicy.doesForceHide(w, attrs)) {
                            if (!wasAnimating && animating) {
                                if (DEBUG_VISIBILITY) Slog.v(TAG,
                                        "Animation done that could impact force hide: "
                                        + w);
                                wallpaperForceHidingChanged = true;
                                mFocusMayChange = true;
                            } else if (w.isReadyForDisplay() && w.mAnimation == null) {
                                forceHiding = true;
                            }
                        } else if (mPolicy.canBeForceHidden(w, attrs)) {
                            boolean changed;
                            if (forceHiding) {
                                changed = w.hideLw(false, false);
                                if (DEBUG_VISIBILITY && changed) Slog.v(TAG,
                                        "Now policy hidden: " + w);
                            } else {
                                changed = w.showLw(false, false);
                                if (DEBUG_VISIBILITY && changed) Slog.v(TAG,
                                        "Now policy shown: " + w);
                                if (changed) {
                                    if (wallpaperForceHidingChanged
                                            && w.isVisibleNow() ) {
                                        Animation a = mPolicy.createForceHideEnterAnimation();
                                        if (a != null) {
                                            w.setAnimation(a);
                                        }
                                    }
                                    if (mCurrentFocus == null ||
                                            mCurrentFocus.mLayer < w.mLayer) {
                                        mFocusMayChange = true;
                                    }
                                }
                            }
                            if (changed && (attrs.flags
                                    & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER) != 0) {
                                wallpaperMayChange = true;
                            }
                        }
                        mPolicy.animatingWindowLw(w, attrs);
                    }
                    final AppWindowToken atoken = w.mAppToken;
                    if (atoken != null && (!atoken.allDrawn || atoken.freezingScreen)) {
                        if (atoken.lastTransactionSequence != transactionSequence) {
                            atoken.lastTransactionSequence = transactionSequence;
                            atoken.numInterestingWindows = atoken.numDrawnWindows = 0;
                            atoken.startingDisplayed = false;
                        }
                        if ((w.isOnScreen() || w.mAttrs.type
                                == WindowManager.LayoutParams.TYPE_BASE_APPLICATION)
                                && !w.mExiting && !w.mDestroying) {
                            if (DEBUG_VISIBILITY || DEBUG_ORIENTATION) {
                                Slog.v(TAG, "Eval win " + w + ": isDrawn="
                                        + w.isDrawnLw()
                                        + ", isAnimating=" + w.isAnimating());
                                if (!w.isDrawnLw()) {
                                    Slog.v(TAG, "Not displayed: s=" + w.mSurface
                                            + " pv=" + w.mPolicyVisibility
                                            + " dp=" + w.mDrawPending
                                            + " cdp=" + w.mCommitDrawPending
                                            + " ah=" + w.mAttachedHidden
                                            + " th=" + atoken.hiddenRequested
                                            + " a=" + w.mAnimating);
                                }
                            }
                            if (w != atoken.startingWindow) {
                                if (!atoken.freezingScreen || !w.mAppFreezing) {
                                    atoken.numInterestingWindows++;
                                    if (w.isDrawnLw()) {
                                        atoken.numDrawnWindows++;
                                        if (DEBUG_VISIBILITY || DEBUG_ORIENTATION) Slog.v(TAG,
                                                "tokenMayBeDrawn: " + atoken
                                                + " freezingScreen=" + atoken.freezingScreen
                                                + " mAppFreezing=" + w.mAppFreezing);
                                        tokenMayBeDrawn = true;
                                    }
                                }
                            } else if (w.isDrawnLw()) {
                                atoken.startingDisplayed = true;
                            }
                        }
                    } else if (w.mReadyToShow) {
                        w.performShowLocked();
                    }
                }
                changes |= mPolicy.finishAnimationLw();
                if (tokenMayBeDrawn) {
                    final int NT = mTokenList.size();
                    for (i=0; i<NT; i++) {
                        AppWindowToken wtoken = mTokenList.get(i).appWindowToken;
                        if (wtoken == null) {
                            continue;
                        }
                        if (wtoken.freezingScreen) {
                            int numInteresting = wtoken.numInterestingWindows;
                            if (numInteresting > 0 && wtoken.numDrawnWindows >= numInteresting) {
                                if (DEBUG_VISIBILITY) Slog.v(TAG,
                                        "allDrawn: " + wtoken
                                        + " interesting=" + numInteresting
                                        + " drawn=" + wtoken.numDrawnWindows);
                                wtoken.showAllWindowsLocked();
                                unsetAppFreezingScreenLocked(wtoken, false, true);
                                orientationChangeComplete = true;
                            }
                        } else if (!wtoken.allDrawn) {
                            int numInteresting = wtoken.numInterestingWindows;
                            if (numInteresting > 0 && wtoken.numDrawnWindows >= numInteresting) {
                                if (DEBUG_VISIBILITY) Slog.v(TAG,
                                        "allDrawn: " + wtoken
                                        + " interesting=" + numInteresting
                                        + " drawn=" + wtoken.numDrawnWindows);
                                wtoken.allDrawn = true;
                                changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_ANIM;
                                if (!mOpeningApps.contains(wtoken)) {
                                    wtoken.showAllWindowsLocked();
                                }
                            }
                        }
                    }
                }
                if (mAppTransitionReady) {
                    int NN = mOpeningApps.size();
                    boolean goodToGo = true;
                    if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                            "Checking " + NN + " opening apps (frozen="
                            + mDisplayFrozen + " timeout="
                            + mAppTransitionTimeout + ")...");
                    if (!mDisplayFrozen && !mAppTransitionTimeout) {
                        for (i=0; i<NN && goodToGo; i++) {
                            AppWindowToken wtoken = mOpeningApps.get(i);
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "Check opening app" + wtoken + ": allDrawn="
                                    + wtoken.allDrawn + " startingDisplayed="
                                    + wtoken.startingDisplayed);
                            if (!wtoken.allDrawn && !wtoken.startingDisplayed
                                    && !wtoken.startingMoved) {
                                goodToGo = false;
                            }
                        }
                    }
                    if (goodToGo) {
                        if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "**** GOOD TO GO");
                        int transit = mNextAppTransition;
                        if (mSkipAppTransitionAnimation) {
                            transit = WindowManagerPolicy.TRANSIT_UNSET;
                        }
                        mNextAppTransition = WindowManagerPolicy.TRANSIT_UNSET;
                        mAppTransitionReady = false;
                        mAppTransitionRunning = true;
                        mAppTransitionTimeout = false;
                        mStartingIconInTransition = false;
                        mSkipAppTransitionAnimation = false;
                        mH.removeMessages(H.APP_TRANSITION_TIMEOUT);
                        if (mToTopApps.size() > 0) {
                            NN = mAppTokens.size();
                            for (i=0; i<NN; i++) {
                                AppWindowToken wtoken = mAppTokens.get(i);
                                if (wtoken.sendingToTop) {
                                    wtoken.sendingToTop = false;
                                    moveAppWindowsLocked(wtoken, NN, false);
                                }
                            }
                            mToTopApps.clear();
                        }
                        WindowState oldWallpaper = mWallpaperTarget;
                        adjustWallpaperWindowsLocked();
                        wallpaperMayChange = false;
                        LayoutParams animLp = null;
                        AppWindowToken animToken = null;
                        int bestAnimLayer = -1;
                        if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                "New wallpaper target=" + mWallpaperTarget
                                + ", lower target=" + mLowerWallpaperTarget
                                + ", upper target=" + mUpperWallpaperTarget);
                        int foundWallpapers = 0;
                        final int NC = mClosingApps.size();
                        NN = NC + mOpeningApps.size();
                        for (i=0; i<NN; i++) {
                            AppWindowToken wtoken;
                            int mode;
                            if (i < NC) {
                                wtoken = mClosingApps.get(i);
                                mode = 1;
                            } else {
                                wtoken = mOpeningApps.get(i-NC);
                                mode = 2;
                            }
                            if (mLowerWallpaperTarget != null) {
                                if (mLowerWallpaperTarget.mAppToken == wtoken
                                        || mUpperWallpaperTarget.mAppToken == wtoken) {
                                    foundWallpapers |= mode;
                                }
                            }
                            if (wtoken.appFullscreen) {
                                WindowState ws = wtoken.findMainWindow();
                                if (ws != null) {
                                    if ((ws.mAttrs.flags&FLAG_COMPATIBLE_WINDOW) != 0) {
                                        animLp = ws.mAttrs;
                                        animToken = ws.mAppToken;
                                        bestAnimLayer = Integer.MAX_VALUE;
                                    } else if (ws.mLayer > bestAnimLayer) {
                                        animLp = ws.mAttrs;
                                        animToken = ws.mAppToken;
                                        bestAnimLayer = ws.mLayer;
                                    }
                                }
                            }
                        }
                        if (foundWallpapers == 3) {
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "Wallpaper animation!");
                            switch (transit) {
                                case WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN:
                                case WindowManagerPolicy.TRANSIT_TASK_OPEN:
                                case WindowManagerPolicy.TRANSIT_TASK_TO_FRONT:
                                    transit = WindowManagerPolicy.TRANSIT_WALLPAPER_INTRA_OPEN;
                                    break;
                                case WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE:
                                case WindowManagerPolicy.TRANSIT_TASK_CLOSE:
                                case WindowManagerPolicy.TRANSIT_TASK_TO_BACK:
                                    transit = WindowManagerPolicy.TRANSIT_WALLPAPER_INTRA_CLOSE;
                                    break;
                            }
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "New transit: " + transit);
                        } else if (oldWallpaper != null) {
                            transit = WindowManagerPolicy.TRANSIT_WALLPAPER_CLOSE;
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "New transit away from wallpaper: " + transit);
                        } else if (mWallpaperTarget != null) {
                            transit = WindowManagerPolicy.TRANSIT_WALLPAPER_OPEN;
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "New transit into wallpaper: " + transit);
                        }
                        if ((transit&WindowManagerPolicy.TRANSIT_ENTER_MASK) != 0) {
                            mLastEnterAnimToken = animToken;
                            mLastEnterAnimParams = animLp;
                        } else if (mLastEnterAnimParams != null) {
                            animLp = mLastEnterAnimParams;
                            mLastEnterAnimToken = null;
                            mLastEnterAnimParams = null;
                        }
                        if (!mPolicy.allowAppAnimationsLw()) {
                            animLp = null;
                        }
                        NN = mOpeningApps.size();
                        for (i=0; i<NN; i++) {
                            AppWindowToken wtoken = mOpeningApps.get(i);
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "Now opening app" + wtoken);
                            wtoken.reportedVisible = false;
                            wtoken.inPendingTransaction = false;
                            wtoken.animation = null;
                            setTokenVisibilityLocked(wtoken, animLp, true, transit, false);
                            wtoken.updateReportedVisibilityLocked();
                            wtoken.waitingToShow = false;
                            wtoken.showAllWindowsLocked();
                        }
                        NN = mClosingApps.size();
                        for (i=0; i<NN; i++) {
                            AppWindowToken wtoken = mClosingApps.get(i);
                            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG,
                                    "Now closing app" + wtoken);
                            wtoken.inPendingTransaction = false;
                            wtoken.animation = null;
                            setTokenVisibilityLocked(wtoken, animLp, false, transit, false);
                            wtoken.updateReportedVisibilityLocked();
                            wtoken.waitingToHide = false;
                            wtoken.allDrawn = true;
                        }
                        mNextAppTransitionPackage = null;
                        mOpeningApps.clear();
                        mClosingApps.clear();
                        changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_LAYOUT;
                        mLayoutNeeded = true;
                        if (!moveInputMethodWindowsIfNeededLocked(true)) {
                            assignLayersLocked();
                        }
                        updateFocusedWindowLocked(UPDATE_FOCUS_PLACING_SURFACES);
                        mFocusMayChange = false;
                    }
                }
                int adjResult = 0;
                if (!animating && mAppTransitionRunning) {
                    mAppTransitionRunning = false;
                    mToBottomApps.clear();
                    rebuildAppWindowListLocked();
                    changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_LAYOUT;
                    adjResult |= ADJUST_WALLPAPER_LAYERS_CHANGED;
                    moveInputMethodWindowsIfNeededLocked(false);
                    wallpaperMayChange = true;
                    mFocusMayChange = true;
                }
                if (wallpaperForceHidingChanged && changes == 0 && !mAppTransitionReady) {
                    WindowState oldWallpaper = mWallpaperTarget;
                    if (mLowerWallpaperTarget != null
                            && mLowerWallpaperTarget.mAppToken != null) {
                        if (DEBUG_WALLPAPER) Slog.v(TAG,
                                "wallpaperForceHiding changed with lower="
                                + mLowerWallpaperTarget);
                        if (DEBUG_WALLPAPER) Slog.v(TAG,
                                "hidden=" + mLowerWallpaperTarget.mAppToken.hidden +
                                " hiddenRequested=" + mLowerWallpaperTarget.mAppToken.hiddenRequested);
                        if (mLowerWallpaperTarget.mAppToken.hidden) {
                            mLowerWallpaperTarget = mUpperWallpaperTarget = null;
                            changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_ANIM;
                        }
                    }
                    adjResult |= adjustWallpaperWindowsLocked();
                    wallpaperMayChange = false;
                    wallpaperForceHidingChanged = false;
                    if (DEBUG_WALLPAPER) Slog.v(TAG, "****** OLD: " + oldWallpaper
                            + " NEW: " + mWallpaperTarget
                            + " LOWER: " + mLowerWallpaperTarget);
                    if (mLowerWallpaperTarget == null) {
                        forceHiding = false;
                        for (i=N-1; i>=0; i--) {
                            WindowState w = (WindowState)mWindows.get(i);
                            if (w.mSurface != null) {
                                final WindowManager.LayoutParams attrs = w.mAttrs;
                                if (mPolicy.doesForceHide(w, attrs) && w.isVisibleLw()) {
                                    if (DEBUG_FOCUS) Slog.i(TAG, "win=" + w + " force hides other windows");
                                    forceHiding = true;
                                } else if (mPolicy.canBeForceHidden(w, attrs)) {
                                    if (!w.mAnimating) {
                                        w.clearAnimation();
                                    }
                                }
                            }
                        }
                    }
                }
                if (wallpaperMayChange) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Wallpaper may change!  Adjusting");
                    adjResult |= adjustWallpaperWindowsLocked();
                }
                if ((adjResult&ADJUST_WALLPAPER_LAYERS_CHANGED) != 0) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Wallpaper layer changed: assigning layers + relayout");
                    changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_LAYOUT;
                    assignLayersLocked();
                } else if ((adjResult&ADJUST_WALLPAPER_VISIBILITY_CHANGED) != 0) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Wallpaper visibility changed: relayout");
                    changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_LAYOUT;
                }
                if (mFocusMayChange) {
                    mFocusMayChange = false;
                    if (updateFocusedWindowLocked(UPDATE_FOCUS_PLACING_SURFACES)) {
                        changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_ANIM;
                        adjResult = 0;
                    }
                }
                if (mLayoutNeeded) {
                    changes |= PhoneWindowManager.FINISH_LAYOUT_REDO_LAYOUT;
                }
                if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "*** ANIM STEP: changes=0x"
                        + Integer.toHexString(changes));
            } while (changes != 0);
            final boolean someoneLosingFocus = mLosingFocus.size() != 0;
            boolean obscured = false;
            boolean blurring = false;
            boolean dimming = false;
            boolean covered = false;
            boolean syswin = false;
            boolean backgroundFillerShown = false;
            final int N = mWindows.size();
            for (i=N-1; i>=0; i--) {
                WindowState w = (WindowState)mWindows.get(i);
                boolean displayed = false;
                final WindowManager.LayoutParams attrs = w.mAttrs;
                final int attrFlags = attrs.flags;
                if (w.mSurface != null) {
                    w.computeShownFrameLocked();
                    if (localLOGV) Slog.v(
                            TAG, "Placing surface #" + i + " " + w.mSurface
                            + ": new=" + w.mShownFrame + ", old="
                            + w.mLastShownFrame);
                    boolean resize;
                    int width, height;
                    if ((w.mAttrs.flags & w.mAttrs.FLAG_SCALED) != 0) {
                        resize = w.mLastRequestedWidth != w.mRequestedWidth ||
                        w.mLastRequestedHeight != w.mRequestedHeight;
                        width  = w.mRequestedWidth;
                        height = w.mRequestedHeight;
                        w.mLastRequestedWidth = width;
                        w.mLastRequestedHeight = height;
                        w.mLastShownFrame.set(w.mShownFrame);
                        try {
                            if (SHOW_TRANSACTIONS) logSurface(w,
                                    "POS " + w.mShownFrame.left
                                    + ", " + w.mShownFrame.top, null);
                            w.mSurfaceX = w.mShownFrame.left;
                            w.mSurfaceY = w.mShownFrame.top;
                            w.mSurface.setPosition(w.mShownFrame.left, w.mShownFrame.top);
                        } catch (RuntimeException e) {
                            Slog.w(TAG, "Error positioning surface in " + w, e);
                            if (!recoveringMemory) {
                                reclaimSomeSurfaceMemoryLocked(w, "position");
                            }
                        }
                    } else {
                        resize = !w.mLastShownFrame.equals(w.mShownFrame);
                        width = w.mShownFrame.width();
                        height = w.mShownFrame.height();
                        w.mLastShownFrame.set(w.mShownFrame);
                    }
                    if (resize) {
                        if (width < 1) width = 1;
                        if (height < 1) height = 1;
                        if (w.mSurface != null) {
                            try {
                                if (SHOW_TRANSACTIONS) logSurface(w,
                                        "POS " + w.mShownFrame.left + ","
                                        + w.mShownFrame.top + " SIZE "
                                        + w.mShownFrame.width() + "x"
                                        + w.mShownFrame.height(), null);
                                w.mSurfaceResized = true;
                                w.mSurfaceW = width;
                                w.mSurfaceH = height;
                                w.mSurface.setSize(width, height);
                                w.mSurfaceX = w.mShownFrame.left;
                                w.mSurfaceY = w.mShownFrame.top;
                                w.mSurface.setPosition(w.mShownFrame.left,
                                        w.mShownFrame.top);
                            } catch (RuntimeException e) {
                                Slog.e(TAG, "Failure updating surface of " + w
                                        + "size=(" + width + "x" + height
                                        + "), pos=(" + w.mShownFrame.left
                                        + "," + w.mShownFrame.top + ")", e);
                                if (!recoveringMemory) {
                                    reclaimSomeSurfaceMemoryLocked(w, "size");
                                }
                            }
                        }
                    }
                    if (!w.mAppFreezing && w.mLayoutSeq == mLayoutSeq) {
                        w.mContentInsetsChanged =
                            !w.mLastContentInsets.equals(w.mContentInsets);
                        w.mVisibleInsetsChanged =
                            !w.mLastVisibleInsets.equals(w.mVisibleInsets);
                        boolean configChanged =
                            w.mConfiguration != mCurConfiguration
                            && (w.mConfiguration == null
                                    || mCurConfiguration.diff(w.mConfiguration) != 0);
                        if (DEBUG_CONFIGURATION && configChanged) {
                            Slog.v(TAG, "Win " + w + " config changed: "
                                    + mCurConfiguration);
                        }
                        if (localLOGV) Slog.v(TAG, "Resizing " + w
                                + ": configChanged=" + configChanged
                                + " last=" + w.mLastFrame + " frame=" + w.mFrame);
                        if (!w.mLastFrame.equals(w.mFrame)
                                || w.mContentInsetsChanged
                                || w.mVisibleInsetsChanged
                                || w.mSurfaceResized
                                || configChanged) {
                            w.mLastFrame.set(w.mFrame);
                            w.mLastContentInsets.set(w.mContentInsets);
                            w.mLastVisibleInsets.set(w.mVisibleInsets);
                            if (mDisplayFrozen) {
                                if (DEBUG_ORIENTATION) Slog.v(TAG,
                                        "Resizing while display frozen: " + w);
                                w.mOrientationChanging = true;
                                if (!mWindowsFreezingScreen) {
                                    mWindowsFreezingScreen = true;
                                    mH.removeMessages(H.WINDOW_FREEZE_TIMEOUT);
                                    mH.sendMessageDelayed(mH.obtainMessage(
                                            H.WINDOW_FREEZE_TIMEOUT), 2000);
                                }
                            }
                            if (w.mOrientationChanging) {
                                if (DEBUG_ORIENTATION) Slog.v(TAG,
                                        "Orientation start waiting for draw in "
                                        + w + ", surface " + w.mSurface);
                                w.mDrawPending = true;
                                w.mCommitDrawPending = false;
                                w.mReadyToShow = false;
                                if (w.mAppToken != null) {
                                    w.mAppToken.allDrawn = false;
                                }
                            }
                            if (DEBUG_RESIZE || DEBUG_ORIENTATION) Slog.v(TAG,
                                    "Resizing window " + w + " to " + w.mFrame);
                            mResizingWindows.add(w);
                        } else if (w.mOrientationChanging) {
                            if (!w.mDrawPending && !w.mCommitDrawPending) {
                                if (DEBUG_ORIENTATION) Slog.v(TAG,
                                        "Orientation not waiting for draw in "
                                        + w + ", surface " + w.mSurface);
                                w.mOrientationChanging = false;
                            }
                        }
                    }
                    if (w.mAttachedHidden || !w.isReadyForDisplay()) {
                        if (!w.mLastHidden) {
                            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Window hiding: waitingToShow="
                                    + w.mRootToken.waitingToShow + " polvis="
                                    + w.mPolicyVisibility + " atthid="
                                    + w.mAttachedHidden + " tokhid="
                                    + w.mRootToken.hidden + " vis="
                                    + w.mViewVisibility);
                            w.mLastHidden = true;
                            if (SHOW_TRANSACTIONS) logSurface(w,
                                    "HIDE (performLayout)", null);
                            if (w.mSurface != null) {
                                w.mSurfaceShown = false;
                                try {
                                    w.mSurface.hide();
                                } catch (RuntimeException e) {
                                    Slog.w(TAG, "Exception hiding surface in " + w);
                                }
                            }
                            mKeyWaiter.releasePendingPointerLocked(w.mSession);
                        }
                        if (w.mOrientationChanging) {
                            w.mOrientationChanging = false;
                            if (DEBUG_ORIENTATION) Slog.v(TAG,
                                    "Orientation change skips hidden " + w);
                        }
                    } else if (w.mLastLayer != w.mAnimLayer
                            || w.mLastAlpha != w.mShownAlpha
                            || w.mLastDsDx != w.mDsDx
                            || w.mLastDtDx != w.mDtDx
                            || w.mLastDsDy != w.mDsDy
                            || w.mLastDtDy != w.mDtDy
                            || w.mLastHScale != w.mHScale
                            || w.mLastVScale != w.mVScale
                            || w.mLastHidden) {
                        displayed = true;
                        w.mLastAlpha = w.mShownAlpha;
                        w.mLastLayer = w.mAnimLayer;
                        w.mLastDsDx = w.mDsDx;
                        w.mLastDtDx = w.mDtDx;
                        w.mLastDsDy = w.mDsDy;
                        w.mLastDtDy = w.mDtDy;
                        w.mLastHScale = w.mHScale;
                        w.mLastVScale = w.mVScale;
                        if (SHOW_TRANSACTIONS) logSurface(w,
                                "alpha=" + w.mShownAlpha + " layer=" + w.mAnimLayer
                                + " matrix=[" + (w.mDsDx*w.mHScale)
                                + "," + (w.mDtDx*w.mVScale)
                                + "][" + (w.mDsDy*w.mHScale)
                                + "," + (w.mDtDy*w.mVScale) + "]", null);
                        if (w.mSurface != null) {
                            try {
                                w.mSurfaceAlpha = w.mShownAlpha;
                                w.mSurface.setAlpha(w.mShownAlpha);
                                w.mSurfaceLayer = w.mAnimLayer;
                                w.mSurface.setLayer(w.mAnimLayer);
                                w.mSurface.setMatrix(
                                        w.mDsDx*w.mHScale, w.mDtDx*w.mVScale,
                                        w.mDsDy*w.mHScale, w.mDtDy*w.mVScale);
                            } catch (RuntimeException e) {
                                Slog.w(TAG, "Error updating surface in " + w, e);
                                if (!recoveringMemory) {
                                    reclaimSomeSurfaceMemoryLocked(w, "update");
                                }
                            }
                        }
                        if (w.mLastHidden && !w.mDrawPending
                                && !w.mCommitDrawPending
                                && !w.mReadyToShow) {
                            if (SHOW_TRANSACTIONS) logSurface(w,
                                    "SHOW (performLayout)", null);
                            if (DEBUG_VISIBILITY) Slog.v(TAG, "Showing " + w
                                    + " during relayout");
                            if (showSurfaceRobustlyLocked(w)) {
                                w.mHasDrawn = true;
                                w.mLastHidden = false;
                            } else {
                                w.mOrientationChanging = false;
                            }
                        }
                        if (w.mSurface != null) {
                            w.mToken.hasVisible = true;
                        }
                    } else {
                        displayed = true;
                    }
                    if (displayed) {
                        if (!covered) {
                            if (attrs.width == LayoutParams.MATCH_PARENT
                                    && attrs.height == LayoutParams.MATCH_PARENT) {
                                covered = true;
                            }
                        }
                        if (w.mOrientationChanging) {
                            if (w.mDrawPending || w.mCommitDrawPending) {
                                orientationChangeComplete = false;
                                if (DEBUG_ORIENTATION) Slog.v(TAG,
                                        "Orientation continue waiting for draw in " + w);
                            } else {
                                w.mOrientationChanging = false;
                                if (DEBUG_ORIENTATION) Slog.v(TAG,
                                        "Orientation change complete in " + w);
                            }
                        }
                        w.mToken.hasVisible = true;
                    }
                } else if (w.mOrientationChanging) {
                    if (DEBUG_ORIENTATION) Slog.v(TAG,
                            "Orientation change skips hidden " + w);
                    w.mOrientationChanging = false;
                }
                final boolean canBeSeen = w.isDisplayedLw();
                if (someoneLosingFocus && w == mCurrentFocus && canBeSeen) {
                    focusDisplayed = true;
                }
                final boolean obscuredChanged = w.mObscured != obscured;
                if (!(w.mObscured=obscured)) {
                    if (w.mSurface != null) {
                        if ((attrFlags&FLAG_KEEP_SCREEN_ON) != 0) {
                            holdScreen = w.mSession;
                        }
                        if (!syswin && w.mAttrs.screenBrightness >= 0
                                && screenBrightness < 0) {
                            screenBrightness = w.mAttrs.screenBrightness;
                        }
                        if (!syswin && w.mAttrs.buttonBrightness >= 0
                                && buttonBrightness < 0) {
                            buttonBrightness = w.mAttrs.buttonBrightness;
                        }
                        if (canBeSeen
                                && (attrs.type == WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG
                                 || attrs.type == WindowManager.LayoutParams.TYPE_KEYGUARD
                                 || attrs.type == WindowManager.LayoutParams.TYPE_SYSTEM_ERROR)) {
                            syswin = true;
                        }
                    }
                    boolean opaqueDrawn = canBeSeen && w.isOpaqueDrawn();
                    if (opaqueDrawn && w.isFullscreen(dw, dh)) {
                        obscured = true;
                    } else if (opaqueDrawn && w.needsBackgroundFiller(dw, dh)) {
                        if (SHOW_TRANSACTIONS) Slog.d(TAG, "showing background filler");
                        obscured = true;
                        if (mBackgroundFillerSurface == null) {
                            try {
                                mBackgroundFillerSurface = new Surface(mFxSession, 0,
                                        "BackGroundFiller",
                                        0, dw, dh,
                                        PixelFormat.OPAQUE,
                                        Surface.FX_SURFACE_NORMAL);
                            } catch (Exception e) {
                                Slog.e(TAG, "Exception creating filler surface", e);
                            }
                        }
                        try {
                            mBackgroundFillerSurface.setPosition(0, 0);
                            mBackgroundFillerSurface.setSize(dw, dh);
                            mBackgroundFillerSurface.setLayer(w.mAnimLayer - 1);
                            mBackgroundFillerSurface.show();
                        } catch (RuntimeException e) {
                            Slog.e(TAG, "Exception showing filler surface");
                        }
                        backgroundFillerShown = true;
                        mBackgroundFillerShown = true;
                    } else if (canBeSeen && !obscured &&
                            (attrFlags&FLAG_BLUR_BEHIND|FLAG_DIM_BEHIND) != 0) {
                        if (localLOGV) Slog.v(TAG, "Win " + w
                                + ": blurring=" + blurring
                                + " obscured=" + obscured
                                + " displayed=" + displayed);
                        if ((attrFlags&FLAG_DIM_BEHIND) != 0) {
                            if (!dimming) {
                                dimming = true;
                                if (mDimAnimator == null) {
                                    mDimAnimator = new DimAnimator(mFxSession);
                                }
                                mDimAnimator.show(dw, dh);
                                mDimAnimator.updateParameters(w, currentTime);
                            }
                        }
                        if ((attrFlags&FLAG_BLUR_BEHIND) != 0) {
                            if (!blurring) {
                                blurring = true;
                                if (mBlurSurface == null) {
                                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "  BLUR "
                                            + mBlurSurface + ": CREATE");
                                    try {
                                        mBlurSurface = new Surface(mFxSession, 0,
                                                "BlurSurface",
                                                -1, 16, 16,
                                                PixelFormat.OPAQUE,
                                                Surface.FX_SURFACE_BLUR);
                                    } catch (Exception e) {
                                        Slog.e(TAG, "Exception creating Blur surface", e);
                                    }
                                }
                                if (mBlurSurface != null) {
                                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "  BLUR "
                                            + mBlurSurface + ": pos=(0,0) (" +
                                            dw + "x" + dh + "), layer=" + (w.mAnimLayer-1));
                                    mBlurSurface.setPosition(0, 0);
                                    mBlurSurface.setSize(dw, dh);
                                    mBlurSurface.setLayer(w.mAnimLayer-2);
                                    if (!mBlurShown) {
                                        try {
                                            if (SHOW_TRANSACTIONS) Slog.i(TAG, "  BLUR "
                                                    + mBlurSurface + ": SHOW");
                                            mBlurSurface.show();
                                        } catch (RuntimeException e) {
                                            Slog.w(TAG, "Failure showing blur surface", e);
                                        }
                                        mBlurShown = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (obscuredChanged && mWallpaperTarget == w) {
                    updateWallpaperVisibilityLocked();
                }
            }
            if (backgroundFillerShown == false && mBackgroundFillerShown) {
                mBackgroundFillerShown = false;
                if (SHOW_TRANSACTIONS) Slog.d(TAG, "hiding background filler");
                try {
                    mBackgroundFillerSurface.hide();
                } catch (RuntimeException e) {
                    Slog.e(TAG, "Exception hiding filler surface", e);
                }
            }
            if (mDimAnimator != null && mDimAnimator.mDimShown) {
                animating |= mDimAnimator.updateSurface(dimming, currentTime,
                        mDisplayFrozen || !mPolicy.isScreenOn());
            }
            if (!blurring && mBlurShown) {
                if (SHOW_TRANSACTIONS) Slog.i(TAG, "  BLUR " + mBlurSurface
                        + ": HIDE");
                try {
                    mBlurSurface.hide();
                } catch (IllegalArgumentException e) {
                    Slog.w(TAG, "Illegal argument exception hiding blur surface");
                }
                mBlurShown = false;
            }
            if (SHOW_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION");
        } catch (RuntimeException e) {
            Slog.e(TAG, "Unhandled exception in Window Manager", e);
        }
        Surface.closeTransaction();
        if (DEBUG_ORIENTATION && mDisplayFrozen) Slog.v(TAG,
                "With display frozen, orientationChangeComplete="
                + orientationChangeComplete);
        if (orientationChangeComplete) {
            if (mWindowsFreezingScreen) {
                mWindowsFreezingScreen = false;
                mH.removeMessages(H.WINDOW_FREEZE_TIMEOUT);
            }
            stopFreezingDisplayLocked();
        }
        i = mResizingWindows.size();
        if (i > 0) {
            do {
                i--;
                WindowState win = mResizingWindows.get(i);
                try {
                    if (DEBUG_RESIZE || DEBUG_ORIENTATION) Slog.v(TAG,
                            "Reporting new frame to " + win + ": " + win.mFrame);
                    int diff = 0;
                    boolean configChanged =
                        win.mConfiguration != mCurConfiguration
                        && (win.mConfiguration == null
                                || (diff=mCurConfiguration.diff(win.mConfiguration)) != 0);
                    if ((DEBUG_RESIZE || DEBUG_ORIENTATION || DEBUG_CONFIGURATION)
                            && configChanged) {
                        Slog.i(TAG, "Sending new config to window " + win + ": "
                                + win.mFrame.width() + "x" + win.mFrame.height()
                                + " / " + mCurConfiguration + " / 0x"
                                + Integer.toHexString(diff));
                    }
                    win.mConfiguration = mCurConfiguration;
                    win.mClient.resized(win.mFrame.width(),
                            win.mFrame.height(), win.mLastContentInsets,
                            win.mLastVisibleInsets, win.mDrawPending,
                            configChanged ? win.mConfiguration : null);
                    win.mContentInsetsChanged = false;
                    win.mVisibleInsetsChanged = false;
                    win.mSurfaceResized = false;
                } catch (RemoteException e) {
                    win.mOrientationChanging = false;
                }
            } while (i > 0);
            mResizingWindows.clear();
        }
        boolean wallpaperDestroyed = false;
        i = mDestroySurface.size();
        if (i > 0) {
            do {
                i--;
                WindowState win = mDestroySurface.get(i);
                win.mDestroying = false;
                if (mInputMethodWindow == win) {
                    mInputMethodWindow = null;
                }
                if (win == mWallpaperTarget) {
                    wallpaperDestroyed = true;
                }
                win.destroySurfaceLocked();
            } while (i > 0);
            mDestroySurface.clear();
        }
        for (i=mExitingTokens.size()-1; i>=0; i--) {
            WindowToken token = mExitingTokens.get(i);
            if (!token.hasVisible) {
                mExitingTokens.remove(i);
                if (token.windowType == TYPE_WALLPAPER) {
                    mWallpaperTokens.remove(token);
                }
            }
        }
        for (i=mExitingAppTokens.size()-1; i>=0; i--) {
            AppWindowToken token = mExitingAppTokens.get(i);
            if (!token.hasVisible && !mClosingApps.contains(token)) {
                token.animation = null;
                token.animating = false;
                mAppTokens.remove(token);
                mExitingAppTokens.remove(i);
                if (mLastEnterAnimToken == token) {
                    mLastEnterAnimToken = null;
                    mLastEnterAnimParams = null;
                }
            }
        }
        boolean needRelayout = false;
        if (!animating && mAppTransitionRunning) {
            mAppTransitionRunning = false;
            needRelayout = true;
            rebuildAppWindowListLocked();
            assignLayersLocked();
            mToBottomApps.clear();
        }
        if (focusDisplayed) {
            mH.sendEmptyMessage(H.REPORT_LOSING_FOCUS);
        }
        if (wallpaperDestroyed) {
            needRelayout = adjustWallpaperWindowsLocked() != 0;
        }
        if (needRelayout) {
            requestAnimationLocked(0);
        } else if (animating) {
            requestAnimationLocked(currentTime+(1000/60)-SystemClock.uptimeMillis());
        }
        if (DEBUG_FREEZE) Slog.v(TAG, "Layout: mDisplayFrozen=" + mDisplayFrozen
                + " holdScreen=" + holdScreen);
        if (!mDisplayFrozen) {
            mQueue.setHoldScreenLocked(holdScreen != null);
            if (screenBrightness < 0 || screenBrightness > 1.0f) {
                mPowerManager.setScreenBrightnessOverride(-1);
            } else {
                mPowerManager.setScreenBrightnessOverride((int)
                        (screenBrightness * Power.BRIGHTNESS_ON));
            }
            if (buttonBrightness < 0 || buttonBrightness > 1.0f) {
                mPowerManager.setButtonBrightnessOverride(-1);
            } else {
                mPowerManager.setButtonBrightnessOverride((int)
                        (buttonBrightness * Power.BRIGHTNESS_ON));
            }
            if (holdScreen != mHoldingScreenOn) {
                mHoldingScreenOn = holdScreen;
                Message m = mH.obtainMessage(H.HOLD_SCREEN_CHANGED, holdScreen);
                mH.sendMessage(m);
            }
        }
        if (mTurnOnScreen) {
            if (DEBUG_VISIBILITY) Slog.v(TAG, "Turning screen on after layout!");
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false,
                    LocalPowerManager.BUTTON_EVENT, true);
            mTurnOnScreen = false;
        }
        enableScreenIfNeededLocked();
    }
    void requestAnimationLocked(long delay) {
        if (!mAnimationPending) {
            mAnimationPending = true;
            mH.sendMessageDelayed(mH.obtainMessage(H.ANIMATE), delay);
        }
    }
    boolean showSurfaceRobustlyLocked(WindowState win) {
        try {
            if (win.mSurface != null) {
                win.mSurfaceShown = true;
                win.mSurface.show();
                if (win.mTurnOnScreen) {
                    if (DEBUG_VISIBILITY) Slog.v(TAG,
                            "Show surface turning screen on: " + win);
                    win.mTurnOnScreen = false;
                    mTurnOnScreen = true;
                }
            }
            return true;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failure showing surface " + win.mSurface + " in " + win);
        }
        reclaimSomeSurfaceMemoryLocked(win, "show");
        return false;
    }
    void reclaimSomeSurfaceMemoryLocked(WindowState win, String operation) {
        final Surface surface = win.mSurface;
        EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, win.toString(),
                win.mSession.mPid, operation);
        if (mForceRemoves == null) {
            mForceRemoves = new ArrayList<WindowState>();
        }
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            int N = mWindows.size();
            boolean leakedSurface = false;
            Slog.i(TAG, "Out of memory for surface!  Looking for leaks...");
            for (int i=0; i<N; i++) {
                WindowState ws = (WindowState)mWindows.get(i);
                if (ws.mSurface != null) {
                    if (!mSessions.contains(ws.mSession)) {
                        Slog.w(TAG, "LEAKED SURFACE (session doesn't exist): "
                                + ws + " surface=" + ws.mSurface
                                + " token=" + win.mToken
                                + " pid=" + ws.mSession.mPid
                                + " uid=" + ws.mSession.mUid);
                        ws.mSurface.destroy();
                        ws.mSurfaceShown = false;
                        ws.mSurface = null;
                        mForceRemoves.add(ws);
                        i--;
                        N--;
                        leakedSurface = true;
                    } else if (win.mAppToken != null && win.mAppToken.clientHidden) {
                        Slog.w(TAG, "LEAKED SURFACE (app token hidden): "
                                + ws + " surface=" + ws.mSurface
                                + " token=" + win.mAppToken);
                        ws.mSurface.destroy();
                        ws.mSurfaceShown = false;
                        ws.mSurface = null;
                        leakedSurface = true;
                    }
                }
            }
            boolean killedApps = false;
            if (!leakedSurface) {
                Slog.w(TAG, "No leaked surfaces; killing applicatons!");
                SparseIntArray pidCandidates = new SparseIntArray();
                for (int i=0; i<N; i++) {
                    WindowState ws = (WindowState)mWindows.get(i);
                    if (ws.mSurface != null) {
                        pidCandidates.append(ws.mSession.mPid, ws.mSession.mPid);
                    }
                }
                if (pidCandidates.size() > 0) {
                    int[] pids = new int[pidCandidates.size()];
                    for (int i=0; i<pids.length; i++) {
                        pids[i] = pidCandidates.keyAt(i);
                    }
                    try {
                        if (mActivityManager.killPids(pids, "Free memory")) {
                            killedApps = true;
                        }
                    } catch (RemoteException e) {
                    }
                }
            }
            if (leakedSurface || killedApps) {
                Slog.w(TAG, "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surface != null) {
                    surface.destroy();
                    win.mSurfaceShown = false;
                    win.mSurface = null;
                }
                try {
                    win.mClient.dispatchGetNewSurface();
                } catch (RemoteException e) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }
    private boolean updateFocusedWindowLocked(int mode) {
        WindowState newFocus = computeFocusedWindowLocked();
        if (mCurrentFocus != newFocus) {
            mH.removeMessages(H.REPORT_FOCUS_CHANGE);
            mH.sendEmptyMessage(H.REPORT_FOCUS_CHANGE);
            if (localLOGV) Slog.v(
                TAG, "Changing focus from " + mCurrentFocus + " to " + newFocus);
            final WindowState oldFocus = mCurrentFocus;
            mCurrentFocus = newFocus;
            mLosingFocus.remove(newFocus);
            final WindowState imWindow = mInputMethodWindow;
            if (newFocus != imWindow && oldFocus != imWindow) {
                if (moveInputMethodWindowsIfNeededLocked(
                        mode != UPDATE_FOCUS_WILL_ASSIGN_LAYERS &&
                        mode != UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                    mLayoutNeeded = true;
                }
                if (mode == UPDATE_FOCUS_PLACING_SURFACES) {
                    performLayoutLockedInner();
                } else if (mode == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                    assignLayersLocked();
                }
            }
            if (newFocus != null && mode != UPDATE_FOCUS_WILL_ASSIGN_LAYERS) {
                mKeyWaiter.handleNewWindowLocked(newFocus);
            }
            return true;
        }
        return false;
    }
    private WindowState computeFocusedWindowLocked() {
        WindowState result = null;
        WindowState win;
        int i = mWindows.size() - 1;
        int nextAppIndex = mAppTokens.size()-1;
        WindowToken nextApp = nextAppIndex >= 0
            ? mAppTokens.get(nextAppIndex) : null;
        while (i >= 0) {
            win = (WindowState)mWindows.get(i);
            if (localLOGV || DEBUG_FOCUS) Slog.v(
                TAG, "Looking for focus: " + i
                + " = " + win
                + ", flags=" + win.mAttrs.flags
                + ", canReceive=" + win.canReceiveKeys());
            AppWindowToken thisApp = win.mAppToken;
            if (thisApp != null && thisApp.removed) {
                i--;
                continue;
            }
            if (thisApp != null && nextApp != null && thisApp != nextApp
                    && win.mAttrs.type != TYPE_APPLICATION_STARTING) {
                int origAppIndex = nextAppIndex;
                while (nextAppIndex > 0) {
                    if (nextApp == mFocusedApp) {
                        if (localLOGV || DEBUG_FOCUS) Slog.v(
                            TAG, "Reached focused app: " + mFocusedApp);
                        return null;
                    }
                    nextAppIndex--;
                    nextApp = mAppTokens.get(nextAppIndex);
                    if (nextApp == thisApp) {
                        break;
                    }
                }
                if (thisApp != nextApp) {
                    nextAppIndex = origAppIndex;
                    nextApp = mAppTokens.get(nextAppIndex);
                }
            }
            if (win.canReceiveKeys()) {
                if (DEBUG_FOCUS) Slog.v(
                        TAG, "Found focus @ " + i + " = " + win);
                result = win;
                break;
            }
            i--;
        }
        return result;
    }
    private void startFreezingDisplayLocked() {
        if (mDisplayFrozen) {
            synchronized (mKeyWaiter) {
                mKeyWaiter.mWasFrozen = true;
            }
            return;
        }
        mScreenFrozenLock.acquire();
        long now = SystemClock.uptimeMillis();
        if (mFreezeGcPending != 0) {
            if (now > (mFreezeGcPending+1000)) {
                mH.removeMessages(H.FORCE_GC);
                Runtime.getRuntime().gc();
                mFreezeGcPending = now;
            }
        } else {
            mFreezeGcPending = now;
        }
        if (DEBUG_FREEZE) Slog.v(TAG, "*** FREEZING DISPLAY", new RuntimeException());
        mDisplayFrozen = true;
        if (mNextAppTransition != WindowManagerPolicy.TRANSIT_UNSET) {
            mNextAppTransition = WindowManagerPolicy.TRANSIT_UNSET;
            mNextAppTransitionPackage = null;
            mAppTransitionReady = true;
        }
        if (PROFILE_ORIENTATION) {
            File file = new File("/data/system/frozen");
            Debug.startMethodTracing(file.toString(), 8 * 1024 * 1024);
        }
        Surface.freezeDisplay(0);
    }
    private void stopFreezingDisplayLocked() {
        if (!mDisplayFrozen) {
            return;
        }
        if (mWaitingForConfig || mAppsFreezingScreen > 0 || mWindowsFreezingScreen) {
            return;
        }
        if (DEBUG_FREEZE) Slog.v(TAG, "*** UNFREEZING DISPLAY", new RuntimeException());
        mDisplayFrozen = false;
        mH.removeMessages(H.APP_FREEZE_TIMEOUT);
        if (PROFILE_ORIENTATION) {
            Debug.stopMethodTracing();
        }
        Surface.unfreezeDisplay(0);
        synchronized (mKeyWaiter) {
            mKeyWaiter.mWasFrozen = true;
            mKeyWaiter.notifyAll();
        }
        if (updateOrientationFromAppTokensLocked()) {
            mH.sendEmptyMessage(H.SEND_NEW_CONFIGURATION);
        }
        mH.removeMessages(H.FORCE_GC);
        mH.sendMessageDelayed(mH.obtainMessage(H.FORCE_GC),
                2000);
        mScreenFrozenLock.release();
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission("android.permission.DUMP")
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump WindowManager from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Input State:");
        mQueue.dump(pw, "  ");
        pw.println(" ");
        synchronized(mWindowMap) {
            pw.println("Current Window Manager state:");
            for (int i=mWindows.size()-1; i>=0; i--) {
                WindowState w = (WindowState)mWindows.get(i);
                pw.print("  Window #"); pw.print(i); pw.print(' ');
                        pw.print(w); pw.println(":");
                w.dump(pw, "    ");
            }
            if (mInputMethodDialogs.size() > 0) {
                pw.println(" ");
                pw.println("  Input method dialogs:");
                for (int i=mInputMethodDialogs.size()-1; i>=0; i--) {
                    WindowState w = mInputMethodDialogs.get(i);
                    pw.print("  IM Dialog #"); pw.print(i); pw.print(": "); pw.println(w);
                }
            }
            if (mPendingRemove.size() > 0) {
                pw.println(" ");
                pw.println("  Remove pending for:");
                for (int i=mPendingRemove.size()-1; i>=0; i--) {
                    WindowState w = mPendingRemove.get(i);
                    pw.print("  Remove #"); pw.print(i); pw.print(' ');
                            pw.print(w); pw.println(":");
                    w.dump(pw, "    ");
                }
            }
            if (mForceRemoves != null && mForceRemoves.size() > 0) {
                pw.println(" ");
                pw.println("  Windows force removing:");
                for (int i=mForceRemoves.size()-1; i>=0; i--) {
                    WindowState w = mForceRemoves.get(i);
                    pw.print("  Removing #"); pw.print(i); pw.print(' ');
                            pw.print(w); pw.println(":");
                    w.dump(pw, "    ");
                }
            }
            if (mDestroySurface.size() > 0) {
                pw.println(" ");
                pw.println("  Windows waiting to destroy their surface:");
                for (int i=mDestroySurface.size()-1; i>=0; i--) {
                    WindowState w = mDestroySurface.get(i);
                    pw.print("  Destroy #"); pw.print(i); pw.print(' ');
                            pw.print(w); pw.println(":");
                    w.dump(pw, "    ");
                }
            }
            if (mLosingFocus.size() > 0) {
                pw.println(" ");
                pw.println("  Windows losing focus:");
                for (int i=mLosingFocus.size()-1; i>=0; i--) {
                    WindowState w = mLosingFocus.get(i);
                    pw.print("  Losing #"); pw.print(i); pw.print(' ');
                            pw.print(w); pw.println(":");
                    w.dump(pw, "    ");
                }
            }
            if (mResizingWindows.size() > 0) {
                pw.println(" ");
                pw.println("  Windows waiting to resize:");
                for (int i=mResizingWindows.size()-1; i>=0; i--) {
                    WindowState w = mResizingWindows.get(i);
                    pw.print("  Resizing #"); pw.print(i); pw.print(' ');
                            pw.print(w); pw.println(":");
                    w.dump(pw, "    ");
                }
            }
            if (mSessions.size() > 0) {
                pw.println(" ");
                pw.println("  All active sessions:");
                Iterator<Session> it = mSessions.iterator();
                while (it.hasNext()) {
                    Session s = it.next();
                    pw.print("  Session "); pw.print(s); pw.println(':');
                    s.dump(pw, "    ");
                }
            }
            if (mTokenMap.size() > 0) {
                pw.println(" ");
                pw.println("  All tokens:");
                Iterator<WindowToken> it = mTokenMap.values().iterator();
                while (it.hasNext()) {
                    WindowToken token = it.next();
                    pw.print("  Token "); pw.print(token.token); pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            if (mTokenList.size() > 0) {
                pw.println(" ");
                pw.println("  Window token list:");
                for (int i=0; i<mTokenList.size(); i++) {
                    pw.print("  #"); pw.print(i); pw.print(": ");
                            pw.println(mTokenList.get(i));
                }
            }
            if (mWallpaperTokens.size() > 0) {
                pw.println(" ");
                pw.println("  Wallpaper tokens:");
                for (int i=mWallpaperTokens.size()-1; i>=0; i--) {
                    WindowToken token = mWallpaperTokens.get(i);
                    pw.print("  Wallpaper #"); pw.print(i);
                            pw.print(' '); pw.print(token); pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            if (mAppTokens.size() > 0) {
                pw.println(" ");
                pw.println("  Application tokens in Z order:");
                for (int i=mAppTokens.size()-1; i>=0; i--) {
                    pw.print("  App #"); pw.print(i); pw.print(": ");
                            pw.println(mAppTokens.get(i));
                }
            }
            if (mFinishedStarting.size() > 0) {
                pw.println(" ");
                pw.println("  Finishing start of application tokens:");
                for (int i=mFinishedStarting.size()-1; i>=0; i--) {
                    WindowToken token = mFinishedStarting.get(i);
                    pw.print("  Finished Starting #"); pw.print(i);
                            pw.print(' '); pw.print(token); pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            if (mExitingTokens.size() > 0) {
                pw.println(" ");
                pw.println("  Exiting tokens:");
                for (int i=mExitingTokens.size()-1; i>=0; i--) {
                    WindowToken token = mExitingTokens.get(i);
                    pw.print("  Exiting #"); pw.print(i);
                            pw.print(' '); pw.print(token); pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            if (mExitingAppTokens.size() > 0) {
                pw.println(" ");
                pw.println("  Exiting application tokens:");
                for (int i=mExitingAppTokens.size()-1; i>=0; i--) {
                    WindowToken token = mExitingAppTokens.get(i);
                    pw.print("  Exiting App #"); pw.print(i);
                            pw.print(' '); pw.print(token); pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            pw.println(" ");
            pw.print("  mCurrentFocus="); pw.println(mCurrentFocus);
            pw.print("  mLastFocus="); pw.println(mLastFocus);
            pw.print("  mFocusedApp="); pw.println(mFocusedApp);
            pw.print("  mInputMethodTarget="); pw.println(mInputMethodTarget);
            pw.print("  mInputMethodWindow="); pw.println(mInputMethodWindow);
            pw.print("  mWallpaperTarget="); pw.println(mWallpaperTarget);
            if (mLowerWallpaperTarget != null && mUpperWallpaperTarget != null) {
                pw.print("  mLowerWallpaperTarget="); pw.println(mLowerWallpaperTarget);
                pw.print("  mUpperWallpaperTarget="); pw.println(mUpperWallpaperTarget);
            }
            pw.print("  mCurConfiguration="); pw.println(this.mCurConfiguration);
            pw.print("  mInTouchMode="); pw.print(mInTouchMode);
                    pw.print(" mLayoutSeq="); pw.println(mLayoutSeq);
            pw.print("  mSystemBooted="); pw.print(mSystemBooted);
                    pw.print(" mDisplayEnabled="); pw.println(mDisplayEnabled);
            pw.print("  mLayoutNeeded="); pw.print(mLayoutNeeded);
                    pw.print(" mBlurShown="); pw.println(mBlurShown);
            if (mDimAnimator != null) {
                mDimAnimator.printTo(pw);
            } else {
                pw.println( "  no DimAnimator ");
            }
            pw.print("  mInputMethodAnimLayerAdjustment=");
                    pw.print(mInputMethodAnimLayerAdjustment);
                    pw.print("  mWallpaperAnimLayerAdjustment=");
                    pw.println(mWallpaperAnimLayerAdjustment);
            pw.print("  mLastWallpaperX="); pw.print(mLastWallpaperX);
                    pw.print(" mLastWallpaperY="); pw.println(mLastWallpaperY);
            pw.print("  mDisplayFrozen="); pw.print(mDisplayFrozen);
                    pw.print(" mWindowsFreezingScreen="); pw.print(mWindowsFreezingScreen);
                    pw.print(" mAppsFreezingScreen="); pw.print(mAppsFreezingScreen);
                    pw.print(" mWaitingForConfig="); pw.println(mWaitingForConfig);
            pw.print("  mRotation="); pw.print(mRotation);
                    pw.print(", mForcedAppOrientation="); pw.print(mForcedAppOrientation);
                    pw.print(", mRequestedRotation="); pw.println(mRequestedRotation);
            pw.print("  mAnimationPending="); pw.print(mAnimationPending);
                    pw.print(" mWindowAnimationScale="); pw.print(mWindowAnimationScale);
                    pw.print(" mTransitionWindowAnimationScale="); pw.println(mTransitionAnimationScale);
            pw.print("  mNextAppTransition=0x");
                    pw.print(Integer.toHexString(mNextAppTransition));
                    pw.print(", mAppTransitionReady="); pw.print(mAppTransitionReady);
                    pw.print(", mAppTransitionRunning="); pw.print(mAppTransitionRunning);
                    pw.print(", mAppTransitionTimeout="); pw.println( mAppTransitionTimeout);
            if (mNextAppTransitionPackage != null) {
                pw.print("  mNextAppTransitionPackage=");
                    pw.print(mNextAppTransitionPackage);
                    pw.print(", mNextAppTransitionEnter=0x");
                    pw.print(Integer.toHexString(mNextAppTransitionEnter));
                    pw.print(", mNextAppTransitionExit=0x");
                    pw.print(Integer.toHexString(mNextAppTransitionExit));
            }
            pw.print("  mStartingIconInTransition="); pw.print(mStartingIconInTransition);
                    pw.print(", mSkipAppTransitionAnimation="); pw.println(mSkipAppTransitionAnimation);
            if (mLastEnterAnimToken != null || mLastEnterAnimToken != null) {
                pw.print("  mLastEnterAnimToken="); pw.print(mLastEnterAnimToken);
                        pw.print(", mLastEnterAnimParams="); pw.println(mLastEnterAnimParams);
            }
            if (mOpeningApps.size() > 0) {
                pw.print("  mOpeningApps="); pw.println(mOpeningApps);
            }
            if (mClosingApps.size() > 0) {
                pw.print("  mClosingApps="); pw.println(mClosingApps);
            }
            if (mToTopApps.size() > 0) {
                pw.print("  mToTopApps="); pw.println(mToTopApps);
            }
            if (mToBottomApps.size() > 0) {
                pw.print("  mToBottomApps="); pw.println(mToBottomApps);
            }
            pw.print("  DisplayWidth="); pw.print(mDisplay.getWidth());
                    pw.print(" DisplayHeight="); pw.println(mDisplay.getHeight());
            pw.println("  KeyWaiter state:");
            pw.print("    mLastWin="); pw.print(mKeyWaiter.mLastWin);
                    pw.print(" mLastBinder="); pw.println(mKeyWaiter.mLastBinder);
            pw.print("    mFinished="); pw.print(mKeyWaiter.mFinished);
                    pw.print(" mGotFirstWindow="); pw.print(mKeyWaiter.mGotFirstWindow);
                    pw.print(" mEventDispatching="); pw.print(mKeyWaiter.mEventDispatching);
                    pw.print(" mTimeToSwitch="); pw.println(mKeyWaiter.mTimeToSwitch);
        }
    }
    public void monitor() {
        synchronized (mWindowMap) { }
        synchronized (mKeyguardTokenWatcher) { }
        synchronized (mKeyWaiter) { }
    }
    public void virtualKeyFeedback(KeyEvent event) {
        mPolicy.keyFeedbackFromInput(event);
    }
    private static class DimAnimator {
        Surface mDimSurface;
        boolean mDimShown = false;
        float mDimCurrentAlpha;
        float mDimTargetAlpha;
        float mDimDeltaPerMs;
        long mLastDimAnimTime;
        int mLastDimWidth, mLastDimHeight;
        DimAnimator (SurfaceSession session) {
            if (mDimSurface == null) {
                if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM "
                        + mDimSurface + ": CREATE");
                try {
                    mDimSurface = new Surface(session, 0,
                            "DimSurface",
                            -1, 16, 16, PixelFormat.OPAQUE,
                            Surface.FX_SURFACE_DIM);
                } catch (Exception e) {
                    Slog.e(TAG, "Exception creating Dim surface", e);
                }
            }
        }
        void show(int dw, int dh) {
            if (!mDimShown) {
                if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM " + mDimSurface + ": SHOW pos=(0,0) (" +
                        dw + "x" + dh + ")");
                mDimShown = true;
                try {
                    mLastDimWidth = dw;
                    mLastDimHeight = dh;
                    mDimSurface.setPosition(0, 0);
                    mDimSurface.setSize(dw, dh);
                    mDimSurface.show();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Failure showing dim surface", e);
                }
            } else if (mLastDimWidth != dw || mLastDimHeight != dh) {
                mLastDimWidth = dw;
                mLastDimHeight = dh;
                mDimSurface.setSize(dw, dh);
            }
        }
        void updateParameters(WindowState w, long currentTime) {
            mDimSurface.setLayer(w.mAnimLayer-1);
            final float target = w.mExiting ? 0 : w.mAttrs.dimAmount;
            if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM " + mDimSurface
                    + ": layer=" + (w.mAnimLayer-1) + " target=" + target);
            if (mDimTargetAlpha != target) {
                mLastDimAnimTime = currentTime;
                long duration = (w.mAnimating && w.mAnimation != null)
                        ? w.mAnimation.computeDurationHint()
                        : DEFAULT_DIM_DURATION;
                if (target > mDimTargetAlpha) {
                    duration *= DIM_DURATION_MULTIPLIER;
                }
                if (duration < 1) {
                    duration = 1;
                }
                mDimTargetAlpha = target;
                mDimDeltaPerMs = (mDimTargetAlpha-mDimCurrentAlpha) / duration;
            }
        }
        boolean updateSurface(boolean dimming, long currentTime, boolean displayFrozen) {
            if (!dimming) {
                if (mDimTargetAlpha != 0) {
                    mLastDimAnimTime = currentTime;
                    mDimTargetAlpha = 0;
                    mDimDeltaPerMs = (-mDimCurrentAlpha) / DEFAULT_DIM_DURATION;
                }
            }
            boolean animating = false;
            if (mLastDimAnimTime != 0) {
                mDimCurrentAlpha += mDimDeltaPerMs
                        * (currentTime-mLastDimAnimTime);
                boolean more = true;
                if (displayFrozen) {
                    more = false;
                } else if (mDimDeltaPerMs > 0) {
                    if (mDimCurrentAlpha > mDimTargetAlpha) {
                        more = false;
                    }
                } else if (mDimDeltaPerMs < 0) {
                    if (mDimCurrentAlpha < mDimTargetAlpha) {
                        more = false;
                    }
                } else {
                    more = false;
                }
                if (more) {
                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM "
                            + mDimSurface + ": alpha=" + mDimCurrentAlpha);
                    mLastDimAnimTime = currentTime;
                    mDimSurface.setAlpha(mDimCurrentAlpha);
                    animating = true;
                } else {
                    mDimCurrentAlpha = mDimTargetAlpha;
                    mLastDimAnimTime = 0;
                    if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM "
                            + mDimSurface + ": final alpha=" + mDimCurrentAlpha);
                    mDimSurface.setAlpha(mDimCurrentAlpha);
                    if (!dimming) {
                        if (SHOW_TRANSACTIONS) Slog.i(TAG, "  DIM " + mDimSurface
                                + ": HIDE");
                        try {
                            mDimSurface.hide();
                        } catch (RuntimeException e) {
                            Slog.w(TAG, "Illegal argument exception hiding dim surface");
                        }
                        mDimShown = false;
                    }
                }
            }
            return animating;
        }
        public void printTo(PrintWriter pw) {
            pw.print("  mDimShown="); pw.print(mDimShown);
            pw.print(" current="); pw.print(mDimCurrentAlpha);
            pw.print(" target="); pw.print(mDimTargetAlpha);
            pw.print(" delta="); pw.print(mDimDeltaPerMs);
            pw.print(" lastAnimTime="); pw.println(mLastDimAnimTime);
        }
    }
    private static class FadeInOutAnimation extends Animation {
        int mWidth;
        boolean mFadeIn;
        public FadeInOutAnimation(boolean fadeIn) {
            setInterpolator(new AccelerateInterpolator());
            setDuration(DEFAULT_FADE_IN_OUT_DURATION);
            mFadeIn = fadeIn;
        }
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float x = interpolatedTime;
            if (!mFadeIn) {
                x = 1.0f - x; 
            }
            if (x < 0.5) {
                t.getMatrix().setTranslate(mWidth, 0);
            } else {
                t.getMatrix().setTranslate(0, 0);
                t.setAlpha((x - 0.5f) * 2);
            }
        }
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            mWidth = width;
        }
        @Override
        public int getZAdjustment() {
            return Animation.ZORDER_TOP;
        }
    }
}
