public class BundlePrinter extends ResultPrinter {
    private Bundle mResults;
    private boolean mFailure;
    private boolean mError;
    public BundlePrinter(PrintStream writer, Bundle result) {
        super(writer);
        mResults = result;
    }
    @Override
    public void addError(Test test, Throwable t) {
        mResults.putString(getComboName(test), BaseTestRunner.getFilteredTrace(t));
        mFailure = true;
        super.addError(test, t);
    }
    @Override
    public void addFailure(Test test, AssertionFailedError t) {
        mResults.putString(getComboName(test), BaseTestRunner.getFilteredTrace(t));
        mError = true;
        super.addFailure(test, t);
    }
    @Override
    public void endTest(Test test) {
        if (!mFailure && !mError) {
            mResults.putString(getComboName(test), "passed");
        }
        super.endTest(test);
    }
    @Override
    public void startTest(Test test) {
        mFailure = false;
        mError = false;
        super.startTest(test);
    }
    private String getComboName(Test test) {
        return test.getClass().getName() + ":" + ((TestCase) test).getName();
    }
}
