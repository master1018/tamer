public class FuncTests extends TestSuite {
    static final String FUNC_TEST_PACKAGE = "com.android.ide.eclipse.tests.functests";
    public FuncTests() {
    }
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ApiDemosRenderingTest.class);
        return suite;
    }
}
