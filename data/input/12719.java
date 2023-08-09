public class NoPermissionException extends NamingSecurityException {
    public NoPermissionException(String explanation) {
        super(explanation);
    }
    public NoPermissionException() {
        super();
    }
    private static final long serialVersionUID = 8395332708699751775L;
}
