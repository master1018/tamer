public class InternalError extends Error {
    private static final long serialVersionUID = 8114054446416187030L;
    InternalError(Throwable t, Object... args) {
        super("Internal error", t);
        this.args = args;
    }
    InternalError(Object... args) {
        super("Internal error");
        this.args = args;
    }
    public final Object[] args;
}
