public class InvalidConstantPoolFormatException extends Exception {
    private static final long serialVersionUID=-6103888330523770949L;
    public InvalidConstantPoolFormatException(String message,Throwable cause) {
        super(message,cause);
    }
    public InvalidConstantPoolFormatException(String message) {
        super(message);
    }
    public InvalidConstantPoolFormatException(Throwable cause) {
        super(cause);
    }
}
