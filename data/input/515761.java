public class DeviceConnector {
    private static final int MAX_WAIT_DEVICE_TIME = 5000;
    public IDevice connectToDevice(String deviceSerial) {
        AndroidDebugBridge.init(false );
        AndroidDebugBridge adbBridge = AndroidDebugBridge.createBridge();
        for (IDevice device : adbBridge.getDevices()) {
            if (deviceSerial == null) {
                return device;
            } else if (deviceSerial.equals(device.getSerialNumber())) {
                return device;
            }
        }
        System.out.println("Waiting for device...");
        NewDeviceListener listener = new NewDeviceListener(deviceSerial);
        AndroidDebugBridge.addDeviceChangeListener(listener);
        IDevice device = listener.waitForDevice(MAX_WAIT_DEVICE_TIME);
        AndroidDebugBridge.removeDeviceChangeListener(listener);
        if (device == null) {
            throw new IllegalArgumentException("Could not connect to device");
        } else {
            System.out.println(String.format("Connected to %s", device.getSerialNumber()));
        }
        return device;
    }
    private static class NewDeviceListener implements IDeviceChangeListener {
        private IDevice mDevice;
        private String mSerial;
        public NewDeviceListener(String serial) {
            mSerial = serial;
        }
        public void deviceChanged(IDevice device, int changeMask) {
        }
        public void deviceConnected(IDevice device) {
            if (mSerial == null) {
                setDevice(device);
            } else if (mSerial.equals(device.getSerialNumber())) {
                setDevice(device);
            }
        }
        private synchronized void setDevice(IDevice device) {
            mDevice = device;
            notify();
        }
        public void deviceDisconnected(IDevice device) {
        }
        public IDevice waitForDevice(long waitTime) {
            synchronized(this) {
                if (mDevice == null) {
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                        System.out.println("Waiting for device interrupted");
                    }
                }
            }
            return mDevice;
        }
    }
}
