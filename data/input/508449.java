public class ThrowableWrapper extends Throwable {
    private static final long serialVersionUID = -4434322855124959723L;
    private final Throwable throwable;
    public ThrowableWrapper(final Throwable throwable) {
        this.throwable = throwable;
    }
    public Throwable getThrowable() {
        return throwable;
    }
}
