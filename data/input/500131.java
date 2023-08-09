public class InstrumentationResultParserTest extends TestCase {
    private InstrumentationResultParser mParser;
    private VerifyingTestResult mTestResult;
    private static final String CLASS_NAME = "com.test.FooTest";
    private static final String TEST_NAME = "testFoo";
    private static final String STACK_TRACE = "java.lang.AssertionFailedException";
    public InstrumentationResultParserTest(String name) {
        super(name);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTestResult = new VerifyingTestResult();
        mParser = new InstrumentationResultParser(mTestResult);
    }
    public void testTestStarted() {
        StringBuilder output = buildCommonResult();
        addStartCode(output);
        injectTestString(output.toString());
        assertCommonAttributes();
    }
    public void testTestSuccess() {
        StringBuilder output = createSuccessTest();
        injectTestString(output.toString());
        assertCommonAttributes();
        assertEquals(1, mTestResult.mNumTestsRun);
        assertEquals(null, mTestResult.mTestStatus);
    }
    private StringBuilder createSuccessTest() {
        StringBuilder output = buildCommonResult();
        addStartCode(output);
        addCommonStatus(output);
        addSuccessCode(output);
        return output;
    }
    public void testTestFailed() {
        StringBuilder output = buildCommonResult();
        addStartCode(output);
        addCommonStatus(output);
        addStackTrace(output);
        addFailureCode(output);
        injectTestString(output.toString());
        assertCommonAttributes();
        assertEquals(1, mTestResult.mNumTestsRun);
        assertEquals(ITestRunListener.TestFailure.FAILURE, mTestResult.mTestStatus);
        assertEquals(STACK_TRACE, mTestResult.mTrace);
    }
    public void testTimeParsing() {
        StringBuilder output = createSuccessTest();
        output.append("Time: 4.9");
        injectTestString(output.toString());
        assertEquals(4900, mTestResult.mTestTime);
    }
    public void testRunFailed() {
        StringBuilder output = new StringBuilder();
        final String errorMessage = "Unable to find instrumentation info";
        addStatusKey(output, "Error", errorMessage);
        addStatusCode(output, "-1");
        output.append("INSTRUMENTATION_FAILED: com.dummy/android.test.InstrumentationTestRunner");
        addLineBreak(output);
        injectTestString(output.toString());
        assertEquals(errorMessage, mTestResult.mRunFailedMessage);
    }
    public void testRunFailedResult() {
        StringBuilder output = new StringBuilder();
        final String errorMessage = "Unable to instantiate instrumentation";
        output.append("INSTRUMENTATION_RESULT: shortMsg=");
        output.append(errorMessage);
        addLineBreak(output);
        output.append("INSTRUMENTATION_CODE: 0");
        addLineBreak(output);
        injectTestString(output.toString());
        assertEquals(errorMessage, mTestResult.mRunFailedMessage);
    }
    public void testRunIncomplete() {
        StringBuilder output = new StringBuilder();
        addCommonStatus(output);
        addStartCode(output);
        injectTestString(output.toString());
        assertTrue(mTestResult.mRunFailedMessage.startsWith("Test run incomplete."));
        assertEquals(TestFailure.ERROR, mTestResult.mTestStatus);
    }
    public void testRunAmFailed() {
        StringBuilder output = new StringBuilder();
        output.append("usage: am [subcommand] [options]");
        addLineBreak(output);
        output.append("start an Activity: am start [-D] [-W] <INTENT>");
        addLineBreak(output);
        output.append("-D: enable debugging");
        addLineBreak(output);
        output.append("-W: wait for launch to complete");
        addLineBreak(output);
        output.append("start a Service: am startservice <INTENT>");
        addLineBreak(output);
        output.append("Error: Bad component name: wfsdafddfasasdf");
        injectTestString(output.toString());
        assertEquals(InstrumentationResultParser.NO_TEST_RESULTS_MSG,
                mTestResult.mRunFailedMessage);
    }
    private StringBuilder buildCommonResult() {
        StringBuilder output = new StringBuilder();
        addCommonStatus(output);
        addStatusCode(output, "1");
        addCommonStatus(output);
        return output;
    }
    private void addCommonStatus(StringBuilder output) {
        addStatusKey(output, "stream", "\r\n" + CLASS_NAME);
        addStatusKey(output, "test", TEST_NAME);
        addStatusKey(output, "class", CLASS_NAME);
        addStatusKey(output, "current", "1");
        addStatusKey(output, "numtests", "1");
        addStatusKey(output, "id", "InstrumentationTestRunner");
    }
    private void addStackTrace(StringBuilder output) {
        addStatusKey(output, "stack", STACK_TRACE);
    }
    private void addStatusKey(StringBuilder outputBuilder, String key,
            String value) {
        outputBuilder.append("INSTRUMENTATION_STATUS: ");
        outputBuilder.append(key);
        outputBuilder.append('=');
        outputBuilder.append(value);
        addLineBreak(outputBuilder);
    }
    private void addLineBreak(StringBuilder outputBuilder) {
        outputBuilder.append("\r\n");
    }
    private void addStartCode(StringBuilder outputBuilder) {
        addStatusCode(outputBuilder, "1");
    }
    private void addSuccessCode(StringBuilder outputBuilder) {
        addStatusCode(outputBuilder, "0");
    }
    private void addFailureCode(StringBuilder outputBuilder) {
        addStatusCode(outputBuilder, "-2");
    }
    private void addStatusCode(StringBuilder outputBuilder, String value) {
        outputBuilder.append("INSTRUMENTATION_STATUS_CODE: ");
        outputBuilder.append(value);
        addLineBreak(outputBuilder);
    }
    private void injectTestString(String result) {
        byte[] data = result.getBytes();
        mParser.addOutput(data, 0, data.length);
        mParser.flush();
    }
    private void assertCommonAttributes() {
        assertEquals(CLASS_NAME, mTestResult.mSuiteName);
        assertEquals(1, mTestResult.mTestCount);
        assertEquals(TEST_NAME, mTestResult.mTestName);
    }
    private class VerifyingTestResult implements ITestRunListener {
        String mSuiteName;
        int mTestCount;
        int mNumTestsRun;
        String mTestName;
        long mTestTime;
        TestFailure mTestStatus;
        String mTrace;
        boolean mStopped;
        String mRunFailedMessage;
        VerifyingTestResult() {
            mNumTestsRun = 0;
            mTestStatus = null;
            mStopped = false;
            mRunFailedMessage = null;
        }
        public void testEnded(TestIdentifier test) {
            mNumTestsRun++;
            assertEquals("Unexpected class name", mSuiteName, test.getClassName());
            assertEquals("Unexpected test ended", mTestName, test.getTestName());
        }
        public void testFailed(TestFailure status, TestIdentifier test, String trace) {
            mTestStatus = status;
            mTrace = trace;
            assertEquals("Unexpected class name", mSuiteName, test.getClassName());
            assertEquals("Unexpected test ended", mTestName, test.getTestName());
        }
        public void testRunEnded(long elapsedTime) {
            mTestTime = elapsedTime;
        }
        public void testRunStarted(int testCount) {
            mTestCount = testCount;
        }
        public void testRunStopped(long elapsedTime) {
            mTestTime = elapsedTime;
            mStopped = true;
        }
        public void testStarted(TestIdentifier test) {
            mSuiteName = test.getClassName();
            mTestName = test.getTestName();
        }
        public void testRunFailed(String errorMessage) {
            mRunFailedMessage = errorMessage;
        }
    }
}
