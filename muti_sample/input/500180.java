public class RemoteAdtTestRunner extends RemoteTestRunner {
    private AndroidJUnitLaunchInfo mLaunchInfo;
    private TestExecution mExecution;
    protected void init(String[] args, AndroidJUnitLaunchInfo launchInfo) {
        defaultInit(args);
        mLaunchInfo = launchInfo;
    }
    @Override
    public void runTests(String[] testClassNames, String testName, TestExecution execution) {
        mExecution = execution;
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(mLaunchInfo.getAppPackage(),
                mLaunchInfo.getRunner(), mLaunchInfo.getDevice());
        if (mLaunchInfo.getTestClass() != null) {
            if (mLaunchInfo.getTestMethod() != null) {
                runner.setMethodName(mLaunchInfo.getTestClass(), mLaunchInfo.getTestMethod());
            } else {
                runner.setClassName(mLaunchInfo.getTestClass());
            }
        }
        if (mLaunchInfo.getTestPackage() != null) {
            runner.setTestPackageName(mLaunchInfo.getTestPackage());
        }
        runner.setLogOnly(true);
        TestCollector collector = new TestCollector();
        AdtPlugin.printToConsole(mLaunchInfo.getProject(), "Collecting test information");
        runner.run(collector);
        if (collector.getErrorMessage() != null) {
            reportError(collector.getErrorMessage());
            notifyTestRunEnded(0);
            return;
        }
        notifyTestRunStarted(collector.getTestCaseCount());
        AdtPlugin.printToConsole(mLaunchInfo.getProject(),
                "Sending test information to Eclipse");
        collector.sendTrees(this);
        runner.setLogOnly(false);
        if (mLaunchInfo.isDebugMode()) {
            runner.setDebug(true);
        }
        AdtPlugin.printToConsole(mLaunchInfo.getProject(), "Running tests...");
        runner.run(new TestRunListener());
    }
    public void runTests(String[] programArgs, AndroidJUnitLaunchInfo junitInfo) {
        init(programArgs, junitInfo);
        run();
    }
    public void terminate() {
        stop();
    }
    @Override
    protected void stop() {
        if (mExecution != null) {
            mExecution.stop();
        }
    }
    private void notifyTestRunEnded(long elapsedTime) {
        sendMessage(MessageIds.TEST_RUN_END + elapsedTime);
        flush();
    }
    private void reportError(String errorMessage) {
        AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(),
                String.format(LaunchMessages.RemoteAdtTestRunner_RunFailedMsg_s, errorMessage));
    }
    private class TestRunListener implements ITestRunListener {
        public void testEnded(TestIdentifier test) {
            mExecution.getListener().notifyTestEnded(new TestCaseReference(test));
        }
        public void testFailed(TestFailure status, TestIdentifier test, String trace) {
            String statusString;
            if (status == TestFailure.ERROR) {
                statusString = MessageIds.TEST_ERROR;
            } else {
                statusString = MessageIds.TEST_FAILED;
            }
            TestReferenceFailure failure =
                new TestReferenceFailure(new TestCaseReference(test),
                        statusString, trace, null);
            mExecution.getListener().notifyTestFailed(failure);
        }
        public void testRunEnded(long elapsedTime) {
            notifyTestRunEnded(elapsedTime);
            AdtPlugin.printToConsole(mLaunchInfo.getProject(),
                    LaunchMessages.RemoteAdtTestRunner_RunCompleteMsg);
        }
        public void testRunFailed(String errorMessage) {
            reportError(errorMessage);
        }
        public void testRunStarted(int testCount) {
        }
        public void testRunStopped(long elapsedTime) {
            notifyTestRunStopped(elapsedTime);
            AdtPlugin.printToConsole(mLaunchInfo.getProject(),
                    LaunchMessages.RemoteAdtTestRunner_RunStoppedMsg);
        }
        public void testStarted(TestIdentifier test) {
            TestCaseReference testId = new TestCaseReference(test);
            mExecution.getListener().notifyTestStarted(testId);
        }
    }
    @Override
    protected boolean connect() {
        boolean result = super.connect();
        if (!result) {
            AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(),
                    "Connect to Eclipse test result listener failed");
        }
        return result;
    }
    @Override
    public void runFailed(String message, Exception exception) {
        if (exception != null) {
            AdtPlugin.logAndPrintError(exception, mLaunchInfo.getProject().getName(),
                    "Test launch failed: %s", message);
        } else {
            AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(), "Test launch failed: %s",
                    message);
        }
    }
}