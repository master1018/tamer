public class MessagingException extends Exception {
    public static final long serialVersionUID = -1;
    public static final int NO_ERROR = -1;
    public static final int UNSPECIFIED_EXCEPTION = 0;
    public static final int IOERROR = 1;
    public static final int TLS_REQUIRED = 2;
    public static final int AUTH_REQUIRED = 3;
    public static final int GENERAL_SECURITY = 4;
    public static final int AUTHENTICATION_FAILED = 5;
    public static final int DUPLICATE_ACCOUNT = 6;
    public static final int SECURITY_POLICIES_REQUIRED = 7;
    public static final int SECURITY_POLICIES_UNSUPPORTED = 8;
    public static final int PROTOCOL_VERSION_UNSUPPORTED = 9;
    protected int mExceptionType;
    public MessagingException(String message) {
        super(message);
        mExceptionType = UNSPECIFIED_EXCEPTION;
    }
    public MessagingException(String message, Throwable throwable) {
        super(message, throwable);
        mExceptionType = UNSPECIFIED_EXCEPTION;
    }
    public MessagingException(int exceptionType) {
        super();
        mExceptionType = exceptionType;
    }
    public MessagingException(int exceptionType, String message) {
        super(message);
        mExceptionType = exceptionType;
    }
    public int getExceptionType() {
        return mExceptionType;
    }
}
