public final class IMP_LIMIT extends SystemException {
    public IMP_LIMIT() {
        this("");
    }
    public IMP_LIMIT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public IMP_LIMIT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public IMP_LIMIT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
