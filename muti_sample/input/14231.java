public class NoSuchMechanismException extends RuntimeException {
    private static final long serialVersionUID = 4189669069570660166L;
    private Throwable cause;
    public NoSuchMechanismException() {
        super();
    }
    public NoSuchMechanismException(String message) {
        super(message);
    }
    public NoSuchMechanismException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    public NoSuchMechanismException(Throwable cause) {
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
