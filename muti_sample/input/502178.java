public class AllTestsIo
{
    public static void run() {
        TestRunner.main(new String[] { AllTestsIo.class.getName() });
    }
    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("Tests for java.io");
        suite.addTest(tests.api.java.io.AllTests.suite());
        suite.addTest(org.apache.harmony.luni.tests.java.io.AllTests.suite());
        return suite;
    }
}
