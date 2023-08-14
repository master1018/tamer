public class CoreTestResult extends TestResult {
    protected int fFlags;
    protected int fTimeout;
    protected int fTotalTestCount;
    protected int fAndroidOnlyCount;
    protected int fBrokenTestCount;
    protected int fKnownFailureCount;
    protected int fSideEffectCount;
    protected int fNormalTestCount;
    protected int fIgnoredCount;
    public CoreTestResult(int flags, int timeout) {
        super();
        fFlags = flags;
        fTimeout = timeout;
    }
    @SuppressWarnings("unchecked")
    boolean hasAnnotation(TestCase test, Class clazz) {
        try {
            Method method = test.getClass().getMethod(test.getName());
            return method.getAnnotation(clazz) != null;
        } catch (Exception e) {
        }
        return false;
    }
    @Override
    @SuppressWarnings("deprecation")
    public void runProtected(final Test test, Protectable p) {
        if ((fFlags & CoreTestSuite.DRY_RUN) == 0) {
            if (test instanceof TestCase) {
                TestCase testCase = (TestCase)test;
                boolean invert = hasAnnotation(testCase, KnownFailure.class) &&
                        (fFlags & CoreTestSuite.INVERT_KNOWN_FAILURES) != 0;
                boolean isolate = hasAnnotation(testCase, SideEffect.class) &&
                        (fFlags & CoreTestSuite.ISOLATE_NONE) == 0 ||
                        (fFlags & CoreTestSuite.ISOLATE_ALL) != 0;
                CoreTestRunnable runnable = new CoreTestRunnable(
                        testCase, this, p, invert, isolate);
                if (fTimeout > 0) {
                    Thread thread = new Thread(runnable);
                    thread.start();
                    try {
                        thread.join(fTimeout * 1000);
                    } catch (InterruptedException ex) {
                    }
                    if (thread.isAlive()) {
                        StackTraceElement[] trace = thread.getStackTrace();
                        runnable.stop();
                        thread.stop();
                        try {
                            thread.join(fTimeout * 1000);
                        } catch (InterruptedException ex) {
                        }
                        CoreTestTimeout timeout = new CoreTestTimeout("Test timed out");
                        timeout.setStackTrace(trace);
                        addError(test, timeout);
                    }
                } else {
                    runnable.run();
                }
            }        
        }
    }
    void updateStats(int total, int androidOnly, int broken, int knownFailure,
            int normal, int ignored, int sideEffect) {
        this.fTotalTestCount += total;
        this.fAndroidOnlyCount += androidOnly;
        this.fBrokenTestCount += broken;
        this.fKnownFailureCount += knownFailure;
        this.fNormalTestCount += normal;
        this.fIgnoredCount += ignored;
        this.fSideEffectCount += sideEffect;
    }
}
