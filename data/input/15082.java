public class KeyAlreadyExistsException extends IllegalArgumentException {
    private static final long serialVersionUID = 1845183636745282866L;
    public KeyAlreadyExistsException() {
        super();
    }
    public KeyAlreadyExistsException(String msg) {
        super(msg);
    }
}
