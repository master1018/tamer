public class InvalidKeySpecException extends GeneralSecurityException {
    private static final long serialVersionUID = 3546139293998810778L;
    public InvalidKeySpecException(String msg) {
        super(msg);
    }
    public InvalidKeySpecException() {
    }
    public InvalidKeySpecException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidKeySpecException(Throwable cause) {
        super(cause);
    }
}
