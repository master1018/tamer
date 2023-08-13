public final class INV_POLICY extends SystemException {
    public INV_POLICY() {
        this("");
    }
    public INV_POLICY(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public INV_POLICY(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public INV_POLICY(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
