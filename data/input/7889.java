public class ContextNotEmptyException extends NamingException {
    public ContextNotEmptyException(String explanation) {
        super(explanation);
    }
    public ContextNotEmptyException() {
        super();
    }
    private static final long serialVersionUID = 1090963683348219877L;
}
