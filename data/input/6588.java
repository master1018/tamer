public class SizeLimitExceededException extends LimitExceededException {
    public SizeLimitExceededException() {
        super();
    }
    public SizeLimitExceededException(String explanation) {
        super(explanation);
    }
    private static final long serialVersionUID = 7129289564879168579L;
}
