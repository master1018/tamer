public class MBeanServerPermission extends BasicPermission {
    private static final long serialVersionUID = -5661980843569388590L;
    private final static int
        CREATE = 0,
        FIND = 1,
        NEW = 2,
        RELEASE = 3,
        N_NAMES = 4;
    private final static String[] names = {
        "createMBeanServer",
        "findMBeanServer",
        "newMBeanServer",
        "releaseMBeanServer",
    };
    private final static int
        CREATE_MASK = 1<<CREATE,
        FIND_MASK = 1<<FIND,
        NEW_MASK = 1<<NEW,
        RELEASE_MASK = 1<<RELEASE,
        ALL_MASK = CREATE_MASK|FIND_MASK|NEW_MASK|RELEASE_MASK;
    private final static String[] canonicalNames = new String[1 << N_NAMES];
    transient int mask;
    public MBeanServerPermission(String name) {
        this(name, null);
    }
    public MBeanServerPermission(String name, String actions) {
        super(getCanonicalName(parseMask(name)), actions);
        mask = parseMask(name);
        if (actions != null && actions.length() > 0)
            throw new IllegalArgumentException("MBeanServerPermission " +
                                               "actions must be null: " +
                                               actions);
    }
    MBeanServerPermission(int mask) {
        super(getCanonicalName(mask));
        this.mask = impliedMask(mask);
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mask = parseMask(getName());
    }
    static int simplifyMask(int mask) {
        if ((mask & CREATE_MASK) != 0)
            mask &= ~NEW_MASK;
        return mask;
    }
    static int impliedMask(int mask) {
        if ((mask & CREATE_MASK) != 0)
            mask |= NEW_MASK;
        return mask;
    }
    static String getCanonicalName(int mask) {
        if (mask == ALL_MASK)
            return "*";
        mask = simplifyMask(mask);
        synchronized (canonicalNames) {
            if (canonicalNames[mask] == null)
                canonicalNames[mask] = makeCanonicalName(mask);
        }
        return canonicalNames[mask];
    }
    private static String makeCanonicalName(int mask) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < N_NAMES; i++) {
            if ((mask & (1<<i)) != 0) {
                if (buf.length() > 0)
                    buf.append(',');
                buf.append(names[i]);
            }
        }
        return buf.toString().intern();
    }
    private static int parseMask(String name) {
        if (name == null) {
            throw new NullPointerException("MBeanServerPermission: " +
                                           "target name can't be null");
        }
        name = name.trim();
        if (name.equals("*"))
            return ALL_MASK;
        if (name.indexOf(',') < 0)
            return impliedMask(1 << nameIndex(name.trim()));
        int mask = 0;
        StringTokenizer tok = new StringTokenizer(name, ",");
        while (tok.hasMoreTokens()) {
            String action = tok.nextToken();
            int i = nameIndex(action.trim());
            mask |= (1 << i);
        }
        return impliedMask(mask);
    }
    private static int nameIndex(String name)
            throws IllegalArgumentException {
        for (int i = 0; i < N_NAMES; i++) {
            if (names[i].equals(name))
                return i;
        }
        final String msg =
            "Invalid MBeanServerPermission name: \"" + name + "\"";
        throw new IllegalArgumentException(msg);
    }
    public int hashCode() {
        return mask;
    }
    public boolean implies(Permission p) {
        if (!(p instanceof MBeanServerPermission))
            return false;
        MBeanServerPermission that = (MBeanServerPermission) p;
        return ((this.mask & that.mask) == that.mask);
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (! (obj instanceof MBeanServerPermission))
            return false;
        MBeanServerPermission that = (MBeanServerPermission) obj;
        return (this.mask == that.mask);
    }
    public PermissionCollection newPermissionCollection() {
        return new MBeanServerPermissionCollection();
    }
}
class MBeanServerPermissionCollection extends PermissionCollection {
    private MBeanServerPermission collectionPermission;
    private static final long serialVersionUID = -5661980843569388590L;
    public synchronized void add(Permission permission) {
        if (!(permission instanceof MBeanServerPermission)) {
            final String msg =
                "Permission not an MBeanServerPermission: " + permission;
            throw new IllegalArgumentException(msg);
        }
        if (isReadOnly())
            throw new SecurityException("Read-only permission collection");
        MBeanServerPermission mbsp = (MBeanServerPermission) permission;
        if (collectionPermission == null)
            collectionPermission = mbsp;
        else if (!collectionPermission.implies(permission)) {
            int newmask = collectionPermission.mask | mbsp.mask;
            collectionPermission = new MBeanServerPermission(newmask);
        }
    }
    public synchronized boolean implies(Permission permission) {
        return (collectionPermission != null &&
                collectionPermission.implies(permission));
    }
    public synchronized Enumeration<Permission> elements() {
        Set<Permission> set;
        if (collectionPermission == null)
            set = Collections.emptySet();
        else
            set = Collections.singleton((Permission) collectionPermission);
        return Collections.enumeration(set);
    }
}
