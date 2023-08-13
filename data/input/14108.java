public final class ACTIVITY_REQUIRED extends SystemException {
    public ACTIVITY_REQUIRED() {
        this("");
    }
    public ACTIVITY_REQUIRED(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public ACTIVITY_REQUIRED(int minorCode,
                             CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public ACTIVITY_REQUIRED(String detailMessage,
                             int minorCode,
                             CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
