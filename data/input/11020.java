public class InvalidNameException extends NamingException {
    public InvalidNameException(String explanation) {
        super(explanation);
    }
    public InvalidNameException() {
        super();
    }
    private static final long serialVersionUID = -8370672380823801105L;
}
