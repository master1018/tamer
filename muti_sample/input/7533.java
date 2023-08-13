public class InsufficientResourcesException extends NamingException {
    public InsufficientResourcesException(String explanation) {
        super(explanation);
    }
    public InsufficientResourcesException() {
        super();
    }
    private static final long serialVersionUID = 6227672693037844532L;
}
