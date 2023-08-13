public class FrameworkTests {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(FrameworkTests.class.getName());
        suite.addTestSuite(MultipartTest.class);
        suite.addTestSuite(LoggingPrintStreamTest.class);
        suite.addTestSuite(LockPatternKeyguardViewTest.class);
        return suite;
    }
}
