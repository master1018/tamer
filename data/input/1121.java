public final class NO_RESPONSE extends SystemException {
    public NO_RESPONSE() {
        this("");
    }
    public NO_RESPONSE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public NO_RESPONSE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public NO_RESPONSE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
