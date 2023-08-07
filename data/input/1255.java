public class ContentTypeNotMappedException extends Exception {
    private static final long serialVersionUID = -4939180266989096154L;
    public ContentTypeNotMappedException() {
    }
    public ContentTypeNotMappedException(final String message) {
        super(message);
    }
    public ContentTypeNotMappedException(final Throwable cause) {
        super(cause);
    }
    public ContentTypeNotMappedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
