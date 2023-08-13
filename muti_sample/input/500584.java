public class ConnectionClosedException extends IOException {
    private static final long serialVersionUID = 617550366255636674L;
    public ConnectionClosedException(final String message) {
        super(message);
    }
}
