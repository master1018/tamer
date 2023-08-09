public class PortUnreachableException extends SocketException {
    private static final long serialVersionUID = 8462541992376507323L;
    public PortUnreachableException() {
    }
    public PortUnreachableException(String detailMessage) {
        super(detailMessage);
    }
}
