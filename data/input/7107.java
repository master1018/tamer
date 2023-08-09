public class IIOException extends IOException {
    public IIOException(String message) {
        super(message);
    }
    public IIOException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
