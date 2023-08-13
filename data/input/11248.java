public final class NO_PERMISSION extends SystemException {
    public NO_PERMISSION() {
        this("");
    }
    public NO_PERMISSION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public NO_PERMISSION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public NO_PERMISSION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
