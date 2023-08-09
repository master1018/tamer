public class AuthenticationException extends ProtocolException {
    private static final long serialVersionUID = -6794031905674764776L;
    public AuthenticationException() {
        super();
    }
    public AuthenticationException(String message) {
        super(message);
    }
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
