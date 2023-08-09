final class CryptoAllPermission extends CryptoPermission {
    private static final long serialVersionUID = -5066513634293192112L;
    static final String ALG_NAME = "CryptoAllPermission";
    static final CryptoAllPermission INSTANCE =
        new CryptoAllPermission();
    private CryptoAllPermission() {
        super(ALG_NAME);
    }
    public boolean implies(Permission p) {
         return (p instanceof CryptoPermission);
    }
    public boolean equals(Object obj) {
        return (obj == INSTANCE);
    }
    public int hashCode() {
        return 1;
    }
    public PermissionCollection newPermissionCollection() {
        return new CryptoAllPermissionCollection();
    }
}
final class CryptoAllPermissionCollection extends PermissionCollection
    implements java.io.Serializable
{
    private static final long serialVersionUID = 7450076868380144072L;
    private boolean all_allowed;
    CryptoAllPermissionCollection() {
        all_allowed = false;
    }
    public void add(Permission permission)
    {
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission to " +
                                        "a readonly PermissionCollection");
        if (permission != CryptoAllPermission.INSTANCE)
            return;
        all_allowed = true;
    }
    public boolean implies(Permission permission)
    {
        if (!(permission instanceof CryptoPermission)) {
            return false;
        }
        return all_allowed;
    }
    public Enumeration elements() {
        Vector v = new Vector(1);
        if (all_allowed) v.add(CryptoAllPermission.INSTANCE);
        return v.elements();
    }
}
