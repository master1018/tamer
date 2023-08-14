public final class INV_FLAG extends SystemException {
    public INV_FLAG() {
        this("");
    }
    public INV_FLAG(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public INV_FLAG(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public INV_FLAG(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
