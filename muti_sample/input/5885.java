public final class TRANSACTION_UNAVAILABLE extends SystemException {
    public TRANSACTION_UNAVAILABLE() {
        this("");
    }
    public TRANSACTION_UNAVAILABLE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public TRANSACTION_UNAVAILABLE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public TRANSACTION_UNAVAILABLE(String s, int minor,
                                   CompletionStatus completed) {
        super(s, minor, completed);
    }
}
