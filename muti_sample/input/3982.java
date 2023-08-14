public abstract class BasicPermission extends Permission
implements java.io.Serializable
{
    private static final long serialVersionUID = 6279438298436773498L;
    private transient boolean wildcard;
    private transient String path;
    private transient boolean exitVM;
    private void init(String name)
    {
        if (name == null)
            throw new NullPointerException("name can't be null");
        int len = name.length();
        if (len == 0) {
            throw new IllegalArgumentException("name can't be empty");
        }
        char last = name.charAt(len - 1);
        if (last == '*' && (len == 1 || name.charAt(len - 2) == '.')) {
            wildcard = true;
            if (len == 1) {
                path = "";
            } else {
                path = name.substring(0, len - 1);
            }
        } else {
            if (name.equals("exitVM")) {
                wildcard = true;
                path = "exitVM.";
                exitVM = true;
            } else {
                path = name;
            }
        }
    }
    public BasicPermission(String name)
    {
        super(name);
        init(name);
    }
    public BasicPermission(String name, String actions)
    {
        super(name);
        init(name);
    }
    public boolean implies(Permission p) {
        if ((p == null) || (p.getClass() != getClass()))
            return false;
        BasicPermission that = (BasicPermission) p;
        if (this.wildcard) {
            if (that.wildcard) {
                return that.path.startsWith(path);
            } else {
                return (that.path.length() > this.path.length()) &&
                    that.path.startsWith(this.path);
            }
        } else {
            if (that.wildcard) {
                return false;
            }
            else {
                return this.path.equals(that.path);
            }
        }
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj == null) || (obj.getClass() != getClass()))
            return false;
        BasicPermission bp = (BasicPermission) obj;
        return getName().equals(bp.getName());
    }
    public int hashCode() {
        return this.getName().hashCode();
    }
    public String getActions()
    {
        return "";
    }
    public PermissionCollection newPermissionCollection() {
        return new BasicPermissionCollection(this.getClass());
    }
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        init(getName());
    }
    final String getCanonicalName() {
        return exitVM ? "exitVM.*" : getName();
    }
}
final class BasicPermissionCollection
extends PermissionCollection
implements java.io.Serializable
{
    private static final long serialVersionUID = 739301742472979399L;
    private transient Map<String, Permission> perms;
    private boolean all_allowed;
    private Class permClass;
    public BasicPermissionCollection(Class clazz) {
        perms = new HashMap<String, Permission>(11);
        all_allowed = false;
        permClass = clazz;
    }
    public void add(Permission permission)
    {
        if (! (permission instanceof BasicPermission))
            throw new IllegalArgumentException("invalid permission: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        BasicPermission bp = (BasicPermission) permission;
        if (permClass == null) {
            permClass = bp.getClass();
        } else {
            if (bp.getClass() != permClass)
                throw new IllegalArgumentException("invalid permission: " +
                                                permission);
        }
        synchronized (this) {
            perms.put(bp.getCanonicalName(), permission);
        }
        if (!all_allowed) {
            if (bp.getCanonicalName().equals("*"))
                all_allowed = true;
        }
    }
    public boolean implies(Permission permission)
    {
        if (! (permission instanceof BasicPermission))
                return false;
        BasicPermission bp = (BasicPermission) permission;
        if (bp.getClass() != permClass)
            return false;
        if (all_allowed)
            return true;
        String path = bp.getCanonicalName();
        Permission x;
        synchronized (this) {
            x = perms.get(path);
        }
        if (x != null) {
            return x.implies(permission);
        }
        int last, offset;
        offset = path.length()-1;
        while ((last = path.lastIndexOf(".", offset)) != -1) {
            path = path.substring(0, last+1) + "*";
            synchronized (this) {
                x = perms.get(path);
            }
            if (x != null) {
                return x.implies(permission);
            }
            offset = last -1;
        }
        return false;
    }
    public Enumeration<Permission> elements() {
        synchronized (this) {
            return Collections.enumeration(perms.values());
        }
    }
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
        new ObjectStreamField("all_allowed", Boolean.TYPE),
        new ObjectStreamField("permClass", Class.class),
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<String, Permission> permissions =
                new Hashtable<>(perms.size()*2);
        synchronized (this) {
            permissions.putAll(perms);
        }
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("all_allowed", all_allowed);
        pfields.put("permissions", permissions);
        pfields.put("permClass", permClass);
        out.writeFields();
    }
    private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException
    {
        ObjectInputStream.GetField gfields = in.readFields();
        Hashtable<String, Permission> permissions =
                (Hashtable<String, Permission>)gfields.get("permissions", null);
        perms = new HashMap<String, Permission>(permissions.size()*2);
        perms.putAll(permissions);
        all_allowed = gfields.get("all_allowed", false);
        permClass = (Class) gfields.get("permClass", null);
        if (permClass == null) {
            Enumeration<Permission> e = permissions.elements();
            if (e.hasMoreElements()) {
                Permission p = e.nextElement();
                permClass = p.getClass();
            }
        }
    }
}
