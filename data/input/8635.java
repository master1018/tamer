public final class UNKNOWN extends SystemException {
    public UNKNOWN() {
        this("");
    }
    public UNKNOWN(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public UNKNOWN(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public UNKNOWN(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
