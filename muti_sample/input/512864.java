class PowerManagerService extends IPowerManager.Stub
        implements LocalPowerManager, Watchdog.Monitor {
    private static final String TAG = "PowerManagerService";
    static final String PARTIAL_NAME = "PowerManagerService";
    private static final boolean LOG_PARTIAL_WL = false;
    private static final boolean LOG_TOUCH_DOWNS = true;
    private static final int LOCK_MASK = PowerManager.PARTIAL_WAKE_LOCK
                                        | PowerManager.SCREEN_DIM_WAKE_LOCK
                                        | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                        | PowerManager.FULL_WAKE_LOCK
                                        | PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
    private static final int SHORT_KEYLIGHT_DELAY_DEFAULT = 6000; 
    private static final int MEDIUM_KEYLIGHT_DELAY = 15000;       
    private static final int LONG_KEYLIGHT_DELAY = 6000;        
    private static final int LONG_DIM_TIME = 7000;              
    private static final int LIGHT_SENSOR_DELAY = 2000;
    private static final int PROXIMITY_SENSOR_DELAY = 1000;
    private static final float PROXIMITY_THRESHOLD = 5.0f;
    private int mShortKeylightDelay = SHORT_KEYLIGHT_DELAY_DEFAULT;
    private static final int SCREEN_ON_BIT          = 0x00000001;
    private static final int SCREEN_BRIGHT_BIT      = 0x00000002;
    private static final int BUTTON_BRIGHT_BIT      = 0x00000004;
    private static final int KEYBOARD_BRIGHT_BIT    = 0x00000008;
    private static final int BATTERY_LOW_BIT        = 0x00000010;
    private static final int SCREEN_OFF         = 0x00000000;
    private static final int SCREEN_DIM         = SCREEN_ON_BIT;
    private static final int SCREEN_BRIGHT      = SCREEN_ON_BIT | SCREEN_BRIGHT_BIT;
    private static final int SCREEN_BUTTON_BRIGHT  = SCREEN_BRIGHT | BUTTON_BRIGHT_BIT;
    private static final int ALL_BRIGHT         = SCREEN_BUTTON_BRIGHT | KEYBOARD_BRIGHT_BIT;
    private static final int LIGHTS_MASK        = SCREEN_BRIGHT_BIT | BUTTON_BRIGHT_BIT | KEYBOARD_BRIGHT_BIT;
    static final boolean ANIMATE_SCREEN_LIGHTS = true;
    static final boolean ANIMATE_BUTTON_LIGHTS = false;
    static final boolean ANIMATE_KEYBOARD_LIGHTS = false;
    static final int ANIM_STEPS = 60/4;
    static final int AUTOBRIGHTNESS_ANIM_STEPS = 60;
    static final int INITIAL_SCREEN_BRIGHTNESS = 255;
    static final int INITIAL_BUTTON_BRIGHTNESS = Power.BRIGHTNESS_OFF;
    static final int INITIAL_KEYBOARD_BRIGHTNESS = Power.BRIGHTNESS_OFF;
    private final int MY_UID;
    private boolean mDoneBooting = false;
    private boolean mBootCompleted = false;
    private int mStayOnConditions = 0;
    private final int[] mBroadcastQueue = new int[] { -1, -1, -1 };
    private final int[] mBroadcastWhy = new int[3];
    private int mPartialCount = 0;
    private int mPowerState;
    private int mScreenOffReason;
    private int mUserState;
    private boolean mKeyboardVisible = false;
    private boolean mUserActivityAllowed = true;
    private int mProximityWakeLockCount = 0;
    private boolean mProximitySensorEnabled = false;
    private boolean mProximitySensorActive = false;
    private int mProximityPendingValue = -1; 
    private long mLastProximityEventTime;
    private int mScreenOffTimeoutSetting;
    private int mMaximumScreenOffTimeout = Integer.MAX_VALUE;
    private int mKeylightDelay;
    private int mDimDelay;
    private int mScreenOffDelay;
    private int mWakeLockState;
    private long mLastEventTime = 0;
    private long mScreenOffTime;
    private volatile WindowManagerPolicy mPolicy;
    private final LockList mLocks = new LockList();
    private Intent mScreenOffIntent;
    private Intent mScreenOnIntent;
    private LightsService mLightsService;
    private Context mContext;
    private LightsService.Light mLcdLight;
    private LightsService.Light mButtonLight;
    private LightsService.Light mKeyboardLight;
    private LightsService.Light mAttentionLight;
    private UnsynchronizedWakeLock mBroadcastWakeLock;
    private UnsynchronizedWakeLock mStayOnWhilePluggedInScreenDimLock;
    private UnsynchronizedWakeLock mStayOnWhilePluggedInPartialLock;
    private UnsynchronizedWakeLock mPreventScreenOnPartialLock;
    private UnsynchronizedWakeLock mProximityPartialLock;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private final TimeoutTask mTimeoutTask = new TimeoutTask();
    private final LightAnimator mLightAnimator = new LightAnimator();
    private final BrightnessState mScreenBrightness
            = new BrightnessState(SCREEN_BRIGHT_BIT);
    private final BrightnessState mKeyboardBrightness
            = new BrightnessState(KEYBOARD_BRIGHT_BIT);
    private final BrightnessState mButtonBrightness
            = new BrightnessState(BUTTON_BRIGHT_BIT);
    private boolean mStillNeedSleepNotification;
    private boolean mIsPowered = false;
    private IActivityManager mActivityService;
    private IBatteryStats mBatteryStats;
    private BatteryService mBatteryService;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private Sensor mLightSensor;
    private boolean mLightSensorEnabled;
    private float mLightSensorValue = -1;
    private int mHighestLightSensorValue = -1;
    private float mLightSensorPendingValue = -1;
    private int mLightSensorScreenBrightness = -1;
    private int mLightSensorButtonBrightness = -1;
    private int mLightSensorKeyboardBrightness = -1;
    private boolean mDimScreen = true;
    private boolean mIsDocked = false;
    private long mNextTimeout;
    private volatile int mPokey = 0;
    private volatile boolean mPokeAwakeOnSet = false;
    private volatile boolean mInitComplete = false;
    private final HashMap<IBinder,PokeLock> mPokeLocks = new HashMap<IBinder,PokeLock>();
    private long mLastScreenOnTime;
    private boolean mPreventScreenOn;
    private int mScreenBrightnessOverride = -1;
    private int mButtonBrightnessOverride = -1;
    private boolean mUseSoftwareAutoBrightness;
    private boolean mAutoBrightessEnabled;
    private int[] mAutoBrightnessLevels;
    private int[] mLcdBacklightValues;
    private int[] mButtonBacklightValues;
    private int[] mKeyboardBacklightValues;
    private int mLightSensorWarmupTime;
    private long mTotalTouchDownTime;
    private long mLastTouchDown;
    private int mTouchCycles;
    private static final boolean mSpew = false;
    private static final boolean mDebugProximitySensor = (true || mSpew);
    private static final boolean mDebugLightSensor = (false || mSpew);
    private class UnsynchronizedWakeLock {
        int mFlags;
        String mTag;
        IBinder mToken;
        int mCount = 0;
        boolean mRefCounted;
        boolean mHeld;
        UnsynchronizedWakeLock(int flags, String tag, boolean refCounted) {
            mFlags = flags;
            mTag = tag;
            mToken = new Binder();
            mRefCounted = refCounted;
        }
        public void acquire() {
            if (!mRefCounted || mCount++ == 0) {
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.acquireWakeLockLocked(mFlags, mToken,
                            MY_UID, mTag);
                    mHeld = true;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        public void release() {
            if (!mRefCounted || --mCount == 0) {
                PowerManagerService.this.releaseWakeLockLocked(mToken, 0, false);
                mHeld = false;
            }
            if (mCount < 0) {
                throw new RuntimeException("WakeLock under-locked " + mTag);
            }
        }
        public boolean isHeld()
        {
            return mHeld;
        }
        public String toString() {
            return "UnsynchronizedWakeLock(mFlags=0x" + Integer.toHexString(mFlags)
                    + " mCount=" + mCount + " mHeld=" + mHeld + ")";
        }
    }
    private final class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (mLocks) {
                boolean wasPowered = mIsPowered;
                mIsPowered = mBatteryService.isPowered();
                if (mIsPowered != wasPowered) {
                    updateWakeLockLocked();
                    synchronized (mLocks) {
                        if (!wasPowered || (mPowerState & SCREEN_ON_BIT) != 0) {
                            forceUserActivityLocked();
                        }
                    }
                }
            }
        }
    }
    private final class BootCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            bootCompleted();
        }
    }
    private final class DockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                    Intent.EXTRA_DOCK_STATE_UNDOCKED);
            dockStateChanged(state);
        }
    }
    public void setStayOnSetting(int val) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WRITE_SETTINGS, null);
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.STAY_ON_WHILE_PLUGGED_IN, val);
    }
    public void setMaximumScreenOffTimeount(int timeMs) {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.WRITE_SECURE_SETTINGS, null);
        synchronized (mLocks) {
            mMaximumScreenOffTimeout = timeMs;
            setScreenOffTimeoutsLocked();
        }
    }
    private class SettingsObserver implements Observer {
        private int getInt(String name) {
            return mSettings.getValues(name).getAsInteger(Settings.System.VALUE);
        }
        public void update(Observable o, Object arg) {
            synchronized (mLocks) {
                mStayOnConditions = getInt(STAY_ON_WHILE_PLUGGED_IN);
                updateWakeLockLocked();
                mScreenOffTimeoutSetting = getInt(SCREEN_OFF_TIMEOUT);
                setScreenBrightnessMode(getInt(SCREEN_BRIGHTNESS_MODE));
                setScreenOffTimeoutsLocked();
            }
        }
    }
    PowerManagerService()
    {
        long token = Binder.clearCallingIdentity();
        MY_UID = Binder.getCallingUid();
        Binder.restoreCallingIdentity(token);
        Power.setLastUserActivityTimeout(7*24*3600*1000); 
        mUserState = mPowerState = 0;
        Watchdog.getInstance().addMonitor(this);
    }
    private ContentQueryMap mSettings;
    void init(Context context, LightsService lights, IActivityManager activity,
            BatteryService battery) {
        mLightsService = lights;
        mContext = context;
        mActivityService = activity;
        mBatteryStats = BatteryStatsService.getService();
        mBatteryService = battery;
        mLcdLight = lights.getLight(LightsService.LIGHT_ID_BACKLIGHT);
        mButtonLight = lights.getLight(LightsService.LIGHT_ID_BUTTONS);
        mKeyboardLight = lights.getLight(LightsService.LIGHT_ID_KEYBOARD);
        mAttentionLight = lights.getLight(LightsService.LIGHT_ID_ATTENTION);
        mHandlerThread = new HandlerThread("PowerManagerService") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                initInThread();
            }
        };
        mHandlerThread.start();
        synchronized (mHandlerThread) {
            while (!mInitComplete) {
                try {
                    mHandlerThread.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    void initInThread() {
        mHandler = new Handler();
        mBroadcastWakeLock = new UnsynchronizedWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK, "sleep_broadcast", true);
        mStayOnWhilePluggedInScreenDimLock = new UnsynchronizedWakeLock(
                                PowerManager.SCREEN_DIM_WAKE_LOCK, "StayOnWhilePluggedIn Screen Dim", false);
        mStayOnWhilePluggedInPartialLock = new UnsynchronizedWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK, "StayOnWhilePluggedIn Partial", false);
        mPreventScreenOnPartialLock = new UnsynchronizedWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK, "PreventScreenOn Partial", false);
        mProximityPartialLock = new UnsynchronizedWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK, "Proximity Partial", false);
        mScreenOnIntent = new Intent(Intent.ACTION_SCREEN_ON);
        mScreenOnIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mScreenOffIntent = new Intent(Intent.ACTION_SCREEN_OFF);
        mScreenOffIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        Resources resources = mContext.getResources();
        mUseSoftwareAutoBrightness = resources.getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (mUseSoftwareAutoBrightness) {
            mAutoBrightnessLevels = resources.getIntArray(
                    com.android.internal.R.array.config_autoBrightnessLevels);
            mLcdBacklightValues = resources.getIntArray(
                    com.android.internal.R.array.config_autoBrightnessLcdBacklightValues);
            mButtonBacklightValues = resources.getIntArray(
                    com.android.internal.R.array.config_autoBrightnessButtonBacklightValues);
            mKeyboardBacklightValues = resources.getIntArray(
                    com.android.internal.R.array.config_autoBrightnessKeyboardBacklightValues);
            mLightSensorWarmupTime = resources.getInteger(
                    com.android.internal.R.integer.config_lightSensorWarmupTime);
        }
       ContentResolver resolver = mContext.getContentResolver();
        Cursor settingsCursor = resolver.query(Settings.System.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?) or ("
                        + Settings.System.NAME + "=?) or ("
                        + Settings.System.NAME + "=?) or ("
                        + Settings.System.NAME + "=?)",
                new String[]{STAY_ON_WHILE_PLUGGED_IN, SCREEN_OFF_TIMEOUT, DIM_SCREEN,
                        SCREEN_BRIGHTNESS_MODE},
                null);
        mSettings = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, mHandler);
        SettingsObserver settingsObserver = new SettingsObserver();
        mSettings.addObserver(settingsObserver);
        settingsObserver.update(mSettings, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(new BatteryReceiver(), filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(new BootCompletedReceiver(), filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DOCK_EVENT);
        mContext.registerReceiver(new DockReceiver(), filter);
        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.CONTENT_URI, true,
            new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    updateSettingsValues();
                }
            });
        updateSettingsValues();
        synchronized (mHandlerThread) {
            mInitComplete = true;
            mHandlerThread.notifyAll();
        }
    }
    private class WakeLock implements IBinder.DeathRecipient
    {
        WakeLock(int f, IBinder b, String t, int u) {
            super();
            flags = f;
            binder = b;
            tag = t;
            uid = u == MY_UID ? Process.SYSTEM_UID : u;
            pid = Binder.getCallingPid();
            if (u != MY_UID || (
                    !"KEEP_SCREEN_ON_FLAG".equals(tag)
                    && !"KeyInputQueue".equals(tag))) {
                monitorType = (f & LOCK_MASK) == PowerManager.PARTIAL_WAKE_LOCK
                        ? BatteryStats.WAKE_TYPE_PARTIAL
                        : BatteryStats.WAKE_TYPE_FULL;
            } else {
                monitorType = -1;
            }
            try {
                b.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }
        public void binderDied() {
            synchronized (mLocks) {
                releaseWakeLockLocked(this.binder, 0, true);
            }
        }
        final int flags;
        final IBinder binder;
        final String tag;
        final int uid;
        final int pid;
        final int monitorType;
        boolean activated = true;
        int minState;
    }
    private void updateWakeLockLocked() {
        if (mStayOnConditions != 0 && mBatteryService.isPowered(mStayOnConditions)) {
            mStayOnWhilePluggedInScreenDimLock.acquire();
            mStayOnWhilePluggedInPartialLock.acquire();
        } else {
            mStayOnWhilePluggedInScreenDimLock.release();
            mStayOnWhilePluggedInPartialLock.release();
        }
    }
    private boolean isScreenLock(int flags)
    {
        int n = flags & LOCK_MASK;
        return n == PowerManager.FULL_WAKE_LOCK
                || n == PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                || n == PowerManager.SCREEN_DIM_WAKE_LOCK;
    }
    public void acquireWakeLock(int flags, IBinder lock, String tag) {
        int uid = Binder.getCallingUid();
        if (uid != Process.myUid()) {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mLocks) {
                acquireWakeLockLocked(flags, lock, uid, tag);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    public void acquireWakeLockLocked(int flags, IBinder lock, int uid, String tag) {
        int acquireUid = -1;
        String acquireName = null;
        int acquireType = -1;
        if (mSpew) {
            Slog.d(TAG, "acquireWakeLock flags=0x" + Integer.toHexString(flags) + " tag=" + tag);
        }
        int index = mLocks.getIndex(lock);
        WakeLock wl;
        boolean newlock;
        if (index < 0) {
            wl = new WakeLock(flags, lock, tag, uid);
            switch (wl.flags & LOCK_MASK)
            {
                case PowerManager.FULL_WAKE_LOCK:
                    if (mUseSoftwareAutoBrightness) {
                        wl.minState = SCREEN_BRIGHT;
                    } else {
                        wl.minState = (mKeyboardVisible ? ALL_BRIGHT : SCREEN_BUTTON_BRIGHT);
                    }
                    break;
                case PowerManager.SCREEN_BRIGHT_WAKE_LOCK:
                    wl.minState = SCREEN_BRIGHT;
                    break;
                case PowerManager.SCREEN_DIM_WAKE_LOCK:
                    wl.minState = SCREEN_DIM;
                    break;
                case PowerManager.PARTIAL_WAKE_LOCK:
                case PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK:
                    break;
                default:
                    Slog.e(TAG, "bad wakelock type for lock '" + tag + "' "
                            + " flags=" + flags);
                    return;
            }
            mLocks.addLock(wl);
            newlock = true;
        } else {
            wl = mLocks.get(index);
            newlock = false;
        }
        if (isScreenLock(flags)) {
            if ((wl.flags & PowerManager.ACQUIRE_CAUSES_WAKEUP) != 0) {
                int oldWakeLockState = mWakeLockState;
                mWakeLockState = mLocks.reactivateScreenLocksLocked();
                if (mSpew) {
                    Slog.d(TAG, "wakeup here mUserState=0x" + Integer.toHexString(mUserState)
                            + " mWakeLockState=0x"
                            + Integer.toHexString(mWakeLockState)
                            + " previous wakeLockState=0x" + Integer.toHexString(oldWakeLockState));
                }
            } else {
                if (mSpew) {
                    Slog.d(TAG, "here mUserState=0x" + Integer.toHexString(mUserState)
                            + " mLocks.gatherState()=0x"
                            + Integer.toHexString(mLocks.gatherState())
                            + " mWakeLockState=0x" + Integer.toHexString(mWakeLockState));
                }
                mWakeLockState = (mUserState | mWakeLockState) & mLocks.gatherState();
            }
            setPowerState(mWakeLockState | mUserState);
        }
        else if ((flags & LOCK_MASK) == PowerManager.PARTIAL_WAKE_LOCK) {
            if (newlock) {
                mPartialCount++;
                if (mPartialCount == 1) {
                    if (LOG_PARTIAL_WL) EventLog.writeEvent(EventLogTags.POWER_PARTIAL_WAKE_STATE, 1, tag);
                }
            }
            Power.acquireWakeLock(Power.PARTIAL_WAKE_LOCK,PARTIAL_NAME);
        } else if ((flags & LOCK_MASK) == PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) {
            mProximityWakeLockCount++;
            if (mProximityWakeLockCount == 1) {
                enableProximityLockLocked();
            }
        }
        if (newlock) {
            acquireUid = wl.uid;
            acquireName = wl.tag;
            acquireType = wl.monitorType;
        }
        if (acquireType >= 0) {
            try {
                mBatteryStats.noteStartWakelock(acquireUid, acquireName, acquireType);
            } catch (RemoteException e) {
            }
        }
    }
    public void releaseWakeLock(IBinder lock, int flags) {
        int uid = Binder.getCallingUid();
        if (uid != Process.myUid()) {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        }
        synchronized (mLocks) {
            releaseWakeLockLocked(lock, flags, false);
        }
    }
    private void releaseWakeLockLocked(IBinder lock, int flags, boolean death) {
        int releaseUid;
        String releaseName;
        int releaseType;
        WakeLock wl = mLocks.removeLock(lock);
        if (wl == null) {
            return;
        }
        if (mSpew) {
            Slog.d(TAG, "releaseWakeLock flags=0x"
                    + Integer.toHexString(wl.flags) + " tag=" + wl.tag);
        }
        if (isScreenLock(wl.flags)) {
            mWakeLockState = mLocks.gatherState();
            if ((wl.flags & PowerManager.ON_AFTER_RELEASE) != 0) {
                userActivity(SystemClock.uptimeMillis(), false);
            }
            setPowerState(mWakeLockState | mUserState);
        }
        else if ((wl.flags & LOCK_MASK) == PowerManager.PARTIAL_WAKE_LOCK) {
            mPartialCount--;
            if (mPartialCount == 0) {
                if (LOG_PARTIAL_WL) EventLog.writeEvent(EventLogTags.POWER_PARTIAL_WAKE_STATE, 0, wl.tag);
                Power.releaseWakeLock(PARTIAL_NAME);
            }
        } else if ((wl.flags & LOCK_MASK) == PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) {
            mProximityWakeLockCount--;
            if (mProximityWakeLockCount == 0) {
                if (mProximitySensorActive &&
                        ((flags & PowerManager.WAIT_FOR_PROXIMITY_NEGATIVE) != 0)) {
                    if (mDebugProximitySensor) {
                        Slog.d(TAG, "waiting for proximity sensor to go negative");
                    }
                } else {
                    disableProximityLockLocked();
                }
            }
        }
        wl.binder.unlinkToDeath(wl, 0);
        releaseUid = wl.uid;
        releaseName = wl.tag;
        releaseType = wl.monitorType;
        if (releaseType >= 0) {
            long origId = Binder.clearCallingIdentity();
            try {
                mBatteryStats.noteStopWakelock(releaseUid, releaseName, releaseType);
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    private class PokeLock implements IBinder.DeathRecipient
    {
        PokeLock(int p, IBinder b, String t) {
            super();
            this.pokey = p;
            this.binder = b;
            this.tag = t;
            try {
                b.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }
        public void binderDied() {
            setPokeLock(0, this.binder, this.tag);
        }
        int pokey;
        IBinder binder;
        String tag;
        boolean awakeOnSet;
    }
    public void setPokeLock(int pokey, IBinder token, String tag) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        if (token == null) {
            Slog.e(TAG, "setPokeLock got null token for tag='" + tag + "'");
            return;
        }
        if ((pokey & POKE_LOCK_TIMEOUT_MASK) == POKE_LOCK_TIMEOUT_MASK) {
            throw new IllegalArgumentException("setPokeLock can't have both POKE_LOCK_SHORT_TIMEOUT"
                    + " and POKE_LOCK_MEDIUM_TIMEOUT");
        }
        synchronized (mLocks) {
            if (pokey != 0) {
                PokeLock p = mPokeLocks.get(token);
                int oldPokey = 0;
                if (p != null) {
                    oldPokey = p.pokey;
                    p.pokey = pokey;
                } else {
                    p = new PokeLock(pokey, token, tag);
                    mPokeLocks.put(token, p);
                }
                int oldTimeout = oldPokey & POKE_LOCK_TIMEOUT_MASK;
                int newTimeout = pokey & POKE_LOCK_TIMEOUT_MASK;
                if (((mPowerState & SCREEN_ON_BIT) == 0) && (oldTimeout != newTimeout)) {
                    p.awakeOnSet = true;
                }
            } else {
                PokeLock rLock = mPokeLocks.remove(token);
                if (rLock != null) {
                    token.unlinkToDeath(rLock, 0);
                }
            }
            int oldPokey = mPokey;
            int cumulative = 0;
            boolean oldAwakeOnSet = mPokeAwakeOnSet;
            boolean awakeOnSet = false;
            for (PokeLock p: mPokeLocks.values()) {
                cumulative |= p.pokey;
                if (p.awakeOnSet) {
                    awakeOnSet = true;
                }
            }
            mPokey = cumulative;
            mPokeAwakeOnSet = awakeOnSet;
            int oldCumulativeTimeout = oldPokey & POKE_LOCK_TIMEOUT_MASK;
            int newCumulativeTimeout = pokey & POKE_LOCK_TIMEOUT_MASK;
            if (oldCumulativeTimeout != newCumulativeTimeout) {
                setScreenOffTimeoutsLocked();
                setTimeoutLocked(SystemClock.uptimeMillis(), mTimeoutTask.nextState);
            }
        }
    }
    private static String lockType(int type)
    {
        switch (type)
        {
            case PowerManager.FULL_WAKE_LOCK:
                return "FULL_WAKE_LOCK                ";
            case PowerManager.SCREEN_BRIGHT_WAKE_LOCK:
                return "SCREEN_BRIGHT_WAKE_LOCK       ";
            case PowerManager.SCREEN_DIM_WAKE_LOCK:
                return "SCREEN_DIM_WAKE_LOCK          ";
            case PowerManager.PARTIAL_WAKE_LOCK:
                return "PARTIAL_WAKE_LOCK             ";
            case PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK:
                return "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
            default:
                return "???                           ";
        }
    }
    private static String dumpPowerState(int state) {
        return (((state & KEYBOARD_BRIGHT_BIT) != 0)
                        ? "KEYBOARD_BRIGHT_BIT " : "")
                + (((state & SCREEN_BRIGHT_BIT) != 0)
                        ? "SCREEN_BRIGHT_BIT " : "")
                + (((state & SCREEN_ON_BIT) != 0)
                        ? "SCREEN_ON_BIT " : "")
                + (((state & BATTERY_LOW_BIT) != 0)
                        ? "BATTERY_LOW_BIT " : "");
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump PowerManager from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        long now = SystemClock.uptimeMillis();
        synchronized (mLocks) {
            pw.println("Power Manager State:");
            pw.println("  mIsPowered=" + mIsPowered
                    + " mPowerState=" + mPowerState
                    + " mScreenOffTime=" + (SystemClock.elapsedRealtime()-mScreenOffTime)
                    + " ms");
            pw.println("  mPartialCount=" + mPartialCount);
            pw.println("  mWakeLockState=" + dumpPowerState(mWakeLockState));
            pw.println("  mUserState=" + dumpPowerState(mUserState));
            pw.println("  mPowerState=" + dumpPowerState(mPowerState));
            pw.println("  mLocks.gather=" + dumpPowerState(mLocks.gatherState()));
            pw.println("  mNextTimeout=" + mNextTimeout + " now=" + now
                    + " " + ((mNextTimeout-now)/1000) + "s from now");
            pw.println("  mDimScreen=" + mDimScreen
                    + " mStayOnConditions=" + mStayOnConditions);
            pw.println("  mScreenOffReason=" + mScreenOffReason
                    + " mUserState=" + mUserState);
            pw.println("  mBroadcastQueue={" + mBroadcastQueue[0] + ',' + mBroadcastQueue[1]
                    + ',' + mBroadcastQueue[2] + "}");
            pw.println("  mBroadcastWhy={" + mBroadcastWhy[0] + ',' + mBroadcastWhy[1]
                    + ',' + mBroadcastWhy[2] + "}");
            pw.println("  mPokey=" + mPokey + " mPokeAwakeonSet=" + mPokeAwakeOnSet);
            pw.println("  mKeyboardVisible=" + mKeyboardVisible
                    + " mUserActivityAllowed=" + mUserActivityAllowed);
            pw.println("  mKeylightDelay=" + mKeylightDelay + " mDimDelay=" + mDimDelay
                    + " mScreenOffDelay=" + mScreenOffDelay);
            pw.println("  mPreventScreenOn=" + mPreventScreenOn
                    + "  mScreenBrightnessOverride=" + mScreenBrightnessOverride
                    + "  mButtonBrightnessOverride=" + mButtonBrightnessOverride);
            pw.println("  mScreenOffTimeoutSetting=" + mScreenOffTimeoutSetting
                    + " mMaximumScreenOffTimeout=" + mMaximumScreenOffTimeout);
            pw.println("  mLastScreenOnTime=" + mLastScreenOnTime);
            pw.println("  mBroadcastWakeLock=" + mBroadcastWakeLock);
            pw.println("  mStayOnWhilePluggedInScreenDimLock=" + mStayOnWhilePluggedInScreenDimLock);
            pw.println("  mStayOnWhilePluggedInPartialLock=" + mStayOnWhilePluggedInPartialLock);
            pw.println("  mPreventScreenOnPartialLock=" + mPreventScreenOnPartialLock);
            pw.println("  mProximityPartialLock=" + mProximityPartialLock);
            pw.println("  mProximityWakeLockCount=" + mProximityWakeLockCount);
            pw.println("  mProximitySensorEnabled=" + mProximitySensorEnabled);
            pw.println("  mProximitySensorActive=" + mProximitySensorActive);
            pw.println("  mProximityPendingValue=" + mProximityPendingValue);
            pw.println("  mLastProximityEventTime=" + mLastProximityEventTime);
            pw.println("  mLightSensorEnabled=" + mLightSensorEnabled);
            pw.println("  mLightSensorValue=" + mLightSensorValue
                    + " mLightSensorPendingValue=" + mLightSensorPendingValue);
            pw.println("  mLightSensorScreenBrightness=" + mLightSensorScreenBrightness
                    + " mLightSensorButtonBrightness=" + mLightSensorButtonBrightness
                    + " mLightSensorKeyboardBrightness=" + mLightSensorKeyboardBrightness);
            pw.println("  mUseSoftwareAutoBrightness=" + mUseSoftwareAutoBrightness);
            pw.println("  mAutoBrightessEnabled=" + mAutoBrightessEnabled);
            mScreenBrightness.dump(pw, "  mScreenBrightness: ");
            mKeyboardBrightness.dump(pw, "  mKeyboardBrightness: ");
            mButtonBrightness.dump(pw, "  mButtonBrightness: ");
            int N = mLocks.size();
            pw.println();
            pw.println("mLocks.size=" + N + ":");
            for (int i=0; i<N; i++) {
                WakeLock wl = mLocks.get(i);
                String type = lockType(wl.flags & LOCK_MASK);
                String acquireCausesWakeup = "";
                if ((wl.flags & PowerManager.ACQUIRE_CAUSES_WAKEUP) != 0) {
                    acquireCausesWakeup = "ACQUIRE_CAUSES_WAKEUP ";
                }
                String activated = "";
                if (wl.activated) {
                   activated = " activated";
                }
                pw.println("  " + type + " '" + wl.tag + "'" + acquireCausesWakeup
                        + activated + " (minState=" + wl.minState + ", uid=" + wl.uid
                        + ", pid=" + wl.pid + ")");
            }
            pw.println();
            pw.println("mPokeLocks.size=" + mPokeLocks.size() + ":");
            for (PokeLock p: mPokeLocks.values()) {
                pw.println("    poke lock '" + p.tag + "':"
                        + ((p.pokey & POKE_LOCK_IGNORE_CHEEK_EVENTS) != 0
                                ? " POKE_LOCK_IGNORE_CHEEK_EVENTS" : "")
                        + ((p.pokey & POKE_LOCK_IGNORE_TOUCH_AND_CHEEK_EVENTS) != 0
                                ? " POKE_LOCK_IGNORE_TOUCH_AND_CHEEK_EVENTS" : "")
                        + ((p.pokey & POKE_LOCK_SHORT_TIMEOUT) != 0
                                ? " POKE_LOCK_SHORT_TIMEOUT" : "")
                        + ((p.pokey & POKE_LOCK_MEDIUM_TIMEOUT) != 0
                                ? " POKE_LOCK_MEDIUM_TIMEOUT" : ""));
            }
            pw.println();
        }
    }
    private void setTimeoutLocked(long now, int nextState) {
        setTimeoutLocked(now, -1, nextState);
    }
    private void setTimeoutLocked(long now, final long originalTimeoutOverride, int nextState) {
        long timeoutOverride = originalTimeoutOverride;
        if (mBootCompleted) {
            synchronized (mLocks) {
                long when = 0;
                if (timeoutOverride <= 0) {
                    switch (nextState)
                    {
                        case SCREEN_BRIGHT:
                            when = now + mKeylightDelay;
                            break;
                        case SCREEN_DIM:
                            if (mDimDelay >= 0) {
                                when = now + mDimDelay;
                                break;
                            } else {
                                Slog.w(TAG, "mDimDelay=" + mDimDelay + " while trying to dim");
                            }
                       case SCREEN_OFF:
                            synchronized (mLocks) {
                                when = now + mScreenOffDelay;
                            }
                            break;
                        default:
                            when = now;
                            break;
                    }
                } else {
                    override: {
                        if (timeoutOverride <= mScreenOffDelay) {
                            when = now + timeoutOverride;
                            nextState = SCREEN_OFF;
                            break override;
                        }
                        timeoutOverride -= mScreenOffDelay;
                        if (mDimDelay >= 0) {
                             if (timeoutOverride <= mDimDelay) {
                                when = now + timeoutOverride;
                                nextState = SCREEN_DIM;
                                break override;
                            }
                            timeoutOverride -= mDimDelay;
                        }
                        when = now + timeoutOverride;
                        nextState = SCREEN_BRIGHT;
                    }
                }
                if (mSpew) {
                    Slog.d(TAG, "setTimeoutLocked now=" + now
                            + " timeoutOverride=" + timeoutOverride
                            + " nextState=" + nextState + " when=" + when);
                }
                mHandler.removeCallbacks(mTimeoutTask);
                mTimeoutTask.nextState = nextState;
                mTimeoutTask.remainingTimeoutOverride = timeoutOverride > 0
                        ? (originalTimeoutOverride - timeoutOverride)
                        : -1;
                mHandler.postAtTime(mTimeoutTask, when);
                mNextTimeout = when; 
            }
        }
    }
    private void cancelTimerLocked()
    {
        mHandler.removeCallbacks(mTimeoutTask);
        mTimeoutTask.nextState = -1;
    }
    private class TimeoutTask implements Runnable
    {
        int nextState; 
        long remainingTimeoutOverride;
        public void run()
        {
            synchronized (mLocks) {
                if (mSpew) {
                    Slog.d(TAG, "user activity timeout timed out nextState=" + this.nextState);
                }
                if (nextState == -1) {
                    return;
                }
                mUserState = this.nextState;
                setPowerState(this.nextState | mWakeLockState);
                long now = SystemClock.uptimeMillis();
                switch (this.nextState)
                {
                    case SCREEN_BRIGHT:
                        if (mDimDelay >= 0) {
                            setTimeoutLocked(now, remainingTimeoutOverride, SCREEN_DIM);
                            break;
                        }
                    case SCREEN_DIM:
                        setTimeoutLocked(now, remainingTimeoutOverride, SCREEN_OFF);
                        break;
                }
            }
        }
    }
    private void sendNotificationLocked(boolean on, int why)
    {
        if (!on) {
            mStillNeedSleepNotification = false;
        }
        int index = 0;
        while (mBroadcastQueue[index] != -1) {
            index++;
        }
        mBroadcastQueue[index] = on ? 1 : 0;
        mBroadcastWhy[index] = why;
        if (index == 2) {
            if (!on && mBroadcastWhy[0] > why) {
                mBroadcastWhy[0] = why;
            }
            mBroadcastQueue[0] = on ? 1 : 0;
            mBroadcastQueue[1] = -1;
            mBroadcastQueue[2] = -1;
            mBroadcastWakeLock.release();
            mBroadcastWakeLock.release();
            index = 0;
        }
        if (index == 1 && !on) {
            mBroadcastQueue[0] = -1;
            mBroadcastQueue[1] = -1;
            index = -1;
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, 1, mBroadcastWakeLock.mCount);
            mBroadcastWakeLock.release();
        }
        if (index >= 0) {
            mBroadcastWakeLock.acquire();
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_SEND, mBroadcastWakeLock.mCount);
            mHandler.post(mNotificationTask);
        }
    }
    private Runnable mNotificationTask = new Runnable()
    {
        public void run()
        {
            while (true) {
                int value;
                int why;
                WindowManagerPolicy policy;
                synchronized (mLocks) {
                    value = mBroadcastQueue[0];
                    why = mBroadcastWhy[0];
                    for (int i=0; i<2; i++) {
                        mBroadcastQueue[i] = mBroadcastQueue[i+1];
                        mBroadcastWhy[i] = mBroadcastWhy[i+1];
                    }
                    policy = getPolicyLocked();
                }
                if (value == 1) {
                    mScreenOnStart = SystemClock.uptimeMillis();
                    policy.screenTurnedOn();
                    try {
                        ActivityManagerNative.getDefault().wakingUp();
                    } catch (RemoteException e) {
                    }
                    if (mSpew) {
                        Slog.d(TAG, "mBroadcastWakeLock=" + mBroadcastWakeLock);
                    }
                    if (mContext != null && ActivityManagerNative.isSystemReady()) {
                        mContext.sendOrderedBroadcast(mScreenOnIntent, null,
                                mScreenOnBroadcastDone, mHandler, 0, null, null);
                    } else {
                        synchronized (mLocks) {
                            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, 2,
                                    mBroadcastWakeLock.mCount);
                            mBroadcastWakeLock.release();
                        }
                    }
                }
                else if (value == 0) {
                    mScreenOffStart = SystemClock.uptimeMillis();
                    policy.screenTurnedOff(why);
                    try {
                        ActivityManagerNative.getDefault().goingToSleep();
                    } catch (RemoteException e) {
                    }
                    if (mContext != null && ActivityManagerNative.isSystemReady()) {
                        mContext.sendOrderedBroadcast(mScreenOffIntent, null,
                                mScreenOffBroadcastDone, mHandler, 0, null, null);
                    } else {
                        synchronized (mLocks) {
                            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, 3,
                                    mBroadcastWakeLock.mCount);
                            mBroadcastWakeLock.release();
                        }
                    }
                }
                else {
                    break;
                }
            }
        }
    };
    long mScreenOnStart;
    private BroadcastReceiver mScreenOnBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (mLocks) {
                EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, 1,
                        SystemClock.uptimeMillis() - mScreenOnStart, mBroadcastWakeLock.mCount);
                mBroadcastWakeLock.release();
            }
        }
    };
    long mScreenOffStart;
    private BroadcastReceiver mScreenOffBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (mLocks) {
                EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, 0,
                        SystemClock.uptimeMillis() - mScreenOffStart, mBroadcastWakeLock.mCount);
                mBroadcastWakeLock.release();
            }
        }
    };
    void logPointerUpEvent() {
        if (LOG_TOUCH_DOWNS) {
            mTotalTouchDownTime += SystemClock.elapsedRealtime() - mLastTouchDown;
            mLastTouchDown = 0;
        }
    }
    void logPointerDownEvent() {
        if (LOG_TOUCH_DOWNS) {
            if (mLastTouchDown == 0) {
                mLastTouchDown = SystemClock.elapsedRealtime();
                mTouchCycles++;
            }
        }
    }
    public void preventScreenOn(boolean prevent) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        synchronized (mLocks) {
            if (prevent) {
                mPreventScreenOnPartialLock.acquire();
                mHandler.removeCallbacks(mForceReenableScreenTask);
                mHandler.postDelayed(mForceReenableScreenTask, 5000);
                mPreventScreenOn = true;
            } else {
                mPreventScreenOn = false;
                mHandler.removeCallbacks(mForceReenableScreenTask);
                if (!mProximitySensorActive && (mPowerState & SCREEN_ON_BIT) != 0) {
                    if (mSpew) {
                        Slog.d(TAG,
                              "preventScreenOn: turning on after a prior preventScreenOn(true)!");
                    }
                    int err = setScreenStateLocked(true);
                    if (err != 0) {
                        Slog.w(TAG, "preventScreenOn: error from setScreenStateLocked(): " + err);
                    }
                }
                mPreventScreenOnPartialLock.release();
            }
        }
    }
    public void setScreenBrightnessOverride(int brightness) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        if (mSpew) Slog.d(TAG, "setScreenBrightnessOverride " + brightness);
        synchronized (mLocks) {
            if (mScreenBrightnessOverride != brightness) {
                mScreenBrightnessOverride = brightness;
                if (isScreenOn()) {
                    updateLightsLocked(mPowerState, SCREEN_ON_BIT);
                }
            }
        }
    }
    public void setButtonBrightnessOverride(int brightness) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        if (mSpew) Slog.d(TAG, "setButtonBrightnessOverride " + brightness);
         synchronized (mLocks) {
           if (mButtonBrightnessOverride != brightness) {
                mButtonBrightnessOverride = brightness;
                if (isScreenOn()) {
                    updateLightsLocked(mPowerState, BUTTON_BRIGHT_BIT | KEYBOARD_BRIGHT_BIT);
                }
            }
        }
    }
    private void forceReenableScreen() {
        if (!mPreventScreenOn) {
            Slog.w(TAG, "forceReenableScreen: mPreventScreenOn is false, nothing to do");
            return;
        }
        Slog.w(TAG, "App called preventScreenOn(true) but didn't promptly reenable the screen! "
              + "Forcing the screen back on...");
        preventScreenOn(false);
    }
    private Runnable mForceReenableScreenTask = new Runnable() {
            public void run() {
                forceReenableScreen();
            }
        };
    private int setScreenStateLocked(boolean on) {
        int err = Power.setScreenState(on);
        if (err == 0) {
            mLastScreenOnTime = (on ? SystemClock.elapsedRealtime() : 0);
            if (mUseSoftwareAutoBrightness) {
                enableLightSensor(on);
                if (!on) {
                    mButtonLight.turnOff();
                    mKeyboardLight.turnOff();
                    mLightSensorValue = -1;
                    mHighestLightSensorValue = -1;
                }
            }
        }
        return err;
    }
    private void setPowerState(int state)
    {
        setPowerState(state, false, WindowManagerPolicy.OFF_BECAUSE_OF_TIMEOUT);
    }
    private void setPowerState(int newState, boolean noChangeLights, int reason)
    {
        synchronized (mLocks) {
            int err;
            if (mSpew) {
                Slog.d(TAG, "setPowerState: mPowerState=0x" + Integer.toHexString(mPowerState)
                        + " newState=0x" + Integer.toHexString(newState)
                        + " noChangeLights=" + noChangeLights
                        + " reason=" + reason);
            }
            if (noChangeLights) {
                newState = (newState & ~LIGHTS_MASK) | (mPowerState & LIGHTS_MASK);
            }
            if (mProximitySensorActive) {
                newState = (newState & ~SCREEN_BRIGHT);
            }
            if (batteryIsLow()) {
                newState |= BATTERY_LOW_BIT;
            } else {
                newState &= ~BATTERY_LOW_BIT;
            }
            if (newState == mPowerState) {
                return;
            }
            if (!mBootCompleted && !mUseSoftwareAutoBrightness) {
                newState |= ALL_BRIGHT;
            }
            boolean oldScreenOn = (mPowerState & SCREEN_ON_BIT) != 0;
            boolean newScreenOn = (newState & SCREEN_ON_BIT) != 0;
            if (mSpew) {
                Slog.d(TAG, "setPowerState: mPowerState=" + mPowerState
                        + " newState=" + newState + " noChangeLights=" + noChangeLights);
                Slog.d(TAG, "  oldKeyboardBright=" + ((mPowerState & KEYBOARD_BRIGHT_BIT) != 0)
                         + " newKeyboardBright=" + ((newState & KEYBOARD_BRIGHT_BIT) != 0));
                Slog.d(TAG, "  oldScreenBright=" + ((mPowerState & SCREEN_BRIGHT_BIT) != 0)
                         + " newScreenBright=" + ((newState & SCREEN_BRIGHT_BIT) != 0));
                Slog.d(TAG, "  oldButtonBright=" + ((mPowerState & BUTTON_BRIGHT_BIT) != 0)
                         + " newButtonBright=" + ((newState & BUTTON_BRIGHT_BIT) != 0));
                Slog.d(TAG, "  oldScreenOn=" + oldScreenOn
                         + " newScreenOn=" + newScreenOn);
                Slog.d(TAG, "  oldBatteryLow=" + ((mPowerState & BATTERY_LOW_BIT) != 0)
                         + " newBatteryLow=" + ((newState & BATTERY_LOW_BIT) != 0));
            }
            if (mPowerState != newState) {
                updateLightsLocked(newState, 0);
                mPowerState = (mPowerState & ~LIGHTS_MASK) | (newState & LIGHTS_MASK);
            }
            if (oldScreenOn != newScreenOn) {
                if (newScreenOn) {
                    if (mStillNeedSleepNotification) {
                        sendNotificationLocked(false, WindowManagerPolicy.OFF_BECAUSE_OF_USER);
                    }
                    boolean reallyTurnScreenOn = true;
                    if (mSpew) {
                        Slog.d(TAG, "- turning screen on...  mPreventScreenOn = "
                              + mPreventScreenOn);
                    }
                    if (mPreventScreenOn) {
                        if (mSpew) {
                            Slog.d(TAG, "- PREVENTING screen from really turning on!");
                        }
                        reallyTurnScreenOn = false;
                    }
                    if (reallyTurnScreenOn) {
                        err = setScreenStateLocked(true);
                        long identity = Binder.clearCallingIdentity();
                        try {
                            mBatteryStats.noteScreenBrightness(getPreferredBrightness());
                            mBatteryStats.noteScreenOn();
                        } catch (RemoteException e) {
                            Slog.w(TAG, "RemoteException calling noteScreenOn on BatteryStatsService", e);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    } else {
                        setScreenStateLocked(false);
                        err = 0;
                    }
                    mLastTouchDown = 0;
                    mTotalTouchDownTime = 0;
                    mTouchCycles = 0;
                    EventLog.writeEvent(EventLogTags.POWER_SCREEN_STATE, 1, reason,
                            mTotalTouchDownTime, mTouchCycles);
                    if (err == 0) {
                        mPowerState |= SCREEN_ON_BIT;
                        sendNotificationLocked(true, -1);
                    }
                } else {
                    mHandler.removeCallbacks(mAutoBrightnessTask);
                    mScreenOffTime = SystemClock.elapsedRealtime();
                    long identity = Binder.clearCallingIdentity();
                    try {
                        mBatteryStats.noteScreenOff();
                    } catch (RemoteException e) {
                        Slog.w(TAG, "RemoteException calling noteScreenOff on BatteryStatsService", e);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                    mPowerState &= ~SCREEN_ON_BIT;
                    mScreenOffReason = reason;
                    if (!mScreenBrightness.animating) {
                        err = screenOffFinishedAnimatingLocked(reason);
                    } else {
                        err = 0;
                        mLastTouchDown = 0;
                    }
                }
            }
        }
    }
    private int screenOffFinishedAnimatingLocked(int reason) {
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_STATE, 0, reason, mTotalTouchDownTime, mTouchCycles);
        mLastTouchDown = 0;
        int err = setScreenStateLocked(false);
        if (err == 0) {
            mScreenOffReason = reason;
            sendNotificationLocked(false, reason);
        }
        return err;
    }
    private boolean batteryIsLow() {
        return (!mIsPowered &&
                mBatteryService.getBatteryLevel() <= Power.LOW_BATTERY_THRESHOLD);
    }
    private void updateLightsLocked(int newState, int forceState) {
        final int oldState = mPowerState;
        newState = applyButtonState(newState);
        newState = applyKeyboardState(newState);
        final int realDifference = (newState ^ oldState);
        final int difference = realDifference | forceState;
        if (difference == 0) {
            return;
        }
        int offMask = 0;
        int dimMask = 0;
        int onMask = 0;
        int preferredBrightness = getPreferredBrightness();
        boolean startAnimation = false;
        if ((difference & KEYBOARD_BRIGHT_BIT) != 0) {
            if (ANIMATE_KEYBOARD_LIGHTS) {
                if ((newState & KEYBOARD_BRIGHT_BIT) == 0) {
                    mKeyboardBrightness.setTargetLocked(Power.BRIGHTNESS_OFF,
                            ANIM_STEPS, INITIAL_KEYBOARD_BRIGHTNESS,
                            Power.BRIGHTNESS_ON);
                } else {
                    mKeyboardBrightness.setTargetLocked(Power.BRIGHTNESS_ON,
                            ANIM_STEPS, INITIAL_KEYBOARD_BRIGHTNESS,
                            Power.BRIGHTNESS_OFF);
                }
                startAnimation = true;
            } else {
                if ((newState & KEYBOARD_BRIGHT_BIT) == 0) {
                    offMask |= KEYBOARD_BRIGHT_BIT;
                } else {
                    onMask |= KEYBOARD_BRIGHT_BIT;
                }
            }
        }
        if ((difference & BUTTON_BRIGHT_BIT) != 0) {
            if (ANIMATE_BUTTON_LIGHTS) {
                if ((newState & BUTTON_BRIGHT_BIT) == 0) {
                    mButtonBrightness.setTargetLocked(Power.BRIGHTNESS_OFF,
                            ANIM_STEPS, INITIAL_BUTTON_BRIGHTNESS,
                            Power.BRIGHTNESS_ON);
                } else {
                    mButtonBrightness.setTargetLocked(Power.BRIGHTNESS_ON,
                            ANIM_STEPS, INITIAL_BUTTON_BRIGHTNESS,
                            Power.BRIGHTNESS_OFF);
                }
                startAnimation = true;
            } else {
                if ((newState & BUTTON_BRIGHT_BIT) == 0) {
                    offMask |= BUTTON_BRIGHT_BIT;
                } else {
                    onMask |= BUTTON_BRIGHT_BIT;
                }
            }
        }
        if ((difference & (SCREEN_ON_BIT | SCREEN_BRIGHT_BIT)) != 0) {
            if (ANIMATE_SCREEN_LIGHTS) {
                int nominalCurrentValue = -1;
                if ((realDifference & (SCREEN_ON_BIT | SCREEN_BRIGHT_BIT)) != 0) {
                    switch (oldState & (SCREEN_BRIGHT_BIT|SCREEN_ON_BIT)) {
                        case SCREEN_BRIGHT_BIT | SCREEN_ON_BIT:
                            nominalCurrentValue = preferredBrightness;
                            break;
                        case SCREEN_ON_BIT:
                            nominalCurrentValue = Power.BRIGHTNESS_DIM;
                            break;
                        case 0:
                            nominalCurrentValue = Power.BRIGHTNESS_OFF;
                            break;
                        case SCREEN_BRIGHT_BIT:
                        default:
                            nominalCurrentValue = (int)mScreenBrightness.curValue;
                            break;
                    }
                }
                int brightness = preferredBrightness;
                int steps = ANIM_STEPS;
                if ((newState & SCREEN_BRIGHT_BIT) == 0) {
                    final float scale = 1.5f;
                    float ratio = (((float)Power.BRIGHTNESS_DIM)/preferredBrightness);
                    if (ratio > 1.0f) ratio = 1.0f;
                    if ((newState & SCREEN_ON_BIT) == 0) {
                        if ((oldState & SCREEN_BRIGHT_BIT) != 0) {
                            steps = ANIM_STEPS;
                        } else {
                            steps = (int)(ANIM_STEPS*ratio*scale);
                        }
                        brightness = Power.BRIGHTNESS_OFF;
                    } else {
                        if ((oldState & SCREEN_ON_BIT) != 0) {
                            steps = (int)(ANIM_STEPS*(1.0f-ratio)*scale);
                        } else {
                            steps = (int)(ANIM_STEPS*ratio);
                        }
                        if (mStayOnConditions != 0 && mBatteryService.isPowered(mStayOnConditions)) {
                            mScreenOffTime = SystemClock.elapsedRealtime();
                        }
                        brightness = Power.BRIGHTNESS_DIM;
                    }
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    mBatteryStats.noteScreenBrightness(brightness);
                } catch (RemoteException e) {
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
                if (mScreenBrightness.setTargetLocked(brightness,
                        steps, INITIAL_SCREEN_BRIGHTNESS, nominalCurrentValue)) {
                    startAnimation = true;
                }
            } else {
                if ((newState & SCREEN_BRIGHT_BIT) == 0) {
                    if ((newState & SCREEN_ON_BIT) == 0) {
                        offMask |= SCREEN_BRIGHT_BIT;
                    } else {
                        dimMask |= SCREEN_BRIGHT_BIT;
                    }
                } else {
                    onMask |= SCREEN_BRIGHT_BIT;
                }
            }
        }
        if (startAnimation) {
            if (mSpew) {
                Slog.i(TAG, "Scheduling light animator!");
            }
            mHandler.removeCallbacks(mLightAnimator);
            mHandler.post(mLightAnimator);
        }
        if (offMask != 0) {
            if (mSpew) Slog.i(TAG, "Setting brightess off: " + offMask);
            setLightBrightness(offMask, Power.BRIGHTNESS_OFF);
        }
        if (dimMask != 0) {
            int brightness = Power.BRIGHTNESS_DIM;
            if ((newState & BATTERY_LOW_BIT) != 0 &&
                    brightness > Power.BRIGHTNESS_LOW_BATTERY) {
                brightness = Power.BRIGHTNESS_LOW_BATTERY;
            }
            if (mSpew) Slog.i(TAG, "Setting brightess dim " + brightness + ": " + dimMask);
            setLightBrightness(dimMask, brightness);
        }
        if (onMask != 0) {
            int brightness = getPreferredBrightness();
            if ((newState & BATTERY_LOW_BIT) != 0 &&
                    brightness > Power.BRIGHTNESS_LOW_BATTERY) {
                brightness = Power.BRIGHTNESS_LOW_BATTERY;
            }
            if (mSpew) Slog.i(TAG, "Setting brightess on " + brightness + ": " + onMask);
            setLightBrightness(onMask, brightness);
        }
    }
    private void setLightBrightness(int mask, int value) {
        int brightnessMode = (mAutoBrightessEnabled
                            ? LightsService.BRIGHTNESS_MODE_SENSOR
                            : LightsService.BRIGHTNESS_MODE_USER);
        if ((mask & SCREEN_BRIGHT_BIT) != 0) {
            mLcdLight.setBrightness(value, brightnessMode);
        }
        if ((mask & BUTTON_BRIGHT_BIT) != 0) {
            mButtonLight.setBrightness(value);
        }
        if ((mask & KEYBOARD_BRIGHT_BIT) != 0) {
            mKeyboardLight.setBrightness(value);
        }
    }
    class BrightnessState {
        final int mask;
        boolean initialized;
        int targetValue;
        float curValue;
        float delta;
        boolean animating;
        BrightnessState(int m) {
            mask = m;
        }
        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "animating=" + animating
                    + " targetValue=" + targetValue
                    + " curValue=" + curValue
                    + " delta=" + delta);
        }
        boolean setTargetLocked(int target, int stepsToTarget, int initialValue,
                int nominalCurrentValue) {
            if (!initialized) {
                initialized = true;
                curValue = (float)initialValue;
            } else if (targetValue == target) {
                return false;
            }
            targetValue = target;
            delta = (targetValue -
                    (nominalCurrentValue >= 0 ? nominalCurrentValue : curValue))
                    / stepsToTarget;
            if (mSpew) {
                String noticeMe = nominalCurrentValue == curValue ? "" : "  ******************";
                Slog.i(TAG, "Setting target " + mask + ": cur=" + curValue
                        + " target=" + targetValue + " delta=" + delta
                        + " nominalCurrentValue=" + nominalCurrentValue
                        + noticeMe);
            }
            animating = true;
            return true;
        }
        boolean stepLocked() {
            if (!animating) return false;
            if (false && mSpew) {
                Slog.i(TAG, "Step target " + mask + ": cur=" + curValue
                        + " target=" + targetValue + " delta=" + delta);
            }
            curValue += delta;
            int curIntValue = (int)curValue;
            boolean more = true;
            if (delta == 0) {
                curValue = curIntValue = targetValue;
                more = false;
            } else if (delta > 0) {
                if (curIntValue >= targetValue) {
                    curValue = curIntValue = targetValue;
                    more = false;
                }
            } else {
                if (curIntValue <= targetValue) {
                    curValue = curIntValue = targetValue;
                    more = false;
                }
            }
            setLightBrightness(mask, curIntValue);
            animating = more;
            if (!more) {
                if (mask == SCREEN_BRIGHT_BIT && curIntValue == Power.BRIGHTNESS_OFF) {
                    screenOffFinishedAnimatingLocked(mScreenOffReason);
                }
            }
            return more;
        }
    }
    private class LightAnimator implements Runnable {
        public void run() {
            synchronized (mLocks) {
                long now = SystemClock.uptimeMillis();
                boolean more = mScreenBrightness.stepLocked();
                if (mKeyboardBrightness.stepLocked()) {
                    more = true;
                }
                if (mButtonBrightness.stepLocked()) {
                    more = true;
                }
                if (more) {
                    mHandler.postAtTime(mLightAnimator, now+(1000/60));
                }
            }
        }
    }
    private int getPreferredBrightness() {
        try {
            if (mScreenBrightnessOverride >= 0) {
                return mScreenBrightnessOverride;
            } else if (mLightSensorScreenBrightness >= 0 && mUseSoftwareAutoBrightness
                    && mAutoBrightessEnabled) {
                return mLightSensorScreenBrightness;
            }
            final int brightness = Settings.System.getInt(mContext.getContentResolver(),
                                                          SCREEN_BRIGHTNESS);
            return Math.max(brightness, Power.BRIGHTNESS_DIM);
        } catch (SettingNotFoundException snfe) {
            return Power.BRIGHTNESS_ON;
        }
    }
    private int applyButtonState(int state) {
        int brightness = -1;
        if ((state & BATTERY_LOW_BIT) != 0) {
            return state;
        }
        if (mButtonBrightnessOverride >= 0) {
            brightness = mButtonBrightnessOverride;
        } else if (mLightSensorButtonBrightness >= 0 && mUseSoftwareAutoBrightness) {
            brightness = mLightSensorButtonBrightness;
        }
        if (brightness > 0) {
            return state | BUTTON_BRIGHT_BIT;
        } else if (brightness == 0) {
            return state & ~BUTTON_BRIGHT_BIT;
        } else {
            return state;
        }
    }
    private int applyKeyboardState(int state) {
        int brightness = -1;
        if ((state & BATTERY_LOW_BIT) != 0) {
            return state;
        }
        if (!mKeyboardVisible) {
            brightness = 0;
        } else if (mButtonBrightnessOverride >= 0) {
            brightness = mButtonBrightnessOverride;
        } else if (mLightSensorKeyboardBrightness >= 0 && mUseSoftwareAutoBrightness) {
            brightness =  mLightSensorKeyboardBrightness;
        }
        if (brightness > 0) {
            return state | KEYBOARD_BRIGHT_BIT;
        } else if (brightness == 0) {
            return state & ~KEYBOARD_BRIGHT_BIT;
        } else {
            return state;
        }
    }
    public boolean isScreenOn() {
        synchronized (mLocks) {
            return (mPowerState & SCREEN_ON_BIT) != 0;
        }
    }
    boolean isScreenBright() {
        synchronized (mLocks) {
            return (mPowerState & SCREEN_BRIGHT) == SCREEN_BRIGHT;
        }
    }
    private boolean isScreenTurningOffLocked() {
        return (mScreenBrightness.animating && mScreenBrightness.targetValue == 0);
    }
    private void forceUserActivityLocked() {
        if (isScreenTurningOffLocked()) {
            mScreenBrightness.animating = false;
        }
        boolean savedActivityAllowed = mUserActivityAllowed;
        mUserActivityAllowed = true;
        userActivity(SystemClock.uptimeMillis(), false);
        mUserActivityAllowed = savedActivityAllowed;
    }
    public void userActivityWithForce(long time, boolean noChangeLights, boolean force) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        userActivity(time, -1, noChangeLights, OTHER_EVENT, force);
    }
    public void userActivity(long time, boolean noChangeLights) {
        userActivity(time, -1, noChangeLights, OTHER_EVENT, false);
    }
    public void userActivity(long time, boolean noChangeLights, int eventType) {
        userActivity(time, -1, noChangeLights, eventType, false);
    }
    public void userActivity(long time, boolean noChangeLights, int eventType, boolean force) {
        userActivity(time, -1, noChangeLights, eventType, force);
    }
    public void clearUserActivityTimeout(long now, long timeout) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        Slog.i(TAG, "clearUserActivity for " + timeout + "ms from now");
        userActivity(now, timeout, false, OTHER_EVENT, false);
    }
    private void userActivity(long time, long timeoutOverride, boolean noChangeLights,
            int eventType, boolean force) {
        if (((mPokey & POKE_LOCK_IGNORE_CHEEK_EVENTS) != 0)
                && (eventType == CHEEK_EVENT || eventType == TOUCH_EVENT)) {
            if (false) {
                Slog.d(TAG, "dropping cheek or short event mPokey=0x" + Integer.toHexString(mPokey));
            }
            return;
        }
        if (((mPokey & POKE_LOCK_IGNORE_TOUCH_AND_CHEEK_EVENTS) != 0)
                && (eventType == TOUCH_EVENT || eventType == TOUCH_UP_EVENT
                    || eventType == LONG_TOUCH_EVENT || eventType == CHEEK_EVENT)) {
            if (false) {
                Slog.d(TAG, "dropping touch mPokey=0x" + Integer.toHexString(mPokey));
            }
            return;
        }
        if (false) {
            if (((mPokey & POKE_LOCK_IGNORE_CHEEK_EVENTS) != 0)) {
                Slog.d(TAG, "userActivity !!!");
            } else {
                Slog.d(TAG, "mPokey=0x" + Integer.toHexString(mPokey));
            }
        }
        synchronized (mLocks) {
            if (mSpew) {
                Slog.d(TAG, "userActivity mLastEventTime=" + mLastEventTime + " time=" + time
                        + " mUserActivityAllowed=" + mUserActivityAllowed
                        + " mUserState=0x" + Integer.toHexString(mUserState)
                        + " mWakeLockState=0x" + Integer.toHexString(mWakeLockState)
                        + " mProximitySensorActive=" + mProximitySensorActive
                        + " timeoutOverride=" + timeoutOverride
                        + " force=" + force);
            }
            if (isScreenTurningOffLocked()) {
                Slog.d(TAG, "ignoring user activity while turning off screen");
                return;
            }
            if (mProximitySensorActive && mProximityWakeLockCount == 0) {
                mProximitySensorActive = false;
            }
            if (mLastEventTime <= time || force) {
                mLastEventTime = time;
                if ((mUserActivityAllowed && !mProximitySensorActive) || force) {
                    if (eventType == BUTTON_EVENT && !mUseSoftwareAutoBrightness) {
                        mUserState = (mKeyboardVisible ? ALL_BRIGHT : SCREEN_BUTTON_BRIGHT);
                    } else {
                        mUserState |= SCREEN_BRIGHT;
                    }
                    int uid = Binder.getCallingUid();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        mBatteryStats.noteUserActivity(uid, eventType);
                    } catch (RemoteException e) {
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                    mWakeLockState = mLocks.reactivateScreenLocksLocked();
                    setPowerState(mUserState | mWakeLockState, noChangeLights,
                            WindowManagerPolicy.OFF_BECAUSE_OF_USER);
                    setTimeoutLocked(time, timeoutOverride, SCREEN_BRIGHT);
                }
            }
        }
        if (mPolicy != null) {
            mPolicy.userActivity();
        }
    }
    private int getAutoBrightnessValue(int sensorValue, int[] values) {
        try {
            int i;
            for (i = 0; i < mAutoBrightnessLevels.length; i++) {
                if (sensorValue < mAutoBrightnessLevels[i]) {
                    break;
                }
            }
            return values[i];
        } catch (Exception e) {
            Slog.e(TAG, "getAutoBrightnessValue", e);
            return 255;
        }
    }
    private Runnable mProximityTask = new Runnable() {
        public void run() {
            synchronized (mLocks) {
                if (mProximityPendingValue != -1) {
                    proximityChangedLocked(mProximityPendingValue == 1);
                    mProximityPendingValue = -1;
                }
                if (mProximityPartialLock.isHeld()) {
                    mProximityPartialLock.release();
                }
            }
        }
    };
    private Runnable mAutoBrightnessTask = new Runnable() {
        public void run() {
            synchronized (mLocks) {
                int value = (int)mLightSensorPendingValue;
                if (value >= 0) {
                    mLightSensorPendingValue = -1;
                    lightSensorChangedLocked(value);
                }
            }
        }
    };
    private void dockStateChanged(int state) {
        synchronized (mLocks) {
            mIsDocked = (state != Intent.EXTRA_DOCK_STATE_UNDOCKED);
            if (mIsDocked) {
                mHighestLightSensorValue = -1;
            }
            if ((mPowerState & SCREEN_ON_BIT) != 0) {
                int value = (int)mLightSensorValue;
                mLightSensorValue = -1;
                lightSensorChangedLocked(value);
            }
        }
    }
    private void lightSensorChangedLocked(int value) {
        if (mDebugLightSensor) {
            Slog.d(TAG, "lightSensorChangedLocked " + value);
        }
        if (mHighestLightSensorValue < value) {
            mHighestLightSensorValue = value;
        }
        if (mLightSensorValue != value) {
            mLightSensorValue = value;
            if ((mPowerState & BATTERY_LOW_BIT) == 0) {
                int lcdValue = getAutoBrightnessValue(
                        (mIsDocked ? value : mHighestLightSensorValue),
                        mLcdBacklightValues);
                int buttonValue = getAutoBrightnessValue(value, mButtonBacklightValues);
                int keyboardValue;
                if (mKeyboardVisible) {
                    keyboardValue = getAutoBrightnessValue(value, mKeyboardBacklightValues);
                } else {
                    keyboardValue = 0;
                }
                mLightSensorScreenBrightness = lcdValue;
                mLightSensorButtonBrightness = buttonValue;
                mLightSensorKeyboardBrightness = keyboardValue;
                if (mDebugLightSensor) {
                    Slog.d(TAG, "lcdValue " + lcdValue);
                    Slog.d(TAG, "buttonValue " + buttonValue);
                    Slog.d(TAG, "keyboardValue " + keyboardValue);
                }
                boolean startAnimation = false;
                if (mAutoBrightessEnabled && mScreenBrightnessOverride < 0) {
                    if (ANIMATE_SCREEN_LIGHTS) {
                        if (mScreenBrightness.setTargetLocked(lcdValue,
                                AUTOBRIGHTNESS_ANIM_STEPS, INITIAL_SCREEN_BRIGHTNESS,
                                (int)mScreenBrightness.curValue)) {
                            startAnimation = true;
                        }
                    } else {
                        int brightnessMode = (mAutoBrightessEnabled
                                            ? LightsService.BRIGHTNESS_MODE_SENSOR
                                            : LightsService.BRIGHTNESS_MODE_USER);
                        mLcdLight.setBrightness(lcdValue, brightnessMode);
                    }
                }
                if (mButtonBrightnessOverride < 0) {
                    if (ANIMATE_BUTTON_LIGHTS) {
                        if (mButtonBrightness.setTargetLocked(buttonValue,
                                AUTOBRIGHTNESS_ANIM_STEPS, INITIAL_BUTTON_BRIGHTNESS,
                                (int)mButtonBrightness.curValue)) {
                            startAnimation = true;
                        }
                    } else {
                         mButtonLight.setBrightness(buttonValue);
                    }
                }
                if (mButtonBrightnessOverride < 0 || !mKeyboardVisible) {
                    if (ANIMATE_KEYBOARD_LIGHTS) {
                        if (mKeyboardBrightness.setTargetLocked(keyboardValue,
                                AUTOBRIGHTNESS_ANIM_STEPS, INITIAL_BUTTON_BRIGHTNESS,
                                (int)mKeyboardBrightness.curValue)) {
                            startAnimation = true;
                        }
                    } else {
                        mKeyboardLight.setBrightness(keyboardValue);
                    }
                }
                if (startAnimation) {
                    if (mDebugLightSensor) {
                        Slog.i(TAG, "lightSensorChangedLocked scheduling light animator");
                    }
                    mHandler.removeCallbacks(mLightAnimator);
                    mHandler.post(mLightAnimator);
                }
            }
        }
    }
    public void goToSleep(long time)
    {
        goToSleepWithReason(time, WindowManagerPolicy.OFF_BECAUSE_OF_USER);
    }
    public void goToSleepWithReason(long time, int reason)
    {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        synchronized (mLocks) {
            goToSleepLocked(time, reason);
        }
    }
    public void reboot(String reason)
    {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.REBOOT, null);
        if (mHandler == null || !ActivityManagerNative.isSystemReady()) {
            throw new IllegalStateException("Too early to call reboot()");
        }
        final String finalReason = reason;
        Runnable runnable = new Runnable() {
            public void run() {
                synchronized (this) {
                    ShutdownThread.reboot(mContext, finalReason, false);
                }
            }
        };
        mHandler.post(runnable);
        synchronized (runnable) {
            while (true) {
                try {
                    runnable.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    public void crash(final String message)
    {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.REBOOT, null);
        Thread t = new Thread("PowerManagerService.crash()") {
            public void run() { throw new RuntimeException(message); }
        };
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            Log.wtf(TAG, e);
        }
    }
    private void goToSleepLocked(long time, int reason) {
        if (mLastEventTime <= time) {
            mLastEventTime = time;
            mWakeLockState = SCREEN_OFF;
            int N = mLocks.size();
            int numCleared = 0;
            for (int i=0; i<N; i++) {
                WakeLock wl = mLocks.get(i);
                if (isScreenLock(wl.flags)) {
                    mLocks.get(i).activated = false;
                    numCleared++;
                }
            }
            EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numCleared);
            mStillNeedSleepNotification = true;
            mUserState = SCREEN_OFF;
            setPowerState(SCREEN_OFF, false, reason);
            cancelTimerLocked();
        }
    }
    public long timeSinceScreenOn() {
        synchronized (mLocks) {
            if ((mPowerState & SCREEN_ON_BIT) != 0) {
                return 0;
            }
            return SystemClock.elapsedRealtime() - mScreenOffTime;
        }
    }
    public void setKeyboardVisibility(boolean visible) {
        synchronized (mLocks) {
            if (mSpew) {
                Slog.d(TAG, "setKeyboardVisibility: " + visible);
            }
            if (mKeyboardVisible != visible) {
                mKeyboardVisible = visible;
                if ((mPowerState & SCREEN_ON_BIT) != 0) {
                    if (mUseSoftwareAutoBrightness) {
                        if (mLightSensorValue >= 0) {
                            int value = (int)mLightSensorValue;
                            mLightSensorValue = -1;
                            lightSensorChangedLocked(value);
                        }
                    }
                    userActivity(SystemClock.uptimeMillis(), false, BUTTON_EVENT, true);
                }
            }
        }
    }
    public void enableUserActivity(boolean enabled) {
        if (mSpew) {
            Slog.d(TAG, "enableUserActivity " + enabled);
        }
        synchronized (mLocks) {
            mUserActivityAllowed = enabled;
            if (!enabled) {
                setTimeoutLocked(SystemClock.uptimeMillis(), 0);
            }
        }
    }
    private void setScreenBrightnessMode(int mode) {
        boolean enabled = (mode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        if (mUseSoftwareAutoBrightness && mAutoBrightessEnabled != enabled) {
            mAutoBrightessEnabled = enabled;
            if (isScreenOn()) {
                if (mLightSensorValue >= 0) {
                    int value = (int)mLightSensorValue;
                    mLightSensorValue = -1;
                    lightSensorChangedLocked(value);
                }
            }
        }
    }
    private void setScreenOffTimeoutsLocked() {
        if ((mPokey & POKE_LOCK_SHORT_TIMEOUT) != 0) {
            mKeylightDelay = mShortKeylightDelay;  
            mDimDelay = -1;
            mScreenOffDelay = 0;
        } else if ((mPokey & POKE_LOCK_MEDIUM_TIMEOUT) != 0) {
            mKeylightDelay = MEDIUM_KEYLIGHT_DELAY;
            mDimDelay = -1;
            mScreenOffDelay = 0;
        } else {
            int totalDelay = mScreenOffTimeoutSetting;
            if (totalDelay > mMaximumScreenOffTimeout) {
                totalDelay = mMaximumScreenOffTimeout;
            }
            mKeylightDelay = LONG_KEYLIGHT_DELAY;
            if (totalDelay < 0) {
                mScreenOffDelay = Integer.MAX_VALUE;
            } else if (mKeylightDelay < totalDelay) {
                mScreenOffDelay = totalDelay - mKeylightDelay;
            } else {
                mScreenOffDelay = 0;
            }
            if (mDimScreen && totalDelay >= (LONG_KEYLIGHT_DELAY + LONG_DIM_TIME)) {
                mDimDelay = mScreenOffDelay - LONG_DIM_TIME;
                mScreenOffDelay = LONG_DIM_TIME;
            } else {
                mDimDelay = -1;
            }
        }
        if (mSpew) {
            Slog.d(TAG, "setScreenOffTimeouts mKeylightDelay=" + mKeylightDelay
                    + " mDimDelay=" + mDimDelay + " mScreenOffDelay=" + mScreenOffDelay
                    + " mDimScreen=" + mDimScreen);
        }
    }
    private void updateSettingsValues() {
        mShortKeylightDelay = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.SHORT_KEYLIGHT_DELAY_MS,
                SHORT_KEYLIGHT_DELAY_DEFAULT);
    }
    private class LockList extends ArrayList<WakeLock>
    {
        void addLock(WakeLock wl)
        {
            int index = getIndex(wl.binder);
            if (index < 0) {
                this.add(wl);
            }
        }
        WakeLock removeLock(IBinder binder)
        {
            int index = getIndex(binder);
            if (index >= 0) {
                return this.remove(index);
            } else {
                return null;
            }
        }
        int getIndex(IBinder binder)
        {
            int N = this.size();
            for (int i=0; i<N; i++) {
                if (this.get(i).binder == binder) {
                    return i;
                }
            }
            return -1;
        }
        int gatherState()
        {
            int result = 0;
            int N = this.size();
            for (int i=0; i<N; i++) {
                WakeLock wl = this.get(i);
                if (wl.activated) {
                    if (isScreenLock(wl.flags)) {
                        result |= wl.minState;
                    }
                }
            }
            return result;
        }
        int reactivateScreenLocksLocked()
        {
            int result = 0;
            int N = this.size();
            for (int i=0; i<N; i++) {
                WakeLock wl = this.get(i);
                if (isScreenLock(wl.flags)) {
                    wl.activated = true;
                    result |= wl.minState;
                }
            }
            return result;
        }
    }
    void setPolicy(WindowManagerPolicy p) {
        synchronized (mLocks) {
            mPolicy = p;
            mLocks.notifyAll();
        }
    }
    WindowManagerPolicy getPolicyLocked() {
        while (mPolicy == null || !mDoneBooting) {
            try {
                mLocks.wait();
            } catch (InterruptedException e) {
            }
        }
        return mPolicy;
    }
    void systemReady() {
        mSensorManager = new SensorManager(mHandlerThread.getLooper());
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mUseSoftwareAutoBrightness) {
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            enableLightSensor(true);
        }
        if (mUseSoftwareAutoBrightness) {
            setPowerState(SCREEN_BRIGHT);
        } else {
            setPowerState(ALL_BRIGHT);
        }
        synchronized (mLocks) {
            Slog.d(TAG, "system ready!");
            mDoneBooting = true;
            long identity = Binder.clearCallingIdentity();
            try {
                mBatteryStats.noteScreenBrightness(getPreferredBrightness());
                mBatteryStats.noteScreenOn();
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }
    void bootCompleted() {
        Slog.d(TAG, "bootCompleted");
        synchronized (mLocks) {
            mBootCompleted = true;
            userActivity(SystemClock.uptimeMillis(), false, BUTTON_EVENT, true);
            updateWakeLockLocked();
            mLocks.notifyAll();
        }
    }
    public void monitor() {
        synchronized (mLocks) { }
    }
    public int getSupportedWakeLockFlags() {
        int result = PowerManager.PARTIAL_WAKE_LOCK
                   | PowerManager.FULL_WAKE_LOCK
                   | PowerManager.SCREEN_DIM_WAKE_LOCK;
        if (mProximitySensor != null) {
            result |= PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
        }
        return result;
    }
    public void setBacklightBrightness(int brightness) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        brightness = Math.max(brightness, Power.BRIGHTNESS_DIM);
        mLcdLight.setBrightness(brightness);
        mKeyboardLight.setBrightness(mKeyboardVisible ? brightness : 0);
        mButtonLight.setBrightness(brightness);
        long identity = Binder.clearCallingIdentity();
        try {
            mBatteryStats.noteScreenBrightness(brightness);
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException calling noteScreenBrightness on BatteryStatsService", e);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        if (ANIMATE_SCREEN_LIGHTS) {
            mScreenBrightness.curValue = brightness;
            mScreenBrightness.animating = false;
            mScreenBrightness.targetValue = -1;
        }
        if (ANIMATE_KEYBOARD_LIGHTS) {
            mKeyboardBrightness.curValue = brightness;
            mKeyboardBrightness.animating = false;
            mKeyboardBrightness.targetValue = -1;
        }
        if (ANIMATE_BUTTON_LIGHTS) {
            mButtonBrightness.curValue = brightness;
            mButtonBrightness.animating = false;
            mButtonBrightness.targetValue = -1;
        }
    }
    public void setAttentionLight(boolean on, int color) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        mAttentionLight.setFlashing(color, LightsService.LIGHT_FLASH_HARDWARE, (on ? 3 : 0), 0);
    }
    private void enableProximityLockLocked() {
        if (mDebugProximitySensor) {
            Slog.d(TAG, "enableProximityLockLocked");
        }
        if (!mProximitySensorEnabled) {
            long identity = Binder.clearCallingIdentity();
            try {
                mSensorManager.registerListener(mProximityListener, mProximitySensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
                mProximitySensorEnabled = true;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }
    private void disableProximityLockLocked() {
        if (mDebugProximitySensor) {
            Slog.d(TAG, "disableProximityLockLocked");
        }
        if (mProximitySensorEnabled) {
            long identity = Binder.clearCallingIdentity();
            try {
                mSensorManager.unregisterListener(mProximityListener);
                mHandler.removeCallbacks(mProximityTask);
                if (mProximityPartialLock.isHeld()) {
                    mProximityPartialLock.release();
                }
                mProximitySensorEnabled = false;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
            if (mProximitySensorActive) {
                mProximitySensorActive = false;
                forceUserActivityLocked();
            }
        }
    }
    private void proximityChangedLocked(boolean active) {
        if (mDebugProximitySensor) {
            Slog.d(TAG, "proximityChangedLocked, active: " + active);
        }
        if (!mProximitySensorEnabled) {
            Slog.d(TAG, "Ignoring proximity change after sensor is disabled");
            return;
        }
        if (active) {
            goToSleepLocked(SystemClock.uptimeMillis(),
                    WindowManagerPolicy.OFF_BECAUSE_OF_PROX_SENSOR);
            mProximitySensorActive = true;
        } else {
            mProximitySensorActive = false;
            forceUserActivityLocked();
            if (mProximityWakeLockCount == 0) {
                disableProximityLockLocked();
            }
        }
    }
    private void enableLightSensor(boolean enable) {
        if (mDebugLightSensor) {
            Slog.d(TAG, "enableLightSensor " + enable);
        }
        if (mSensorManager != null && mLightSensorEnabled != enable) {
            mLightSensorEnabled = enable;
            long identity = Binder.clearCallingIdentity();
            try {
                if (enable) {
                    mSensorManager.registerListener(mLightListener, mLightSensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    mSensorManager.unregisterListener(mLightListener);
                    mHandler.removeCallbacks(mAutoBrightnessTask);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }
    SensorEventListener mProximityListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            long milliseconds = SystemClock.elapsedRealtime();
            synchronized (mLocks) {
                float distance = event.values[0];
                long timeSinceLastEvent = milliseconds - mLastProximityEventTime;
                mLastProximityEventTime = milliseconds;
                mHandler.removeCallbacks(mProximityTask);
                boolean proximityTaskQueued = false;
                boolean active = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD &&
                        distance < mProximitySensor.getMaximumRange());
                if (mDebugProximitySensor) {
                    Slog.d(TAG, "mProximityListener.onSensorChanged active: " + active);
                }
                if (timeSinceLastEvent < PROXIMITY_SENSOR_DELAY) {
                    mProximityPendingValue = (active ? 1 : 0);
                    mHandler.postDelayed(mProximityTask, PROXIMITY_SENSOR_DELAY - timeSinceLastEvent);
                    proximityTaskQueued = true;
                } else {
                    mProximityPendingValue = -1;
                    proximityChangedLocked(active);
                }
                boolean held = mProximityPartialLock.isHeld();
                if (!held && proximityTaskQueued) {
                    mProximityPartialLock.acquire();
                } else if (held && !proximityTaskQueued) {
                    mProximityPartialLock.release();
                }
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    SensorEventListener mLightListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (mLocks) {
                if (isScreenTurningOffLocked()) {
                    return;
                }
                int value = (int)event.values[0];
                long milliseconds = SystemClock.elapsedRealtime();
                if (mDebugLightSensor) {
                    Slog.d(TAG, "onSensorChanged: light value: " + value);
                }
                mHandler.removeCallbacks(mAutoBrightnessTask);
                if (mLightSensorValue != value) {
                    if (mLightSensorValue == -1 ||
                            milliseconds < mLastScreenOnTime + mLightSensorWarmupTime) {
                        lightSensorChangedLocked(value);
                    } else {
                        mLightSensorPendingValue = value;
                        mHandler.postDelayed(mAutoBrightnessTask, LIGHT_SENSOR_DELAY);
                    }
                } else {
                    mLightSensorPendingValue = -1;
                }
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
