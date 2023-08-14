public class NoRouteToHostException extends SocketException {
    private static final long serialVersionUID = -1897550894873493790L;
    public NoRouteToHostException() {
        super();
    }
    public NoRouteToHostException(String detailMessage) {
        super(detailMessage);
    }
}
