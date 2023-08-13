public class InvocationTargetException extends Exception {
    private static final long serialVersionUID = 4085088731926701167L;
    private Throwable target;
    protected InvocationTargetException() {
        super((Throwable) null);
    }
    public InvocationTargetException(Throwable exception) {
        super(null, exception);
        target = exception;
    }
    public InvocationTargetException(Throwable exception, String detailMessage) {
        super(detailMessage, exception);
        target = exception;
    }
    public Throwable getTargetException() {
        return target;
    }
    @Override
    public Throwable getCause() {
        return target;
    }
}
