public final class NO_MEMORY extends SystemException {
    public NO_MEMORY() {
        this("");
    }
    public NO_MEMORY(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public NO_MEMORY(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public NO_MEMORY(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
