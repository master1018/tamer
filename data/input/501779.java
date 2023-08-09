public class ConnectException extends SocketException {
    private static final long serialVersionUID = 3831404271622369215L;
    public ConnectException() {
        super();
    }
    public ConnectException(String detailMessage) {
        super(detailMessage);
    }
}
