public class KeySelectorException extends Exception {
    private static final long serialVersionUID = -7480033639322531109L;
    private Throwable cause;
    public KeySelectorException() {
        super();
    }
    public KeySelectorException(String message) {
        super(message);
    }
    public KeySelectorException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    public KeySelectorException(Throwable cause) {
        super(cause==null ? null : cause.toString());
        this.cause = cause;
    }
    public Throwable getCause() {
        return cause;
    }
    public void printStackTrace() {
        super.printStackTrace();
    }
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
    }
}
