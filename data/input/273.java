public class DecryptionFailedException extends DbIoException {
    private static final long serialVersionUID = -7514620034696932443L;
    public DecryptionFailedException() {
    }
    public DecryptionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public DecryptionFailedException(String message) {
        super(message);
    }
    public DecryptionFailedException(Throwable cause) {
        super(cause);
    }
}
