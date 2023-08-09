public class AndroidException extends Exception {
    public AndroidException() {
    }
    public AndroidException(String name) {
        super(name);
    }
    public AndroidException(Exception cause) {
        super(cause);
    }
};
