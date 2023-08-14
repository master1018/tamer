public class MarshalException extends Exception {
    private static final long serialVersionUID = -863185580332643547L;
    private Throwable cause;
    public MarshalException() {
        super();
    }
    public MarshalException(String message) {
        super(message);
    }
    public MarshalException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    public MarshalException(Throwable cause) {
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
