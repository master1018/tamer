public final class ExecOptionPermission extends Permission
{
    private transient boolean wildcard;
    private transient String name;
    private static final long serialVersionUID = 5842294756823092756L;
    public ExecOptionPermission(String name) {
        super(name);
        init(name);
    }
    public ExecOptionPermission(String name, String actions) {
        this(name);
    }
    public boolean implies(Permission p) {
        if (!(p instanceof ExecOptionPermission))
            return false;
        ExecOptionPermission that = (ExecOptionPermission) p;
        if (this.wildcard) {
            if (that.wildcard) {
                return that.name.startsWith(name);
            } else {
                return (that.name.length() > this.name.length()) &&
                    that.name.startsWith(this.name);
            }
        } else {
            if (that.wildcard) {
                return false;
            } else {
                return this.name.equals(that.name);
            }
        }
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj == null) || (obj.getClass() != getClass()))
            return false;
        ExecOptionPermission that = (ExecOptionPermission) obj;
        return this.getName().equals(that.getName());
    }
    public int hashCode() {
        return this.getName().hashCode();
    }
    public String getActions() {
        return "";
    }
    public PermissionCollection newPermissionCollection() {
        return new ExecOptionPermissionCollection();
    }
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        init(getName());
    }
    private void init(String name)
    {
        if (name == null)
            throw new NullPointerException("name can't be null");
        if (name.equals("")) {
            throw new IllegalArgumentException("name can't be empty");
        }
        if (name.endsWith(".*") || name.endsWith("=*") || name.equals("*")) {
            wildcard = true;
            if (name.length() == 1) {
                this.name = "";
            } else {
                this.name = name.substring(0, name.length()-1);
            }
        } else {
            this.name = name;
        }
    }
    private static class ExecOptionPermissionCollection
        extends PermissionCollection
        implements java.io.Serializable
    {
        private Hashtable permissions;
        private boolean all_allowed; 
        private static final long serialVersionUID = -1242475729790124375L;
        public ExecOptionPermissionCollection() {
            permissions = new Hashtable(11);
            all_allowed = false;
        }
        public void add(Permission permission)
        {
            if (! (permission instanceof ExecOptionPermission))
                throw new IllegalArgumentException("invalid permission: "+
                                                   permission);
            if (isReadOnly())
                throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
            ExecOptionPermission p = (ExecOptionPermission) permission;
            permissions.put(p.getName(), permission);
            if (!all_allowed) {
                if (p.getName().equals("*"))
                    all_allowed = true;
            }
        }
        public boolean implies(Permission permission)
        {
            if (! (permission instanceof ExecOptionPermission))
                return false;
            ExecOptionPermission p = (ExecOptionPermission) permission;
            if (all_allowed)
                return true;
            String pname = p.getName();
            Permission x = (Permission) permissions.get(pname);
            if (x != null)
                return x.implies(permission);
            int last, offset;
            offset = pname.length() - 1;
            while ((last = pname.lastIndexOf(".", offset)) != -1) {
                pname = pname.substring(0, last+1) + "*";
                x = (Permission) permissions.get(pname);
                if (x != null) {
                    return x.implies(permission);
                }
                offset = last - 1;
            }
            pname = p.getName();
            offset = pname.length() - 1;
            while ((last = pname.lastIndexOf("=", offset)) != -1) {
                pname = pname.substring(0, last+1) + "*";
                x = (Permission) permissions.get(pname);
                if (x != null) {
                    return x.implies(permission);
                }
                offset = last - 1;
            }
            return false;
        }
        public Enumeration elements()
        {
            return permissions.elements();
        }
    }
}
