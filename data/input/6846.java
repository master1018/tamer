public class TimeLimitExceededException extends LimitExceededException {
    public TimeLimitExceededException() {
        super();
    }
    public TimeLimitExceededException(String explanation) {
        super(explanation);
    }
    private static final long serialVersionUID = -3597009011385034696L;
}
