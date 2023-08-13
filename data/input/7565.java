public final class BAD_OPERATION extends SystemException {
    public BAD_OPERATION() {
        this("");
    }
    public BAD_OPERATION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public BAD_OPERATION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public BAD_OPERATION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
