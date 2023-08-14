public class NameNotFoundException extends NamingException {
    public NameNotFoundException(String explanation) {
        super(explanation);
    }
    public NameNotFoundException() {
        super();
    }
    private static final long serialVersionUID = -8007156725367842053L;
}
