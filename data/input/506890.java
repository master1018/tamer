public class OsTests {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(OsTests.class.getName());
        suite.addTestSuite(AidlTest.class);
        suite.addTestSuite(BroadcasterTest.class);
        suite.addTestSuite(FileObserverTest.class);
        suite.addTestSuite(IdleHandlerTest.class);
        suite.addTestSuite(MessageQueueTest.class);
        suite.addTestSuite(MessengerTest.class);
        suite.addTestSuite(SystemPropertiesTest.class);
        return suite;
    }
}
