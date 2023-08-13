public class SecurityException extends java.lang.RuntimeException {
    private static final long serialVersionUID = 6878364983674394167L;
    public SecurityException() {
        super();
    }
    public SecurityException(String detailMessage) {
        super(detailMessage);
    }
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
    public SecurityException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
