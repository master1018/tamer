public class HostSideOnlyTest extends Test {
    private HostSideTestRunner mHostSideTestRunner;
    public HostSideOnlyTest(final TestCase parentCase, final String name,
            final String type, final String knownFailure, final int resCode) {
        super(parentCase, name, type, knownFailure, resCode);
        mHostSideTestRunner = null;
    }
    class HostSideTestRunner extends Thread {
        private HostSideOnlyTest mTest;
        public HostSideTestRunner(final HostSideOnlyTest test) {
            mTest = test;
        }
        @Override
        public void run() {
            HostUnitTestRunner runner = new HostUnitTestRunner(mTest);
            TestController controller = mTest.getTestController();
            TestResult testResult = null;
            try {
                testResult = runner.runTest(controller.getJarPath(),
                        controller.getPackageName(), controller.getClassName(),
                        controller.getMethodName());
            } catch (IOException e) {
                Log.e("IOException while running test from " +
                      controller.getJarPath(), e);
            } catch (ClassNotFoundException e) {
                Log.e("The host controller JAR (" + controller.getJarPath() +
                        ") file doesn't contain class: "
                        + controller.getPackageName() + "."
                        + controller.getClassName(), e);
            }
            synchronized (mTimeOutTimer) {
                mResult.setResult(testResult);
                if (!mTimeOutTimer.isTimeOut()) {
                    Log.d("HostSideTestRunnerThread() detects that it needs to "
                            + "cancel mTimeOutTimer");
                    mTimeOutTimer.sendNotify();
                }
            }
        }
    }
    @Override
    protected void runImpl() {
        mHostSideTestRunner = new HostSideTestRunner(this);
        mHostSideTestRunner.start();
    }
}
