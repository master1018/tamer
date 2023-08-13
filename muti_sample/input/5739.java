public final class INITIALIZE extends SystemException {
    public INITIALIZE() {
        this("");
    }
    public INITIALIZE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public INITIALIZE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public INITIALIZE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
