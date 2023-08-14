public class StatusBarPolicy {
    private static final String TAG = "StatusBarPolicy";
    private static StatusBarPolicy sInstance;
    private static final int EVENT_BATTERY_CLOSE = 4;
    private final Context mContext;
    private final StatusBarService mService;
    private final Handler mHandler = new StatusBarHandler();
    private final IBatteryStats mBatteryStats;
    private Calendar mCalendar;
    private String mClockFormatString;
    private SimpleDateFormat mClockFormat;
    private IBinder mClockIcon;
    private IconData mClockData;
    private StorageManager mStorageManager;
    private IBinder mBatteryIcon;
    private IconData mBatteryData;
    private boolean mBatteryFirst = true;
    private boolean mBatteryPlugged;
    private int mBatteryLevel;
    private AlertDialog mLowBatteryDialog;
    private TextView mBatteryLevelTextView;
    private View mBatteryView;
    private int mBatteryViewSequence;
    private boolean mBatteryShowLowOnEndCall = false;
    private static final boolean SHOW_LOW_BATTERY_WARNING = true;
    private static final boolean SHOW_BATTERY_WARNINGS_IN_CALL = true;
    private TelephonyManager mPhone;
    private IBinder mPhoneIcon;
    private IconData mPhoneData;
    private static final int[] sSignalImages = new int[] {
        com.android.internal.R.drawable.stat_sys_signal_0,
        com.android.internal.R.drawable.stat_sys_signal_1,
        com.android.internal.R.drawable.stat_sys_signal_2,
        com.android.internal.R.drawable.stat_sys_signal_3,
        com.android.internal.R.drawable.stat_sys_signal_4
    };
    private static final int[] sSignalImages_r = new int[] {
        com.android.internal.R.drawable.stat_sys_r_signal_0,
        com.android.internal.R.drawable.stat_sys_r_signal_1,
        com.android.internal.R.drawable.stat_sys_r_signal_2,
        com.android.internal.R.drawable.stat_sys_r_signal_3,
        com.android.internal.R.drawable.stat_sys_r_signal_4
    };
    private static final int[] sRoamingIndicatorImages_cdma = new int[] {
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0, 
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0, 
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0, 
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0, 
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0,
        com.android.internal.R.drawable.stat_sys_roaming_cdma_0 
    };
    private int[] mDataIconList = sDataNetType_g;
    private static final int[] sDataNetType_g = new int[] {
            com.android.internal.R.drawable.stat_sys_data_connected_g,
            com.android.internal.R.drawable.stat_sys_data_in_g,
            com.android.internal.R.drawable.stat_sys_data_out_g,
            com.android.internal.R.drawable.stat_sys_data_inandout_g,
        };
    private static final int[] sDataNetType_3g = new int[] {
            com.android.internal.R.drawable.stat_sys_data_connected_3g,
            com.android.internal.R.drawable.stat_sys_data_in_3g,
            com.android.internal.R.drawable.stat_sys_data_out_3g,
            com.android.internal.R.drawable.stat_sys_data_inandout_3g,
        };
    private static final int[] sDataNetType_e = new int[] {
            com.android.internal.R.drawable.stat_sys_data_connected_e,
            com.android.internal.R.drawable.stat_sys_data_in_e,
            com.android.internal.R.drawable.stat_sys_data_out_e,
            com.android.internal.R.drawable.stat_sys_data_inandout_e,
        };
    private static final int[] sDataNetType_h = new int[] {
            com.android.internal.R.drawable.stat_sys_data_connected_h,
            com.android.internal.R.drawable.stat_sys_data_in_h,
            com.android.internal.R.drawable.stat_sys_data_out_h,
            com.android.internal.R.drawable.stat_sys_data_inandout_h,
    };
    private static final int[] sDataNetType_1x = new int[] {
        com.android.internal.R.drawable.stat_sys_data_connected_1x,
        com.android.internal.R.drawable.stat_sys_data_in_1x,
        com.android.internal.R.drawable.stat_sys_data_out_1x,
        com.android.internal.R.drawable.stat_sys_data_inandout_1x,
    };
    IccCard.State mSimState = IccCard.State.READY;
    int mPhoneState = TelephonyManager.CALL_STATE_IDLE;
    int mDataState = TelephonyManager.DATA_DISCONNECTED;
    int mDataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
    ServiceState mServiceState;
    SignalStrength mSignalStrength;
    private IBinder mDataIcon;
    private IconData mDataData;
    private boolean mDataIconVisible;
    private boolean mHspaDataDistinguishable;
    private IBinder mVolumeIcon;
    private IconData mVolumeData;
    private boolean mVolumeVisible;
    private IBinder mBluetoothIcon;
    private IconData mBluetoothData;
    private int mBluetoothHeadsetState;
    private boolean mBluetoothA2dpConnected;
    private int mBluetoothPbapState;
    private boolean mBluetoothEnabled;
    private static final int[] sWifiSignalImages = new int[] {
            com.android.internal.R.drawable.stat_sys_wifi_signal_1,
            com.android.internal.R.drawable.stat_sys_wifi_signal_2,
            com.android.internal.R.drawable.stat_sys_wifi_signal_3,
            com.android.internal.R.drawable.stat_sys_wifi_signal_4,
        };
    private static final int sWifiTemporarilyNotConnectedImage =
            com.android.internal.R.drawable.stat_sys_wifi_signal_0;
    private int mLastWifiSignalLevel = -1;
    private boolean mIsWifiConnected = false;
    private IBinder mWifiIcon;
    private IconData mWifiData;
    private IBinder mGpsIcon;
    private IconData mGpsEnabledIconData;
    private IconData mGpsFixIconData;
    private IBinder mAlarmClockIcon;
    private IconData mAlarmClockIconData;
    private IBinder mSyncActiveIcon;
    private IBinder mSyncFailingIcon;
    private IBinder mTTYModeIcon;
    private IconData mTTYModeEnableIconData;
    private IBinder mCdmaRoamingIndicatorIcon;
    private IconData mCdmaRoamingIndicatorIconData;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                updateClock();
            }
            else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                updateClock();
            }
            else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                updateBattery(intent);
            }
            else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                updateClock();
            }
            else if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                if (mClockFormat != null) {
                    mClockFormat.setTimeZone(mCalendar.getTimeZone());
                }
                updateClock();
            }
            else if (action.equals(Intent.ACTION_ALARM_CHANGED)) {
                updateAlarm(intent);
            }
            else if (action.equals(Intent.ACTION_SYNC_STATE_CHANGED)) {
                updateSyncState(intent);
            }
            else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                onBatteryLow(intent);
            }
            else if (action.equals(Intent.ACTION_BATTERY_OKAY)
                    || action.equals(Intent.ACTION_POWER_CONNECTED)) {
                onBatteryOkay(intent);
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ||
                    action.equals(BluetoothHeadset.ACTION_STATE_CHANGED) ||
                    action.equals(BluetoothA2dp.ACTION_SINK_STATE_CHANGED) ||
                    action.equals(BluetoothPbap.PBAP_STATE_CHANGED_ACTION)) {
                updateBluetooth(intent);
            }
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                    action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
                    action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                updateWifi(intent);
            }
            else if (action.equals(GpsLocationProvider.GPS_ENABLED_CHANGE_ACTION) ||
                    action.equals(GpsLocationProvider.GPS_FIX_CHANGE_ACTION)) {
                updateGps(intent);
            }
            else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) ||
                    action.equals(AudioManager.VIBRATE_SETTING_CHANGED_ACTION)) {
                updateVolume();
            }
            else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                updateSimState(intent);
            }
            else if (action.equals(TtyIntent.TTY_ENABLED_CHANGE_ACTION)) {
                updateTTY(intent);
            }
        }
    };
    private StatusBarPolicy(Context context, StatusBarService service) {
        mContext = context;
        mService = service;
        mSignalStrength = new SignalStrength();
        mBatteryStats = BatteryStatsService.getService();
        mCalendar = Calendar.getInstance(TimeZone.getDefault());
        mClockData = IconData.makeText("clock", "");
        mClockIcon = service.addIcon(mClockData, null);
        updateClock();
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(
                new com.android.server.status.StorageNotification(context));
        mBatteryData = IconData.makeIcon("battery",
                null, com.android.internal.R.drawable.stat_sys_battery_unknown, 0, 0);
        mBatteryIcon = service.addIcon(mBatteryData, null);
        mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneData = IconData.makeIcon("phone_signal",
                null, com.android.internal.R.drawable.stat_sys_signal_null, 0, 0);
        mPhoneIcon = service.addIcon(mPhoneData, null);
        ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                .listen(mPhoneStateListener,
                          PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY);
        mDataData = IconData.makeIcon("data_connection",
                null, com.android.internal.R.drawable.stat_sys_data_connected_g, 0, 0);
        mDataIcon = service.addIcon(mDataData, null);
        service.setIconVisibility(mDataIcon, false);
        mWifiData = IconData.makeIcon("wifi", null, sWifiSignalImages[0], 0, 0);
        mWifiIcon = service.addIcon(mWifiData, null);
        service.setIconVisibility(mWifiIcon, false);
        mTTYModeEnableIconData = IconData.makeIcon("tty",
                null, com.android.internal.R.drawable.stat_sys_tty_mode, 0, 0);
        mTTYModeIcon = service.addIcon(mTTYModeEnableIconData, null);
        service.setIconVisibility(mTTYModeIcon, false);
        mCdmaRoamingIndicatorIconData = IconData.makeIcon("cdma_eri",
                null, com.android.internal.R.drawable.stat_sys_roaming_cdma_0, 0, 0);
        mCdmaRoamingIndicatorIcon = service.addIcon(mCdmaRoamingIndicatorIconData, null);
        service.setIconVisibility(mCdmaRoamingIndicatorIcon, false);
        mBluetoothData = IconData.makeIcon("bluetooth",
                null, com.android.internal.R.drawable.stat_sys_data_bluetooth, 0, 0);
        mBluetoothIcon = service.addIcon(mBluetoothData, null);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            mBluetoothEnabled = adapter.isEnabled();
        } else {
            mBluetoothEnabled = false;
        }
        mBluetoothA2dpConnected = false;
        mBluetoothHeadsetState = BluetoothHeadset.STATE_DISCONNECTED;
        mBluetoothPbapState = BluetoothPbap.STATE_DISCONNECTED;
        mService.setIconVisibility(mBluetoothIcon, mBluetoothEnabled);
        mGpsEnabledIconData = IconData.makeIcon("gps",
                null, com.android.internal.R.drawable.stat_sys_gps_acquiring_anim, 0, 0);
        mGpsFixIconData = IconData.makeIcon("gps",
                null, com.android.internal.R.drawable.stat_sys_gps_on, 0, 0);
        mGpsIcon = service.addIcon(mGpsEnabledIconData, null);
        service.setIconVisibility(mGpsIcon, false);
        mAlarmClockIconData = IconData.makeIcon(
                "alarm_clock",
                null, com.android.internal.R.drawable.stat_notify_alarm, 0, 0);
        mAlarmClockIcon = service.addIcon(mAlarmClockIconData, null);
        service.setIconVisibility(mAlarmClockIcon, false);
        mSyncActiveIcon = service.addIcon(IconData.makeIcon("sync_active",
                null, R.drawable.stat_notify_sync_anim0, 0, 0), null);
        mSyncFailingIcon = service.addIcon(IconData.makeIcon("sync_failing",
                null, R.drawable.stat_notify_sync_error, 0, 0), null);
        service.setIconVisibility(mSyncActiveIcon, false);
        service.setIconVisibility(mSyncFailingIcon, false);
        mVolumeData = IconData.makeIcon("volume",
                null, com.android.internal.R.drawable.stat_sys_ringer_silent, 0, 0);
        mVolumeIcon = service.addIcon(mVolumeData, null);
        service.setIconVisibility(mVolumeIcon, false);
        updateVolume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_ALARM_CHANGED);
        filter.addAction(Intent.ACTION_SYNC_STATE_CHANGED);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_SINK_STATE_CHANGED);
        filter.addAction(BluetoothPbap.PBAP_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(GpsLocationProvider.GPS_ENABLED_CHANGE_ACTION);
        filter.addAction(GpsLocationProvider.GPS_FIX_CHANGE_ACTION);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(TtyIntent.TTY_ENABLED_CHANGE_ACTION);
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
        try {
            mHspaDataDistinguishable = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_hspa_data_distinguishable);
        } catch (Exception e) {
            mHspaDataDistinguishable = false;
        }
    }
    public static void installIcons(Context context, StatusBarService service) {
        sInstance = new StatusBarPolicy(context, service);
    }
    private final CharSequence getSmallTime() {
        boolean b24 = DateFormat.is24HourFormat(mContext);
        int res;
        if (b24) {
            res = R.string.twenty_four_hour_time_format;
        } else {
            res = R.string.twelve_hour_time_format;
        }
        final char MAGIC1 = '\uEF00';
        final char MAGIC2 = '\uEF01';
        SimpleDateFormat sdf;
        String format = mContext.getString(res);
        if (!format.equals(mClockFormatString)) {
            int a = -1;
            boolean quoted = false;
            for (int i = 0; i < format.length(); i++) {
                char c = format.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                }
                if (!quoted && c == 'a') {
                    a = i;
                    break;
                }
            }
            if (a >= 0) {
                final int b = a;
                while (a > 0 && Character.isWhitespace(format.charAt(a-1))) {
                    a--;
                }
                format = format.substring(0, a) + MAGIC1 + format.substring(a, b)
                        + "a" + MAGIC2 + format.substring(b + 1);
            }
            mClockFormat = sdf = new SimpleDateFormat(format);
            mClockFormatString = format;
        } else {
            sdf = mClockFormat;
        }
        String result = sdf.format(mCalendar.getTime());
        int magic1 = result.indexOf(MAGIC1);
        int magic2 = result.indexOf(MAGIC2);
        if (magic1 >= 0 && magic2 > magic1) {
            SpannableStringBuilder formatted = new SpannableStringBuilder(result);
            formatted.setSpan(new RelativeSizeSpan(0.7f), magic1, magic2,
                              Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            formatted.delete(magic2, magic2 + 1);
            formatted.delete(magic1, magic1 + 1);
            return formatted;
        } else {
            return result;
        }
    }
    private final void updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mClockData.text = getSmallTime();
        mService.updateIcon(mClockIcon, mClockData, null);
    }
    private final void updateAlarm(Intent intent) {
        boolean alarmSet = intent.getBooleanExtra("alarmSet", false);
        mService.setIconVisibility(mAlarmClockIcon, alarmSet);
    }
    private final void updateSyncState(Intent intent) {
        boolean isActive = intent.getBooleanExtra("active", false);
        boolean isFailing = intent.getBooleanExtra("failing", false);
        mService.setIconVisibility(mSyncActiveIcon, isActive);
    }
    private final void updateBattery(Intent intent) {
        mBatteryData.iconId = intent.getIntExtra("icon-small", 0);
        mBatteryData.iconLevel = intent.getIntExtra("level", 0);
        mService.updateIcon(mBatteryIcon, mBatteryData, null);
        boolean plugged = intent.getIntExtra("plugged", 0) != 0;
        int level = intent.getIntExtra("level", -1);
        if (false) {
            Slog.d(TAG, "updateBattery level=" + level
                    + " plugged=" + plugged
                    + " mBatteryPlugged=" + mBatteryPlugged
                    + " mBatteryLevel=" + mBatteryLevel
                    + " mBatteryFirst=" + mBatteryFirst);
        }
        boolean oldPlugged = mBatteryPlugged;
        mBatteryPlugged = plugged;
        mBatteryLevel = level;
        if (mBatteryFirst) {
            mBatteryFirst = false;
        }
        if (false) {
            Slog.d(TAG, "plugged=" + plugged + " oldPlugged=" + oldPlugged + " level=" + level);
        }
    }
    private void onBatteryLow(Intent intent) {
        if (SHOW_LOW_BATTERY_WARNING) {
            if (false) {
                Slog.d(TAG, "mPhoneState=" + mPhoneState
                      + " mLowBatteryDialog=" + mLowBatteryDialog
                      + " mBatteryShowLowOnEndCall=" + mBatteryShowLowOnEndCall);
            }
            if (SHOW_BATTERY_WARNINGS_IN_CALL || mPhoneState == TelephonyManager.CALL_STATE_IDLE) {
                showLowBatteryWarning();
            } else {
                mBatteryShowLowOnEndCall = true;
            }
        }
    }
    private void onBatteryOkay(Intent intent) {
        if (mLowBatteryDialog != null
                && SHOW_LOW_BATTERY_WARNING) {
            mLowBatteryDialog.dismiss();
            mBatteryShowLowOnEndCall = false;
        }
    }
    private void showBatteryView() {
        closeLastBatteryView();
        if (mLowBatteryDialog != null) {
            mLowBatteryDialog.dismiss();
        }
        int level = mBatteryLevel;
        View v = View.inflate(mContext, com.android.internal.R.layout.battery_status, null);
        mBatteryView = v;
        int pixelFormat = PixelFormat.TRANSLUCENT;
        Drawable bg = v.getBackground();
        if (bg != null) {
            pixelFormat = bg.getOpacity();
        }
        int flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            flags |= WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                flags, pixelFormat);
        TypedArray a = mContext.obtainStyledAttributes(
                com.android.internal.R.styleable.Theme);
        lp.dimAmount = a.getFloat(android.R.styleable.Theme_backgroundDimAmount, 0.5f);
        a.recycle();
        lp.setTitle("Battery");
        TextView levelTextView = (TextView)v.findViewById(com.android.internal.R.id.level_percent);
        levelTextView.setText(mContext.getString(
                    com.android.internal.R.string.battery_status_text_percent_format, level));
        setBatteryLevel(v, com.android.internal.R.id.spacer, 100-level, 0, 0);
        setBatteryLevel(v, com.android.internal.R.id.level, level,
                com.android.internal.R.drawable.battery_charge_fill, level);
        WindowManagerImpl.getDefault().addView(v, lp);
        scheduleCloseBatteryView();
    }
    private void setBatteryLevel(View parent, int id, int height, int background, int level) {
        ImageView v = (ImageView)parent.findViewById(id);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
        lp.weight = height;
        if (background != 0) {
            v.setBackgroundResource(background);
            Drawable bkg = v.getBackground();
            bkg.setLevel(level);
        }
    }
    private void showLowBatteryWarning() {
        closeLastBatteryView();
        CharSequence levelText = mContext.getString(
                    com.android.internal.R.string.battery_low_percent_format, mBatteryLevel);
        if (mBatteryLevelTextView != null) {
            mBatteryLevelTextView.setText(levelText);
        } else {
            View v = View.inflate(mContext, com.android.internal.R.layout.battery_low, null);
            mBatteryLevelTextView=(TextView)v.findViewById(com.android.internal.R.id.level_percent);
            mBatteryLevelTextView.setText(levelText);
            AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setCancelable(true);
                b.setTitle(com.android.internal.R.string.battery_low_title);
                b.setView(v);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setPositiveButton(android.R.string.ok, null);
                final Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    b.setNegativeButton(com.android.internal.R.string.battery_low_why,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mContext.startActivity(intent);
                            if (mLowBatteryDialog != null) {
                                mLowBatteryDialog.dismiss();
                            }
                        }
                    });
                }
            AlertDialog d = b.create();
            d.setOnDismissListener(mLowBatteryListener);
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            d.show();
            mLowBatteryDialog = d;
        }
        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.System.getInt(cr,
                Settings.System.POWER_SOUNDS_ENABLED, 1) == 1)
        {
            final String soundPath = Settings.System.getString(cr,
                Settings.System.LOW_BATTERY_SOUND);
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file:
                if (soundUri != null) {
                    final Ringtone sfx = RingtoneManager.getRingtone(mContext, soundUri);
                    if (sfx != null) {
                        sfx.setStreamType(AudioManager.STREAM_SYSTEM);
                        sfx.play();
                    }
                }
            }
        }
    }
    private final void updateCallState(int state) {
        mPhoneState = state;
        if (false) {
            Slog.d(TAG, "mPhoneState=" + mPhoneState
                    + " mLowBatteryDialog=" + mLowBatteryDialog
                    + " mBatteryShowLowOnEndCall=" + mBatteryShowLowOnEndCall);
        }
        if (mPhoneState == TelephonyManager.CALL_STATE_IDLE) {
            if (mBatteryShowLowOnEndCall) {
                if (!mBatteryPlugged) {
                    showLowBatteryWarning();
                }
                mBatteryShowLowOnEndCall = false;
            }
        } else {
            if (mLowBatteryDialog != null) {
                mLowBatteryDialog.dismiss();
                mBatteryShowLowOnEndCall = true;
            }
        }
    }
    private DialogInterface.OnDismissListener mLowBatteryListener
            = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            mLowBatteryDialog = null;
            mBatteryLevelTextView = null;
        }
    };
    private void scheduleCloseBatteryView() {
        Message m = mHandler.obtainMessage(EVENT_BATTERY_CLOSE);
        m.arg1 = (++mBatteryViewSequence);
        mHandler.sendMessageDelayed(m, 3000);
    }
    private void closeLastBatteryView() {
        if (mBatteryView != null) {
            WindowManagerImpl.getDefault().removeView(mBatteryView);
            mBatteryView = null;
        }
    }
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength;
            updateSignalStrength();
        }
        @Override
        public void onServiceStateChanged(ServiceState state) {
            mServiceState = state;
            updateSignalStrength();
            updateCdmaRoamingIcon(state);
            updateDataIcon();
        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            updateCallState(state);
            if (isCdma()) {
                updateSignalStrength();
            }
        }
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            mDataState = state;
            updateDataNetType(networkType);
            updateDataIcon();
        }
        @Override
        public void onDataActivity(int direction) {
            mDataActivity = direction;
            updateDataIcon();
        }
    };
    private final void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
        if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            mSimState = IccCard.State.ABSENT;
        }
        else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            mSimState = IccCard.State.READY;
        }
        else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason = intent.getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
            if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                mSimState = IccCard.State.PIN_REQUIRED;
            }
            else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                mSimState = IccCard.State.PUK_REQUIRED;
            }
            else {
                mSimState = IccCard.State.NETWORK_LOCKED;
            }
        } else {
            mSimState = IccCard.State.UNKNOWN;
        }
        updateDataIcon();
    }
    private boolean isCdma() {
        return (mSignalStrength != null) && !mSignalStrength.isGsm();
    }
    private boolean isEvdo() {
        return ( (mServiceState != null)
                 && ((mServiceState.getRadioTechnology()
                        == ServiceState.RADIO_TECHNOLOGY_EVDO_0)
                     || (mServiceState.getRadioTechnology()
                        == ServiceState.RADIO_TECHNOLOGY_EVDO_A)));
    }
    private boolean hasService() {
        if (mServiceState != null) {
            switch (mServiceState.getState()) {
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_POWER_OFF:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }
    private final void updateSignalStrength() {
        int iconLevel = -1;
        int[] iconList;
        if (!hasService() && !mServiceState.isEmergencyOnly()) {
            if (Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
                mPhoneData.iconId = com.android.internal.R.drawable.stat_sys_signal_flightmode;
            } else {
                mPhoneData.iconId = com.android.internal.R.drawable.stat_sys_signal_null;
            }
            mService.updateIcon(mPhoneIcon, mPhoneData, null);
            return;
        }
        if (!isCdma()) {
            int asu = mSignalStrength.getGsmSignalStrength();
            if (asu <= 2 || asu == 99) iconLevel = 0;
            else if (asu >= 12) iconLevel = 4;
            else if (asu >= 8)  iconLevel = 3;
            else if (asu >= 5)  iconLevel = 2;
            else iconLevel = 1;
            if (mPhone.isNetworkRoaming()) {
                iconList = sSignalImages_r;
            } else {
                iconList = sSignalImages;
            }
        } else {
            iconList = this.sSignalImages;
            if ((mPhoneState == TelephonyManager.CALL_STATE_IDLE) && isEvdo()){
                iconLevel = getEvdoLevel();
                if (false) {
                    Slog.d(TAG, "use Evdo level=" + iconLevel + " to replace Cdma Level=" + getCdmaLevel());
                }
            } else {
                iconLevel = getCdmaLevel();
            }
        }
        mPhoneData.iconId = iconList[iconLevel];
        mService.updateIcon(mPhoneIcon, mPhoneData, null);
    }
    private int getCdmaLevel() {
        final int cdmaDbm = mSignalStrength.getCdmaDbm();
        final int cdmaEcio = mSignalStrength.getCdmaEcio();
        int levelDbm = 0;
        int levelEcio = 0;
        if (cdmaDbm >= -75) levelDbm = 4;
        else if (cdmaDbm >= -85) levelDbm = 3;
        else if (cdmaDbm >= -95) levelDbm = 2;
        else if (cdmaDbm >= -100) levelDbm = 1;
        else levelDbm = 0;
        if (cdmaEcio >= -90) levelEcio = 4;
        else if (cdmaEcio >= -110) levelEcio = 3;
        else if (cdmaEcio >= -130) levelEcio = 2;
        else if (cdmaEcio >= -150) levelEcio = 1;
        else levelEcio = 0;
        return (levelDbm < levelEcio) ? levelDbm : levelEcio;
    }
    private int getEvdoLevel() {
        int evdoDbm = mSignalStrength.getEvdoDbm();
        int evdoSnr = mSignalStrength.getEvdoSnr();
        int levelEvdoDbm = 0;
        int levelEvdoSnr = 0;
        if (evdoDbm >= -65) levelEvdoDbm = 4;
        else if (evdoDbm >= -75) levelEvdoDbm = 3;
        else if (evdoDbm >= -90) levelEvdoDbm = 2;
        else if (evdoDbm >= -105) levelEvdoDbm = 1;
        else levelEvdoDbm = 0;
        if (evdoSnr >= 7) levelEvdoSnr = 4;
        else if (evdoSnr >= 5) levelEvdoSnr = 3;
        else if (evdoSnr >= 3) levelEvdoSnr = 2;
        else if (evdoSnr >= 1) levelEvdoSnr = 1;
        else levelEvdoSnr = 0;
        return (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
    }
    private final void updateDataNetType(int net) {
        switch (net) {
        case TelephonyManager.NETWORK_TYPE_EDGE:
            mDataIconList = sDataNetType_e;
            break;
        case TelephonyManager.NETWORK_TYPE_UMTS:
            mDataIconList = sDataNetType_3g;
            break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
        case TelephonyManager.NETWORK_TYPE_HSUPA:
        case TelephonyManager.NETWORK_TYPE_HSPA:
            if (mHspaDataDistinguishable) {
                mDataIconList = sDataNetType_h;
            } else {
                mDataIconList = sDataNetType_3g;
            }
            break;
        case TelephonyManager.NETWORK_TYPE_CDMA:
            mDataIconList = this.sDataNetType_1x;
            break;
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            mDataIconList = this.sDataNetType_1x;
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0: 
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            mDataIconList = sDataNetType_3g;
            break;
        default:
            mDataIconList = sDataNetType_g;
        break;
        }
    }
    private final void updateDataIcon() {
        int iconId;
        boolean visible = true;
        if (!isCdma()) {
            if (mSimState == IccCard.State.READY || mSimState == IccCard.State.UNKNOWN) {
                if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                    switch (mDataActivity) {
                        case TelephonyManager.DATA_ACTIVITY_IN:
                            iconId = mDataIconList[1];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_OUT:
                            iconId = mDataIconList[2];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_INOUT:
                            iconId = mDataIconList[3];
                            break;
                        default:
                            iconId = mDataIconList[0];
                            break;
                    }
                    mDataData.iconId = iconId;
                    mService.updateIcon(mDataIcon, mDataData, null);
                } else {
                    visible = false;
                }
            } else {
                mDataData.iconId = com.android.internal.R.drawable.stat_sys_no_sim;
                mService.updateIcon(mDataIcon, mDataData, null);
            }
        } else {
            if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                switch (mDataActivity) {
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        iconId = mDataIconList[1];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        iconId = mDataIconList[2];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        iconId = mDataIconList[3];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    default:
                        iconId = mDataIconList[0];
                        break;
                }
                mDataData.iconId = iconId;
                mService.updateIcon(mDataIcon, mDataData, null);
            } else {
                visible = false;
            }
        }
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneDataConnectionState(mPhone.getNetworkType(), visible);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (mDataIconVisible != visible) {
            mService.setIconVisibility(mDataIcon, visible);
            mDataIconVisible = visible;
        }
    }
    private final void updateVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int ringerMode = audioManager.getRingerMode();
        final boolean visible = ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        final int iconId = (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                ? com.android.internal.R.drawable.stat_sys_ringer_vibrate
                : com.android.internal.R.drawable.stat_sys_ringer_silent;
        if (visible) {
            mVolumeData.iconId = iconId;
            mService.updateIcon(mVolumeIcon, mVolumeData, null);
        }
        if (visible != mVolumeVisible) {
            mService.setIconVisibility(mVolumeIcon, visible);
            mVolumeVisible = visible;
        }
    }
    private final void updateBluetooth(Intent intent) {
        int iconId = com.android.internal.R.drawable.stat_sys_data_bluetooth;
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            mBluetoothEnabled = state == BluetoothAdapter.STATE_ON;
        } else if (action.equals(BluetoothHeadset.ACTION_STATE_CHANGED)) {
            mBluetoothHeadsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_ERROR);
        } else if (action.equals(BluetoothA2dp.ACTION_SINK_STATE_CHANGED)) {
            BluetoothA2dp a2dp = new BluetoothA2dp(mContext);
            if (a2dp.getConnectedSinks().size() != 0) {
                mBluetoothA2dpConnected = true;
            } else {
                mBluetoothA2dpConnected = false;
            }
        } else if (action.equals(BluetoothPbap.PBAP_STATE_CHANGED_ACTION)) {
            mBluetoothPbapState = intent.getIntExtra(BluetoothPbap.PBAP_STATE,
                    BluetoothPbap.STATE_DISCONNECTED);
        } else {
            return;
        }
        if (mBluetoothHeadsetState == BluetoothHeadset.STATE_CONNECTED || mBluetoothA2dpConnected ||
                mBluetoothPbapState == BluetoothPbap.STATE_CONNECTED) {
            iconId = com.android.internal.R.drawable.stat_sys_data_bluetooth_connected;
        }
        mBluetoothData.iconId = iconId;
        mService.updateIcon(mBluetoothIcon, mBluetoothData, null);
        mService.setIconVisibility(mBluetoothIcon, mBluetoothEnabled);
    }
    private final void updateWifi(Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
            if (!enabled) {
                mService.setIconVisibility(mWifiIcon, false);
            }
        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            final boolean enabled = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,
                                                           false);
            if (!enabled) {
                mService.setIconVisibility(mWifiIcon, false);
            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            final NetworkInfo networkInfo = (NetworkInfo)
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            int iconId;
            if (networkInfo != null && networkInfo.isConnected()) {
                mIsWifiConnected = true;
                if (mLastWifiSignalLevel == -1) {
                    iconId = sWifiSignalImages[0];
                } else {
                    iconId = sWifiSignalImages[mLastWifiSignalLevel];
                }
                mService.setIconVisibility(mWifiIcon, true);
            } else {
                mLastWifiSignalLevel = -1;
                mIsWifiConnected = false;
                iconId = sWifiSignalImages[0];
                mService.setIconVisibility(mWifiIcon, false);
            }
            mWifiData.iconId = iconId;
            mService.updateIcon(mWifiIcon, mWifiData, null);
        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
            int newSignalLevel = WifiManager.calculateSignalLevel(newRssi,
                                                                  sWifiSignalImages.length);
            if (newSignalLevel != mLastWifiSignalLevel) {
                mLastWifiSignalLevel = newSignalLevel;
                if (mIsWifiConnected) {
                    mWifiData.iconId = sWifiSignalImages[newSignalLevel];
                } else {
                    mWifiData.iconId = sWifiTemporarilyNotConnectedImage;
                }
                mService.updateIcon(mWifiIcon, mWifiData, null);
            }
        }
    }
    private final void updateGps(Intent intent) {
        final String action = intent.getAction();
        final boolean enabled = intent.getBooleanExtra(GpsLocationProvider.EXTRA_ENABLED, false);
        if (action.equals(GpsLocationProvider.GPS_FIX_CHANGE_ACTION) && enabled) {
            mService.updateIcon(mGpsIcon, mGpsFixIconData, null);
            mService.setIconVisibility(mGpsIcon, true);
        } else if (action.equals(GpsLocationProvider.GPS_ENABLED_CHANGE_ACTION) && !enabled) {
            mService.setIconVisibility(mGpsIcon, false);
        } else {
            mService.updateIcon(mGpsIcon, mGpsEnabledIconData, null);
            mService.setIconVisibility(mGpsIcon, true);
        }
    }
    private final void updateTTY(Intent intent) {
        final String action = intent.getAction();
        final boolean enabled = intent.getBooleanExtra(TtyIntent.TTY_ENABLED, false);
        if (false) Slog.v(TAG, "updateTTY: enabled: " + enabled);
        if (enabled) {
            if (false) Slog.v(TAG, "updateTTY: set TTY on");
            mService.updateIcon(mTTYModeIcon, mTTYModeEnableIconData, null);
            mService.setIconVisibility(mTTYModeIcon, true);
        } else {
            if (false) Slog.v(TAG, "updateTTY: set TTY off");
            mService.setIconVisibility(mTTYModeIcon, false);
        }
    }
    private final void updateCdmaRoamingIcon(ServiceState state) {
        if (!hasService()) {
            mService.setIconVisibility(mCdmaRoamingIndicatorIcon, false);
            return;
        }
        if (!isCdma()) {
            mService.setIconVisibility(mCdmaRoamingIndicatorIcon, false);
            return;
        }
        int[] iconList = sRoamingIndicatorImages_cdma;
        int iconIndex = state.getCdmaEriIconIndex();
        int iconMode = state.getCdmaEriIconMode();
        if (iconIndex == -1) {
            Slog.e(TAG, "getCdmaEriIconIndex returned null, skipping ERI icon update");
            return;
        }
        if (iconMode == -1) {
            Slog.e(TAG, "getCdmeEriIconMode returned null, skipping ERI icon update");
            return;
        }
        if (iconIndex == EriInfo.ROAMING_INDICATOR_OFF) {
            if (false) Slog.v(TAG, "Cdma ROAMING_INDICATOR_OFF, removing ERI icon");
            mService.setIconVisibility(mCdmaRoamingIndicatorIcon, false);
            return;
        }
        switch (iconMode) {
            case EriInfo.ROAMING_ICON_MODE_NORMAL:
                mCdmaRoamingIndicatorIconData.iconId = iconList[iconIndex];
                mService.updateIcon(mCdmaRoamingIndicatorIcon, mCdmaRoamingIndicatorIconData, null);
                mService.setIconVisibility(mCdmaRoamingIndicatorIcon, true);
                break;
            case EriInfo.ROAMING_ICON_MODE_FLASH:
                mCdmaRoamingIndicatorIconData.iconId =
                        com.android.internal.R.drawable.stat_sys_roaming_cdma_flash;
                mService.updateIcon(mCdmaRoamingIndicatorIcon, mCdmaRoamingIndicatorIconData, null);
                mService.setIconVisibility(mCdmaRoamingIndicatorIcon, true);
                break;
        }
        mService.updateIcon(mPhoneIcon, mPhoneData, null);
    }
    private class StatusBarHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_BATTERY_CLOSE:
                if (msg.arg1 == mBatteryViewSequence) {
                    closeLastBatteryView();
                }
                break;
            }
        }
    }
}
