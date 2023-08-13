public final class DdmsPlugin extends AbstractUIPlugin implements IDeviceChangeListener,
        IUiSelectionListener {
    public static final String PLUGIN_ID = "com.android.ide.eclipse.ddms"; 
    private static final String ADB_LOCATION = PLUGIN_ID + ".adb"; 
    private static DdmsPlugin sPlugin;
    private static String sAdbLocation;
    private static String sToolsFolder;
    private static String sHprofConverter;
    private static IDebugLauncher sRunningAppDebugLauncher;
    private MessageConsole mDdmsConsole;
    private ImageLoader mLoader;
    private IDevice mCurrentDevice;
    private Client mCurrentClient;
    private boolean mListeningToUiSelection = false;
    private final ArrayList<ISelectionListener> mListeners = new ArrayList<ISelectionListener>();
    private Color mRed;
    private boolean mDdmlibInitialized;
    public interface IDebugLauncher {
        public boolean debug(String packageName, int port);
    }
    public interface ISelectionListener {
        public void selectionChanged(Client selectedClient);
        public void selectionChanged(IDevice selectedDevice);
    }
    public DdmsPlugin() {
        sPlugin = this;
    }
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        final Display display = getDisplay();
        final IPreferenceStore eclipseStore = getPreferenceStore();
        AndroidDebugBridge.addDeviceChangeListener(this);
        DdmUiPreferences.setStore(eclipseStore);
        mDdmsConsole = new MessageConsole("DDMS", null); 
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(
                new IConsole[] {
                    mDdmsConsole
                });
        final MessageConsoleStream consoleStream = mDdmsConsole.newMessageStream();
        final MessageConsoleStream errorConsoleStream = mDdmsConsole.newMessageStream();
        mRed = new Color(display, 0xFF, 0x00, 0x00);
        display.asyncExec(new Runnable() {
            public void run() {
                errorConsoleStream.setColor(mRed);
            }
        });
        Log.setLogOutput(new ILogOutput() {
            public void printLog(LogLevel logLevel, String tag, String message) {
                if (logLevel.getPriority() >= LogLevel.ERROR.getPriority()) {
                    printToStream(errorConsoleStream, tag, message);
                    ConsolePlugin.getDefault().getConsoleManager().showConsoleView(mDdmsConsole);
                } else {
                    printToStream(consoleStream, tag, message);
                }
            }
            public void printAndPromptLog(final LogLevel logLevel, final String tag,
                    final String message) {
                printLog(logLevel, tag, message);
                display.asyncExec(new Runnable() {
                    public void run() {
                        Shell shell = display.getActiveShell();
                        if (logLevel == LogLevel.ERROR) {
                            MessageDialog.openError(shell, tag, message);
                        } else {
                            MessageDialog.openWarning(shell, tag, message);
                        }
                    }
                });
            }
        });
        mLoader = new ImageLoader(this);
        Preferences prefs = getPluginPreferences();
        prefs.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (PreferenceInitializer.ATTR_DEBUG_PORT_BASE.equals(property)) {
                    DdmPreferences.setDebugPortBase(
                            eclipseStore.getInt(PreferenceInitializer.ATTR_DEBUG_PORT_BASE));
                } else if (PreferenceInitializer.ATTR_SELECTED_DEBUG_PORT.equals(property)) {
                    DdmPreferences.setSelectedDebugPort(
                            eclipseStore.getInt(PreferenceInitializer.ATTR_SELECTED_DEBUG_PORT));
                } else if (PreferenceInitializer.ATTR_THREAD_INTERVAL.equals(property)) {
                    DdmUiPreferences.setThreadRefreshInterval(
                            eclipseStore.getInt(PreferenceInitializer.ATTR_THREAD_INTERVAL));
                } else if (PreferenceInitializer.ATTR_LOG_LEVEL.equals(property)) {
                    DdmPreferences.setLogLevel(
                            eclipseStore.getString(PreferenceInitializer.ATTR_LOG_LEVEL));
                } else if (PreferenceInitializer.ATTR_TIME_OUT.equals(property)) {
                    DdmPreferences.setTimeOut(
                            eclipseStore.getInt(PreferenceInitializer.ATTR_TIME_OUT));
                }
            }
        });
        final boolean adbValid = setAdbLocation(eclipseStore.getString(ADB_LOCATION));
        new Thread() {
            @Override
            public void run() {
                getDefault().initDdmlib();
                if (adbValid) {
                    AndroidDebugBridge.createBridge(sAdbLocation, true );
                }
            }
        }.start();
    }
    public static Display getDisplay() {
        IWorkbench bench = sPlugin.getWorkbench();
        if (bench != null) {
            return bench.getDisplay();
        }
        return null;
    }
    @Override
    public void stop(BundleContext context) throws Exception {
        AndroidDebugBridge.removeDeviceChangeListener(this);
        AndroidDebugBridge.terminate();
        mRed.dispose();
        sPlugin = null;
        super.stop(context);
    }
    public static DdmsPlugin getDefault() {
        return sPlugin;
    }
    public static ImageLoader getImageLoader() {
        if (sPlugin != null) {
            return sPlugin.mLoader;
        }
        return null;
    }
    public static String getAdb() {
        return sAdbLocation;
    }
    public static String getToolsFolder() {
        return sToolsFolder;
    }
    public static String getHprofConverter() {
        return sHprofConverter;
    }
    private static boolean setAdbLocation(String adbLocation) {
        File adb = new File(adbLocation);
        if (adb.isFile()) {
            sAdbLocation = adbLocation;
            File toolsFolder = adb.getParentFile();
            sToolsFolder = toolsFolder.getAbsolutePath();
            File hprofConverter = new File(toolsFolder, DdmConstants.FN_HPROF_CONVERTER);
            sHprofConverter = hprofConverter.getAbsolutePath();
            File traceview = new File(toolsFolder, DdmConstants.FN_TRACEVIEW);
            DdmUiPreferences.setTraceviewLocation(traceview.getAbsolutePath());
            return true;
        }
        return false;
    }
    public static void setAdb(String adb, boolean startAdb) {
        if (adb != null) {
            if (setAdbLocation(adb)) {
                sPlugin.getPreferenceStore().setValue(ADB_LOCATION, sAdbLocation);
                if (startAdb) {
                    new Thread() {
                        @Override
                        public void run() {
                            getDefault().initDdmlib();
                            AndroidDebugBridge.createBridge(sAdbLocation,
                                    false );
                        }
                    }.start();
                }
            }
        }
    }
    private synchronized void initDdmlib() {
        if (mDdmlibInitialized == false) {
            PreferenceInitializer.setupPreferences();
            AndroidDebugBridge.init(true );
            mDdmlibInitialized = true;
        }
    }
    public static void setRunningAppDebugLauncher(IDebugLauncher launcher) {
        sRunningAppDebugLauncher = launcher;
        Display display = getDisplay();
        if (display != null && display.isDisposed() == false) {
            display.asyncExec(new Runnable() {
                public void run() {
                    DeviceView dv = DeviceView.getInstance();
                    if (dv != null) {
                        dv.setDebugLauncher(sRunningAppDebugLauncher);
                    }
                }
            });
        }
    }
    public static IDebugLauncher getRunningAppDebugLauncher() {
        return sRunningAppDebugLauncher;
    }
    public synchronized void addSelectionListener(ISelectionListener listener) {
        mListeners.add(listener);
       listener.selectionChanged(mCurrentDevice);
       listener.selectionChanged(mCurrentClient);
    }
    public synchronized void removeSelectionListener(ISelectionListener listener) {
        mListeners.remove(listener);
    }
    public synchronized void setListeningState(boolean state) {
        mListeningToUiSelection = state;
    }
    public void deviceConnected(IDevice device) {
        if (mListeningToUiSelection == false) {
            if (mCurrentDevice == null) {
                handleDefaultSelection(device);
            }
        }
    }
    public void deviceDisconnected(IDevice device) {
        if (mListeningToUiSelection == false) {
            if (mCurrentDevice == device) {
                AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
                if (bridge != null) {
                    IDevice[] devices = bridge.getDevices();
                    if (devices.length == 0) {
                        handleDefaultSelection((IDevice)null);
                    } else {
                        handleDefaultSelection(devices[0]);
                    }
                } else {
                    handleDefaultSelection((IDevice)null);
                }
            }
        }
    }
    public void deviceChanged(IDevice device, int changeMask) {
        if (mListeningToUiSelection == false) {
            if (device == mCurrentDevice) {
                if (mCurrentClient == null) {
                    handleDefaultSelection(device);
                } else {
                    Client[] clients = device.getClients();
                    boolean foundClient = false;
                    for (Client client : clients) {
                        if (client == mCurrentClient) {
                            foundClient = true;
                            break;
                        }
                    }
                    if (foundClient == false) {
                        mCurrentClient = null;
                        handleDefaultSelection(device);
                    }
                }
            }
        }
    }
    public synchronized void selectionChanged(IDevice selectedDevice, Client selectedClient) {
        if (mCurrentDevice != selectedDevice) {
            mCurrentDevice = selectedDevice;
            for (ISelectionListener listener : mListeners) {
                listener.selectionChanged(mCurrentDevice);
            }
        }
        if (mCurrentClient != selectedClient) {
            mCurrentClient = selectedClient;
            for (ISelectionListener listener : mListeners) {
                listener.selectionChanged(mCurrentClient);
            }
        }
    }
    private void handleDefaultSelection(final IDevice device) {
        try {
            Display display = getDisplay();
            display.asyncExec(new Runnable() {
                public void run() {
                    boolean newDevice = false;
                    if (mCurrentDevice != device) {
                        mCurrentDevice = device;
                        newDevice = true;
                        for (ISelectionListener listener : mListeners) {
                            listener.selectionChanged(mCurrentDevice);
                        }
                    }
                    if (device != null) {
                        if (newDevice || mCurrentClient == null) {
                            Client[] clients =  device.getClients();
                            if (clients.length > 0) {
                                handleDefaultSelection(clients[0]);
                            } else {
                                handleDefaultSelection((Client)null);
                            }
                        }
                    } else {
                        handleDefaultSelection((Client)null);
                    }
                }
            });
        } catch (SWTException e) {
        }
    }
    private void handleDefaultSelection(Client client) {
        mCurrentClient = client;
        for (ISelectionListener listener : mListeners) {
            listener.selectionChanged(mCurrentClient);
        }
    }
    private static synchronized void printToStream(MessageConsoleStream stream, String tag,
            String message) {
        String dateTag = getMessageTag(tag);
        stream.print(dateTag);
        stream.println(message);
    }
    private static String getMessageTag(String tag) {
        Calendar c = Calendar.getInstance();
        if (tag == null) {
            return String.format("[%1$tF %1$tT]", c);
        }
        return String.format("[%1$tF %1$tT - %2$s]", c, tag);
    }
}
