public class DeviceTestRunner extends TestRunner {
    private static final String LOG_TAG = "DeviceTestRunner";
    private String mDeviceSerial = null;
    private IDevice mDevice = null;
    private String mTestDataPath = null;
    private DeviceTestRunner() {
    }
    @Override
    public TestResult start(String[] args) throws Exception {
        List<String> parentArgs = new ArrayList<String>();
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                i++;
                mDeviceSerial = extractArg(args, i);
            } else if (args[i].equals("-p")) {
                i++;
                mTestDataPath = extractArg(args, i);
            } else {
                parentArgs.add(args[i]);
            }
        }
        DeviceConnector connector = new DeviceConnector();
        mDevice = connector.connectToDevice(mDeviceSerial);
        return super.start(parentArgs.toArray(new String[parentArgs.size()]));
    }
    private String extractArg(String[] args, int index) {
        if (args.length <= index) {
            printUsage();
            throw new IllegalArgumentException("Error: not enough arguments");
        }
        return args[index];
    }
    public static void main(String[] args) {
        DeviceTestRunner aTestRunner = new DeviceTestRunner();
        try {
            TestResult r = aTestRunner.start(args);
            if (!r.wasSuccessful())
                System.exit(FAILURE_EXIT);
            System.exit(SUCCESS_EXIT);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
    }
    private static void printUsage() {
        System.out.println("Usage: DeviceTestRunner <test_class> [-s device_serial] " +
                "[-p test_data_path]");
    }
    @Override
    public TestResult doRun(Test test, boolean wait) {
        if (test instanceof DeviceTest) {
            DeviceTest deviceTest = (DeviceTest)test;
            deviceTest.setDevice(mDevice);
            deviceTest.setTestAppPath(mTestDataPath);
        } else {
            Log.w(LOG_TAG, String.format("%s test class is not a DeviceTest.",
                    test.getClass().getName()));
        }
        return super.doRun(test, wait);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected TestResult runSingleMethod(String testCase, String method, boolean wait)
    throws Exception {
        Class testClass = loadSuiteClass(testCase);
        Test test = DeviceTestSuite.createTest(testClass, method);
        return doRun(test, wait);
    }
}
