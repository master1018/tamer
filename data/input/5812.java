public final class TIMEOUT extends SystemException {
    public TIMEOUT() {
        this("");
    }
    public TIMEOUT(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public TIMEOUT(int minorCode,
                   CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public TIMEOUT(String detailMessage,
                   int minorCode,
                   CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
