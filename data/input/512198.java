public class CalculatorInstrumentationTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(CalculatorHitSomeButtons.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return CalculatorInstrumentationTestRunner.class.getClassLoader();
    }
}
