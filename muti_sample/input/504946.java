public class AllTests extends TestSuite {
    public AllTests() {
    }
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(FuncTests.suite());
        suite.addTest(UnitTests.suite());
        suite.addTest(GroovyTestsSuite.suite());
        return suite;
    }
}
