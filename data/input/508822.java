class UiModeManagerService extends IUiModeManager.Stub {
    private static final String TAG = UiModeManager.class.getSimpleName();
    private static final boolean LOG = false;
    private static final String KEY_LAST_UPDATE_INTERVAL = "LAST_UPDATE_INTERVAL";
    private static final int MSG_UPDATE_TWILIGHT = 0;
    private static final int MSG_ENABLE_LOCATION_UPDATES = 1;
    private static final int MSG_GET_NEW_LOCATION_UPDATE = 2;
    private static final long LOCATION_UPDATE_MS = 24 * DateUtils.HOUR_IN_MILLIS;
    private static final long MIN_LOCATION_UPDATE_MS = 30 * DateUtils.MINUTE_IN_MILLIS;
    private static final float LOCATION_UPDATE_DISTANCE_METER = 1000 * 20;
    private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MIN = 5000;
    private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MAX = 15 * DateUtils.MINUTE_IN_MILLIS;
    private static final double FACTOR_GMT_OFFSET_LONGITUDE = 1000.0 * 360.0 / DateUtils.DAY_IN_MILLIS;
    private static final String ACTION_UPDATE_NIGHT_MODE = "com.android.server.action.UPDATE_NIGHT_MODE";
    private final Context mContext;
    final Object mLock = new Object();
    private int mDockState = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    private int mLastBroadcastState = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    private int mNightMode = UiModeManager.MODE_NIGHT_NO;
    private boolean mCarModeEnabled = false;
    private boolean mCharging = false;
    private final boolean mCarModeKeepsScreenOn;
    private final boolean mDeskModeKeepsScreenOn;
    private boolean mComputedNightMode;
    private int mCurUiMode = 0;
    private int mSetUiMode = 0;
    private boolean mHoldingConfiguration = false;
    private Configuration mConfiguration = new Configuration();
    private boolean mSystemReady;
    private NotificationManager mNotificationManager;
    private AlarmManager mAlarmManager;
    private LocationManager mLocationManager;
    private Location mLocation;
    private StatusBarManager mStatusBarManager;
    private final PowerManager.WakeLock mWakeLock;
    static Intent buildHomeIntent(String category) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }
    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() != Activity.RESULT_OK) {
                return;
            }
            final int  enableFlags = intent.getIntExtra("enableFlags", 0);
            final int  disableFlags = intent.getIntExtra("disableFlags", 0);
            synchronized (mLock) {
                String category = null;
                if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
                    if ((enableFlags&UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME) != 0) {
                        category = Intent.CATEGORY_CAR_DOCK;
                    }
                } else if (UiModeManager.ACTION_ENTER_DESK_MODE.equals(intent.getAction())) {
                    if ((enableFlags&UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME) != 0) {
                        category = Intent.CATEGORY_DESK_DOCK;
                    }
                } else {
                    if ((disableFlags&UiModeManager.DISABLE_CAR_MODE_GO_HOME) != 0) {
                        category = Intent.CATEGORY_HOME;
                    }
                }
                if (category != null) {
                    Intent homeIntent = buildHomeIntent(category);
                    Configuration newConfig = null;
                    if (mHoldingConfiguration) {
                        mHoldingConfiguration = false;
                        updateConfigurationLocked(false);
                        newConfig = mConfiguration;
                    }
                    try {
                        ActivityManagerNative.getDefault().startActivityWithConfig(
                                null, homeIntent, null, null, 0, null, null, 0, false, false,
                                newConfig);
                        mHoldingConfiguration = false;
                    } catch (RemoteException e) {
                        Slog.w(TAG, e.getCause());
                    }
                }
                if (mHoldingConfiguration) {
                    mHoldingConfiguration = false;
                    updateConfigurationLocked(true);
                }
            }
        }
    };
    private final BroadcastReceiver mTwilightUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDoingNightMode() && mNightMode == UiModeManager.MODE_NIGHT_AUTO) {
                mHandler.sendEmptyMessage(MSG_UPDATE_TWILIGHT);
            }
        }
    };
    private final BroadcastReceiver mDockModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                    Intent.EXTRA_DOCK_STATE_UNDOCKED);
            updateDockState(state);
        }
    };
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCharging = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0);
            synchronized (mLock) {
                if (mSystemReady) {
                    updateLocked(0, 0);
                }
            }
        }
    };
    private final BroadcastReceiver mUpdateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                if (!intent.getBooleanExtra("state", false)) {
                    mHandler.sendEmptyMessage(MSG_GET_NEW_LOCATION_UPDATE);
                }
            } else {
                mHandler.sendEmptyMessage(MSG_GET_NEW_LOCATION_UPDATE);
            }
        }
    };
    private final LocationListener mEmptyLocationListener =  new LocationListener() {
        public void onLocationChanged(Location location) {
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            final boolean hasMoved = hasMoved(location);
            final boolean hasBetterAccuracy = mLocation == null
                    || location.getAccuracy() < mLocation.getAccuracy();
            if (hasMoved || hasBetterAccuracy) {
                synchronized (mLock) {
                    mLocation = location;
                    if (hasMoved && isDoingNightMode()
                            && mNightMode == UiModeManager.MODE_NIGHT_AUTO) {
                        mHandler.sendEmptyMessage(MSG_UPDATE_TWILIGHT);
                    }
                }
            }
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        private boolean hasMoved(Location location) {
            if (location == null) {
                return false;
            }
            if (mLocation == null) {
                return true;
            }
            if (location.getTime() < mLocation.getTime()) {
                return false;
            }
            float distance = mLocation.distanceTo(location);
            float totalAccuracy = mLocation.getAccuracy() + location.getAccuracy();
            return distance >= totalAccuracy;
        }
    };
    public UiModeManagerService(Context context) {
        mContext = context;
        ServiceManager.addService(Context.UI_MODE_SERVICE, this);
        mAlarmManager =
            (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        mLocationManager =
            (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        mContext.registerReceiver(mTwilightUpdateReceiver,
                new IntentFilter(ACTION_UPDATE_NIGHT_MODE));
        mContext.registerReceiver(mDockModeReceiver,
                new IntentFilter(Intent.ACTION_DOCK_EVENT));
        mContext.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mUpdateLocationReceiver, filter);
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        mConfiguration.setToDefaults();
        mCarModeKeepsScreenOn = (context.getResources().getInteger(
                com.android.internal.R.integer.config_carDockKeepsScreenOn) == 1);
        mDeskModeKeepsScreenOn = (context.getResources().getInteger(
                com.android.internal.R.integer.config_deskDockKeepsScreenOn) == 1);
        mNightMode = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.UI_NIGHT_MODE, UiModeManager.MODE_NIGHT_AUTO);
    }
    public void disableCarMode(int flags) {
        synchronized (mLock) {
            setCarModeLocked(false);
            if (mSystemReady) {
                updateLocked(0, flags);
            }
        }
    }
    public void enableCarMode(int flags) {
        synchronized (mLock) {
            setCarModeLocked(true);
            if (mSystemReady) {
                updateLocked(flags, 0);
            }
        }
    }
    public int getCurrentModeType() {
        synchronized (mLock) {
            return mCurUiMode & Configuration.UI_MODE_TYPE_MASK;
        }
    }
    public void setNightMode(int mode) throws RemoteException {
        synchronized (mLock) {
            switch (mode) {
                case UiModeManager.MODE_NIGHT_NO:
                case UiModeManager.MODE_NIGHT_YES:
                case UiModeManager.MODE_NIGHT_AUTO:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown mode: " + mode);
            }
            if (!isDoingNightMode()) {
                return;
            }
            if (mNightMode != mode) {
                long ident = Binder.clearCallingIdentity();
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.UI_NIGHT_MODE, mode);
                Binder.restoreCallingIdentity(ident);
                mNightMode = mode;
                updateLocked(0, 0);
            }
        }
    }
    public int getNightMode() throws RemoteException {
        return mNightMode;
    }
    void systemReady() {
        synchronized (mLock) {
            mSystemReady = true;
            mCarModeEnabled = mDockState == Intent.EXTRA_DOCK_STATE_CAR;
            updateLocked(0, 0);
            mHandler.sendEmptyMessage(MSG_ENABLE_LOCATION_UPDATES);
        }
    }
    boolean isDoingNightMode() {
        return mCarModeEnabled || mDockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
    }
    void setCarModeLocked(boolean enabled) {
        if (mCarModeEnabled != enabled) {
            mCarModeEnabled = enabled;
        }
    }
    void updateDockState(int newState) {
        synchronized (mLock) {
            if (newState != mDockState) {
                mDockState = newState;
                setCarModeLocked(mDockState == Intent.EXTRA_DOCK_STATE_CAR);
                if (mSystemReady) {
                    updateLocked(UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME, 0);
                }
            }
        }
    }
    final void updateConfigurationLocked(boolean sendIt) {
        int uiMode = Configuration.UI_MODE_TYPE_NORMAL;
        if (mCarModeEnabled) {
            uiMode = Configuration.UI_MODE_TYPE_CAR;
        } else if (mDockState == Intent.EXTRA_DOCK_STATE_DESK) {
            uiMode = Configuration.UI_MODE_TYPE_DESK;
        }
        if (mCarModeEnabled) {
            if (mNightMode == UiModeManager.MODE_NIGHT_AUTO) {
                updateTwilightLocked();
                uiMode |= mComputedNightMode ? Configuration.UI_MODE_NIGHT_YES
                        : Configuration.UI_MODE_NIGHT_NO;
            } else {
                uiMode |= mNightMode << 4;
            }
        } else {
            uiMode = (uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_NO;
        }
        if (LOG) {
            Slog.d(TAG, 
                "updateConfigurationLocked: mDockState=" + mDockState 
                + "; mCarMode=" + mCarModeEnabled
                + "; mNightMode=" + mNightMode
                + "; uiMode=" + uiMode);
        }
        mCurUiMode = uiMode;
        if (!mHoldingConfiguration && uiMode != mSetUiMode) {
            mSetUiMode = uiMode;
            mConfiguration.uiMode = uiMode;
            if (sendIt) {
                try {
                    ActivityManagerNative.getDefault().updateConfiguration(mConfiguration);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failure communicating with activity manager", e);
                }
            }
        }
    }
    final void updateLocked(int enableFlags, int disableFlags) {
        long ident = Binder.clearCallingIdentity();
        try {
            String action = null;
            String oldAction = null;
            if (mLastBroadcastState == Intent.EXTRA_DOCK_STATE_CAR) {
                adjustStatusBarCarModeLocked();
                oldAction = UiModeManager.ACTION_EXIT_CAR_MODE;
            } else if (mLastBroadcastState == Intent.EXTRA_DOCK_STATE_DESK) {
                oldAction = UiModeManager.ACTION_EXIT_DESK_MODE;
            }
            if (mCarModeEnabled) {
                if (mLastBroadcastState != Intent.EXTRA_DOCK_STATE_CAR) {
                    adjustStatusBarCarModeLocked();
                    if (oldAction != null) {
                        mContext.sendBroadcast(new Intent(oldAction));
                    }
                    mLastBroadcastState = Intent.EXTRA_DOCK_STATE_CAR;
                    action = UiModeManager.ACTION_ENTER_CAR_MODE;
                }
            } else if (mDockState == Intent.EXTRA_DOCK_STATE_DESK) {
                if (mLastBroadcastState != Intent.EXTRA_DOCK_STATE_DESK) {
                    if (oldAction != null) {
                        mContext.sendBroadcast(new Intent(oldAction));
                    }
                    mLastBroadcastState = Intent.EXTRA_DOCK_STATE_DESK;
                    action = UiModeManager.ACTION_ENTER_DESK_MODE;
                }
            } else {
                mLastBroadcastState = Intent.EXTRA_DOCK_STATE_UNDOCKED;
                action = oldAction;
            }
            if (action != null) {
                Intent intent = new Intent(action);
                intent.putExtra("enableFlags", enableFlags);
                intent.putExtra("disableFlags", disableFlags);
                mContext.sendOrderedBroadcast(intent, null,
                        mResultReceiver, null, Activity.RESULT_OK, null, null);
                mHoldingConfiguration = true;
            } else {
                Intent homeIntent = null;
                if (mCarModeEnabled) {
                    if ((enableFlags&UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME) != 0) {
                        homeIntent = buildHomeIntent(Intent.CATEGORY_CAR_DOCK);
                    }
                } else if (mDockState == Intent.EXTRA_DOCK_STATE_DESK) {
                    if ((enableFlags&UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME) != 0) {
                        homeIntent = buildHomeIntent(Intent.CATEGORY_DESK_DOCK);
                    }
                } else {
                    if ((disableFlags&UiModeManager.DISABLE_CAR_MODE_GO_HOME) != 0) {
                        homeIntent = buildHomeIntent(Intent.CATEGORY_HOME);
                    }
                }
                if (homeIntent != null) {
                    try {
                        mContext.startActivity(homeIntent);
                    } catch (ActivityNotFoundException e) {
                    }
                }
            }
            updateConfigurationLocked(true);
            boolean keepScreenOn = mCharging &&
                    ((mCarModeEnabled && mCarModeKeepsScreenOn) ||
                     (mCurUiMode == Configuration.UI_MODE_TYPE_DESK && mDeskModeKeepsScreenOn));
            if (keepScreenOn != mWakeLock.isHeld()) {
                if (keepScreenOn) {
                    mWakeLock.acquire();
                } else {
                    mWakeLock.release();
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    private void adjustStatusBarCarModeLocked() {
        if (mStatusBarManager == null) {
            mStatusBarManager = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        if (mStatusBarManager != null) {
            mStatusBarManager.disable(mCarModeEnabled
                ? StatusBarManager.DISABLE_NOTIFICATION_TICKER
                : StatusBarManager.DISABLE_NONE);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mNotificationManager != null) {
            if (mCarModeEnabled) {
                Intent carModeOffIntent = new Intent(mContext, DisableCarModeActivity.class);
                Notification n = new Notification();
                n.icon = R.drawable.stat_notify_car_mode;
                n.defaults = Notification.DEFAULT_LIGHTS;
                n.flags = Notification.FLAG_ONGOING_EVENT;
                n.when = 0;
                n.setLatestEventInfo(
                        mContext,
                        mContext.getString(R.string.car_mode_disable_notification_title),
                        mContext.getString(R.string.car_mode_disable_notification_message),
                        PendingIntent.getActivity(mContext, 0, carModeOffIntent, 0));
                mNotificationManager.notify(0, n);
            } else {
                mNotificationManager.cancel(0);
            }
        }
    }
    private final Handler mHandler = new Handler() {
        boolean mPassiveListenerEnabled;
        boolean mNetworkListenerEnabled;
        boolean mDidFirstInit;
        long mLastNetworkRegisterTime = -MIN_LOCATION_UPDATE_MS;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TWILIGHT:
                    synchronized (mLock) {
                        if (isDoingNightMode() && mLocation != null
                                && mNightMode == UiModeManager.MODE_NIGHT_AUTO) {
                            updateTwilightLocked();
                            updateLocked(0, 0);
                        }
                    }
                    break;
                case MSG_GET_NEW_LOCATION_UPDATE:
                    if (!mNetworkListenerEnabled) {
                        return;
                    }
                    if ((mLastNetworkRegisterTime+MIN_LOCATION_UPDATE_MS)
                            >= SystemClock.elapsedRealtime()) {
                        return;
                    }
                    mNetworkListenerEnabled = false;
                    mLocationManager.removeUpdates(mEmptyLocationListener);
                case MSG_ENABLE_LOCATION_UPDATES:
                    boolean networkLocationEnabled;
                    try {
                        networkLocationEnabled =
                            mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception e) {
                        networkLocationEnabled = false;
                    }
                    if (!mNetworkListenerEnabled && networkLocationEnabled) {
                        mNetworkListenerEnabled = true;
                        mLastNetworkRegisterTime = SystemClock.elapsedRealtime();
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                LOCATION_UPDATE_MS, 0, mEmptyLocationListener);
                        if (!mDidFirstInit) {
                            mDidFirstInit = true;
                            if (mLocation == null) {
                                retrieveLocation();
                            }
                            synchronized (mLock) {
                                if (isDoingNightMode() && mLocation != null
                                        && mNightMode == UiModeManager.MODE_NIGHT_AUTO) {
                                    updateTwilightLocked();
                                    updateLocked(0, 0);
                                }
                            }
                        }
                    }
                   boolean passiveLocationEnabled;
                    try {
                        passiveLocationEnabled =
                            mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                    } catch (Exception e) {
                        passiveLocationEnabled = false;
                    }
                    if (!mPassiveListenerEnabled && passiveLocationEnabled) {
                        mPassiveListenerEnabled = true;
                        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                0, LOCATION_UPDATE_DISTANCE_METER , mLocationListener);
                    }
                    if (!(mNetworkListenerEnabled && mPassiveListenerEnabled)) {
                        long interval = msg.getData().getLong(KEY_LAST_UPDATE_INTERVAL);
                        interval *= 1.5;
                        if (interval == 0) {
                            interval = LOCATION_UPDATE_ENABLE_INTERVAL_MIN;
                        } else if (interval > LOCATION_UPDATE_ENABLE_INTERVAL_MAX) {
                            interval = LOCATION_UPDATE_ENABLE_INTERVAL_MAX;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putLong(KEY_LAST_UPDATE_INTERVAL, interval);
                        Message newMsg = mHandler.obtainMessage(MSG_ENABLE_LOCATION_UPDATES);
                        newMsg.setData(bundle);
                        mHandler.sendMessageDelayed(newMsg, interval);
                    }
                    break;
            }
        }
        private void retrieveLocation() {
            Location location = null;
            final Iterator<String> providers =
                    mLocationManager.getProviders(new Criteria(), true).iterator();
            while (providers.hasNext()) {
                final Location lastKnownLocation =
                        mLocationManager.getLastKnownLocation(providers.next());
                if (location == null || (lastKnownLocation != null &&
                        location.getTime() < lastKnownLocation.getTime())) {
                    location = lastKnownLocation;
                }
            }
            if (location == null) {
                Time currentTime = new Time();
                currentTime.set(System.currentTimeMillis());
                double lngOffset = FACTOR_GMT_OFFSET_LONGITUDE *
                        (currentTime.gmtoff - (currentTime.isDst > 0 ? 3600 : 0));
                location = new Location("fake");
                location.setLongitude(lngOffset);
                location.setLatitude(0);
                location.setAccuracy(417000.0f);
                location.setTime(System.currentTimeMillis());
            }
            synchronized (mLock) {
                mLocation = location;
            }
        }
    };
    void updateTwilightLocked() {
        if (mLocation == null) {
            return;
        }
        final long currentTime = System.currentTimeMillis();
        boolean nightMode;
        TwilightCalculator tw = new TwilightCalculator();
        tw.calculateTwilight(currentTime,
                mLocation.getLatitude(), mLocation.getLongitude());
        if (tw.mState == TwilightCalculator.DAY) {
            nightMode = false;
        } else {
            nightMode = true;
        }
        long nextUpdate = 0;
        if (tw.mSunrise == -1 || tw.mSunset == -1) {
            nextUpdate = currentTime + 12 * DateUtils.HOUR_IN_MILLIS;
        } else {
            final int mLastTwilightState = tw.mState;
            nextUpdate += DateUtils.MINUTE_IN_MILLIS;
            if (currentTime > tw.mSunset) {
                tw.calculateTwilight(currentTime
                        + DateUtils.DAY_IN_MILLIS, mLocation.getLatitude(),
                        mLocation.getLongitude());
            }
            if (mLastTwilightState == TwilightCalculator.NIGHT) {
                nextUpdate += tw.mSunrise;
            } else {
                nextUpdate += tw.mSunset;
            }
        }
        Intent updateIntent = new Intent(ACTION_UPDATE_NIGHT_MODE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, 0, updateIntent, 0);
        mAlarmManager.cancel(pendingIntent);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        mComputedNightMode = nightMode;
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump uimode service from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (mLock) {
            pw.println("Current UI Mode Service state:");
            pw.print("  mDockState="); pw.print(mDockState);
                    pw.print(" mLastBroadcastState="); pw.println(mLastBroadcastState);
            pw.print("  mNightMode="); pw.print(mNightMode);
                    pw.print(" mCarModeEnabled="); pw.print(mCarModeEnabled);
                    pw.print(" mComputedNightMode="); pw.println(mComputedNightMode);
            pw.print("  mCurUiMode=0x"); pw.print(Integer.toHexString(mCurUiMode));
                    pw.print(" mSetUiMode=0x"); pw.println(Integer.toHexString(mSetUiMode));
            pw.print("  mHoldingConfiguration="); pw.print(mHoldingConfiguration);
                    pw.print(" mSystemReady="); pw.println(mSystemReady);
            if (mLocation != null) {
                pw.print("  mLocation="); pw.println(mLocation);
            }
        }
    }
}
