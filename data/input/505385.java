public final class AndroidDebugBridge {
    private final static int ADB_VERSION_MICRO_MIN = 20;
    private final static int ADB_VERSION_MICRO_MAX = -1;
    private final static Pattern sAdbVersion = Pattern.compile(
            "^.*(\\d+)\\.(\\d+)\\.(\\d+)$"); 
    private final static String ADB = "adb"; 
    private final static String DDMS = "ddms"; 
    final static String ADB_HOST = "127.0.0.1"; 
    final static int ADB_PORT = 5037;
    static InetAddress sHostAddr;
    static InetSocketAddress sSocketAddr;
    static {
        try {
            sHostAddr = InetAddress.getByName(ADB_HOST);
            sSocketAddr = new InetSocketAddress(sHostAddr, ADB_PORT);
        } catch (UnknownHostException e) {
        }
    }
    private static AndroidDebugBridge sThis;
    private static boolean sClientSupport;
    private String mAdbOsLocation = null;
    private boolean mVersionCheck;
    private boolean mStarted = false;
    private DeviceMonitor mDeviceMonitor;
    private final static ArrayList<IDebugBridgeChangeListener> sBridgeListeners =
        new ArrayList<IDebugBridgeChangeListener>();
    private final static ArrayList<IDeviceChangeListener> sDeviceListeners =
        new ArrayList<IDeviceChangeListener>();
    private final static ArrayList<IClientChangeListener> sClientListeners =
        new ArrayList<IClientChangeListener>();
    private static final Object sLock = sBridgeListeners;
    public interface IDebugBridgeChangeListener {
        public void bridgeChanged(AndroidDebugBridge bridge);
    }
    public interface IDeviceChangeListener {
        public void deviceConnected(IDevice device);
        public void deviceDisconnected(IDevice device);
        public void deviceChanged(IDevice device, int changeMask);
    }
    public interface IClientChangeListener {
        public void clientChanged(Client client, int changeMask);
    }
    public static void init(boolean clientSupport) {
        sClientSupport = clientSupport;
        MonitorThread monitorThread = MonitorThread.createInstance();
        monitorThread.start();
        HandleHello.register(monitorThread);
        HandleAppName.register(monitorThread);
        HandleTest.register(monitorThread);
        HandleThread.register(monitorThread);
        HandleHeap.register(monitorThread);
        HandleWait.register(monitorThread);
        HandleProfiling.register(monitorThread);
    }
    public static void terminate() {
        if (sThis != null && sThis.mDeviceMonitor != null) {
            sThis.mDeviceMonitor.stop();
            sThis.mDeviceMonitor = null;
        }
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (monitorThread != null) {
            monitorThread.quit();
        }
    }
    static boolean getClientSupport() {
        return sClientSupport;
    }
    public static AndroidDebugBridge createBridge() {
        synchronized (sLock) {
            if (sThis != null) {
                return sThis;
            }
            try {
                sThis = new AndroidDebugBridge();
                sThis.start();
            } catch (InvalidParameterException e) {
                sThis = null;
            }
            IDebugBridgeChangeListener[] listenersCopy = sBridgeListeners.toArray(
                    new IDebugBridgeChangeListener[sBridgeListeners.size()]);
            for (IDebugBridgeChangeListener listener : listenersCopy) {
                try {
                    listener.bridgeChanged(sThis);
                } catch (Exception e) {
                    Log.e(DDMS, e);
                }
            }
            return sThis;
        }
    }
    public static AndroidDebugBridge createBridge(String osLocation, boolean forceNewBridge) {
        synchronized (sLock) {
            if (sThis != null) {
                if (sThis.mAdbOsLocation != null && sThis.mAdbOsLocation.equals(osLocation) &&
                        forceNewBridge == false) {
                    return sThis;
                } else {
                    sThis.stop();
                }
            }
            try {
                sThis = new AndroidDebugBridge(osLocation);
                sThis.start();
            } catch (InvalidParameterException e) {
                sThis = null;
            }
            IDebugBridgeChangeListener[] listenersCopy = sBridgeListeners.toArray(
                    new IDebugBridgeChangeListener[sBridgeListeners.size()]);
            for (IDebugBridgeChangeListener listener : listenersCopy) {
                try {
                    listener.bridgeChanged(sThis);
                } catch (Exception e) {
                    Log.e(DDMS, e);
                }
            }
            return sThis;
        }
    }
    public static AndroidDebugBridge getBridge() {
        return sThis;
    }
    public static void disconnectBridge() {
        synchronized (sLock) {
            if (sThis != null) {
                sThis.stop();
                sThis = null;
                IDebugBridgeChangeListener[] listenersCopy = sBridgeListeners.toArray(
                        new IDebugBridgeChangeListener[sBridgeListeners.size()]);
                for (IDebugBridgeChangeListener listener : listenersCopy) {
                    try {
                        listener.bridgeChanged(sThis);
                    } catch (Exception e) {
                        Log.e(DDMS, e);
                    }
                }
            }
        }
    }
    public static void addDebugBridgeChangeListener(IDebugBridgeChangeListener listener) {
        synchronized (sLock) {
            if (sBridgeListeners.contains(listener) == false) {
                sBridgeListeners.add(listener);
                if (sThis != null) {
                    try {
                        listener.bridgeChanged(sThis);
                    } catch (Exception e) {
                        Log.e(DDMS, e);
                    }
                }
            }
        }
    }
    public static void removeDebugBridgeChangeListener(IDebugBridgeChangeListener listener) {
        synchronized (sLock) {
            sBridgeListeners.remove(listener);
        }
    }
    public static void addDeviceChangeListener(IDeviceChangeListener listener) {
        synchronized (sLock) {
            if (sDeviceListeners.contains(listener) == false) {
                sDeviceListeners.add(listener);
            }
        }
    }
    public static void removeDeviceChangeListener(IDeviceChangeListener listener) {
        synchronized (sLock) {
            sDeviceListeners.remove(listener);
        }
    }
    public static void addClientChangeListener(IClientChangeListener listener) {
        synchronized (sLock) {
            if (sClientListeners.contains(listener) == false) {
                sClientListeners.add(listener);
            }
        }
    }
    public static void removeClientChangeListener(IClientChangeListener listener) {
        synchronized (sLock) {
            sClientListeners.remove(listener);
        }
    }
    public IDevice[] getDevices() {
        synchronized (sLock) {
            if (mDeviceMonitor != null) {
                return mDeviceMonitor.getDevices();
            }
        }
        return new IDevice[0];
    }
    public boolean hasInitialDeviceList() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.hasInitialDeviceList();
        }
        return false;
    }
    public void setSelectedClient(Client selectedClient) {
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (monitorThread != null) {
            monitorThread.setSelectedClient(selectedClient);
        }
    }
    public boolean isConnected() {
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (mDeviceMonitor != null && monitorThread != null) {
            return mDeviceMonitor.isMonitoring() && monitorThread.getState() != State.TERMINATED;
        }
        return false;
    }
    public int getConnectionAttemptCount() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.getConnectionAttemptCount();
        }
        return -1;
    }
    public int getRestartAttemptCount() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.getRestartAttemptCount();
        }
        return -1;
    }
    private AndroidDebugBridge(String osLocation) throws InvalidParameterException {
        if (osLocation == null || osLocation.length() == 0) {
            throw new InvalidParameterException();
        }
        mAdbOsLocation = osLocation;
        checkAdbVersion();
    }
    private AndroidDebugBridge() {
    }
    private void checkAdbVersion() {
        mVersionCheck = false;
        if (mAdbOsLocation == null) {
            return;
        }
        try {
            String[] command = new String[2];
            command[0] = mAdbOsLocation;
            command[1] = "version"; 
            Log.d(DDMS, String.format("Checking '%1$s version'", mAdbOsLocation)); 
            Process process = Runtime.getRuntime().exec(command);
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            int status = grabProcessOutput(process, errorOutput, stdOutput,
                    true );
            if (status != 0) {
                StringBuilder builder = new StringBuilder("'adb version' failed!"); 
                for (String error : errorOutput) {
                    builder.append('\n');
                    builder.append(error);
                }
                Log.logAndDisplay(LogLevel.ERROR, "adb", builder.toString());
            }
            boolean versionFound = false;
            for (String line : stdOutput) {
                versionFound = scanVersionLine(line);
                if (versionFound) {
                    break;
                }
            }
            if (!versionFound) {
                for (String line : errorOutput) {
                    versionFound = scanVersionLine(line);
                    if (versionFound) {
                        break;
                    }
                }
            }
            if (!versionFound) {
                Log.logAndDisplay(LogLevel.ERROR, ADB,
                        "Failed to parse the output of 'adb version'"); 
            }
        } catch (IOException e) {
            Log.logAndDisplay(LogLevel.ERROR, ADB,
                    "Failed to get the adb version: " + e.getMessage()); 
        } catch (InterruptedException e) {
        } finally {
        }
    }
    private boolean scanVersionLine(String line) {
        if (line != null) {
            Matcher matcher = sAdbVersion.matcher(line);
            if (matcher.matches()) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                int microVersion = Integer.parseInt(matcher.group(3));
                if (microVersion < ADB_VERSION_MICRO_MIN) {
                    String message = String.format(
                            "Required minimum version of adb: %1$d.%2$d.%3$d." 
                            + "Current version is %1$d.%2$d.%4$d", 
                            majorVersion, minorVersion, ADB_VERSION_MICRO_MIN,
                            microVersion);
                    Log.logAndDisplay(LogLevel.ERROR, ADB, message);
                } else if (ADB_VERSION_MICRO_MAX != -1 &&
                        microVersion > ADB_VERSION_MICRO_MAX) {
                    String message = String.format(
                            "Required maximum version of adb: %1$d.%2$d.%3$d." 
                            + "Current version is %1$d.%2$d.%4$d", 
                            majorVersion, minorVersion, ADB_VERSION_MICRO_MAX,
                            microVersion);
                    Log.logAndDisplay(LogLevel.ERROR, ADB, message);
                } else {
                    mVersionCheck = true;
                }
                return true;
            }
        }
        return false;
    }
    boolean start() {
        if (mAdbOsLocation != null && (mVersionCheck == false || startAdb() == false)) {
            return false;
        }
        mStarted = true;
        mDeviceMonitor = new DeviceMonitor(this);
        mDeviceMonitor.start();
        return true;
    }
    boolean stop() {
        if (mStarted == false) {
            return false;
        }
        mDeviceMonitor.stop();
        mDeviceMonitor = null;
        if (stopAdb() == false) {
            return false;
        }
        mStarted = false;
        return true;
    }
    public boolean restart() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                    "Cannot restart adb when AndroidDebugBridge is created without the location of adb."); 
            return false;
        }
        if (mVersionCheck == false) {
            Log.logAndDisplay(LogLevel.ERROR, ADB,
                    "Attempting to restart adb, but version check failed!"); 
            return false;
        }
        synchronized (this) {
            stopAdb();
            boolean restart = startAdb();
            if (restart && mDeviceMonitor == null) {
                mDeviceMonitor = new DeviceMonitor(this);
                mDeviceMonitor.start();
            }
            return restart;
        }
    }
    void deviceConnected(IDevice device) {
        IDeviceChangeListener[] listenersCopy = null;
        synchronized (sLock) {
            listenersCopy = sDeviceListeners.toArray(
                    new IDeviceChangeListener[sDeviceListeners.size()]);
        }
        for (IDeviceChangeListener listener : listenersCopy) {
            try {
                listener.deviceConnected(device);
            } catch (Exception e) {
                Log.e(DDMS, e);
            }
        }
    }
    void deviceDisconnected(IDevice device) {
        IDeviceChangeListener[] listenersCopy = null;
        synchronized (sLock) {
            listenersCopy = sDeviceListeners.toArray(
                    new IDeviceChangeListener[sDeviceListeners.size()]);
        }
        for (IDeviceChangeListener listener : listenersCopy) {
            try {
                listener.deviceDisconnected(device);
            } catch (Exception e) {
                Log.e(DDMS, e);
            }
        }
    }
    void deviceChanged(IDevice device, int changeMask) {
        IDeviceChangeListener[] listenersCopy = null;
        synchronized (sLock) {
            listenersCopy = sDeviceListeners.toArray(
                    new IDeviceChangeListener[sDeviceListeners.size()]);
        }
        for (IDeviceChangeListener listener : listenersCopy) {
            try {
                listener.deviceChanged(device, changeMask);
            } catch (Exception e) {
                Log.e(DDMS, e);
            }
        }
    }
    void clientChanged(Client client, int changeMask) {
        IClientChangeListener[] listenersCopy = null;
        synchronized (sLock) {
            listenersCopy = sClientListeners.toArray(
                    new IClientChangeListener[sClientListeners.size()]);
        }
        for (IClientChangeListener listener : listenersCopy) {
            try {
                listener.clientChanged(client, changeMask);
            } catch (Exception e) {
                Log.e(DDMS, e);
            }
        }
    }
    DeviceMonitor getDeviceMonitor() {
        return mDeviceMonitor;
    }
    synchronized boolean startAdb() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                "Cannot start adb when AndroidDebugBridge is created without the location of adb."); 
            return false;
        }
        Process proc;
        int status = -1;
        try {
            String[] command = new String[2];
            command[0] = mAdbOsLocation;
            command[1] = "start-server"; 
            Log.d(DDMS,
                    String.format("Launching '%1$s %2$s' to ensure ADB is running.", 
                    mAdbOsLocation, command[1]));
            proc = Runtime.getRuntime().exec(command);
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            status = grabProcessOutput(proc, errorOutput, stdOutput,
                    false );
        } catch (IOException ioe) {
            Log.d(DDMS, "Unable to run 'adb': " + ioe.getMessage()); 
        } catch (InterruptedException ie) {
            Log.d(DDMS, "Unable to run 'adb': " + ie.getMessage()); 
        }
        if (status != 0) {
            Log.w(DDMS,
                    "'adb start-server' failed -- run manually if necessary"); 
            return false;
        }
        Log.d(DDMS, "'adb start-server' succeeded"); 
        return true;
    }
    private synchronized boolean stopAdb() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                "Cannot stop adb when AndroidDebugBridge is created without the location of adb."); 
            return false;
        }
        Process proc;
        int status = -1;
        try {
            String[] command = new String[2];
            command[0] = mAdbOsLocation;
            command[1] = "kill-server"; 
            proc = Runtime.getRuntime().exec(command);
            status = proc.waitFor();
        }
        catch (IOException ioe) {
        }
        catch (InterruptedException ie) {
        }
        if (status != 0) {
            Log.w(DDMS,
                    "'adb kill-server' failed -- run manually if necessary"); 
            return false;
        }
        Log.d(DDMS, "'adb kill-server' succeeded"); 
        return true;
    }
    private int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
            final ArrayList<String> stdOutput, boolean waitforReaders)
            throws InterruptedException {
        assert errorOutput != null;
        assert stdOutput != null;
        Thread t1 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            Log.e(ADB, line);
                            errorOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        Thread t2 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            Log.d(ADB, line);
                            stdOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        t1.start();
        t2.start();
        if (waitforReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }
        return process.waitFor();
    }
    static Object getLock() {
        return sLock;
    }
}
