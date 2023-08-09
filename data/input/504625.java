public class KeyStoreTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(android.security.tests.KeyStoreTest.class);
        suite.addTestSuite(android.security.tests.SystemKeyStoreTest.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return KeyStoreTestRunner.class.getClassLoader();
    }
}
