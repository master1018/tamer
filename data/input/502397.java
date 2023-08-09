class ServerThread extends Thread {
    private static final String TAG = "SystemServer";
    private final static boolean INCLUDE_DEMO = false;
    private static final int LOG_BOOT_PROGRESS_SYSTEM_RUN = 3010;
    private ContentResolver mContentResolver;
    private class AdbSettingsObserver extends ContentObserver {
        public AdbSettingsObserver() {
            super(null);
        }
        @Override
        public void onChange(boolean selfChange) {
            boolean enableAdb = (Settings.Secure.getInt(mContentResolver,
                Settings.Secure.ADB_ENABLED, 0) > 0);
           SystemProperties.set("persist.service.adb.enable", enableAdb ? "1" : "0");
        }
    }
    @Override
    public void run() {
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN,
            SystemClock.uptimeMillis());
        Looper.prepare();
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_FOREGROUND);
        BinderInternal.disableBackgroundScheduling(true);
        String factoryTestStr = SystemProperties.get("ro.factorytest");
        int factoryTest = "".equals(factoryTestStr) ? SystemServer.FACTORY_TEST_OFF
                : Integer.parseInt(factoryTestStr);
        LightsService lights = null;
        PowerManagerService power = null;
        BatteryService battery = null;
        ConnectivityService connectivity = null;
        IPackageManager pm = null;
        Context context = null;
        WindowManagerService wm = null;
        BluetoothService bluetooth = null;
        BluetoothA2dpService bluetoothA2dp = null;
        HeadsetObserver headset = null;
        DockObserver dock = null;
        UiModeManagerService uiMode = null;
        RecognitionManagerService recognition = null;
        ThrottleService throttle = null;
        try {
            Slog.i(TAG, "Entropy Service");
            ServiceManager.addService("entropy", new EntropyService());
            Slog.i(TAG, "Power Manager");
            power = new PowerManagerService();
            ServiceManager.addService(Context.POWER_SERVICE, power);
            Slog.i(TAG, "Activity Manager");
            context = ActivityManagerService.main(factoryTest);
            Slog.i(TAG, "Telephony Registry");
            ServiceManager.addService("telephony.registry", new TelephonyRegistry(context));
            AttributeCache.init(context);
            Slog.i(TAG, "Package Manager");
            pm = PackageManagerService.main(context,
                    factoryTest != SystemServer.FACTORY_TEST_OFF);
            ActivityManagerService.setSystemProcess();
            mContentResolver = context.getContentResolver();
            try {
                Slog.i(TAG, "Account Manager");
                ServiceManager.addService(Context.ACCOUNT_SERVICE,
                        new AccountManagerService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Account Manager", e);
            }
            Slog.i(TAG, "Content Manager");
            ContentService.main(context,
                    factoryTest == SystemServer.FACTORY_TEST_LOW_LEVEL);
            Slog.i(TAG, "System Content Providers");
            ActivityManagerService.installSystemProviders();
            Slog.i(TAG, "Battery Service");
            battery = new BatteryService(context);
            ServiceManager.addService("battery", battery);
            Slog.i(TAG, "Lights Service");
            lights = new LightsService(context);
            Slog.i(TAG, "Vibrator Service");
            ServiceManager.addService("vibrator", new VibratorService(context));
            power.init(context, lights, ActivityManagerService.getDefault(), battery);
            Slog.i(TAG, "Alarm Manager");
            AlarmManagerService alarm = new AlarmManagerService(context);
            ServiceManager.addService(Context.ALARM_SERVICE, alarm);
            Slog.i(TAG, "Init Watchdog");
            Watchdog.getInstance().init(context, battery, power, alarm,
                    ActivityManagerService.self());
            Slog.i(TAG, "Sensor Service");
            ServiceManager.addService(Context.SENSOR_SERVICE, new SensorService(context));
            Slog.i(TAG, "Window Manager");
            wm = WindowManagerService.main(context, power,
                    factoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL);
            ServiceManager.addService(Context.WINDOW_SERVICE, wm);
            ((ActivityManagerService)ServiceManager.getService("activity"))
                    .setWindowManager(wm);
            if (SystemProperties.get("ro.kernel.qemu").equals("1")) {
                Slog.i(TAG, "Registering null Bluetooth Service (emulator)");
                ServiceManager.addService(BluetoothAdapter.BLUETOOTH_SERVICE, null);
            } else if (factoryTest == SystemServer.FACTORY_TEST_LOW_LEVEL) {
                Slog.i(TAG, "Registering null Bluetooth Service (factory test)");
                ServiceManager.addService(BluetoothAdapter.BLUETOOTH_SERVICE, null);
            } else {
                Slog.i(TAG, "Bluetooth Service");
                bluetooth = new BluetoothService(context);
                ServiceManager.addService(BluetoothAdapter.BLUETOOTH_SERVICE, bluetooth);
                bluetooth.initAfterRegistration();
                bluetoothA2dp = new BluetoothA2dpService(context, bluetooth);
                ServiceManager.addService(BluetoothA2dpService.BLUETOOTH_A2DP_SERVICE,
                                          bluetoothA2dp);
                int bluetoothOn = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.BLUETOOTH_ON, 0);
                if (bluetoothOn > 0) {
                    bluetooth.enable();
                }
            }
        } catch (RuntimeException e) {
            Slog.e("System", "Failure starting core service", e);
        }
        DevicePolicyManagerService devicePolicy = null;
        StatusBarService statusBar = null;
        InputMethodManagerService imm = null;
        AppWidgetService appWidget = null;
        NotificationManagerService notification = null;
        WallpaperManagerService wallpaper = null;
        LocationManagerService location = null;
        if (factoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL) {
            try {
                Slog.i(TAG, "Device Policy");
                devicePolicy = new DevicePolicyManagerService(context);
                ServiceManager.addService(Context.DEVICE_POLICY_SERVICE, devicePolicy);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting DevicePolicyService", e);
            }
            try {
                Slog.i(TAG, "Status Bar");
                statusBar = new StatusBarService(context);
                ServiceManager.addService(Context.STATUS_BAR_SERVICE, statusBar);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting StatusBarService", e);
            }
            try {
                Slog.i(TAG, "Clipboard Service");
                ServiceManager.addService(Context.CLIPBOARD_SERVICE,
                        new ClipboardService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Clipboard Service", e);
            }
            try {
                Slog.i(TAG, "Input Method Service");
                imm = new InputMethodManagerService(context, statusBar);
                ServiceManager.addService(Context.INPUT_METHOD_SERVICE, imm);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Input Manager Service", e);
            }
            try {
                Slog.i(TAG, "NetStat Service");
                ServiceManager.addService("netstat", new NetStatService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting NetStat Service", e);
            }
            try {
                Slog.i(TAG, "NetworkManagement Service");
                ServiceManager.addService(
                        Context.NETWORKMANAGEMENT_SERVICE, new NetworkManagementService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting NetworkManagement Service", e);
            }
            try {
                Slog.i(TAG, "Connectivity Service");
                connectivity = ConnectivityService.getInstance(context);
                ServiceManager.addService(Context.CONNECTIVITY_SERVICE, connectivity);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Connectivity Service", e);
            }
            try {
                Slog.i(TAG, "Throttle Service");
                throttle = new ThrottleService(context);
                ServiceManager.addService(
                        Context.THROTTLE_SERVICE, throttle);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting ThrottleService", e);
            }
            try {
              Slog.i(TAG, "Accessibility Manager");
              ServiceManager.addService(Context.ACCESSIBILITY_SERVICE,
                      new AccessibilityManagerService(context));
            } catch (Throwable e) {
              Slog.e(TAG, "Failure starting Accessibility Manager", e);
            }
            try {
                Slog.i(TAG, "Mount Service");
                ServiceManager.addService("mount", new MountService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Mount Service", e);
            }
            try {
                Slog.i(TAG, "Notification Manager");
                notification = new NotificationManagerService(context, statusBar, lights);
                ServiceManager.addService(Context.NOTIFICATION_SERVICE, notification);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Notification Manager", e);
            }
            try {
                Slog.i(TAG, "Device Storage Monitor");
                ServiceManager.addService(DeviceStorageMonitorService.SERVICE,
                        new DeviceStorageMonitorService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting DeviceStorageMonitor service", e);
            }
            try {
                Slog.i(TAG, "Location Manager");
                location = new LocationManagerService(context);
                ServiceManager.addService(Context.LOCATION_SERVICE, location);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Location Manager", e);
            }
            try {
                Slog.i(TAG, "Search Service");
                ServiceManager.addService(Context.SEARCH_SERVICE,
                        new SearchManagerService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Search Service", e);
            }
            if (INCLUDE_DEMO) {
                Slog.i(TAG, "Installing demo data...");
                (new DemoThread(context)).start();
            }
            try {
                Slog.i(TAG, "DropBox Service");
                ServiceManager.addService(Context.DROPBOX_SERVICE,
                        new DropBoxManagerService(context, new File("/data/system/dropbox")));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting DropBoxManagerService", e);
            }
            try {
                Slog.i(TAG, "Wallpaper Service");
                wallpaper = new WallpaperManagerService(context);
                ServiceManager.addService(Context.WALLPAPER_SERVICE, wallpaper);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Wallpaper Service", e);
            }
            try {
                Slog.i(TAG, "Audio Service");
                ServiceManager.addService(Context.AUDIO_SERVICE, new AudioService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Audio Service", e);
            }
            try {
                Slog.i(TAG, "Headset Observer");
                headset = new HeadsetObserver(context);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting HeadsetObserver", e);
            }
            try {
                Slog.i(TAG, "Dock Observer");
                dock = new DockObserver(context, power);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting DockObserver", e);
            }
            try {
                Slog.i(TAG, "UI Mode Manager Service");
                uiMode = new UiModeManagerService(context);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting UiModeManagerService", e);
            }
            try {
                Slog.i(TAG, "Backup Service");
                ServiceManager.addService(Context.BACKUP_SERVICE,
                        new BackupManagerService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Backup Service", e);
            }
            try {
                Slog.i(TAG, "AppWidget Service");
                appWidget = new AppWidgetService(context);
                ServiceManager.addService(Context.APPWIDGET_SERVICE, appWidget);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting AppWidget Service", e);
            }
            try {
                Slog.i(TAG, "Recognition Service");
                recognition = new RecognitionManagerService(context);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Recognition Service", e);
            }
            try {
                com.android.server.status.StatusBarPolicy.installIcons(context, statusBar);
            } catch (Throwable e) {
                Slog.e(TAG, "Failure installing status bar icons", e);
            }
            try {
                Slog.i(TAG, "DiskStats Service");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting DiskStats Service", e);
            }
        }
        Settings.Secure.putInt(mContentResolver, Settings.Secure.ADB_ENABLED,
                "1".equals(SystemProperties.get("persist.service.adb.enable")) ? 1 : 0);
        mContentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ADB_ENABLED),
                false, new AdbSettingsObserver());
        final boolean safeMode = wm.detectSafeMode();
        if (safeMode) {
            try {
                ActivityManagerNative.getDefault().enterSafeMode();
                Zygote.systemInSafeMode = true;
                VMRuntime.getRuntime().disableJitCompilation();
            } catch (RemoteException e) {
            }
        } else {
            VMRuntime.getRuntime().startJitCompilation();
        }
        if (devicePolicy != null) {
            devicePolicy.systemReady();
        }
        if (notification != null) {
            notification.systemReady();
        }
        if (statusBar != null) {
            statusBar.systemReady();
        }
        wm.systemReady();
        power.systemReady();
        try {
            pm.systemReady();
        } catch (RemoteException e) {
        }
        final BatteryService batteryF = battery;
        final ConnectivityService connectivityF = connectivity;
        final DockObserver dockF = dock;
        final ThrottleService throttleF = throttle;
        final UiModeManagerService uiModeF = uiMode;
        final AppWidgetService appWidgetF = appWidget;
        final WallpaperManagerService wallpaperF = wallpaper;
        final InputMethodManagerService immF = imm;
        final RecognitionManagerService recognitionF = recognition;
        final LocationManagerService locationF = location;
        ((ActivityManagerService)ActivityManagerNative.getDefault())
                .systemReady(new Runnable() {
            public void run() {
                Slog.i(TAG, "Making services ready");
                if (batteryF != null) batteryF.systemReady();
                if (connectivityF != null) connectivityF.systemReady();
                if (dockF != null) dockF.systemReady();
                if (uiModeF != null) uiModeF.systemReady();
                if (recognitionF != null) recognitionF.systemReady();
                Watchdog.getInstance().start();
                if (appWidgetF != null) appWidgetF.systemReady(safeMode);
                if (wallpaperF != null) wallpaperF.systemReady();
                if (immF != null) immF.systemReady();
                if (locationF != null) locationF.systemReady();
                if (throttleF != null) throttleF.systemReady();
            }
        });
        Looper.loop();
        Slog.d(TAG, "System ServerThread is exiting!");
    }
}
class DemoThread extends Thread
{
    DemoThread(Context context)
    {
        mContext = context;
    }
    @Override
    public void run()
    {
        try {
            Cursor c = mContext.getContentResolver().query(People.CONTENT_URI, null, null, null, null);
            boolean hasData = c != null && c.moveToFirst();
            if (c != null) {
                c.deactivate();
            }
            if (!hasData) {
                DemoDataSet dataset = new DemoDataSet();
                dataset.add(mContext);
            }
        } catch (Throwable e) {
            Slog.e("SystemServer", "Failure installing demo data", e);
        }
    }
    Context mContext;
}
public class SystemServer
{
    private static final String TAG = "SystemServer";
    public static final int FACTORY_TEST_OFF = 0;
    public static final int FACTORY_TEST_LOW_LEVEL = 1;
    public static final int FACTORY_TEST_HIGH_LEVEL = 2;
    static Timer timer;
    static final long SNAPSHOT_INTERVAL = 60 * 60 * 1000; 
    native public static void init1(String[] args);
    public static void main(String[] args) {
        if (SamplingProfilerIntegration.isEnabled()) {
            SamplingProfilerIntegration.start();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SamplingProfilerIntegration.writeSnapshot("system_server");
                }
            }, SNAPSHOT_INTERVAL, SNAPSHOT_INTERVAL);
        }
        VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
        System.loadLibrary("android_servers");
        init1(args);
    }
    public static final void init2() {
        Slog.i(TAG, "Entered the Android system server!");
        Thread thr = new ServerThread();
        thr.setName("android.server.ServerThread");
        thr.start();
    }
}
