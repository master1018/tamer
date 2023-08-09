public final class OBJECT_NOT_EXIST extends SystemException {
    public OBJECT_NOT_EXIST() {
        this("");
    }
    public OBJECT_NOT_EXIST(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public OBJECT_NOT_EXIST(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public OBJECT_NOT_EXIST(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
