public final class NTLMException extends GeneralSecurityException {
    public final static int PACKET_READ_ERROR = 1;
    public final static int NO_DOMAIN_INFO = 2;
    public final static int USER_UNKNOWN = 3;
    public final static int AUTH_FAILED = 4;
    public final static int BAD_VERSION = 5;
    private int errorCode;
    public NTLMException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }
    public int errorCode() {
        return errorCode;
    }
}
