public class NotContextException extends NamingException {
    public NotContextException(String explanation) {
        super(explanation);
    }
    public NotContextException() {
        super();
    }
    private static final long serialVersionUID = 849752551644540417L;
}
