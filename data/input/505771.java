public class AtCommandResult {
    public static final int OK = 0;
    public static final int ERROR = 1;
    public static final int UNSOLICITED = 2;
    private static final String OK_STRING = "OK";
    private static final String ERROR_STRING = "ERROR";
    private int mResultCode;  
    private StringBuilder mResponse; 
    public AtCommandResult(int resultCode) {
        mResultCode = resultCode;
        mResponse = new StringBuilder();
    }
    public AtCommandResult(String response) {
        this(OK);
        addResponse(response);
    }
    public int getResultCode() {
        return mResultCode;
    }
    public void addResponse(String response) {
        appendWithCrlf(mResponse, response);
    }
    public void addResult(AtCommandResult result) {
        if (result != null) {
            appendWithCrlf(mResponse, result.mResponse.toString());
            mResultCode = result.mResultCode;
        }
    }
    public String toString() {
        StringBuilder result = new StringBuilder(mResponse.toString());
        switch (mResultCode) {
        case OK:
            appendWithCrlf(result, OK_STRING);
            break;
        case ERROR:
            appendWithCrlf(result, ERROR_STRING);
            break;
        }
        return result.toString();
    }
    public static void appendWithCrlf(StringBuilder str1, String str2) {
        if (str1.length() > 0 && str2.length() > 0) {
            str1.append("\r\n\r\n");
        }
        str1.append(str2);
    }
};
