public final class DATA_CONVERSION extends SystemException {
    public DATA_CONVERSION() {
        this("");
    }
    public DATA_CONVERSION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public DATA_CONVERSION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public DATA_CONVERSION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
