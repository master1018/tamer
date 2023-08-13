public class CtsTestResult {
    private int mResultCode;
    private String mFailedMessage;
    private String mStackTrace;
    public static final int CODE_INIT = -1;
    public static final int CODE_NOT_EXECUTED = 0;
    public static final int CODE_PASS = 1;
    public static final int CODE_FAIL = 2;
    public static final int CODE_ERROR = 3;
    public static final int CODE_TIMEOUT = 4;
    public static final int CODE_FIRST = CODE_INIT;
    public static final int CODE_LAST = CODE_TIMEOUT;
    public static final String STR_ERROR = "error";
    public static final String STR_TIMEOUT = "timeout";
    public static final String STR_NOT_EXECUTED = "notExecuted";
    public static final String STR_FAIL = "fail";
    public static final String STR_PASS = "pass";
    private static HashMap<Integer, String> sCodeToResultMap;
    private static HashMap<String, Integer> sResultToCodeMap;
    static {
        sCodeToResultMap = new HashMap<Integer, String>();
        sCodeToResultMap.put(CODE_NOT_EXECUTED, STR_NOT_EXECUTED);
        sCodeToResultMap.put(CODE_PASS, STR_PASS);
        sCodeToResultMap.put(CODE_FAIL, STR_FAIL);
        sCodeToResultMap.put(CODE_ERROR, STR_ERROR);
        sCodeToResultMap.put(CODE_TIMEOUT, STR_TIMEOUT);
        sResultToCodeMap = new HashMap<String, Integer>();
        for (int code : sCodeToResultMap.keySet()) {
            sResultToCodeMap.put(sCodeToResultMap.get(code), code);
        }
    }
    public CtsTestResult(int resCode) {
        mResultCode = resCode;
    }
    public CtsTestResult(int resCode, final String failedMessage, final String stackTrace) {
        mResultCode = resCode;
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }
    public CtsTestResult(final String result, final String failedMessage,
            final String stackTrace) throws InvalidTestResultStringException {
        if (!sResultToCodeMap.containsKey(result)) {
            throw new InvalidTestResultStringException(result);
        }
        mResultCode = sResultToCodeMap.get(result);
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }
    public boolean isFail() {
        return mResultCode == CODE_FAIL;
    }
    public boolean isPass() {
        return mResultCode == CODE_PASS;
    }
    public boolean isNotExecuted() {
        return mResultCode == CODE_NOT_EXECUTED;
    }
    public int getResultCode() {
        return mResultCode;
    }
    public String getFailedMessage() {
        return mFailedMessage;
    }
    public String getStackTrace() {
        return mStackTrace;
    }
    @SuppressWarnings("unchecked")
    public void setResult(TestResult testResult) {
        int resCode = CODE_PASS;
        String failedMessage = null;
        String stackTrace = null;
        if ((testResult != null) && (testResult.failureCount() > 0)) {
            resCode = CODE_FAIL;
            Enumeration<TestFailure> failures = testResult.failures();
            while (failures.hasMoreElements()) {
                TestFailure failure = failures.nextElement();
                failedMessage += failure.exceptionMessage();
                stackTrace += failure.trace();
            }
        }
        mResultCode = resCode;
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }
    public void reverse() {
        if (isPass()) {
            mResultCode = CtsTestResult.CODE_FAIL;
        } else if (isFail()){
            mResultCode = CtsTestResult.CODE_PASS;
        }
    }
    public String getResultString() {
        return sCodeToResultMap.get(mResultCode);
    }
    static public boolean isValidResultType(final String resultType) {
        return sResultToCodeMap.containsKey(resultType);
    }
}
