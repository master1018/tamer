public class AllTestsLang
{
    public static void run() {
        TestRunner.main(new String[] { AllTestsLang.class.getName() });
    }
    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("Tests for java.lang");
        suite.addTest(org.apache.harmony.luni.tests.java.lang.AllTests.suite());
        suite.addTest(tests.api.java.lang.AllTests.suite());
        suite.addTest(tests.api.java.lang.ref.AllTests.suite());
        suite.addTest(tests.api.java.lang.reflect.AllTests.suite());
        return suite;
    }
}
