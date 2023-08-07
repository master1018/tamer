public class AuthorizationDeniedException extends Exception {
    private static final long serialVersionUID = 1L;
    public AuthorizationDeniedException(String message) {
        super(message);
    }
}
