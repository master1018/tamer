public class InstrumentationTestSuite extends TestSuite {
    private final Instrumentation mInstrumentation;
    public InstrumentationTestSuite(Instrumentation instr) {
        mInstrumentation = instr;
    }
    public InstrumentationTestSuite(String name, Instrumentation instr) {
        super(name);
        mInstrumentation = instr;
    }
    public InstrumentationTestSuite(final Class theClass, Instrumentation instr) {
        super(theClass);
        mInstrumentation = instr;
    }
    @Override
    public void addTestSuite(Class testClass) {
        addTest(new InstrumentationTestSuite(testClass, mInstrumentation));
    }
    @Override
    public void runTest(Test test, TestResult result) {
        if (test instanceof InstrumentationTestCase) {
            ((InstrumentationTestCase) test).injectInstrumentation(mInstrumentation);
        }
        super.runTest(test, result);
    }
}
