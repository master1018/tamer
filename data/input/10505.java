public class AuthenticationException extends NamingSecurityException {
    public AuthenticationException(String explanation) {
        super(explanation);
    }
    public AuthenticationException() {
        super();
    }
    private static final long serialVersionUID = 3678497619904568096L;
}
