class MutableSecurityManager extends SecurityManager {
    static final RuntimePermission SET_SECURITY_MANAGER = new RuntimePermission("setSecurityManager");
    private PermissionCollection enabled;
    private PermissionCollection denied;
    public boolean isCheckAcceptCalled = false; 
    public boolean isCheckAccessThreadCalled = false;
    public boolean isCheckAccessThreadGroupCalled = false;
    public MutableSecurityManager() {
        super();
        this.enabled = new Permissions();
    }
    public MutableSecurityManager(Permission... permissions) {
        this();
        for (int i = 0; i < permissions.length; i++) {
            this.enabled.add(permissions[i]);
        }
    }
    void addPermission(Permission permission) {
        enabled.add(permission);
    }
    void clearPermissions() {
        enabled = new Permissions();
    }
    void denyPermission(Permission p) {
        if (denied == null) {
            denied = p.newPermissionCollection();
        }
        denied.add(p);
    }
    @Override
    public void checkPermission(Permission permission) 
    {
        if (permission != null) {
            if (denied != null && denied.implies(permission)){
                throw new SecurityException("Denied " + permission);
            }
            if (enabled.implies(permission)) {
                return;
            }
        }
        super.checkPermission(permission);
    }
    @Override
    public void checkPermission(Permission permission, Object context) 
    {
        if (permission != null) {
            if (denied != null && denied.implies(permission)){
                throw new SecurityException("Denied " + permission);
            }
            if (enabled.implies(permission)) {
                return;
            }
        }
        super.checkPermission(permission, context);
    }
    public void checkAccept(String host, int port) {
        isCheckAcceptCalled = true;        
        super.checkAccept(host, port);
    }
}
