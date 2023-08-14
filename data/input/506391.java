public class AllTestsUtil
{
    public static void run() {
        TestRunner.main(new String[] { AllTestsUtil.class.getName() });
    }
    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("Tests for java.util");
        suite.addTest(tests.api.java.util.AllTests.suite());
        suite.addTest(org.apache.harmony.luni.tests.java.util.AllTests.suite());
        return suite;
    }
}
