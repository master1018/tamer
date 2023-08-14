public final class REBIND extends SystemException {
    public REBIND() {
        this("");
    }
    public REBIND(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public REBIND(int minorCode,
                  CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public REBIND(String detailMessage,
                  int minorCode,
                  CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
