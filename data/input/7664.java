public class TransformException extends Exception {
    private static final long serialVersionUID = 5082634801360427800L;
    private Throwable cause;
    public TransformException() {
        super();
    }
    public TransformException(String message) {
        super(message);
    }
    public TransformException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    public TransformException(Throwable cause) {
        super(cause==null ? null : cause.toString());
        this.cause = cause;
    }
    public Throwable getCause() {
        return cause;
    }
    public void printStackTrace() {
        super.printStackTrace();
        if (cause != null) {
            cause.printStackTrace();
        }
    }
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (cause != null) {
            cause.printStackTrace(s);
        }
    }
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (cause != null) {
            cause.printStackTrace(s);
        }
    }
}
