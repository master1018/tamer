public class DeviceBridge {
    private static AndroidDebugBridge bridge;
    private static final HashMap<IDevice, Integer> devicePortMap = new HashMap<IDevice, Integer>();
    private static int nextLocalPort = Configuration.DEFAULT_SERVER_PORT;
    public static void initDebugBridge() {
        if (bridge == null) {
            AndroidDebugBridge.init(false );
        }
        if (bridge == null || !bridge.isConnected()) {
            String adbLocation = System.getProperty("hierarchyviewer.adb");
            if (adbLocation != null && adbLocation.length() != 0) {
                adbLocation += File.separator + "adb";
            } else {
                adbLocation = "adb";
            }
            bridge = AndroidDebugBridge.createBridge(adbLocation, true);
        }
    }
    public static void startListenForDevices(AndroidDebugBridge.IDeviceChangeListener listener) {
        AndroidDebugBridge.addDeviceChangeListener(listener);
    }
    public static void stopListenForDevices(AndroidDebugBridge.IDeviceChangeListener listener) {
        AndroidDebugBridge.removeDeviceChangeListener(listener);
    }
    public static IDevice[] getDevices() {
        return bridge.getDevices();
    }
    public static boolean isViewServerRunning(IDevice device) {
        initDebugBridge();
        final boolean[] result = new boolean[1];
        try {
            if (device.isOnline()) {
                device.executeShellCommand(buildIsServerRunningShellCommand(),
                        new BooleanResultReader(result));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result[0];
    }
    public static boolean startViewServer(IDevice device) {
        return startViewServer(device, Configuration.DEFAULT_SERVER_PORT);
    }
    public static boolean startViewServer(IDevice device, int port) {
        initDebugBridge();
        final boolean[] result = new boolean[1];
        try {
            if (device.isOnline()) {
                device.executeShellCommand(buildStartServerShellCommand(port),
                        new BooleanResultReader(result));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result[0];
    }
    public static boolean stopViewServer(IDevice device) {
        initDebugBridge();
        final boolean[] result = new boolean[1];
        try {
            if (device.isOnline()) {
                device.executeShellCommand(buildStopServerShellCommand(),
                        new BooleanResultReader(result));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result[0];
    }
    public static void terminate() {
        AndroidDebugBridge.terminate();
    }
    public static void setupDeviceForward(IDevice device) {
        synchronized (devicePortMap) {
            if (device.getState() == IDevice.DeviceState.ONLINE) {
                int localPort = nextLocalPort++;
                device.createForward(localPort, Configuration.DEFAULT_SERVER_PORT);
                devicePortMap.put(device, localPort);
            }
        }
    }
    public static void removeDeviceForward(IDevice device) {
        synchronized (devicePortMap) {
            final Integer localPort = devicePortMap.get(device);
            if (localPort != null) {
                device.removeForward(localPort, Configuration.DEFAULT_SERVER_PORT);
                devicePortMap.remove(device);
            }
        }
    }
    public static int getDeviceLocalPort(IDevice device) {
        synchronized (devicePortMap) {
            Integer port = devicePortMap.get(device);
            if (port != null) {
                return port;
            }
            Log.e("hierarchy", "Missing forwarded port for " + device.getSerialNumber());
            return -1;
        }
    }
    private static String buildStartServerShellCommand(int port) {
        return String.format("service call window %d i32 %d",
                Configuration.SERVICE_CODE_START_SERVER, port);
    }
    private static String buildStopServerShellCommand() {
        return String.format("service call window %d", Configuration.SERVICE_CODE_STOP_SERVER);
    }
    private static String buildIsServerRunningShellCommand() {
        return String.format("service call window %d",
                Configuration.SERVICE_CODE_IS_SERVER_RUNNING);
    }
    private static class BooleanResultReader extends MultiLineReceiver {
        private final boolean[] mResult;
        public BooleanResultReader(boolean[] result) {
            mResult = result;
        }
        @Override
        public void processNewLines(String[] strings) {
            if (strings.length > 0) {
                Pattern pattern = Pattern.compile(".*?\\([0-9]{8} ([0-9]{8}).*");
                Matcher matcher = pattern.matcher(strings[0]);
                if (matcher.matches()) {
                    if (Integer.parseInt(matcher.group(1)) == 1) {
                        mResult[0] = true;
                    }
                }
            }
        }
        public boolean isCancelled() {
            return false;
        }
    }
}
