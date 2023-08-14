public final class TRANSACTION_MODE extends SystemException {
    public TRANSACTION_MODE() {
        this("");
    }
    public TRANSACTION_MODE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public TRANSACTION_MODE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public TRANSACTION_MODE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
