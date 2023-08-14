public final class FREE_MEM extends SystemException {
    public FREE_MEM() {
        this("");
    }
    public FREE_MEM(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public FREE_MEM(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public FREE_MEM(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
