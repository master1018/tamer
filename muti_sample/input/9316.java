public final class ExecPermission extends Permission
{
    private static final long serialVersionUID = -6208470287358147919L;
    private transient FilePermission fp;
    public ExecPermission(String path) {
        super(path);
        init(path);
    }
    public ExecPermission(String path, String actions) {
        this(path);
    }
    public boolean implies(Permission p) {
        if (!(p instanceof ExecPermission))
            return false;
        ExecPermission that = (ExecPermission) p;
        return fp.implies(that.fp);
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (! (obj instanceof ExecPermission))
            return false;
        ExecPermission that = (ExecPermission) obj;
        return fp.equals(that.fp);
    }
    public int hashCode() {
        return this.fp.hashCode();
    }
    public String getActions() {
        return "";
    }
    public PermissionCollection newPermissionCollection() {
        return new ExecPermissionCollection();
    }
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        init(getName());
    }
    private void init(String path) {
        this.fp = new FilePermission(path, "execute");
    }
    private static class ExecPermissionCollection
        extends PermissionCollection
        implements java.io.Serializable
    {
        private Vector permissions;
        private static final long serialVersionUID = -3352558508888368273L;
        public ExecPermissionCollection() {
            permissions = new Vector();
        }
        public void add(Permission permission)
        {
            if (! (permission instanceof ExecPermission))
                throw new IllegalArgumentException("invalid permission: "+
                                                   permission);
            if (isReadOnly())
                throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
            permissions.addElement(permission);
        }
        public boolean implies(Permission permission)
        {
            if (! (permission instanceof ExecPermission))
                return false;
            Enumeration e = permissions.elements();
            while (e.hasMoreElements()) {
                ExecPermission x = (ExecPermission) e.nextElement();
                if (x.implies(permission)) {
                    return true;
                }
            }
            return false;
        }
        public Enumeration elements()
        {
            return permissions.elements();
        }
    }
}
