public class AndroidRuntimeException extends RuntimeException {
    public AndroidRuntimeException() {
    }
    public AndroidRuntimeException(String name) {
        super(name);
    }
    public AndroidRuntimeException(Exception cause) {
        super(cause);
    }
};
