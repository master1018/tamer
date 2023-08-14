public class JMXProviderException extends IOException {
    private static final long serialVersionUID = -3166703627550447198L;
    public JMXProviderException() {
    }
    public JMXProviderException(String message) {
        super(message);
    }
    public JMXProviderException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    public Throwable getCause() {
        return cause;
    }
    private Throwable cause = null;
}
