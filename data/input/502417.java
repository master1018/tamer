public class GroovyTestsSuite extends TestSuite {
    static final String GROOVY_TEST_PACKAGE = "com.android.ide.eclipse.tests.groovytests";
    public GroovyTestsSuite() {
    }
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestGroovy.class);
        return suite;
    }
}
