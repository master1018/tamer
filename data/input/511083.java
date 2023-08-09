public class BundleTestListener implements TestListener {
    private Bundle mBundle;
    private boolean mFailed;
    public BundleTestListener(Bundle bundle) {
        mBundle = bundle;
    }
    public void addError(Test test, Throwable t) {
        mBundle.putString(getComboName(test), BaseTestRunner.getFilteredTrace(t));
        mFailed = true;
    }
    public void addFailure(Test test, junit.framework.AssertionFailedError t) {
        mBundle.putString(getComboName(test), BaseTestRunner.getFilteredTrace(t));
        mFailed = true;
    }
    public void endTest(Test test) {
        if (!mFailed) {
            mBundle.putString(getComboName(test), "passed");
        }
    }
    public void startTest(Test test) {
        mFailed = false;
    }
    private String getComboName(Test test) {
        return test.getClass().getName() + ":" + ((TestCase) test).getName();
    }
}
