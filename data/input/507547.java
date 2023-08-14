public class ConnectivityManagerTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(ConnectivityManagerMobileTest.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return ConnectivityManagerTestRunner.class.getClassLoader();
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String testSSID = (String) icicle.get("ssid");
        if (testSSID != null) {
            TEST_SSID = testSSID;
        }
    }
    public String TEST_SSID = null;
}
