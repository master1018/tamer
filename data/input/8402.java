public final class BAD_CONTEXT extends SystemException {
    public BAD_CONTEXT() {
        this("");
    }
    public BAD_CONTEXT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public BAD_CONTEXT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public BAD_CONTEXT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
