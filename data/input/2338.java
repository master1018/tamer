public final class INTERNAL extends SystemException {
    public INTERNAL() {
        this("");
    }
    public INTERNAL(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public INTERNAL(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public INTERNAL(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
