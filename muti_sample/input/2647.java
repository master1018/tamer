public final class ACTIVITY_COMPLETED extends SystemException {
    public ACTIVITY_COMPLETED() {
        this("");
    }
    public ACTIVITY_COMPLETED(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public ACTIVITY_COMPLETED(int minorCode,
                              CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public ACTIVITY_COMPLETED(String detailMessage,
                              int minorCode,
                              CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
