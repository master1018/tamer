public abstract class DeviceTestCase extends TestCase implements DeviceTest {
    private IDevice mDevice = null;
    private String mTestDataPath = null;
    protected DeviceTestCase() {
    }
    public void setDevice(IDevice device) {
        mDevice = device;
    }
    public IDevice getDevice() {
        return mDevice;
    }
    public String getTestAppPath() {
        return mTestDataPath;
    }
    public void setTestAppPath(String path) {
        mTestDataPath = path;
    }
    @Override
    protected void setUp() throws Exception {
        assertNotNull(getDevice());
    }
}
