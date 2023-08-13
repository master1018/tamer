public class IIOException extends IOException {
    private static final long serialVersionUID = -3216210718638985251L;
    public IIOException(String message) {
        super(message);
    }
    public IIOException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
