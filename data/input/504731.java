public class UndeclaredThrowableException extends RuntimeException {
    private static final long serialVersionUID = 330127114055056639L;
    private Throwable undeclaredThrowable;
    public UndeclaredThrowableException(Throwable exception) {
        super();
        this.undeclaredThrowable = exception;
        initCause(exception);
    }
    public UndeclaredThrowableException(Throwable exception,
            String detailMessage) {
        super(detailMessage);
        this.undeclaredThrowable = exception;
        initCause(exception);
    }
    public Throwable getUndeclaredThrowable() {
        return undeclaredThrowable;
    }
    @Override
    public Throwable getCause() {
        return undeclaredThrowable;
    }
}
