public class SettingsAppWidgetProvider extends AppWidgetProvider {
    static final String TAG = "SettingsAppWidgetProvider";
    static final ComponentName THIS_APPWIDGET =
            new ComponentName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
    private static LocalBluetoothManager sLocalBluetoothManager = null;
    private static final int BUTTON_WIFI = 0;
    private static final int BUTTON_BRIGHTNESS = 1;
    private static final int BUTTON_SYNC = 2;
    private static final int BUTTON_GPS = 3;
    private static final int BUTTON_BLUETOOTH = 4;
    private static final int STATE_DISABLED = 0;
    private static final int STATE_ENABLED = 1;
    private static final int STATE_TURNING_ON = 2;
    private static final int STATE_TURNING_OFF = 3;
    private static final int STATE_UNKNOWN = 4;
    private static final int STATE_INTERMEDIATE = 5;
    private static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM + 10;
    private static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;
    private static final int DEFAULT_BACKLIGHT = (int) (android.os.Power.BRIGHTNESS_ON * 0.4f);
    private static final StateTracker sWifiState = new WifiStateTracker();
    private static final StateTracker sBluetoothState = new BluetoothStateTracker();
    private abstract static class StateTracker {
        private boolean mInTransition = false;
        private Boolean mActualState = null;  
        private Boolean mIntendedState = null;  
        private boolean mDeferredStateChangeRequestNeeded = false;
        public final void toggleState(Context context) {
            int currentState = getTriState(context);
            boolean newState = false;
            switch (currentState) {
                case STATE_ENABLED:
                    newState = false;
                    break;
                case STATE_DISABLED:
                    newState = true;
                    break;
                case STATE_INTERMEDIATE:
                    if (mIntendedState != null) {
                        newState = !mIntendedState;
                    }
                    break;
            }
            mIntendedState = newState;
            if (mInTransition) {
                mDeferredStateChangeRequestNeeded = true;
            } else {
                mInTransition = true;
                requestStateChange(context, newState);
            }
        }
        public abstract void onActualStateChange(Context context, Intent intent);
        protected final void setCurrentState(Context context, int newState) {
            final boolean wasInTransition = mInTransition;
            switch (newState) {
                case STATE_DISABLED:
                    mInTransition = false;
                    mActualState = false;
                    break;
                case STATE_ENABLED:
                    mInTransition = false;
                    mActualState = true;
                    break;
                case STATE_TURNING_ON:
                    mInTransition = true;
                    mActualState = false;
                    break;
                case STATE_TURNING_OFF:
                    mInTransition = true;
                    mActualState = true;
                    break;
            }
            if (wasInTransition && !mInTransition) {
                if (mDeferredStateChangeRequestNeeded) {
                    Log.v(TAG, "processing deferred state change");
                    if (mActualState != null && mIntendedState != null &&
                        mIntendedState.equals(mActualState)) {
                        Log.v(TAG, "... but intended state matches, so no changes.");
                    } else if (mIntendedState != null) {
                        mInTransition = true;
                        requestStateChange(context, mIntendedState);
                    }
                    mDeferredStateChangeRequestNeeded = false;
                }
            }
        }
        public final boolean isTurningOn() {
            return mIntendedState != null && mIntendedState;
        }
        public final int getTriState(Context context) {
            if (mInTransition) {
                return STATE_INTERMEDIATE;
            }
            switch (getActualState(context)) {
                case STATE_DISABLED:
                    return STATE_DISABLED;
                case STATE_ENABLED:
                    return STATE_ENABLED;
                default:
                    return STATE_INTERMEDIATE;
            }
        }
        public abstract int getActualState(Context context);
        protected abstract void requestStateChange(Context context, boolean desiredState);
    }
    private static final class WifiStateTracker extends StateTracker {
        @Override
        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return STATE_UNKNOWN;
        }
        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.d(TAG, "No wifiManager.");
                return;
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    int wifiApState = wifiManager.getWifiApState();
                    if (desiredState && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                                         (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        wifiManager.setWifiApEnabled(null, false);
                    }
                    wifiManager.setWifiEnabled(desiredState);
                    return null;
                }
            }.execute();
        }
        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            setCurrentState(context, wifiStateToFiveState(wifiState));
        }
        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    return STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLED:
                    return STATE_ENABLED;
                case WifiManager.WIFI_STATE_DISABLING:
                    return STATE_TURNING_OFF;
                case WifiManager.WIFI_STATE_ENABLING:
                    return STATE_TURNING_ON;
                default:
                    return STATE_UNKNOWN;
            }
        }
    }
    private static final class BluetoothStateTracker extends StateTracker {
        @Override
        public int getActualState(Context context) {
            if (sLocalBluetoothManager == null) {
                sLocalBluetoothManager = LocalBluetoothManager.getInstance(context);
                if (sLocalBluetoothManager == null) {
                    return STATE_UNKNOWN;  
                }
            }
            return bluetoothStateToFiveState(sLocalBluetoothManager.getBluetoothState());
        }
        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            if (sLocalBluetoothManager == null) {
                Log.d(TAG, "No LocalBluetoothManager");
                return;
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    sLocalBluetoothManager.setBluetoothEnabled(desiredState);
                    return null;
                }
            }.execute();
        }
        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                return;
            }
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            setCurrentState(context, bluetoothStateToFiveState(bluetoothState));
        }
        private static int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_OFF:
                    return STATE_DISABLED;
                case BluetoothAdapter.STATE_ON:
                    return STATE_ENABLED;
                case BluetoothAdapter.STATE_TURNING_ON:
                    return STATE_TURNING_ON;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return STATE_TURNING_OFF;
                default:
                    return STATE_UNKNOWN;
            }
        }
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        RemoteViews view = buildUpdate(context, -1);
        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], view);
        }
    }
    @Override
    public void onEnabled(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("com.android.settings", ".widget.SettingsAppWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onDisabled(Context context) {
        Class clazz = com.android.settings.widget.SettingsAppWidgetProvider.class;
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("com.android.settings", ".widget.SettingsAppWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    static RemoteViews buildUpdate(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        views.setOnClickPendingIntent(R.id.btn_wifi, getLaunchPendingIntent(context, appWidgetId,
                BUTTON_WIFI));
        views.setOnClickPendingIntent(R.id.btn_brightness,
                getLaunchPendingIntent(context,
                        appWidgetId, BUTTON_BRIGHTNESS));
        views.setOnClickPendingIntent(R.id.btn_sync,
                getLaunchPendingIntent(context,
                        appWidgetId, BUTTON_SYNC));
        views.setOnClickPendingIntent(R.id.btn_gps,
                getLaunchPendingIntent(context, appWidgetId, BUTTON_GPS));
        views.setOnClickPendingIntent(R.id.btn_bluetooth,
                getLaunchPendingIntent(context,
                        appWidgetId, BUTTON_BLUETOOTH));
        updateButtons(views, context);
        return views;
    }
    public static void updateWidget(Context context) {
        RemoteViews views = buildUpdate(context, -1);
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }
    private static void updateButtons(RemoteViews views, Context context) {
        switch (sWifiState.getTriState(context)) {
            case STATE_DISABLED:
                views.setImageViewResource(R.id.img_wifi,
                                           R.drawable.ic_appwidget_settings_wifi_off);
                views.setImageViewResource(R.id.ind_wifi,
                                           R.drawable.appwidget_settings_ind_off_l);
                break;
            case STATE_ENABLED:
                views.setImageViewResource(R.id.img_wifi,
                                           R.drawable.ic_appwidget_settings_wifi_on);
                views.setImageViewResource(R.id.ind_wifi,
                                           R.drawable.appwidget_settings_ind_on_l);
                break;
            case STATE_INTERMEDIATE:
                if (sWifiState.isTurningOn()) {
                    views.setImageViewResource(R.id.img_wifi,
                                               R.drawable.ic_appwidget_settings_wifi_on);
                    views.setImageViewResource(R.id.ind_wifi,
                                               R.drawable.appwidget_settings_ind_mid_l);
                } else {
                    views.setImageViewResource(R.id.img_wifi,
                                               R.drawable.ic_appwidget_settings_wifi_off);
                    views.setImageViewResource(R.id.ind_wifi,
                                               R.drawable.appwidget_settings_ind_off_l);
                }
                break;
        }
        if (getBrightnessMode(context)) {
            views.setImageViewResource(R.id.img_brightness,
                                       R.drawable.ic_appwidget_settings_brightness_auto);
            views.setImageViewResource(R.id.ind_brightness,
                                       R.drawable.appwidget_settings_ind_on_r);
        } else if (getBrightness(context)) {
            views.setImageViewResource(R.id.img_brightness,
                                       R.drawable.ic_appwidget_settings_brightness_on);
            views.setImageViewResource(R.id.ind_brightness,
                                       R.drawable.appwidget_settings_ind_on_r);
        } else {
            views.setImageViewResource(R.id.img_brightness,
                                       R.drawable.ic_appwidget_settings_brightness_off);
            views.setImageViewResource(R.id.ind_brightness,
                                       R.drawable.appwidget_settings_ind_off_r);
        }
        if (getSync(context)) {
            views.setImageViewResource(R.id.img_sync, R.drawable.ic_appwidget_settings_sync_on);
            views.setImageViewResource(R.id.ind_sync, R.drawable.appwidget_settings_ind_on_c);
        } else {
            views.setImageViewResource(R.id.img_sync, R.drawable.ic_appwidget_settings_sync_off);
            views.setImageViewResource(R.id.ind_sync, R.drawable.appwidget_settings_ind_off_c);
        }
        if (getGpsState(context)) {
            views.setImageViewResource(R.id.img_gps, R.drawable.ic_appwidget_settings_gps_on);
            views.setImageViewResource(R.id.ind_gps, R.drawable.appwidget_settings_ind_on_c);
        } else {
            views.setImageViewResource(R.id.img_gps, R.drawable.ic_appwidget_settings_gps_off);
            views.setImageViewResource(R.id.ind_gps, R.drawable.appwidget_settings_ind_off_c);
        }
        switch (sBluetoothState.getTriState(context)) {
            case STATE_DISABLED:
                views.setImageViewResource(R.id.img_bluetooth,
                                           R.drawable.ic_appwidget_settings_bluetooth_off);
                views.setImageViewResource(R.id.ind_bluetooth,
                                           R.drawable.appwidget_settings_ind_off_c);
                break;
            case STATE_ENABLED:
                views.setImageViewResource(R.id.img_bluetooth,
                                           R.drawable.ic_appwidget_settings_bluetooth_on);
                views.setImageViewResource(R.id.ind_bluetooth,
                                           R.drawable.appwidget_settings_ind_on_c);
                break;
            case STATE_INTERMEDIATE:
                if (sBluetoothState.isTurningOn()) {
                    views.setImageViewResource(R.id.img_bluetooth,
                                               R.drawable.ic_appwidget_settings_bluetooth_on);
                    views.setImageViewResource(R.id.ind_bluetooth,
                                               R.drawable.appwidget_settings_ind_mid_c);
                } else {
                    views.setImageViewResource(R.id.img_bluetooth,
                                               R.drawable.ic_appwidget_settings_bluetooth_off);
                    views.setImageViewResource(R.id.ind_bluetooth,
                                               R.drawable.appwidget_settings_ind_off_c);
                }
                break;
        }
    }
    private static PendingIntent getLaunchPendingIntent(Context context, int appWidgetId,
            int buttonId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, SettingsAppWidgetProvider.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 ,
                launchIntent, 0 );
        return pi;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            sWifiState.onActualStateChange(context, intent);
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            sBluetoothState.onActualStateChange(context, intent);
        } else if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            if (buttonId == BUTTON_WIFI) {
                sWifiState.toggleState(context);
            } else if (buttonId == BUTTON_BRIGHTNESS) {
                toggleBrightness(context);
            } else if (buttonId == BUTTON_SYNC) {
                toggleSync(context);
            } else if (buttonId == BUTTON_GPS) {
                toggleGps(context);
            } else if (buttonId == BUTTON_BLUETOOTH) {
                sBluetoothState.toggleState(context);
            }
        } else {
            return;
        }
        updateWidget(context);
    }
    private static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }
    private static boolean getSync(Context context) {
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = ContentResolver.getMasterSyncAutomatically();
        return backgroundData && sync;
    }
    private void toggleSync(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = ContentResolver.getMasterSyncAutomatically();
        if (!backgroundData && !sync) {
            connManager.setBackgroundDataSetting(true);
            ContentResolver.setMasterSyncAutomatically(true);
        }
        if (!backgroundData && sync) {
            connManager.setBackgroundDataSetting(true);
        }
        if (backgroundData && !sync) {
            ContentResolver.setMasterSyncAutomatically(true);
        }
        if (backgroundData && sync) {
            ContentResolver.setMasterSyncAutomatically(false);
        }
    }
    private static boolean getGpsState(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER);
    }
    private void toggleGps(Context context) {
        ContentResolver resolver = context.getContentResolver();
        boolean enabled = getGpsState(context);
        Settings.Secure.setLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER,
                !enabled);
    }
    private static boolean getBrightness(Context context) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                int brightness = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                return brightness > 100;
            }
        } catch (Exception e) {
            Log.d(TAG, "getBrightness: " + e);
        }
        return false;
    }
    private static boolean getBrightnessMode(Context context) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                int brightnessMode = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
                return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            }
        } catch (Exception e) {
            Log.d(TAG, "getBrightnessMode: " + e);
        }
        return false;
    }
    private void toggleBrightness(Context context) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                ContentResolver cr = context.getContentResolver();
                int brightness = Settings.System.getInt(cr,
                        Settings.System.SCREEN_BRIGHTNESS);
                int brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                if (context.getResources().getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available)) {
                    brightnessMode = Settings.System.getInt(cr,
                            Settings.System.SCREEN_BRIGHTNESS_MODE);
                }
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    brightness = MINIMUM_BACKLIGHT;
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                } else if (brightness < DEFAULT_BACKLIGHT) {
                    brightness = DEFAULT_BACKLIGHT;
                } else if (brightness < MAXIMUM_BACKLIGHT) {
                    brightness = MAXIMUM_BACKLIGHT;
                } else {
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                    brightness = MINIMUM_BACKLIGHT;
                }
                if (context.getResources().getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available)) {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            brightnessMode);
                } else {
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                }
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                    power.setBacklightBrightness(brightness);
                    Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, brightness);
                }
            }
        } catch (RemoteException e) {
            Log.d(TAG, "toggleBrightness: " + e);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "toggleBrightness: " + e);
        }
    }
}
