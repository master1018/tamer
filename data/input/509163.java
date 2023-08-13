public class PowerTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(PowerMeasurement.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return PowerTestRunner.class.getClassLoader();
    }
}
