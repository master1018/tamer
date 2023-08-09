public final class MARSHAL extends SystemException {
    public MARSHAL() {
        this("");
    }
    public MARSHAL(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }
    public MARSHAL(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }
    public MARSHAL(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
