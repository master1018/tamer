public class ActivityTests {
    public static final boolean DEBUG_LIFECYCLE = false;
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(ActivityTests.class.getName());
        suite.addTestSuite(BroadcastTest.class);
        suite.addTestSuite(IntentSenderTest.class);
        suite.addTestSuite(ActivityManagerTest.class);
        suite.addTestSuite(LaunchTest.class);
        suite.addTestSuite(LifecycleTest.class);
        suite.addTestSuite(ServiceTest.class);
        suite.addTestSuite(MetaDataTest.class);
        suite.addTestSuite(SetTimeZonePermissionsTest.class);
        return suite;
    }
}
