public class LimitExceededException extends NamingException {
    public LimitExceededException() {
        super();
    }
    public LimitExceededException(String explanation) {
        super(explanation);
    }
    private static final long serialVersionUID = -776898738660207856L;
}
