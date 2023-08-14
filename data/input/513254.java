public class DeviceManager implements IDeviceChangeListener {
    private static final int SHORT_DELAY = 1000 * 15; 
    private static final int LONG_DELAY = 1000 * 60 * 10; 
    private static final int REBOOT_DELAY = 5 * 1000; 
    private static final int POST_BOOT_DELAY = 1000 * 60; 
    private static final int MAX_ADB_RESTART_ATTEMPTS = 10;
    ArrayList<TestDevice> mDevices;
    private Semaphore mSemaphore = new Semaphore(0);
    public DeviceManager() {
        mDevices = new ArrayList<TestDevice>();
    }
    public void initAdb() {
        String adbLocation = getAdbLocation();
        Log.d("init adb...");
        AndroidDebugBridge.init(true);
        AndroidDebugBridge.addDeviceChangeListener(this);
        AndroidDebugBridge.createBridge(adbLocation, true);
    }
    public static String getAdbLocation() {
        return "adb";
    }
    public TestDevice[] allocateDevices(final int num) throws DeviceNotAvailableException {
        ArrayList<TestDevice> deviceList;
        TestDevice td;
        int index = 0;
        if (num < 0) {
            throw new IllegalArgumentException();
        }
        if (num > mDevices.size()) {
            throw new DeviceNotAvailableException("The number of connected device("
                    + mDevices.size() + " is less than the specified number("
                    + num + "). Please plug in enough devices");
        }
        deviceList = new ArrayList<TestDevice>();
        while (index < mDevices.size() && deviceList.size() != num) {
            td = mDevices.get(index);
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                deviceList.add(td);
            }
            index++;
        }
        if (deviceList.size() != num) {
            throw new DeviceNotAvailableException("Can't get the specified number("
                    + num + ") of idle device(s).");
        }
        return deviceList.toArray(new TestDevice[num]);
    }
    public final TestDevice[] getDeviceList() {
        return mDevices.toArray(new TestDevice[mDevices.size()]);
    }
    public int getCountOfFreeDevices() {
        int count = 0;
        for (TestDevice td : mDevices) {
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                count++;
            }
        }
        return count;
    }
    private void appendDevice(final IDevice device) {
        if (-1 == getDeviceIndex(device)) {
            TestDevice td = new TestDevice(device);
            mDevices.add(td);
        }
    }
    private void removeDevice(final IDevice device) {
        int index = getDeviceIndex(device);
        if (index == -1) {
            Log.d("Can't find " + device + " in device list of DeviceManager");
            return;
        }
        mDevices.get(index).disconnected();
        mDevices.remove(index);
    }
    private int getDeviceIndex(final IDevice device) {
        TestDevice td;
        for (int index = 0; index < mDevices.size(); index++) {
            td = mDevices.get(index);
            if (td.getSerialNumber().equals(device.getSerialNumber())) {
                return index;
            }
        }
        return -1;
    }
    private TestDevice searchTestDevice(final String deviceSerialNumber) {
        for (TestDevice td : mDevices) {
            if (td.getSerialNumber().equals(deviceSerialNumber)) {
                return td;
            }
        }
        return null;
    }
    public void deviceChanged(IDevice device, int changeMask) {
        Log.d("device " + device.getSerialNumber() + " changed with changeMask=" + changeMask);
        Log.d("Device state:" + device.getState());
    }
    public void deviceConnected(IDevice device) {
        new DeviceServiceMonitor(device).start();
    }
    private class DeviceServiceMonitor extends Thread {
        private IDevice mDevice;
        public DeviceServiceMonitor(IDevice device) {
            mDevice = device;
        }
        @Override
        public void run() {
            try {
               while (mDevice.getSyncService() == null || mDevice.getPropertyCount() == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e("", e);
                    }
                }
                CUIOutputStream.println("Device(" + mDevice + ") connected");
                if (!TestSession.isADBServerRestartedMode()) {
                    CUIOutputStream.printPrompt();
                }
                appendDevice(mDevice);
                mSemaphore.release();
            } catch (IOException e) {
            }
        }
    }
    public void deviceDisconnected(IDevice device) {
        removeDevice(device);
    }
    public TestDevice allocateFreeDeviceById(String deviceId) throws DeviceNotAvailableException {
        for (TestDevice td : mDevices) {
            if (td.getSerialNumber().equals(deviceId)) {
                if (td.getStatus() != TestDevice.STATUS_IDLE) {
                    String msg = "The specifed device(" + deviceId + ") is " +
                    td.getStatusAsString();
                    throw new DeviceNotAvailableException(msg);
                }
                return td;
            }
        }
        throw new DeviceNotAvailableException("The specified device(" +
                deviceId + "cannot be found");
    }
    public void resetTestDevice(final TestDevice device) {
        if (device.getStatus() != TestDevice.STATUS_OFFLINE) {
            device.setStatus(TestDevice.STATUS_IDLE);
        }
    }
    public void restartADBServer(TestSession ts) throws DeviceDisconnectedException {
        try {
            Thread.sleep(SHORT_DELAY); 
            Log.i("Restarting device ...");
            rebootDevice(ts);
            Log.i("Restart complete.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void rebootDevice(TestSession ts) throws InterruptedException,
                DeviceDisconnectedException {
        String deviceSerialNumber = ts.getDeviceId();
        if (!deviceSerialNumber.toLowerCase().startsWith("emulator")) {
            executeCommand("adb -s " + deviceSerialNumber + " reboot");
            Thread.sleep(REBOOT_DELAY);
            int attempts = 0;
            boolean deviceConnected = false;
            while (!deviceConnected && (attempts < MAX_ADB_RESTART_ATTEMPTS)) {
                AndroidDebugBridge.disconnectBridge();
                executeCommand("adb kill-server");
                mSemaphore.drainPermits();
                AndroidDebugBridge.createBridge(getAdbLocation(), true);
                boolean deviceFound = false;
                while (!deviceFound) {
                    mSemaphore.tryAcquire(LONG_DELAY, TimeUnit.MILLISECONDS);
                    TestDevice device = searchTestDevice(deviceSerialNumber);
                    if (device != null) {
                        ts.setTestDevice(device);
                        deviceFound = true;
                        deviceConnected = device.waitForBootComplete();
                        try {
                            Thread.sleep(POST_BOOT_DELAY);
                        } catch (InterruptedException ignored) {
                        }
                        TestDevice newDevice = searchTestDevice(deviceSerialNumber);
                        if (newDevice != null) {
                            ts.setTestDevice(newDevice);
                            if (newDevice != device) {
                                String cmd = String.format("adb -s %s shell bugreport -o " +
                                            "/sdcard/bugreports/doubleReboot", deviceSerialNumber);
                                executeCommand(cmd);
                            }
                        } else {
                            deviceFound = false; 
                        }
                    }
                }
                attempts += 1;
            }
        }
    }
    private boolean executeCommand(String command) {
        Log.d("executeCommand(): cmd=" + command);
        try {
            Process proc = Runtime.getRuntime().exec(command);
            TimeoutThread tt = new TimeoutThread(proc, SHORT_DELAY);
            tt.start();
            proc.waitFor(); 
            tt.interrupt(); 
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    class TimeoutThread extends Thread {
        Process mProcess;
        long mTimeout;
        TimeoutThread(Process process, long timeout) {
            mProcess = process;
            mTimeout = timeout;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(mTimeout);
            } catch (InterruptedException e) {
                return;
            }
            mProcess.destroy();
        }
    }
}
