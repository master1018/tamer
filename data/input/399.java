public class DeviceManager {
    private static DeviceManager _instance = null;
    private Map deviceList = new HashMap();
    public static synchronized DeviceManager getInstance() throws Throwable {
        if (_instance == null) {
            _instance = new DeviceManager();
        }
        return _instance;
    }
    private DeviceManager() throws Throwable {
        loadPmpLib();
    }
    public Map getDeviceList() {
        return deviceList;
    }
    public void setDeviceList(Map deviceList) {
        this.deviceList = deviceList;
    }
    public void loadPmpLib() throws Throwable {
        boolean libloaded = false;
        String shortLibName = "";
        String fullLibName = "";
        if (OsHelper.isLinux()) {
            shortLibName = Constant.shortLinuxLibname;
            fullLibName = Constant.fullLinuxLibname;
        }
        if (OsHelper.isWindows()) {
            shortLibName = Constant.shortWindowsLibname;
            fullLibName = Constant.fullWindowsLibname;
        }
        if (shortLibName.trim().length() == 0) {
            throw new Exception("Unknown OS");
        }
        try {
            System.loadLibrary(shortLibName);
            libloaded = true;
        } catch (Throwable t) {
        }
        if (!libloaded) {
            File f = new File(".");
            String currentDir = f.getCanonicalPath();
            System.load(currentDir + File.separator + fullLibName);
        }
    }
    public void dump() {
        synchronized (this) {
            System.out.println("===========================================");
            if (deviceList.size() > 0) {
                for (Iterator it = deviceList.keySet().iterator(); it.hasNext(); ) {
                    String key = (String) it.next();
                    UsbDevice usbdev = (UsbDevice) deviceList.get(key);
                    System.out.println(usbdev.dump());
                }
            } else {
                System.out.println("No device detected.");
            }
            System.out.println("===========================================");
        }
    }
    public void addDevice(UsbDevice usbdev) {
        synchronized (this) {
            if (usbdev != null) {
                System.out.println(usbdev.dump());
                if (!deviceList.containsKey(usbdev.getCanonical())) {
                    deviceList.put(usbdev.getCanonical(), usbdev);
                }
            }
        }
    }
    public void removeDevice(String deviceid) {
        synchronized (this) {
            if (deviceList.containsKey(deviceid)) {
                deviceList.remove(deviceid);
            }
        }
    }
    public long scanDevices() {
        synchronized (this) {
            long result = 0;
            deviceList.clear();
            result = detectDevices();
            return result;
        }
    }
    public native long createInstance();
    public native long releaseInstance();
    public native long setVerbose(int verbose);
    private native long detectDevices();
    public native long checkDevice(String deviceidentifier);
    public native long releaseDevice(String canonical);
}
