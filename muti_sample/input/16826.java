public final class TRANSIENT extends SystemException {
    public TRANSIENT() {
        this("");
    }
    public TRANSIENT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public TRANSIENT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public TRANSIENT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
