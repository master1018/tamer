public class DeviceTestSuite extends TestSuite implements DeviceTest {
    private IDevice mDevice = null;
    private String mTestDataPath = null;
    public DeviceTestSuite(Class testClass) {
        super(testClass);
    }
    public DeviceTestSuite() {
        super();
    }
    @Override
    public void addTestSuite(Class testClass) {
        addTest(new DeviceTestSuite(testClass));
    }
    @Override
    public void runTest(Test test, TestResult result) {
        if (test instanceof DeviceTest) {
            DeviceTest deviceTest = (DeviceTest)test;
            deviceTest.setDevice(mDevice);
            deviceTest.setTestAppPath(mTestDataPath);
        }
        test.run(result);
    }
    public IDevice getDevice() {
        return mDevice;
    }
    public String getTestAppPath() {
        return mTestDataPath;
    }
    public void setDevice(IDevice device) {
        mDevice = device;
    }
    public void setTestAppPath(String path) {
        mTestDataPath = path;
    }
}
