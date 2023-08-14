public class InvalidDataException extends IOException {
    private static final long serialVersionUID = 1L;
    public InvalidDataException() {
        super("Invalid Data!");
    }
    public InvalidDataException(String s) {
        super(s);
    }
}
