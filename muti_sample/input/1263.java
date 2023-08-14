public final class INVALID_ACTIVITY extends SystemException {
    public INVALID_ACTIVITY() {
        this("");
    }
    public INVALID_ACTIVITY(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public INVALID_ACTIVITY(int minorCode,
                            CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public INVALID_ACTIVITY(String detailMessage,
                            int minorCode,
                            CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
