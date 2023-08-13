public class InstrumentationResultParser extends MultiLineReceiver {
    private static class StatusKeys {
        private static final String TEST = "test";
        private static final String CLASS = "class";
        private static final String STACK = "stack";
        private static final String NUMTESTS = "numtests";
        private static final String ERROR = "Error";
        private static final String SHORTMSG = "shortMsg";
    }
    private static class StatusCodes {
        private static final int FAILURE = -2;
        private static final int START = 1;
        private static final int ERROR = -1;
        private static final int OK = 0;
    }
    private static class Prefixes {
        private static final String STATUS = "INSTRUMENTATION_STATUS: ";
        private static final String STATUS_CODE = "INSTRUMENTATION_STATUS_CODE: ";
        private static final String STATUS_FAILED = "INSTRUMENTATION_FAILED: ";
        private static final String CODE = "INSTRUMENTATION_CODE: ";
        private static final String RESULT = "INSTRUMENTATION_RESULT: ";
        private static final String TIME_REPORT = "Time: ";
    }
    private final Collection<ITestRunListener> mTestListeners;
    private static class TestResult {
        private Integer mCode = null;
        private String mTestName = null;
        private String mTestClass = null;
        private String mStackTrace = null;
        private Integer mNumTests = null;
        boolean isComplete() {
            return mCode != null && mTestName != null && mTestClass != null;
        }
        @Override
        public String toString() {
            StringBuilder output = new StringBuilder();
            if (mTestClass != null ) {
                output.append(mTestClass);
                output.append('#');
            }
            if (mTestName != null) {
                output.append(mTestName);
            }
            if (output.length() > 0) {
                return output.toString();
            }
            return "unknown result";
        }
    }
    private TestResult mCurrentTestResult = null;
    private TestResult mLastTestResult = null;
    private String mCurrentKey = null;
    private StringBuilder mCurrentValue = null;
    private boolean mTestStartReported = false;
    private boolean mTestRunFailReported = false;
    private long mTestTime = 0;
    private boolean mIsCancelled = false;
    private int mNumTestsRun = 0;
    private int mNumTestsExpected = 0;
    private static final String LOG_TAG = "InstrumentationResultParser";
    static final String NO_TEST_RESULTS_MSG = "No test results";
    public InstrumentationResultParser(Collection<ITestRunListener> listeners) {
        mTestListeners = new ArrayList<ITestRunListener>(listeners);
    }
    public InstrumentationResultParser(ITestRunListener listener) {
        mTestListeners = new ArrayList<ITestRunListener>(1);
        mTestListeners.add(listener);
    }
    @Override
    public void processNewLines(String[] lines) {
        for (String line : lines) {
            parse(line);
            Log.v(LOG_TAG, line);
        }
    }
    private void parse(String line) {
        if (line.startsWith(Prefixes.STATUS_CODE)) {
            submitCurrentKeyValue();
            parseStatusCode(line);
        } else if (line.startsWith(Prefixes.STATUS)) {
            submitCurrentKeyValue();
            parseKey(line, Prefixes.STATUS.length());
        } else if (line.startsWith(Prefixes.RESULT)) {
            submitCurrentKeyValue();
            parseKey(line, Prefixes.RESULT.length());
        } else if (line.startsWith(Prefixes.STATUS_FAILED) ||
                   line.startsWith(Prefixes.CODE)) {
            submitCurrentKeyValue();
        } else if (line.startsWith(Prefixes.TIME_REPORT)) {
            parseTime(line, Prefixes.TIME_REPORT.length());
        } else {
            if (mCurrentValue != null) {
                mCurrentValue.append("\r\n");
                mCurrentValue.append(line);
            } else {
                Log.i(LOG_TAG, "unrecognized line " + line);
            }
        }
    }
    private void submitCurrentKeyValue() {
        if (mCurrentKey != null && mCurrentValue != null) {
            TestResult testInfo = getCurrentTestInfo();
            String statusValue = mCurrentValue.toString();
            if (mCurrentKey.equals(StatusKeys.CLASS)) {
                testInfo.mTestClass = statusValue.trim();
            } else if (mCurrentKey.equals(StatusKeys.TEST)) {
                testInfo.mTestName = statusValue.trim();
            } else if (mCurrentKey.equals(StatusKeys.NUMTESTS)) {
                try {
                    testInfo.mNumTests = Integer.parseInt(statusValue);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Unexpected integer number of tests, received " + statusValue);
                }
            } else if (mCurrentKey.equals(StatusKeys.ERROR) ||
                    mCurrentKey.equals(StatusKeys.SHORTMSG)) {
                handleTestRunFailed(statusValue);
            } else if (mCurrentKey.equals(StatusKeys.STACK)) {
                testInfo.mStackTrace = statusValue;
            }
            mCurrentKey = null;
            mCurrentValue = null;
        }
    }
    private TestResult getCurrentTestInfo() {
        if (mCurrentTestResult == null) {
            mCurrentTestResult = new TestResult();
        }
        return mCurrentTestResult;
    }
    private void clearCurrentTestInfo() {
        mLastTestResult = mCurrentTestResult;
        mCurrentTestResult = null;
    }
    private void parseKey(String line, int keyStartPos) {
        int endKeyPos = line.indexOf('=', keyStartPos);
        if (endKeyPos != -1) {
            mCurrentKey = line.substring(keyStartPos, endKeyPos).trim();
            parseValue(line, endKeyPos + 1);
        }
    }
    private void parseValue(String line, int valueStartPos) {
        mCurrentValue = new StringBuilder();
        mCurrentValue.append(line.substring(valueStartPos));
    }
    private void parseStatusCode(String line) {
        String value = line.substring(Prefixes.STATUS_CODE.length()).trim();
        TestResult testInfo = getCurrentTestInfo();
        try {
            testInfo.mCode = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Expected integer status code, received: " + value);
        }
        reportResult(testInfo);
        clearCurrentTestInfo();
    }
    public boolean isCancelled() {
        return mIsCancelled;
    }
    public void cancel() {
        mIsCancelled = true;
    }
    private void reportResult(TestResult testInfo) {
        if (!testInfo.isComplete()) {
            Log.w(LOG_TAG, "invalid instrumentation status bundle " + testInfo.toString());
            return;
        }
        reportTestRunStarted(testInfo);
        TestIdentifier testId = new TestIdentifier(testInfo.mTestClass, testInfo.mTestName);
        switch (testInfo.mCode) {
            case StatusCodes.START:
                for (ITestRunListener listener : mTestListeners) {
                    listener.testStarted(testId);
                }
                break;
            case StatusCodes.FAILURE:
                for (ITestRunListener listener : mTestListeners) {
                    listener.testFailed(ITestRunListener.TestFailure.FAILURE, testId,
                        getTrace(testInfo));
                    listener.testEnded(testId);
                }
                mNumTestsRun++;
                break;
            case StatusCodes.ERROR:
                for (ITestRunListener listener : mTestListeners) {
                    listener.testFailed(ITestRunListener.TestFailure.ERROR, testId,
                        getTrace(testInfo));
                    listener.testEnded(testId);
                }
                mNumTestsRun++;
                break;
            case StatusCodes.OK:
                for (ITestRunListener listener : mTestListeners) {
                    listener.testEnded(testId);
                }
                mNumTestsRun++;
                break;
            default:
                Log.e(LOG_TAG, "Unknown status code received: " + testInfo.mCode);
                for (ITestRunListener listener : mTestListeners) {
                    listener.testEnded(testId);
                }
                mNumTestsRun++;
            break;
        }
    }
    private void reportTestRunStarted(TestResult testInfo) {
        if (!mTestStartReported && testInfo.mNumTests != null) {
            for (ITestRunListener listener : mTestListeners) {
                listener.testRunStarted(testInfo.mNumTests);
            }
            mNumTestsExpected = testInfo.mNumTests;
            mTestStartReported = true;
        }
    }
    private String getTrace(TestResult testInfo) {
        if (testInfo.mStackTrace != null) {
            return testInfo.mStackTrace;
        } else {
            Log.e(LOG_TAG, "Could not find stack trace for failed test ");
            return new Throwable("Unknown failure").toString();
        }
    }
    private void parseTime(String line, int startPos) {
        String timeString = line.substring(startPos);
        try {
            float timeSeconds = Float.parseFloat(timeString);
            mTestTime = (long) (timeSeconds * 1000);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Unexpected time format " + timeString);
        }
    }
    private void handleTestRunFailed(String errorMsg) {
        errorMsg = (errorMsg == null ? "Unknown error" : errorMsg);
        Log.i(LOG_TAG, String.format("test run failed %s", errorMsg));
        if (mLastTestResult != null &&
            mLastTestResult.isComplete() &&
            StatusCodes.START == mLastTestResult.mCode) {
            TestIdentifier testId = new TestIdentifier(mLastTestResult.mTestClass,
                    mLastTestResult.mTestName);
            for (ITestRunListener listener : mTestListeners) {
                listener.testFailed(ITestRunListener.TestFailure.ERROR, testId,
                    String.format("Incomplete: %s", errorMsg));
                listener.testEnded(testId);
            }
        }
        for (ITestRunListener listener : mTestListeners) {
            listener.testRunFailed(errorMsg);
        }
        mTestRunFailReported = true;
    }
    @Override
    public void done() {
        super.done();
        if (!mTestRunFailReported && !mTestStartReported) {
            handleTestRunFailed(NO_TEST_RESULTS_MSG);
        } else if (!mTestRunFailReported && mNumTestsExpected > mNumTestsRun) {
            final String message =
                String.format("Test run incomplete. Expected %d tests, received %d",
                        mNumTestsExpected, mNumTestsRun);
            handleTestRunFailed(message);
        } else {
            for (ITestRunListener listener : mTestListeners) {
                listener.testRunEnded(mTestTime);
            }
        }
    }
}
