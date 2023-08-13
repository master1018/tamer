public final class BAD_TYPECODE extends SystemException {
    public BAD_TYPECODE() {
        this("");
    }
    public BAD_TYPECODE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public BAD_TYPECODE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public BAD_TYPECODE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
