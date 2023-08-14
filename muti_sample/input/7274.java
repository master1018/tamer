public final class DelegationPermission extends BasicPermission
    implements java.io.Serializable {
    private static final long serialVersionUID = 883133252142523922L;
    private transient String subordinate, service;
    public DelegationPermission(String principals) {
        super(principals);
        init(principals);
    }
    public DelegationPermission(String principals, String actions) {
        super(principals, actions);
        init(principals);
    }
    private void init(String target) {
        StringTokenizer t = null;
        if (!target.startsWith("\"")) {
            throw new IllegalArgumentException
                ("service principal [" + target +
                 "] syntax invalid: " +
                 "improperly quoted");
        } else {
            t = new StringTokenizer(target, "\"", false);
            subordinate = t.nextToken();
            if (t.countTokens() == 2) {
                t.nextToken();  
                service = t.nextToken();
            } else if (t.countTokens() > 0) {
                throw new IllegalArgumentException
                    ("service principal [" + t.nextToken() +
                     "] syntax invalid: " +
                     "improperly quoted");
            }
        }
    }
    public boolean implies(Permission p) {
        if (!(p instanceof DelegationPermission))
            return false;
        DelegationPermission that = (DelegationPermission) p;
        if (this.subordinate.equals(that.subordinate) &&
            this.service.equals(that.service))
            return true;
        return false;
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (! (obj instanceof DelegationPermission))
            return false;
        DelegationPermission that = (DelegationPermission) obj;
        return implies(that);
    }
    public int hashCode() {
        return getName().hashCode();
    }
    public PermissionCollection newPermissionCollection() {
        return new KrbDelegationPermissionCollection();
    }
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
    }
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        init(getName());
    }
}
final class KrbDelegationPermissionCollection extends PermissionCollection
    implements java.io.Serializable {
    private transient List<Permission> perms;
    public KrbDelegationPermissionCollection() {
        perms = new ArrayList<Permission>();
    }
    public boolean implies(Permission permission) {
        if (! (permission instanceof DelegationPermission))
                return false;
        synchronized (this) {
            for (Permission x : perms) {
                if (x.implies(permission))
                    return true;
            }
        }
        return false;
    }
    public void add(Permission permission) {
        if (! (permission instanceof DelegationPermission))
            throw new IllegalArgumentException("invalid permission: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        synchronized (this) {
            perms.add(0, permission);
        }
    }
    public Enumeration<Permission> elements() {
        synchronized (this) {
            return Collections.enumeration(perms);
        }
    }
    private static final long serialVersionUID = -3383936936589966948L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Vector.class),
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        Vector<Permission> permissions = new Vector<>(perms.size());
        synchronized (this) {
            permissions.addAll(perms);
        }
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        Vector<Permission> permissions =
                (Vector<Permission>)gfields.get("permissions", null);
        perms = new ArrayList<Permission>(permissions.size());
        perms.addAll(permissions);
    }
}
