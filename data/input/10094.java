public final class NO_IMPLEMENT extends SystemException {
    public NO_IMPLEMENT() {
        this("");
    }
    public NO_IMPLEMENT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public NO_IMPLEMENT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public NO_IMPLEMENT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
