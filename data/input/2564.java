public final class CODESET_INCOMPATIBLE extends SystemException {
    public CODESET_INCOMPATIBLE() {
        this("");
    }
    public CODESET_INCOMPATIBLE(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }
    public CODESET_INCOMPATIBLE(int minorCode,
                                CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }
    public CODESET_INCOMPATIBLE(String detailMessage,
                                int minorCode,
                                CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
