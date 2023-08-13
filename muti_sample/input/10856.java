public final class AllPermission extends Permission {
    private static final long serialVersionUID = -2916474571451318075L;
    public AllPermission()
    {
        super("<all permissions>");
    }
    public AllPermission(String name, String actions)
    {
        this();
    }
    public boolean implies(Permission p) {
         return true;
    }
    public boolean equals(Object obj) {
        return (obj instanceof AllPermission);
    }
    public int hashCode() {
        return 1;
    }
    public String getActions()
    {
        return "<all actions>";
    }
    public PermissionCollection newPermissionCollection() {
        return new AllPermissionCollection();
    }
}
final class AllPermissionCollection
extends PermissionCollection
implements java.io.Serializable
{
    private static final long serialVersionUID = -4023755556366636806L;
    private boolean all_allowed; 
    public AllPermissionCollection() {
        all_allowed = false;
    }
    public void add(Permission permission)
    {
        if (! (permission instanceof AllPermission))
            throw new IllegalArgumentException("invalid permission: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        all_allowed = true; 
    }
    public boolean implies(Permission permission)
    {
        return all_allowed; 
    }
    public Enumeration<Permission> elements()
    {
        return new Enumeration<Permission>() {
            private boolean hasMore = all_allowed;
            public boolean hasMoreElements() {
                return hasMore;
            }
            public Permission nextElement() {
                hasMore = false;
                return SecurityConstants.ALL_PERMISSION;
            }
        };
    }
}
