public class RuntimeExceptionWrapper extends RuntimeException {
    private static final long serialVersionUID = -3483500330975410177L;
    private final RuntimeException runtimeException;
    public RuntimeExceptionWrapper(final RuntimeException runtimeException) {
        this.runtimeException = runtimeException;
    }
    public RuntimeException getRuntimeException() {
        return runtimeException;
    }
}
