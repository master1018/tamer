public class SyncFailedException extends IOException {
    private static final long serialVersionUID = -2353342684412443330L;
    public SyncFailedException(String detailMessage) {
        super(detailMessage);
    }
}
