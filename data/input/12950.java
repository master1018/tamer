public final class INV_IDENT extends SystemException {
    public INV_IDENT() {
        this("");
    }
    public INV_IDENT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public INV_IDENT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public INV_IDENT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
