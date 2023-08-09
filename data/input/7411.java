public class FtpProtocolException extends Exception {
    private static final long serialVersionUID = 5978077070276545054L;
    private final FtpReplyCode code;
    public FtpProtocolException(String detail) {
            super(detail);
            code = FtpReplyCode.UNKNOWN_ERROR;
    }
      public FtpProtocolException(String detail, FtpReplyCode code) {
        super(detail);
        this.code = code;
    }
    public FtpReplyCode getReplyCode() {
        return code;
    }
}
