public class DeviceManagerTests extends CtsTestBase {
    private TestDevice d1, d2, d3;
    private static String d1SerialNumber = "mock_device1";
    private static String d2SerialNumber = "mock_device2";
    private static String d3SerialNumber = "mock_device3";
    @Override
    public void setUp() {
        d1 = new TestDevice(d1SerialNumber);
        d2 = new TestDevice(d2SerialNumber);
        d3 = new TestDevice(d3SerialNumber);
    }
    @Override
    public void tearDown() {
        d1 = d2 = d3 = null;
    }
    public void testAllocateDevices() throws DeviceNotAvailableException {
        DeviceManager dm = new DeviceManager();
        TestDevice[] devices;
        try {
            devices = dm.allocateDevices(1);
            fail();
        } catch (DeviceNotAvailableException e) {
        }
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        try {
            devices = dm.allocateDevices(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        devices = dm.allocateDevices(0);
        assertEquals(0, devices.length);
        try {
            devices = dm.allocateDevices(4);
            fail();
        } catch (DeviceNotAvailableException e) {
        }
        devices = dm.allocateDevices(2);
        assertEquals(2, devices.length);
        assertEquals(d1SerialNumber, devices[0].getSerialNumber());
        assertEquals(d2SerialNumber, devices[1].getSerialNumber());
        d1.setStatus(TestDevice.STATUS_BUSY);
        d3.setStatus(TestDevice.STATUS_OFFLINE);
        try {
            devices = dm.allocateDevices(2);
            fail();
        } catch (DeviceNotAvailableException e) {
        }
        devices = dm.allocateDevices(1);
        assertEquals(1, devices.length);
        assertEquals(d2SerialNumber, devices[0].getSerialNumber());
    }
    public void testGetDeviceList() {
        DeviceManager dm = new DeviceManager();
        TestDevice[] devices;
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        devices = dm.getDeviceList();
        assertEquals(3, devices.length);
        assertEquals(d1SerialNumber, devices[0].getSerialNumber());
        assertEquals(d2SerialNumber, devices[1].getSerialNumber());
        assertEquals(d3SerialNumber, devices[2].getSerialNumber());
    }
    public void testGetNumOfAllAvailableDevices() {
        DeviceManager dm = new DeviceManager();
        assertEquals(0, dm.getCountOfFreeDevices());
        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        assertEquals(1, dm.getCountOfFreeDevices());
    }
    public void testDeviceDefaultStatus() {
        assertEquals(TestDevice.STATUS_IDLE, d1.getStatus());
        assertEquals(TestDevice.STATUS_IDLE, d2.getStatus());
        assertEquals(TestDevice.STATUS_IDLE, d3.getStatus());
    }
    public void testAllocateDeviceById() throws DeviceNotAvailableException {
        DeviceManager dm = new DeviceManager();
        TestDevice device;
        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        try {
            device = dm.allocateFreeDeviceById("fake device");
            fail();
        } catch (DeviceNotAvailableException e) {
        }
        device = dm.allocateFreeDeviceById(d3SerialNumber);
        assertEquals(d3SerialNumber, device.getSerialNumber());
    }
    public void testResetTestDevices() {
        DeviceManager dm = new DeviceManager();
        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.resetTestDevice(d1);
        dm.resetTestDevice(d2);
        dm.resetTestDevice(d3);
        assertEquals(d1.getStatus(), TestDevice.STATUS_IDLE);
        assertEquals(d2.getStatus(), TestDevice.STATUS_OFFLINE);
        assertEquals(d3.getStatus(), TestDevice.STATUS_IDLE);
    }
}
