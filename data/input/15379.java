public class SaslException extends IOException {
    private Throwable _exception;
    public SaslException () {
        super();
    }
    public SaslException (String detail) {
        super(detail);
    }
    public SaslException (String detail, Throwable ex) {
        super(detail);
        if (ex != null) {
            initCause(ex);
        }
    }
    public Throwable getCause() {
        return _exception;
    }
    public Throwable initCause(Throwable cause) {
        super.initCause(cause);
        _exception = cause;
        return this;
    }
    public String toString() {
        String answer = super.toString();
        if (_exception != null && _exception != this) {
            answer += " [Caused by " + _exception.toString() + "]";
        }
        return answer;
    }
    private static final long serialVersionUID = 4579784287983423626L;
}
