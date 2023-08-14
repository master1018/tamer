public final class InvalidValueTypeException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidValueTypeException() {
        super("Invalid Type");
    }
    public InvalidValueTypeException(String message) {
        super(message);
    }
    public InvalidValueTypeException(Throwable cause) {
        super(cause);
    }
    public InvalidValueTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
