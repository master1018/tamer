public class KeyException extends GeneralSecurityException {
    private static final long serialVersionUID = -7483676942812432108L;
    public KeyException(String msg) {
        super(msg);
    }
    public KeyException() {
    }
    public KeyException(String message, Throwable cause) {
        super(message, cause);
    }
    public KeyException(Throwable cause) {
        super(cause);
    }
}
