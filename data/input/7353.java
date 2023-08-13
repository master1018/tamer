public class ServiceUnavailableException extends NamingException {
    public ServiceUnavailableException(String explanation) {
        super(explanation);
    }
    public ServiceUnavailableException() {
        super();
    }
    private static final long serialVersionUID = -4996964726566773444L;
}
