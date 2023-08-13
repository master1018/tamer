public final class BAD_QOS extends SystemException {
    public BAD_QOS() {
        this("");
    }
    public BAD_QOS(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public BAD_QOS(int minorCode,
                   CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public BAD_QOS(String detailMessage,
                   int minorCode,
                   CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
