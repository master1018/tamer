public final class TRANSACTION_REQUIRED extends SystemException {
    public TRANSACTION_REQUIRED() {
        this("");
    }
    public TRANSACTION_REQUIRED(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public TRANSACTION_REQUIRED(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public TRANSACTION_REQUIRED(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
