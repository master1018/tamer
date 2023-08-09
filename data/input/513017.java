public class PhoneWindowManager implements WindowManagerPolicy {
    static final String TAG = "WindowManager";
    static final boolean DEBUG = false;
    static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;
    static final boolean DEBUG_LAYOUT = false;
    static final boolean SHOW_STARTING_ANIMATIONS = true;
    static final boolean SHOW_PROCESSES_ON_ALT_MENU = false;
    static final int WALLPAPER_LAYER = 2;
    static final int APPLICATION_LAYER = 2;
    static final int PHONE_LAYER = 3;
    static final int SEARCH_BAR_LAYER = 4;
    static final int STATUS_BAR_PANEL_LAYER = 5;
    static final int SYSTEM_DIALOG_LAYER = 6;
    static final int TOAST_LAYER = 7;
    static final int STATUS_BAR_LAYER = 8;
    static final int PRIORITY_PHONE_LAYER = 9;
    static final int SYSTEM_ALERT_LAYER = 10;
    static final int SYSTEM_ERROR_LAYER = 11;
    static final int INPUT_METHOD_LAYER = 12;
    static final int INPUT_METHOD_DIALOG_LAYER = 13;
    static final int KEYGUARD_LAYER = 14;
    static final int KEYGUARD_DIALOG_LAYER = 15;
    static final int SYSTEM_OVERLAY_LAYER = 16;
    static final int APPLICATION_MEDIA_SUBLAYER = -2;
    static final int APPLICATION_MEDIA_OVERLAY_SUBLAYER = -1;
    static final int APPLICATION_PANEL_SUBLAYER = 1;
    static final int APPLICATION_SUB_PANEL_SUBLAYER = 2;
    static final float SLIDE_TOUCH_EVENT_SIZE_LIMIT = 0.6f;
    static final boolean KEYBOARD_ALWAYS_HIDDEN = false;
    static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static public final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    final Object mLock = new Object();
    Context mContext;
    IWindowManager mWindowManager;
    LocalPowerManager mPowerManager;
    Vibrator mVibrator; 
    long[] mLongPressVibePattern;
    long[] mVirtualKeyVibePattern;
    long[] mKeyboardTapVibePattern;
    long[] mSafeModeDisabledVibePattern;
    long[] mSafeModeEnabledVibePattern;
    boolean mEnableShiftMenuBugReports = false;
    boolean mSafeMode;
    WindowState mStatusBar = null;
    final ArrayList<WindowState> mStatusBarPanels = new ArrayList<WindowState>();
    WindowState mKeyguard = null;
    KeyguardViewMediator mKeyguardMediator;
    GlobalActions mGlobalActions;
    boolean mShouldTurnOffOnKeyUp;
    RecentApplicationsDialog mRecentAppsDialog;
    Handler mHandler;
    boolean mSystemReady;
    boolean mLidOpen;
    int mUiMode = Configuration.UI_MODE_TYPE_NORMAL;
    int mDockMode = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    int mLidOpenRotation;
    int mCarDockRotation;
    int mDeskDockRotation;
    boolean mCarDockEnablesAccelerometer;
    boolean mDeskDockEnablesAccelerometer;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    boolean mScreenOn = false;
    boolean mOrientationSensorEnabled = false;
    int mCurrentAppOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    static final int DEFAULT_ACCELEROMETER_ROTATION = 0;
    int mAccelerometerDefault = DEFAULT_ACCELEROMETER_ROTATION;
    boolean mHasSoftInput = false;
    int mPointerLocationMode = 0;
    PointerLocationView mPointerLocationView = null;
    int mW, mH;
    int mCurLeft, mCurTop, mCurRight, mCurBottom;
    int mContentLeft, mContentTop, mContentRight, mContentBottom;
    int mDockLeft, mDockTop, mDockRight, mDockBottom;
    int mDockLayer;
    static final Rect mTmpParentFrame = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    WindowState mTopFullscreenOpaqueWindowState;
    boolean mForceStatusBar;
    boolean mHideLockScreen;
    boolean mDismissKeyguard;
    boolean mHomePressed;
    Intent mHomeIntent;
    Intent mCarDockIntent;
    Intent mDeskDockIntent;
    boolean mSearchKeyPressed;
    boolean mConsumeSearchKeyUp;
    boolean mAllowLockscreenWhenOn;
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    int mEndcallBehavior;
    int mIncallPowerBehavior;
    int mLandscapeRotation = -1;
    int mPortraitRotation = -1;
    int mFancyRotationAnimation;
    ShortcutManager mShortcutManager;
    PowerManager.WakeLock mBroadcastWakeLock;
    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }
        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.END_BUTTON_BEHAVIOR), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.ACCELEROMETER_ROTATION), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SCREEN_OFF_TIMEOUT), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.POINTER_LOCATION), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    "fancy_rotation_anim"), false, this);
            updateSettings();
        }
        @Override public void onChange(boolean selfChange) {
            updateSettings();
            try {
                mWindowManager.setRotation(USE_LAST_ROTATION, false,
                        mFancyRotationAnimation);
            } catch (RemoteException e) {
            }
        }
    }
    class MyOrientationListener extends WindowOrientationListener {
        MyOrientationListener(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int rotation) {
            if (localLOGV) Log.v(TAG, "onOrientationChanged, rotation changed to " +rotation);
            try {
                mWindowManager.setRotation(rotation, false,
                        mFancyRotationAnimation);
            } catch (RemoteException e) {
            }
        }                                      
    }
    MyOrientationListener mOrientationListener;
    boolean useSensorForOrientationLp(int appOrientation) {
        if (appOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
            return true;
        }
        if (mAccelerometerDefault != 0 &&
                (appOrientation == ActivityInfo.SCREEN_ORIENTATION_USER
                 || appOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)) {
            return true;
        }
        if ((mCarDockEnablesAccelerometer && mDockMode == Intent.EXTRA_DOCK_STATE_CAR)
                || (mDeskDockEnablesAccelerometer && mDockMode == Intent.EXTRA_DOCK_STATE_DESK)) {
            if (appOrientation == ActivityInfo.SCREEN_ORIENTATION_USER
                    || appOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    || appOrientation == ActivityInfo.SCREEN_ORIENTATION_NOSENSOR) {
                return true;
            }
        }
        return false;
    }
    boolean needSensorRunningLp() {
        if (mCurrentAppOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
            return true;
        }
        if ((mCarDockEnablesAccelerometer && mDockMode == Intent.EXTRA_DOCK_STATE_CAR) ||
            (mDeskDockEnablesAccelerometer && mDockMode == Intent.EXTRA_DOCK_STATE_DESK)) {
            return true;
        }
        if (mAccelerometerDefault == 0) {
            return false;
        }
        return true;
    }
    void updateOrientationListenerLp() {
        if (!mOrientationListener.canDetectOrientation()) {
            return;
        }
        if (localLOGV) Log.v(TAG, "Screen status="+mScreenOn+
                ", current orientation="+mCurrentAppOrientation+
                ", SensorEnabled="+mOrientationSensorEnabled);
        boolean disable = true;
        if (mScreenOn) {
            if (needSensorRunningLp()) {
                disable = false;
                if (!mOrientationSensorEnabled) {
                    mOrientationListener.enable();
                    if(localLOGV) Log.v(TAG, "Enabling listeners");
                    mOrientationSensorEnabled = true;
                }
            } 
        } 
        if (disable && mOrientationSensorEnabled) {
            mOrientationListener.disable();
            if(localLOGV) Log.v(TAG, "Disabling listeners");
            mOrientationSensorEnabled = false;
        }
    }
    Runnable mPowerLongPress = new Runnable() {
        public void run() {
            mShouldTurnOffOnKeyUp = false;
            performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false);
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
            showGlobalActionsDialog();
        }
    };
    void showGlobalActionsDialog() {
        if (mGlobalActions == null) {
            mGlobalActions = new GlobalActions(mContext);
        }
        final boolean keyguardShowing = mKeyguardMediator.isShowingAndNotHidden();
        mGlobalActions.showDialog(keyguardShowing, isDeviceProvisioned());
        if (keyguardShowing) {
            mKeyguardMediator.pokeWakelock();
        }
    }
    boolean isDeviceProvisioned() {
        return Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
    }
    Runnable mHomeLongPress = new Runnable() {
        public void run() {
            mHomePressed = false;
            performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false);
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_RECENT_APPS);
            showRecentAppsDialog();
        }
    };
    void showRecentAppsDialog() {
        if (mRecentAppsDialog == null) {
            mRecentAppsDialog = new RecentApplicationsDialog(mContext);
        }
        mRecentAppsDialog.show();
    }
    public void init(Context context, IWindowManager windowManager,
            LocalPowerManager powerManager) {
        mContext = context;
        mWindowManager = windowManager;
        mPowerManager = powerManager;
        mKeyguardMediator = new KeyguardViewMediator(context, this, powerManager);
        mHandler = new Handler();
        mOrientationListener = new MyOrientationListener(mContext);
        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        mShortcutManager = new ShortcutManager(context, mHandler);
        mShortcutManager.observe();
        mHomeIntent =  new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mCarDockIntent =  new Intent(Intent.ACTION_MAIN, null);
        mCarDockIntent.addCategory(Intent.CATEGORY_CAR_DOCK);
        mCarDockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mDeskDockIntent =  new Intent(Intent.ACTION_MAIN, null);
        mDeskDockIntent.addCategory(Intent.CATEGORY_DESK_DOCK);
        mDeskDockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mBroadcastWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneWindowManager.mBroadcastWakeLock");
        mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        mLidOpenRotation = readRotation(
                com.android.internal.R.integer.config_lidOpenRotation);
        mCarDockRotation = readRotation(
                com.android.internal.R.integer.config_carDockRotation);
        mDeskDockRotation = readRotation(
                com.android.internal.R.integer.config_deskDockRotation);
        mCarDockEnablesAccelerometer = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_carDockEnablesAccelerometer);
        mDeskDockEnablesAccelerometer = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_deskDockEnablesAccelerometer);
        mLidKeyboardAccessibility = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lidKeyboardAccessibility);
        mLidNavigationAccessibility = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lidNavigationAccessibility);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction(Intent.ACTION_DOCK_EVENT);
        Intent intent = context.registerReceiver(mDockReceiver, filter);
        if (intent != null) {
            mDockMode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                    Intent.EXTRA_DOCK_STATE_UNDOCKED);
        }
        mVibrator = new Vibrator();
        mLongPressVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_longPressVibePattern);
        mVirtualKeyVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_virtualKeyVibePattern);
        mKeyboardTapVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_keyboardTapVibePattern);
        mSafeModeDisabledVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_safeModeDisabledVibePattern);
        mSafeModeEnabledVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_safeModeEnabledVibePattern);
    }
    public void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        boolean updateRotation = false;
        View addView = null;
        View removeView = null;
        synchronized (mLock) {
            mEndcallBehavior = Settings.System.getInt(resolver,
                    Settings.System.END_BUTTON_BEHAVIOR,
                    Settings.System.END_BUTTON_BEHAVIOR_DEFAULT);
            mIncallPowerBehavior = Settings.Secure.getInt(resolver,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            mFancyRotationAnimation = Settings.System.getInt(resolver,
                    "fancy_rotation_anim", 0) != 0 ? 0x80 : 0;
            int accelerometerDefault = Settings.System.getInt(resolver,
                    Settings.System.ACCELEROMETER_ROTATION, DEFAULT_ACCELEROMETER_ROTATION);
            if (mAccelerometerDefault != accelerometerDefault) {
                mAccelerometerDefault = accelerometerDefault;
                updateOrientationListenerLp();
            }
            if (mSystemReady) {
                int pointerLocation = Settings.System.getInt(resolver,
                        Settings.System.POINTER_LOCATION, 0);
                if (mPointerLocationMode != pointerLocation) {
                    mPointerLocationMode = pointerLocation;
                    if (pointerLocation != 0) {
                        if (mPointerLocationView == null) {
                            mPointerLocationView = new PointerLocationView(mContext);
                            mPointerLocationView.setPrintCoords(false);
                            addView = mPointerLocationView;
                        }
                    } else {
                        removeView = mPointerLocationView;
                        mPointerLocationView = null;
                    }
                }
            }
            mLockScreenTimeout = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            String imId = Settings.Secure.getString(resolver,
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            boolean hasSoftInput = imId != null && imId.length() > 0;
            if (mHasSoftInput != hasSoftInput) {
                mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
        }
        if (updateRotation) {
            updateRotation(0);
        }
        if (addView != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            lp.flags = 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            lp.format = PixelFormat.TRANSLUCENT;
            lp.setTitle("PointerLocation");
            WindowManagerImpl wm = (WindowManagerImpl)
                    mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(addView, lp);
        }
        if (removeView != null) {
            WindowManagerImpl wm = (WindowManagerImpl)
                    mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(removeView);
        }
    }
    private int readRotation(int resID) {
        try {
            int rotation = mContext.getResources().getInteger(resID);
            switch (rotation) {
                case 0:
                    return Surface.ROTATION_0;
                case 90:
                    return Surface.ROTATION_90;
                case 180:
                    return Surface.ROTATION_180;
                case 270:
                    return Surface.ROTATION_270;
            }
        } catch (Resources.NotFoundException e) {
        }
        return -1;
    }
    public int checkAddPermission(WindowManager.LayoutParams attrs) {
        int type = attrs.type;
        if (type < WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW
                || type > WindowManager.LayoutParams.LAST_SYSTEM_WINDOW) {
            return WindowManagerImpl.ADD_OKAY;
        }
        String permission = null;
        switch (type) {
            case TYPE_TOAST:
                break;
            case TYPE_INPUT_METHOD:
            case TYPE_WALLPAPER:
                break;
            case TYPE_PHONE:
            case TYPE_PRIORITY_PHONE:
            case TYPE_SYSTEM_ALERT:
            case TYPE_SYSTEM_ERROR:
            case TYPE_SYSTEM_OVERLAY:
                permission = android.Manifest.permission.SYSTEM_ALERT_WINDOW;
                break;
            default:
                permission = android.Manifest.permission.INTERNAL_SYSTEM_WINDOW;
        }
        if (permission != null) {
            if (mContext.checkCallingOrSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return WindowManagerImpl.ADD_PERMISSION_DENIED;
            }
        }
        return WindowManagerImpl.ADD_OKAY;
    }
    public void adjustWindowParamsLw(WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case TYPE_SYSTEM_OVERLAY:
            case TYPE_TOAST:
                attrs.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                break;
        }
    }
    void readLidState() {
        try {
            int sw = mWindowManager.getSwitchState(RawInputEvent.SW_LID);
            if (sw >= 0) {
                mLidOpen = sw == 0;
            }
        } catch (RemoteException e) {
        }
    }
    private int determineHiddenState(boolean lidOpen,
            int mode, int hiddenValue, int visibleValue) {
        switch (mode) {
            case 1:
                return lidOpen ? visibleValue : hiddenValue;
            case 2:
                return lidOpen ? hiddenValue : visibleValue;
        }
        return visibleValue;
    }
    public void adjustConfigurationLw(Configuration config) {
        readLidState();
        final boolean lidOpen = !KEYBOARD_ALWAYS_HIDDEN && mLidOpen;
        mPowerManager.setKeyboardVisibility(lidOpen);
        config.hardKeyboardHidden = determineHiddenState(lidOpen,
                mLidKeyboardAccessibility, Configuration.HARDKEYBOARDHIDDEN_YES,
                Configuration.HARDKEYBOARDHIDDEN_NO);
        config.navigationHidden = determineHiddenState(lidOpen,
                mLidNavigationAccessibility, Configuration.NAVIGATIONHIDDEN_YES,
                Configuration.NAVIGATIONHIDDEN_NO);
        config.keyboardHidden = (config.hardKeyboardHidden
                        == Configuration.HARDKEYBOARDHIDDEN_NO || mHasSoftInput)
                ? Configuration.KEYBOARDHIDDEN_NO
                : Configuration.KEYBOARDHIDDEN_YES;
    }
    public boolean isCheekPressedAgainstScreen(MotionEvent ev) {
        if(ev.getSize() > SLIDE_TOUCH_EVENT_SIZE_LIMIT) {
            return true;
        }
        int size = ev.getHistorySize();
        for(int i = 0; i < size; i++) {
            if(ev.getHistoricalSize(i) > SLIDE_TOUCH_EVENT_SIZE_LIMIT) {
                return true;
            }
        }
        return false;
    }
    public void dispatchedPointerEventLw(MotionEvent ev, int targetX, int targetY) {
        if (mPointerLocationView == null) {
            return;
        }
        synchronized (mLock) {
            if (mPointerLocationView == null) {
                return;
            }
            ev.offsetLocation(targetX, targetY);
            mPointerLocationView.addTouchEvent(ev);
            ev.offsetLocation(-targetX, -targetY);
        }
    }
    public int windowTypeToLayerLw(int type) {
        if (type >= FIRST_APPLICATION_WINDOW && type <= LAST_APPLICATION_WINDOW) {
            return APPLICATION_LAYER;
        }
        switch (type) {
        case TYPE_STATUS_BAR:
            return STATUS_BAR_LAYER;
        case TYPE_STATUS_BAR_PANEL:
            return STATUS_BAR_PANEL_LAYER;
        case TYPE_SYSTEM_DIALOG:
            return SYSTEM_DIALOG_LAYER;
        case TYPE_SEARCH_BAR:
            return SEARCH_BAR_LAYER;
        case TYPE_PHONE:
            return PHONE_LAYER;
        case TYPE_KEYGUARD:
            return KEYGUARD_LAYER;
        case TYPE_KEYGUARD_DIALOG:
            return KEYGUARD_DIALOG_LAYER;
        case TYPE_SYSTEM_ALERT:
            return SYSTEM_ALERT_LAYER;
        case TYPE_SYSTEM_ERROR:
            return SYSTEM_ERROR_LAYER;
        case TYPE_INPUT_METHOD:
            return INPUT_METHOD_LAYER;
        case TYPE_INPUT_METHOD_DIALOG:
            return INPUT_METHOD_DIALOG_LAYER;
        case TYPE_SYSTEM_OVERLAY:
            return SYSTEM_OVERLAY_LAYER;
        case TYPE_PRIORITY_PHONE:
            return PRIORITY_PHONE_LAYER;
        case TYPE_TOAST:
            return TOAST_LAYER;
        case TYPE_WALLPAPER:
            return WALLPAPER_LAYER;
        }
        Log.e(TAG, "Unknown window type: " + type);
        return APPLICATION_LAYER;
    }
    public int subWindowTypeToLayerLw(int type) {
        switch (type) {
        case TYPE_APPLICATION_PANEL:
        case TYPE_APPLICATION_ATTACHED_DIALOG:
            return APPLICATION_PANEL_SUBLAYER;
        case TYPE_APPLICATION_MEDIA:
            return APPLICATION_MEDIA_SUBLAYER;
        case TYPE_APPLICATION_MEDIA_OVERLAY:
            return APPLICATION_MEDIA_OVERLAY_SUBLAYER;
        case TYPE_APPLICATION_SUB_PANEL:
            return APPLICATION_SUB_PANEL_SUBLAYER;
        }
        Log.e(TAG, "Unknown sub-window type: " + type);
        return 0;
    }
    public int getMaxWallpaperLayer() {
        return STATUS_BAR_LAYER;
    }
    public boolean doesForceHide(WindowState win, WindowManager.LayoutParams attrs) {
        return attrs.type == WindowManager.LayoutParams.TYPE_KEYGUARD;
    }
    public boolean canBeForceHidden(WindowState win, WindowManager.LayoutParams attrs) {
        return attrs.type != WindowManager.LayoutParams.TYPE_STATUS_BAR
                && attrs.type != WindowManager.LayoutParams.TYPE_WALLPAPER;
    }
    public View addStartingWindow(IBinder appToken, String packageName,
                                  int theme, CharSequence nonLocalizedLabel,
                                  int labelRes, int icon) {
        if (!SHOW_STARTING_ANIMATIONS) {
            return null;
        }
        if (packageName == null) {
            return null;
        }
        try {
        	Context context = mContext;
        	boolean setTheme = false;
        	if (theme != 0 || labelRes != 0) {
        	    try {
        	        context = context.createPackageContext(packageName, 0);
        	        if (theme != 0) {
        	            context.setTheme(theme);
        	            setTheme = true;
        	        }
        	    } catch (PackageManager.NameNotFoundException e) {
                }
        	}
        	if (!setTheme) {
        	    context.setTheme(com.android.internal.R.style.Theme);
        	}
            Window win = PolicyManager.makeNewWindow(context);
            if (win.getWindowStyle().getBoolean(
                    com.android.internal.R.styleable.Window_windowDisablePreview, false)) {
                return null;
            }
            Resources r = context.getResources();
            win.setTitle(r.getText(labelRes, nonLocalizedLabel));
            win.setType(
                WindowManager.LayoutParams.TYPE_APPLICATION_STARTING);
            win.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            win.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.MATCH_PARENT);
            final WindowManager.LayoutParams params = win.getAttributes();
            params.token = appToken;
            params.packageName = packageName;
            params.windowAnimations = win.getWindowStyle().getResourceId(
                    com.android.internal.R.styleable.Window_windowAnimationStyle, 0);
            params.setTitle("Starting " + packageName);
            WindowManagerImpl wm = (WindowManagerImpl)
                    context.getSystemService(Context.WINDOW_SERVICE);
            View view = win.getDecorView();
            if (win.isFloating()) {
                return null;
            }
            if (localLOGV) Log.v(
                TAG, "Adding starting window for " + packageName
                + " / " + appToken + ": "
                + (view.getParent() != null ? view : null));
            wm.addView(view, params);
            return view.getParent() != null ? view : null;
        } catch (WindowManagerImpl.BadTokenException e) {
            Log.w(TAG, appToken + " already running, starting window not displayed");
        } catch (RuntimeException e) {
            Log.w(TAG, appToken + " failed creating starting window", e);
        }
        return null;
    }
    public void removeStartingWindow(IBinder appToken, View window) {
        if (localLOGV) Log.v(
            TAG, "Removing starting window for " + appToken + ": " + window);
        if (window != null) {
            WindowManagerImpl wm = (WindowManagerImpl) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(window);
        }
    }
    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case TYPE_STATUS_BAR:
                if (mStatusBar != null) {
                    return WindowManagerImpl.ADD_MULTIPLE_SINGLETON;
                }
                mStatusBar = win;
                break;
            case TYPE_STATUS_BAR_PANEL:
                mStatusBarPanels.add(win);
                break;
            case TYPE_KEYGUARD:
                if (mKeyguard != null) {
                    return WindowManagerImpl.ADD_MULTIPLE_SINGLETON;
                }
                mKeyguard = win;
                break;
        }
        return WindowManagerImpl.ADD_OKAY;
    }
    public void removeWindowLw(WindowState win) {
        if (mStatusBar == win) {
            mStatusBar = null;
        }
        else if (mKeyguard == win) {
            mKeyguard = null;
        } else {
            mStatusBarPanels.remove(win);
        }
    }
    static final boolean PRINT_ANIM = false;
    public int selectAnimationLw(WindowState win, int transit) {
        if (PRINT_ANIM) Log.i(TAG, "selectAnimation in " + win
              + ": transit=" + transit);
        if (transit == TRANSIT_PREVIEW_DONE) {
            if (win.hasAppShownWindows()) {
                if (PRINT_ANIM) Log.i(TAG, "**** STARTING EXIT");
                return com.android.internal.R.anim.app_starting_exit;
            }
        }
        return 0;
    }
    public Animation createForceHideEnterAnimation() {
        return AnimationUtils.loadAnimation(mContext,
                com.android.internal.R.anim.lock_screen_behind_enter);
    }
    static ITelephony getPhoneInterface() {
        return ITelephony.Stub.asInterface(ServiceManager.checkService(Context.TELEPHONY_SERVICE));
    }
    static IAudioService getAudioInterface() {
        return IAudioService.Stub.asInterface(ServiceManager.checkService(Context.AUDIO_SERVICE));
    }
    boolean keyguardOn() {
        return keyguardIsShowingTq() || inKeyguardRestrictedKeyInputMode();
    }
    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
        };
    public boolean interceptKeyTi(WindowState win, int code, int metaKeys, boolean down, 
            int repeatCount, int flags) {
        boolean keyguardOn = keyguardOn();
        if (false) {
            Log.d(TAG, "interceptKeyTi code=" + code + " down=" + down + " repeatCount="
                    + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + mHomePressed);
        }
        if ((code == KeyEvent.KEYCODE_HOME) && !down) {
            mHandler.removeCallbacks(mHomeLongPress);
        }
        if (mHomePressed) {
            if (code == KeyEvent.KEYCODE_HOME) {
                if (!down) {
                    mHomePressed = false;
                    if ((flags&KeyEvent.FLAG_CANCELED) == 0) {
                        boolean incomingRinging = false;
                        try {
                            ITelephony phoneServ = getPhoneInterface();
                            if (phoneServ != null) {
                                incomingRinging = phoneServ.isRinging();
                            } else {
                                Log.w(TAG, "Unable to find ITelephony interface");
                            }
                        } catch (RemoteException ex) {
                            Log.w(TAG, "RemoteException from getPhoneInterface()", ex);
                        }
                        if (incomingRinging) {
                            Log.i(TAG, "Ignoring HOME; there's a ringing incoming call.");
                        } else {
                            launchHomeFromHotKey();
                        }
                    } else {
                        Log.i(TAG, "Ignoring HOME; event canceled.");
                    }
                }
            }
            return true;
        }
        if (code == KeyEvent.KEYCODE_HOME) {
            WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                final int type = attrs.type;
                if (type == WindowManager.LayoutParams.TYPE_KEYGUARD
                        || type == WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG) {
                    return false;
                }
                final int typeCount = WINDOW_TYPES_WHERE_HOME_DOESNT_WORK.length;
                for (int i=0; i<typeCount; i++) {
                    if (type == WINDOW_TYPES_WHERE_HOME_DOESNT_WORK[i]) {
                        return true;
                    }
                }
            }
            if (down && repeatCount == 0) {
                if (!keyguardOn) {
                    mHandler.postDelayed(mHomeLongPress, ViewConfiguration.getGlobalActionKeyTimeout());
                }
                mHomePressed = true;
            }
            return true;
        } else if (code == KeyEvent.KEYCODE_MENU) {
            final int chordBug = KeyEvent.META_SHIFT_ON;
            if (down && repeatCount == 0) {
                if (mEnableShiftMenuBugReports && (metaKeys & chordBug) == chordBug) {
                    Intent intent = new Intent(Intent.ACTION_BUG_REPORT);
                    mContext.sendOrderedBroadcast(intent, null);
                    return true;
                } else if (SHOW_PROCESSES_ON_ALT_MENU &&
                        (metaKeys & KeyEvent.META_ALT_ON) == KeyEvent.META_ALT_ON) {
                    Intent service = new Intent();
                    service.setClassName(mContext, "com.android.server.LoadAverageService");
                    ContentResolver res = mContext.getContentResolver();
                    boolean shown = Settings.System.getInt(
                            res, Settings.System.SHOW_PROCESSES, 0) != 0;
                    if (!shown) {
                        mContext.startService(service);
                    } else {
                        mContext.stopService(service);
                    }
                    Settings.System.putInt(
                            res, Settings.System.SHOW_PROCESSES, shown ? 0 : 1);
                    return true;
                }
            }
        } else if (code == KeyEvent.KEYCODE_NOTIFICATION) {
            if (down) {
                IStatusBar sbs = IStatusBar.Stub.asInterface(ServiceManager.getService("statusbar"));
                if (sbs != null) {
                    try {
                        sbs.toggle();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return true;
        } else if (code == KeyEvent.KEYCODE_SEARCH) {
            if (down) {
                if (repeatCount == 0) {
                    mSearchKeyPressed = true;
                }
            } else {
                mSearchKeyPressed = false;
                if (mConsumeSearchKeyUp) {
                    mConsumeSearchKeyUp = false;
                    return true;
                }
            }
        }
        if (mSearchKeyPressed) {
            if (down && repeatCount == 0 && !keyguardOn) {
                Intent shortcutIntent = mShortcutManager.getIntent(code, metaKeys);
                if (shortcutIntent != null) {
                    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(shortcutIntent);
                    mConsumeSearchKeyUp = true;
                    return true;
                }
            }
        }
        return false;
    }
    void launchHomeFromHotKey() {
        if (mKeyguardMediator.isShowingAndNotHidden()) {
        } else if (!mHideLockScreen && mKeyguardMediator.isInputRestricted()) {
            mKeyguardMediator.verifyUnlock(new OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    if (success) {
                        try {
                            ActivityManagerNative.getDefault().stopAppSwitches();
                        } catch (RemoteException e) {
                        }
                        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
                        startDockOrHome();
                    }
                }
            });
        } else {
            try {
                ActivityManagerNative.getDefault().stopAppSwitches();
            } catch (RemoteException e) {
            }
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
            startDockOrHome();
        }
    }
    public void getContentInsetHintLw(WindowManager.LayoutParams attrs, Rect contentInset) {
        final int fl = attrs.flags;
        if ((fl &
                (FLAG_LAYOUT_IN_SCREEN | FLAG_FULLSCREEN | FLAG_LAYOUT_INSET_DECOR))
                == (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR)) {
            contentInset.set(mCurLeft, mCurTop, mW - mCurRight, mH - mCurBottom);
        } else {
            contentInset.setEmpty();
        }
    }
    public void beginLayoutLw(int displayWidth, int displayHeight) {
        mW = displayWidth;
        mH = displayHeight;
        mDockLeft = mContentLeft = mCurLeft = 0;
        mDockTop = mContentTop = mCurTop = 0;
        mDockRight = mContentRight = mCurRight = displayWidth;
        mDockBottom = mContentBottom = mCurBottom = displayHeight;
        mDockLayer = 0x10000000;
        if (mStatusBar != null) {
            final Rect pf = mTmpParentFrame;
            final Rect df = mTmpDisplayFrame;
            final Rect vf = mTmpVisibleFrame;
            pf.left = df.left = vf.left = 0;
            pf.top = df.top = vf.top = 0;
            pf.right = df.right = vf.right = displayWidth;
            pf.bottom = df.bottom = vf.bottom = displayHeight;
            mStatusBar.computeFrameLw(pf, df, vf, vf);
            if (mStatusBar.isVisibleLw()) {
                mDockTop = mContentTop = mCurTop = mStatusBar.getFrameLw().bottom;
                if (DEBUG_LAYOUT) Log.v(TAG, "Status bar: mDockBottom="
                        + mDockBottom + " mContentBottom="
                        + mContentBottom + " mCurBottom=" + mCurBottom);
            }
        }
    }
    void setAttachedWindowFrames(WindowState win, int fl, int sim,
            WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect cf, Rect vf) {
        if (win.getSurfaceLayer() > mDockLayer && attached.getSurfaceLayer() < mDockLayer) {
            df.left = cf.left = vf.left = mDockLeft;
            df.top = cf.top = vf.top = mDockTop;
            df.right = cf.right = vf.right = mDockRight;
            df.bottom = cf.bottom = vf.bottom = mDockBottom;
        } else {
            if ((sim & SOFT_INPUT_MASK_ADJUST) != SOFT_INPUT_ADJUST_RESIZE) {
                cf.set(attached.getDisplayFrameLw());
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.getSurfaceLayer() < mDockLayer) {
                    if (cf.left < mContentLeft) cf.left = mContentLeft;
                    if (cf.top < mContentTop) cf.top = mContentTop;
                    if (cf.right > mContentRight) cf.right = mContentRight;
                    if (cf.bottom > mContentBottom) cf.bottom = mContentBottom;
                }
            }
            df.set(insetDecors ? attached.getDisplayFrameLw() : cf);
            vf.set(attached.getVisibleFrameLw());
        }
        pf.set((fl & FLAG_LAYOUT_IN_SCREEN) == 0
                ? attached.getFrameLw() : df);
    }
    public void layoutWindowLw(WindowState win, WindowManager.LayoutParams attrs,
            WindowState attached) {
        if (win == mStatusBar) {
            return;
        }
        if (false) {
            if ("com.google.android.youtube".equals(attrs.packageName)
                    && attrs.type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
                Log.i(TAG, "GOTCHA!");
            }
        }
        final int fl = attrs.flags;
        final int sim = attrs.softInputMode;
        final Rect pf = mTmpParentFrame;
        final Rect df = mTmpDisplayFrame;
        final Rect cf = mTmpContentFrame;
        final Rect vf = mTmpVisibleFrame;
        if (attrs.type == TYPE_INPUT_METHOD) {
            pf.left = df.left = cf.left = vf.left = mDockLeft;
            pf.top = df.top = cf.top = vf.top = mDockTop;
            pf.right = df.right = cf.right = vf.right = mDockRight;
            pf.bottom = df.bottom = cf.bottom = vf.bottom = mDockBottom;
            attrs.gravity = Gravity.BOTTOM;
            mDockLayer = win.getSurfaceLayer();
        } else {
            if ((fl &
                    (FLAG_LAYOUT_IN_SCREEN | FLAG_FULLSCREEN | FLAG_LAYOUT_INSET_DECOR))
                    == (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR)) {
                if (attached != null) {
                    setAttachedWindowFrames(win, fl, sim, attached, true, pf, df, cf, vf);
                } else {
                    pf.left = df.left = 0;
                    pf.top = df.top = 0;
                    pf.right = df.right = mW;
                    pf.bottom = df.bottom = mH;
                    if ((sim & SOFT_INPUT_MASK_ADJUST) != SOFT_INPUT_ADJUST_RESIZE) {
                        cf.left = mDockLeft;
                        cf.top = mDockTop;
                        cf.right = mDockRight;
                        cf.bottom = mDockBottom;
                    } else {
                        cf.left = mContentLeft;
                        cf.top = mContentTop;
                        cf.right = mContentRight;
                        cf.bottom = mContentBottom;
                    }
                    vf.left = mCurLeft;
                    vf.top = mCurTop;
                    vf.right = mCurRight;
                    vf.bottom = mCurBottom;
                }
            } else if ((fl & FLAG_LAYOUT_IN_SCREEN) != 0) {
                pf.left = df.left = cf.left = 0;
                pf.top = df.top = cf.top = 0;
                pf.right = df.right = cf.right = mW;
                pf.bottom = df.bottom = cf.bottom = mH;
                vf.left = mCurLeft;
                vf.top = mCurTop;
                vf.right = mCurRight;
                vf.bottom = mCurBottom;
            } else if (attached != null) {
                setAttachedWindowFrames(win, fl, sim, attached, false, pf, df, cf, vf);
            } else {
                pf.left = mContentLeft;
                pf.top = mContentTop;
                pf.right = mContentRight;
                pf.bottom = mContentBottom;
                if ((sim & SOFT_INPUT_MASK_ADJUST) != SOFT_INPUT_ADJUST_RESIZE) {
                    df.left = cf.left = mDockLeft;
                    df.top = cf.top = mDockTop;
                    df.right = cf.right = mDockRight;
                    df.bottom = cf.bottom = mDockBottom;
                } else {
                    df.left = cf.left = mContentLeft;
                    df.top = cf.top = mContentTop;
                    df.right = cf.right = mContentRight;
                    df.bottom = cf.bottom = mContentBottom;
                }
                vf.left = mCurLeft;
                vf.top = mCurTop;
                vf.right = mCurRight;
                vf.bottom = mCurBottom;
            }
        }
        if ((fl & FLAG_LAYOUT_NO_LIMITS) != 0) {
            df.left = df.top = cf.left = cf.top = vf.left = vf.top = -10000;
            df.right = df.bottom = cf.right = cf.bottom = vf.right = vf.bottom = 10000;
        }
        if (DEBUG_LAYOUT) Log.v(TAG, "Compute frame " + attrs.getTitle()
                + ": sim=#" + Integer.toHexString(sim)
                + " pf=" + pf.toShortString() + " df=" + df.toShortString()
                + " cf=" + cf.toShortString() + " vf=" + vf.toShortString());
        if (false) {
            if ("com.google.android.youtube".equals(attrs.packageName)
                    && attrs.type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
                if (true || localLOGV) Log.v(TAG, "Computing frame of " + win +
                        ": sim=#" + Integer.toHexString(sim)
                        + " pf=" + pf.toShortString() + " df=" + df.toShortString()
                        + " cf=" + cf.toShortString() + " vf=" + vf.toShortString());
            }
        }
        win.computeFrameLw(pf, df, cf, vf);
        if (attrs.type == TYPE_INPUT_METHOD && !win.getGivenInsetsPendingLw()) {
            int top = win.getContentFrameLw().top;
            top += win.getGivenContentInsetsLw().top;
            if (mContentBottom > top) {
                mContentBottom = top;
            }
            top = win.getVisibleFrameLw().top;
            top += win.getGivenVisibleInsetsLw().top;
            if (mCurBottom > top) {
                mCurBottom = top;
            }
            if (DEBUG_LAYOUT) Log.v(TAG, "Input method: mDockBottom="
                    + mDockBottom + " mContentBottom="
                    + mContentBottom + " mCurBottom=" + mCurBottom);
        }
    }
    public int finishLayoutLw() {
        return 0;
    }
    public void beginAnimationLw(int displayWidth, int displayHeight) {
        mTopFullscreenOpaqueWindowState = null;
        mForceStatusBar = false;
        mHideLockScreen = false;
        mAllowLockscreenWhenOn = false;
        mDismissKeyguard = false;
    }
    public void animatingWindowLw(WindowState win,
                                WindowManager.LayoutParams attrs) {
        if (mTopFullscreenOpaqueWindowState == null &&
                win.isVisibleOrBehindKeyguardLw()) {
            if ((attrs.flags & FLAG_FORCE_NOT_FULLSCREEN) != 0) {
                mForceStatusBar = true;
            } 
            if (attrs.type >= FIRST_APPLICATION_WINDOW
                    && attrs.type <= LAST_APPLICATION_WINDOW
                    && win.fillsScreenLw(mW, mH, false, false)) {
                if (DEBUG_LAYOUT) Log.v(TAG, "Fullscreen window: " + win);
                mTopFullscreenOpaqueWindowState = win;
                if ((attrs.flags & FLAG_SHOW_WHEN_LOCKED) != 0) {
                    if (localLOGV) Log.v(TAG, "Setting mHideLockScreen to true by win " + win);
                    mHideLockScreen = true;
                }
                if ((attrs.flags & FLAG_DISMISS_KEYGUARD) != 0) {
                    if (localLOGV) Log.v(TAG, "Setting mDismissKeyguard to true by win " + win);
                    mDismissKeyguard = true;
                }
                if ((attrs.flags & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON) != 0) {
                    mAllowLockscreenWhenOn = true;
                }
            }
        }
    }
    public int finishAnimationLw() {
        int changes = 0;
        boolean hiding = false;
        if (mStatusBar != null) {
            if (localLOGV) Log.i(TAG, "force=" + mForceStatusBar
                    + " top=" + mTopFullscreenOpaqueWindowState);
            if (mForceStatusBar) {
                if (DEBUG_LAYOUT) Log.v(TAG, "Showing status bar");
                if (mStatusBar.showLw(true)) changes |= FINISH_LAYOUT_REDO_LAYOUT;
            } else if (mTopFullscreenOpaqueWindowState != null) {
                WindowManager.LayoutParams lp =
                    mTopFullscreenOpaqueWindowState.getAttrs();
                boolean hideStatusBar =
                    (lp.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
                if (hideStatusBar) {
                    if (DEBUG_LAYOUT) Log.v(TAG, "Hiding status bar");
                    if (mStatusBar.hideLw(true)) changes |= FINISH_LAYOUT_REDO_LAYOUT;
                    hiding = true;
                } else {
                    if (DEBUG_LAYOUT) Log.v(TAG, "Showing status bar");
                    if (mStatusBar.showLw(true)) changes |= FINISH_LAYOUT_REDO_LAYOUT;
                }
            }
        }
        if (changes != 0 && hiding) {
            IStatusBar sbs = IStatusBar.Stub.asInterface(ServiceManager.getService("statusbar"));
            if (sbs != null) {
                try {
                    sbs.deactivate();
                } catch (RemoteException e) {
                }
            }
        }
        if (mKeyguard != null) {
            if (localLOGV) Log.v(TAG, "finishLayoutLw::mHideKeyguard="+mHideLockScreen);
            if (mDismissKeyguard && !mKeyguardMediator.isSecure()) {
                if (mKeyguard.hideLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
                if (mKeyguardMediator.isShowing()) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            mKeyguardMediator.keyguardDone(false, false);
                        }
                    });
                }
            } else if (mHideLockScreen) {
                if (mKeyguard.hideLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
                mKeyguardMediator.setHidden(true);
            } else {
                if (mKeyguard.showLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
                mKeyguardMediator.setHidden(false);
            }
        }
        updateLockScreenTimeout();
        return changes;
    }
    public boolean allowAppAnimationsLw() {
        if (mKeyguard != null && mKeyguard.isVisibleLw()) {
            return false;
        }
        if (mStatusBar != null && mStatusBar.isVisibleLw()) {
            Rect rect = new Rect(mStatusBar.getShownFrameLw());
            for (int i=mStatusBarPanels.size()-1; i>=0; i--) {
                WindowState w = mStatusBarPanels.get(i);
                if (w.isVisibleLw()) {
                    rect.union(w.getShownFrameLw());
                }
            }
            final int insetw = mW/10;
            final int inseth = mH/10;
            if (rect.contains(insetw, inseth, mW-insetw, mH-inseth)) {
                return false;
            }
        }
        return true;
    }
    public boolean preprocessInputEventTq(RawInputEvent event) {
        switch (event.type) {
            case RawInputEvent.EV_SW:
                if (event.keycode == RawInputEvent.SW_LID) {
                    mLidOpen = event.value == 0;
                    boolean awakeNow = mKeyguardMediator.doLidChangeTq(mLidOpen);
                    updateRotation(Surface.FLAGS_ORIENTATION_ANIMATION_DISABLE);
                    if (awakeNow) {
                        mKeyguardMediator.pokeWakelock();
                    } else if (keyguardIsShowingTq()) {
                        if (mLidOpen) {
                            mKeyguardMediator.onWakeKeyWhenKeyguardShowingTq(
                                    KeyEvent.KEYCODE_POWER);
                        }
                    } else {
                        if (mLidOpen) {
                            mPowerManager.userActivity(SystemClock.uptimeMillis(), false,
                                    LocalPowerManager.BUTTON_EVENT);
                        } else {
                            mPowerManager.userActivity(SystemClock.uptimeMillis(), false,
                                    LocalPowerManager.OTHER_EVENT);
                        }
                    }
                }
        }
        return false;
    }
    public boolean isAppSwitchKeyTqTiLwLi(int keycode) {
        return keycode == KeyEvent.KEYCODE_HOME
                || keycode == KeyEvent.KEYCODE_ENDCALL;
    }
    public boolean isMovementKeyTi(int keycode) {
        switch (keycode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return true;
        }
        return false;
    }
    boolean isInCall() {
        final ITelephony phone = getPhoneInterface();
        if (phone == null) {
            Log.w(TAG, "couldn't get ITelephony reference");
            return false;
        }
        try {
            return phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "ITelephony.isOffhhook threw RemoteException " + e);
            return false;
        }
    }
    boolean isMusicActive() {
        final AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
            return false;
        }
        return am.isMusicActive();
    }
    void handleVolumeKey(int stream, int keycode) {
        final IAudioService audio = getAudioInterface();
        if (audio == null) {
            Log.w(TAG, "handleVolumeKey: couldn't get IAudioService reference");
            return;
        }
        try {
            mBroadcastWakeLock.acquire();
            audio.adjustStreamVolume(stream,
                keycode == KeyEvent.KEYCODE_VOLUME_UP
                            ? AudioManager.ADJUST_RAISE
                            : AudioManager.ADJUST_LOWER,
                    0);
        } catch (RemoteException e) {
            Log.w(TAG, "IAudioService.adjustStreamVolume() threw RemoteException " + e);
        } finally {
            mBroadcastWakeLock.release();
        }
    }
    static boolean isMediaKey(int code) {
        if (code == KeyEvent.KEYCODE_HEADSETHOOK || 
                code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                code == KeyEvent.KEYCODE_MEDIA_STOP || 
                code == KeyEvent.KEYCODE_MEDIA_NEXT ||
                code == KeyEvent.KEYCODE_MEDIA_PREVIOUS || 
                code == KeyEvent.KEYCODE_MEDIA_REWIND ||
                code == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            return true;
        }
        return false;    
    }
    public int interceptKeyTq(RawInputEvent event, boolean screenIsOn) {
        int result = ACTION_PASS_TO_USER;
        final boolean isWakeKey = isWakeKeyTq(event);
        final boolean keyguardActive = (screenIsOn ?
                                        mKeyguardMediator.isShowingAndNotHidden() :
                                        mKeyguardMediator.isShowing());
        if (false) {
            Log.d(TAG, "interceptKeyTq event=" + event + " keycode=" + event.keycode
                  + " screenIsOn=" + screenIsOn + " keyguardActive=" + keyguardActive);
        }
        if (keyguardActive) {
            if (screenIsOn) {
                result |= ACTION_PASS_TO_USER;
            } else {
                result &= ~ACTION_PASS_TO_USER;
                final boolean isKeyDown =
                        (event.type == RawInputEvent.EV_KEY) && (event.value != 0);
                if (isWakeKey && isKeyDown) {
                    if (!mKeyguardMediator.onWakeKeyWhenKeyguardShowingTq(event.keycode)
                            && (event.keycode == KeyEvent.KEYCODE_VOLUME_DOWN
                                || event.keycode == KeyEvent.KEYCODE_VOLUME_UP)) {
                        if (isInCall()) {
                            handleVolumeKey(AudioManager.STREAM_VOICE_CALL, event.keycode);
                        } else if (isMusicActive()) {
                            handleVolumeKey(AudioManager.STREAM_MUSIC, event.keycode);
                        }
                    }
                }
            }
        } else if (!screenIsOn) {
            if (isInCall() && event.type == RawInputEvent.EV_KEY &&
                     (event.keycode == KeyEvent.KEYCODE_VOLUME_DOWN
                                || event.keycode == KeyEvent.KEYCODE_VOLUME_UP)) {
                result &= ~ACTION_PASS_TO_USER;
                handleVolumeKey(AudioManager.STREAM_VOICE_CALL, event.keycode);
            }
            if (isWakeKey) {
                result |= ACTION_POKE_USER_ACTIVITY;
                result &= ~ACTION_PASS_TO_USER;
            }
        }
        int type = event.type;
        int code = event.keycode;
        boolean down = event.value != 0;
        if (type == RawInputEvent.EV_KEY) {
            if (code == KeyEvent.KEYCODE_ENDCALL
                    || code == KeyEvent.KEYCODE_POWER) {
                if (down) {
                    boolean handled = false;
                    boolean hungUp = false;
                    ITelephony phoneServ = getPhoneInterface();
                    if (phoneServ != null) {
                        try {
                            if (code == KeyEvent.KEYCODE_ENDCALL) {
                                handled = hungUp = phoneServ.endCall();
                            } else if (code == KeyEvent.KEYCODE_POWER) {
                                if (phoneServ.isRinging()) {
                                    phoneServ.silenceRinger();
                                    handled = true;
                                } else if (phoneServ.isOffhook() &&
                                           ((mIncallPowerBehavior
                                             & Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP)
                                            != 0)) {
                                    handled = hungUp = phoneServ.endCall();
                                }
                            }
                        } catch (RemoteException ex) {
                            Log.w(TAG, "ITelephony threw RemoteException" + ex);
                        }
                    } else {
                        Log.w(TAG, "!!! Unable to find ITelephony interface !!!");
                    }
                    if (!screenIsOn
                            || (handled && code != KeyEvent.KEYCODE_POWER)
                            || (handled && hungUp && code == KeyEvent.KEYCODE_POWER)) {
                        mShouldTurnOffOnKeyUp = false;
                    } else {
                        mShouldTurnOffOnKeyUp = true;
                        mHandler.postDelayed(mPowerLongPress,
                                ViewConfiguration.getGlobalActionKeyTimeout());
                        result &= ~ACTION_PASS_TO_USER;
                    }
                } else {
                    mHandler.removeCallbacks(mPowerLongPress);
                    if (mShouldTurnOffOnKeyUp) {
                        mShouldTurnOffOnKeyUp = false;
                        boolean gohome, sleeps;
                        if (code == KeyEvent.KEYCODE_ENDCALL) {
                            gohome = (mEndcallBehavior
                                      & Settings.System.END_BUTTON_BEHAVIOR_HOME) != 0;
                            sleeps = (mEndcallBehavior
                                      & Settings.System.END_BUTTON_BEHAVIOR_SLEEP) != 0;
                        } else {
                            gohome = false;
                            sleeps = true;
                        }
                        if (keyguardActive
                                || (sleeps && !gohome)
                                || (gohome && !goHome() && sleeps)) {
                            Log.d(TAG, "I'm tired mEndcallBehavior=0x"
                                    + Integer.toHexString(mEndcallBehavior));
                            result &= ~ACTION_POKE_USER_ACTIVITY;
                            result |= ACTION_GO_TO_SLEEP;
                        }
                        result &= ~ACTION_PASS_TO_USER;
                    }
                }
            } else if (isMediaKey(code)) {
                if ((result & ACTION_PASS_TO_USER) == 0) {
                    KeyEvent keyEvent = new KeyEvent(event.when, event.when,
                            down ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                            code, 0);
                    mBroadcastWakeLock.acquire();
                    mHandler.post(new PassHeadsetKey(keyEvent));
                }
            } else if (code == KeyEvent.KEYCODE_CALL) {
                if (down) {
                    try {
                        ITelephony phoneServ = getPhoneInterface();
                        if (phoneServ != null) {
                            if (phoneServ.isRinging()) {
                                Log.i(TAG, "interceptKeyTq:"
                                      + " CALL key-down while ringing: Answer the call!");
                                phoneServ.answerRingingCall();
                                result &= ~ACTION_PASS_TO_USER;
                            }
                        } else {
                            Log.w(TAG, "CALL button: Unable to find ITelephony interface");
                        }
                    } catch (RemoteException ex) {
                        Log.w(TAG, "CALL button: RemoteException from getPhoneInterface()", ex);
                    }
                }
            } else if ((code == KeyEvent.KEYCODE_VOLUME_UP)
                       || (code == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (down) {
                    try {
                        ITelephony phoneServ = getPhoneInterface();
                        if (phoneServ != null) {
                            if (phoneServ.isRinging()) {
                                Log.i(TAG, "interceptKeyTq:"
                                      + " VOLUME key-down while ringing: Silence ringer!");
                                phoneServ.silenceRinger();
                                result &= ~ACTION_PASS_TO_USER;
                            }
                        } else {
                            Log.w(TAG, "VOLUME button: Unable to find ITelephony interface");
                        }
                    } catch (RemoteException ex) {
                        Log.w(TAG, "VOLUME button: RemoteException from getPhoneInterface()", ex);
                    }
                }
            }
        }
        return result;
    }
    class PassHeadsetKey implements Runnable {
        KeyEvent mKeyEvent;
        PassHeadsetKey(KeyEvent keyEvent) {
            mKeyEvent = keyEvent;
        }
        public void run() {
            if (ActivityManagerNative.isSystemReady()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, mKeyEvent);
                mContext.sendOrderedBroadcast(intent, null, mBroadcastDone,
                        mHandler, Activity.RESULT_OK, null, null);
            }
        }
    }
    BroadcastReceiver mBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mBroadcastWakeLock.release();
        }
    };
    BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())) {
                mDockMode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                        Intent.EXTRA_DOCK_STATE_UNDOCKED);
            } else {
                try {
                    IUiModeManager uiModeService = IUiModeManager.Stub.asInterface(
                            ServiceManager.getService(Context.UI_MODE_SERVICE));
                    mUiMode = uiModeService.getCurrentModeType();
                } catch (RemoteException e) {
                }
            }
            updateRotation(Surface.FLAGS_ORIENTATION_ANIMATION_DISABLE);
            updateOrientationListenerLp();
        }
    };
    public boolean isWakeRelMovementTq(int device, int classes,
            RawInputEvent event) {
        return ((event.flags & (FLAG_WAKE | FLAG_WAKE_DROPPED)) != 0);
    }
    public boolean isWakeAbsMovementTq(int device, int classes,
            RawInputEvent event) {
        return ((event.flags & (FLAG_WAKE | FLAG_WAKE_DROPPED)) != 0);
    }
    protected boolean isWakeKeyTq(RawInputEvent event) {
        int keycode = event.keycode;
        int flags = event.flags;
        if (keycode == RawInputEvent.BTN_MOUSE) {
            flags |= WindowManagerPolicy.FLAG_WAKE;
        }
        return (flags
                & (WindowManagerPolicy.FLAG_WAKE | WindowManagerPolicy.FLAG_WAKE_DROPPED)) != 0;
    }
    public void screenTurnedOff(int why) {
        EventLog.writeEvent(70000, 0);
        mKeyguardMediator.onScreenTurnedOff(why);
        synchronized (mLock) {
            mScreenOn = false;
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
    }
    public void screenTurnedOn() {
        EventLog.writeEvent(70000, 1);
        mKeyguardMediator.onScreenTurnedOn();
        synchronized (mLock) {
            mScreenOn = true;
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
    }
    public boolean isScreenOn() {
        return mScreenOn;
    }
    public void enableKeyguard(boolean enabled) {
        mKeyguardMediator.setKeyguardEnabled(enabled);
    }
    public void exitKeyguardSecurely(OnKeyguardExitResult callback) {
        mKeyguardMediator.verifyUnlock(callback);
    }
    private boolean keyguardIsShowingTq() {
        return mKeyguardMediator.isShowingAndNotHidden();
    }
    public boolean inKeyguardRestrictedKeyInputMode() {
        return mKeyguardMediator.isInputRestricted();
    }
    void sendCloseSystemWindows() {
        sendCloseSystemWindows(mContext, null);
    }
    void sendCloseSystemWindows(String reason) {
        sendCloseSystemWindows(mContext, reason);
    }
    static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }
    public int rotationForOrientationLw(int orientation, int lastRotation,
            boolean displayEnabled) {
        if (mPortraitRotation < 0) {
            Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            if (d.getWidth() > d.getHeight()) {
                mPortraitRotation = Surface.ROTATION_90;
                mLandscapeRotation = Surface.ROTATION_0;
            } else {
                mPortraitRotation = Surface.ROTATION_0;
                mLandscapeRotation = Surface.ROTATION_90;
            }
        }
        synchronized (mLock) {
            switch (orientation) {
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    return mLandscapeRotation;
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    return mPortraitRotation;
            }
            if (mLidOpen) {
                return mLidOpenRotation;
            } else if (mDockMode == Intent.EXTRA_DOCK_STATE_CAR && mCarDockRotation >= 0) {
                return mCarDockRotation;
            } else if (mDockMode == Intent.EXTRA_DOCK_STATE_DESK && mDeskDockRotation >= 0) {
                return mDeskDockRotation;
            } else {
                if (useSensorForOrientationLp(orientation)) {
                    int curRotation = mOrientationListener.getCurrentRotation();
                    return curRotation >= 0 ? curRotation : lastRotation;
                }
                return Surface.ROTATION_0;
            }
        }
    }
    public boolean detectSafeMode() {
        try {
            int menuState = mWindowManager.getKeycodeState(KeyEvent.KEYCODE_MENU);
            int sState = mWindowManager.getKeycodeState(KeyEvent.KEYCODE_S);
            int dpadState = mWindowManager.getDPadKeycodeState(KeyEvent.KEYCODE_DPAD_CENTER);
            int trackballState = mWindowManager.getTrackballScancodeState(RawInputEvent.BTN_MOUSE);
            mSafeMode = menuState > 0 || sState > 0 || dpadState > 0 || trackballState > 0;
            performHapticFeedbackLw(null, mSafeMode
                    ? HapticFeedbackConstants.SAFE_MODE_ENABLED
                    : HapticFeedbackConstants.SAFE_MODE_DISABLED, true);
            if (mSafeMode) {
                Log.i(TAG, "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState
                        + " dpad=" + dpadState + " trackball=" + trackballState + ")");
            } else {
                Log.i(TAG, "SAFE MODE not enabled");
            }
            return mSafeMode;
        } catch (RemoteException e) {
            throw new RuntimeException("window manager dead");
        }
    }
    static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i=0; i<ar.length; i++) {
            out[i] = ar[i];
        }
        return out;
    }
    public void systemReady() {
        mKeyguardMediator.onSystemReady();
        android.os.SystemProperties.set("dev.bootcomplete", "1"); 
        synchronized (mLock) {
            updateOrientationListenerLp();
            mSystemReady = true;
            mHandler.post(new Runnable() {
                public void run() {
                    updateSettings();
                }
            });
        }
    }
    public void userActivity() {
        synchronized (mScreenLockTimeout) {
            if (mLockScreenTimerActive) {
                mHandler.removeCallbacks(mScreenLockTimeout);
                mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
            }
        }
    }
    Runnable mScreenLockTimeout = new Runnable() {
        public void run() {
            synchronized (this) {
                if (localLOGV) Log.v(TAG, "mScreenLockTimeout activating keyguard");
                mKeyguardMediator.doKeyguardTimeout();
                mLockScreenTimerActive = false;
            }
        }
    };
    private void updateLockScreenTimeout() {
        synchronized (mScreenLockTimeout) {
            boolean enable = (mAllowLockscreenWhenOn && mScreenOn && mKeyguardMediator.isSecure());
            if (mLockScreenTimerActive != enable) {
                if (enable) {
                    if (localLOGV) Log.v(TAG, "setting lockscreen timer");
                    mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
                } else {
                    if (localLOGV) Log.v(TAG, "clearing lockscreen timer");
                    mHandler.removeCallbacks(mScreenLockTimeout);
                }
                mLockScreenTimerActive = enable;
            }
        }
    }
    public void enableScreenAfterBoot() {
        readLidState();
        updateRotation(Surface.FLAGS_ORIENTATION_ANIMATION_DISABLE);
    }
    void updateRotation(int animFlags) {
        mPowerManager.setKeyboardVisibility(mLidOpen);
        int rotation = Surface.ROTATION_0;
        if (mLidOpen) {
            rotation = mLidOpenRotation;
        } else if (mDockMode == Intent.EXTRA_DOCK_STATE_CAR && mCarDockRotation >= 0) {
            rotation = mCarDockRotation;
        } else if (mDockMode == Intent.EXTRA_DOCK_STATE_DESK && mDeskDockRotation >= 0) {
            rotation = mDeskDockRotation;
        }
        try {
            mWindowManager.setRotation(rotation, true,
                    mFancyRotationAnimation | animFlags);
        } catch (RemoteException e) {
        }
    }
    Intent createHomeDockIntent() {
        Intent intent;
        if (mUiMode == Configuration.UI_MODE_TYPE_CAR) {
            intent = mCarDockIntent;
        } else if (mUiMode == Configuration.UI_MODE_TYPE_DESK) {
            intent = mDeskDockIntent;
        } else {
            return null;
        }
        ActivityInfo ai = intent.resolveActivityInfo(
                mContext.getPackageManager(), PackageManager.GET_META_DATA);
        if (ai == null) {
            return null;
        }
        if (ai.metaData != null && ai.metaData.getBoolean(Intent.METADATA_DOCK_HOME)) {
            intent = new Intent(intent);
            intent.setClassName(ai.packageName, ai.name);
            return intent;
        }
        return null;
    }
    void startDockOrHome() {
        Intent dock = createHomeDockIntent();
        if (dock != null) {
            try {
                mContext.startActivity(dock);
                return;
            } catch (ActivityNotFoundException e) {
            }
        }
        mContext.startActivity(mHomeIntent);
    }
    boolean goHome() {
        if (false) {
            try {
                ActivityManagerNative.getDefault().stopAppSwitches();
            } catch (RemoteException e) {
            }
            sendCloseSystemWindows();
            startDockOrHome();
        } else {
            try {
                if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                    Log.d(TAG, "UTS-TEST-MODE");
                } else {
                    ActivityManagerNative.getDefault().stopAppSwitches();
                    sendCloseSystemWindows();
                    Intent dock = createHomeDockIntent();
                    if (dock != null) {
                        int result = ActivityManagerNative.getDefault()
                                .startActivity(null, dock,
                                        dock.resolveTypeIfNeeded(mContext.getContentResolver()),
                                        null, 0, null, null, 0, true , false);
                        if (result == IActivityManager.START_RETURN_INTENT_TO_CALLER) {
                            return false;
                        }
                    }
                }
                int result = ActivityManagerNative.getDefault()
                        .startActivity(null, mHomeIntent,
                                mHomeIntent.resolveTypeIfNeeded(mContext.getContentResolver()),
                                null, 0, null, null, 0, true , false);
                if (result == IActivityManager.START_RETURN_INTENT_TO_CALLER) {
                    return false;
                }
            } catch (RemoteException ex) {
            }
        }
        return true;
    }
    public void setCurrentOrientationLw(int newOrientation) {
        synchronized (mLock) {
            if (newOrientation != mCurrentAppOrientation) {
                mCurrentAppOrientation = newOrientation;
                updateOrientationListenerLp();
            }
        }
    }
    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        final boolean hapticsDisabled = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 0;
        if (!always && (hapticsDisabled || mKeyguardMediator.isShowingAndNotHidden())) {
            return false;
        }
        long[] pattern = null;
        switch (effectId) {
            case HapticFeedbackConstants.LONG_PRESS:
                pattern = mLongPressVibePattern;
                break;
            case HapticFeedbackConstants.VIRTUAL_KEY:
                pattern = mVirtualKeyVibePattern;
                break;
            case HapticFeedbackConstants.KEYBOARD_TAP:
                pattern = mKeyboardTapVibePattern;
                break;
            case HapticFeedbackConstants.SAFE_MODE_DISABLED:
                pattern = mSafeModeDisabledVibePattern;
                break;
            case HapticFeedbackConstants.SAFE_MODE_ENABLED:
                pattern = mSafeModeEnabledVibePattern;
                break;
            default:
                return false;
        }
        if (pattern.length == 1) {
            mVibrator.vibrate(pattern[0]);
        } else {
            mVibrator.vibrate(pattern, -1);
        }
        return true;
    }
    public void keyFeedbackFromInput(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (event.getFlags()&KeyEvent.FLAG_VIRTUAL_HARD_KEY) != 0) {
            performHapticFeedbackLw(null, HapticFeedbackConstants.VIRTUAL_KEY, false);
        }
    }
    public void screenOnStoppedLw() {
        if (!mKeyguardMediator.isShowingAndNotHidden() && mPowerManager.isScreenOn()) {
            long curTime = SystemClock.uptimeMillis();
            mPowerManager.userActivity(curTime, false, LocalPowerManager.OTHER_EVENT);
        }
    }
    public boolean allowKeyRepeat() {
        return mScreenOn;
    }
}
