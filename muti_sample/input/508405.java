public class MalformedCookieException extends ProtocolException {
    private static final long serialVersionUID = -6695462944287282185L;
    public MalformedCookieException() {
        super();
    }
    public MalformedCookieException(String message) {
        super(message);
    }
    public MalformedCookieException(String message, Throwable cause) {
        super(message, cause);
    }
}
