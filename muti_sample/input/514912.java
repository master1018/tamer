public class PrivilegedActionException extends Exception {
    private static final long serialVersionUID = 4724086851538908602l;
    private Exception exception;
    public PrivilegedActionException(Exception ex) {
        super(ex);
        this.exception = ex;
    }
    public Exception getException() {
        return exception; 
    }
    @Override
    public Throwable getCause() {
        return exception;
    }
    @Override
    public String toString() {
        String s = getClass().getName();
        return exception == null ? s : s + ": " + exception; 
    }
}
