public final class ServicePermission extends Permission
    implements java.io.Serializable {
    private static final long serialVersionUID = -1227585031618624935L;
    private final static int INITIATE   = 0x1;
    private final static int ACCEPT     = 0x2;
    private final static int ALL        = INITIATE|ACCEPT;
    private final static int NONE    = 0x0;
    private transient int mask;
    private String actions; 
    public ServicePermission(String servicePrincipal, String action) {
        super(servicePrincipal);
        init(servicePrincipal, getMask(action));
    }
    private void init(String servicePrincipal, int mask) {
        if (servicePrincipal == null)
                throw new NullPointerException("service principal can't be null");
        if ((mask & ALL) != mask)
            throw new IllegalArgumentException("invalid actions mask");
        this.mask = mask;
    }
    public boolean implies(Permission p) {
        if (!(p instanceof ServicePermission))
            return false;
        ServicePermission that = (ServicePermission) p;
        return ((this.mask & that.mask) == that.mask) &&
            impliesIgnoreMask(that);
    }
    boolean impliesIgnoreMask(ServicePermission p) {
        return ((this.getName().equals("*")) ||
                this.getName().equals(p.getName()));
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (! (obj instanceof ServicePermission))
            return false;
        ServicePermission that = (ServicePermission) obj;
        return ((this.mask & that.mask) == that.mask) &&
            this.getName().equals(that.getName());
    }
    public int hashCode() {
        return (getName().hashCode() ^ mask);
    }
    private static String getActions(int mask)
    {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        if ((mask & INITIATE) == INITIATE) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("initiate");
        }
        if ((mask & ACCEPT) == ACCEPT) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("accept");
        }
        return sb.toString();
    }
    public String getActions() {
        if (actions == null)
            actions = getActions(this.mask);
        return actions;
    }
    public PermissionCollection newPermissionCollection() {
        return new KrbServicePermissionCollection();
    }
    int getMask() {
        return mask;
    }
    private static int getMask(String action) {
        if (action == null) {
            throw new NullPointerException("action can't be null");
        }
        if (action.equals("")) {
            throw new IllegalArgumentException("action can't be empty");
        }
        int mask = NONE;
        char[] a = action.toCharArray();
        int i = a.length - 1;
        if (i < 0)
            return mask;
        while (i != -1) {
            char c;
            while ((i!=-1) && ((c = a[i]) == ' ' ||
                               c == '\r' ||
                               c == '\n' ||
                               c == '\f' ||
                               c == '\t'))
                i--;
            int matchlen;
            if (i >= 7 && (a[i-7] == 'i' || a[i-7] == 'I') &&
                          (a[i-6] == 'n' || a[i-6] == 'N') &&
                          (a[i-5] == 'i' || a[i-5] == 'I') &&
                          (a[i-4] == 't' || a[i-4] == 'T') &&
                          (a[i-3] == 'i' || a[i-3] == 'I') &&
                          (a[i-2] == 'a' || a[i-2] == 'A') &&
                          (a[i-1] == 't' || a[i-1] == 'T') &&
                          (a[i] == 'e' || a[i] == 'E'))
            {
                matchlen = 8;
                mask |= INITIATE;
            } else if (i >= 5 && (a[i-5] == 'a' || a[i-5] == 'A') &&
                                 (a[i-4] == 'c' || a[i-4] == 'C') &&
                                 (a[i-3] == 'c' || a[i-3] == 'C') &&
                                 (a[i-2] == 'e' || a[i-2] == 'E') &&
                                 (a[i-1] == 'p' || a[i-1] == 'P') &&
                                 (a[i] == 't' || a[i] == 'T'))
            {
                matchlen = 6;
                mask |= ACCEPT;
            } else {
                throw new IllegalArgumentException(
                        "invalid permission: " + action);
            }
            boolean seencomma = false;
            while (i >= matchlen && !seencomma) {
                switch(a[i-matchlen]) {
                case ',':
                    seencomma = true;
                case ' ': case '\r': case '\n':
                case '\f': case '\t':
                    break;
                default:
                    throw new IllegalArgumentException(
                            "invalid permission: " + action);
                }
                i--;
            }
            i -= matchlen;
        }
        return mask;
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        if (actions == null)
            getActions();
        s.defaultWriteObject();
    }
    private void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        init(getName(),getMask(actions));
    }
}
final class KrbServicePermissionCollection extends PermissionCollection
    implements java.io.Serializable {
    private transient List<Permission> perms;
    public KrbServicePermissionCollection() {
        perms = new ArrayList<Permission>();
    }
    public boolean implies(Permission permission) {
        if (! (permission instanceof ServicePermission))
                return false;
        ServicePermission np = (ServicePermission) permission;
        int desired = np.getMask();
        int effective = 0;
        int needed = desired;
        synchronized (this) {
            int len = perms.size();
            for (int i = 0; i < len; i++) {
                ServicePermission x = (ServicePermission) perms.get(i);
                if (((needed & x.getMask()) != 0) && x.impliesIgnoreMask(np)) {
                    effective |=  x.getMask();
                    if ((effective & desired) == desired)
                        return true;
                    needed = (desired ^ effective);
                }
            }
        }
        return false;
    }
    public void add(Permission permission) {
        if (! (permission instanceof ServicePermission))
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
    private static final long serialVersionUID = -4118834211490102011L;
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
