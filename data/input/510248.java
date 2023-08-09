public class HttpException extends Exception {
    private static final long serialVersionUID = -5437299376222011036L;
    public HttpException() {
        super();
    }
    public HttpException(final String message) {
        super(message);
    }
    public HttpException(final String message, final Throwable cause) {
        super(message);
        ExceptionUtils.initCause(this, cause);
    }
}
