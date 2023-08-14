public class SocketTimeoutException extends InterruptedIOException {
    private static final long serialVersionUID = -8846654841826352300L;
    public SocketTimeoutException() {
        super();
    }
    public SocketTimeoutException(String detailMessage) {
        super(detailMessage);
    }
}
