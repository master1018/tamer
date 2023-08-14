public class AccessControlException extends SecurityException {
    private static final long serialVersionUID = 5138225684096988535L;
    private Permission perm; 
    public AccessControlException(String message) {
        super(message);
    }
    public AccessControlException(String message, Permission perm) {
        super(message);
        this.perm = perm;
    }
    public Permission getPermission() {
        return perm;
    }
}
