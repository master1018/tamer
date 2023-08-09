public class JMRuntimeException extends RuntimeException   {
    private static final long serialVersionUID = 6573344628407841861L;
    public JMRuntimeException() {
        super();
    }
    public JMRuntimeException(String message) {
        super(message);
    }
    JMRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
