public final class OBJ_ADAPTER extends SystemException {
    public OBJ_ADAPTER() {
        this("");
    }
    public OBJ_ADAPTER(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public OBJ_ADAPTER(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public OBJ_ADAPTER(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
