public class AuthenticationNotSupportedException extends NamingSecurityException {
    public AuthenticationNotSupportedException(String explanation) {
        super(explanation);
    }
    public AuthenticationNotSupportedException() {
        super();
    }
    private static final long serialVersionUID = -7149033933259492300L;
}
