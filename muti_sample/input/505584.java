public class AllTestsNet
{
    public static void run() {
        TestRunner.main(new String[] { AllTestsNet.class.getName() });
    }
    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("Tests for java.net");
        suite.addTest(org.apache.harmony.luni.tests.java.net.AllTests.suite());
        suite.addTest(tests.api.java.net.AllTests.suite());
        suite.addTest(org.apache.harmony.luni.tests.internal.net.www.protocol.http.AllTests.suite());
        suite.addTest(org.apache.harmony.luni.tests.internal.net.www.protocol.https.AllTests.suite());
        return suite;
    }
}
