public class ClassNotFoundException extends Exception {
    private static final long serialVersionUID = 9176873029745254542L;
    private Throwable ex;
    public ClassNotFoundException() {
        super((Throwable) null);
    }
    public ClassNotFoundException(String detailMessage) {
        super(detailMessage, null);
    }
    public ClassNotFoundException(String detailMessage, Throwable exception) {
        super(detailMessage);
        ex = exception;
    }
    public Throwable getException() {
        return ex;
    }
    @Override
    public Throwable getCause() {
        return ex;
    }
}
