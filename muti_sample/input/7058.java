public class InvalidStreamException extends IllegalArgumentException {
    private static final long serialVersionUID = -9172703378046665558L;
    public InvalidStreamException() { }
    public InvalidStreamException(String msg) {
        super(msg);
    }
}
