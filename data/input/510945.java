public class ConnectTimeoutException extends InterruptedIOException {
    private static final long serialVersionUID = -4816682903149535989L;
    public ConnectTimeoutException() {
        super();
    }
    public ConnectTimeoutException(final String message) {
        super(message);
    }
}
