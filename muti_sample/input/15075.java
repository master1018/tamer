public final class Permissions extends PermissionCollection
implements Serializable
{
    private transient Map<Class<?>, PermissionCollection> permsMap;
    private transient boolean hasUnresolved = false;
    PermissionCollection allPermission;
    public Permissions() {
        permsMap = new HashMap<Class<?>, PermissionCollection>(11);
        allPermission = null;
    }
    public void add(Permission permission) {
        if (isReadOnly())
            throw new SecurityException(
              "attempt to add a Permission to a readonly Permissions object");
        PermissionCollection pc;
        synchronized (this) {
            pc = getPermissionCollection(permission, true);
            pc.add(permission);
        }
        if (permission instanceof AllPermission) {
            allPermission = pc;
        }
        if (permission instanceof UnresolvedPermission) {
            hasUnresolved = true;
        }
    }
    public boolean implies(Permission permission) {
        if (allPermission != null) {
            return true; 
        } else {
            synchronized (this) {
                PermissionCollection pc = getPermissionCollection(permission,
                    false);
                if (pc != null) {
                    return pc.implies(permission);
                } else {
                    return false;
                }
            }
        }
    }
    public Enumeration<Permission> elements() {
        synchronized (this) {
            return new PermissionsEnumerator(permsMap.values().iterator());
        }
    }
    private PermissionCollection getPermissionCollection(Permission p,
        boolean createEmpty) {
        Class c = p.getClass();
        PermissionCollection pc = permsMap.get(c);
        if (!hasUnresolved && !createEmpty) {
            return pc;
        } else if (pc == null) {
            pc = (hasUnresolved ? getUnresolvedPermissions(p) : null);
            if (pc == null && createEmpty) {
                pc = p.newPermissionCollection();
                if (pc == null)
                    pc = new PermissionsHash();
            }
            if (pc != null) {
                permsMap.put(c, pc);
            }
        }
        return pc;
    }
    private PermissionCollection getUnresolvedPermissions(Permission p)
    {
        UnresolvedPermissionCollection uc =
        (UnresolvedPermissionCollection) permsMap.get(UnresolvedPermission.class);
        if (uc == null)
            return null;
        List<UnresolvedPermission> unresolvedPerms =
                                        uc.getUnresolvedPermissions(p);
        if (unresolvedPerms == null)
            return null;
        java.security.cert.Certificate certs[] = null;
        Object signers[] = p.getClass().getSigners();
        int n = 0;
        if (signers != null) {
            for (int j=0; j < signers.length; j++) {
                if (signers[j] instanceof java.security.cert.Certificate) {
                    n++;
                }
            }
            certs = new java.security.cert.Certificate[n];
            n = 0;
            for (int j=0; j < signers.length; j++) {
                if (signers[j] instanceof java.security.cert.Certificate) {
                    certs[n++] = (java.security.cert.Certificate)signers[j];
                }
            }
        }
        PermissionCollection pc = null;
        synchronized (unresolvedPerms) {
            int len = unresolvedPerms.size();
            for (int i = 0; i < len; i++) {
                UnresolvedPermission up = unresolvedPerms.get(i);
                Permission perm = up.resolve(p, certs);
                if (perm != null) {
                    if (pc == null) {
                        pc = p.newPermissionCollection();
                        if (pc == null)
                            pc = new PermissionsHash();
                    }
                    pc.add(perm);
                }
            }
        }
        return pc;
    }
    private static final long serialVersionUID = 4858622370623524688L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("perms", Hashtable.class),
        new ObjectStreamField("allPermission", PermissionCollection.class),
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<Class<?>, PermissionCollection> perms =
            new Hashtable<>(permsMap.size()*2); 
        synchronized (this) {
            perms.putAll(permsMap);
        }
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("allPermission", allPermission); 
        pfields.put("perms", perms);
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        allPermission = (PermissionCollection) gfields.get("allPermission", null);
        Hashtable<Class<?>, PermissionCollection> perms =
            (Hashtable<Class<?>, PermissionCollection>)gfields.get("perms", null);
        permsMap = new HashMap<Class<?>, PermissionCollection>(perms.size()*2);
        permsMap.putAll(perms);
        UnresolvedPermissionCollection uc =
        (UnresolvedPermissionCollection) permsMap.get(UnresolvedPermission.class);
        hasUnresolved = (uc != null && uc.elements().hasMoreElements());
    }
}
final class PermissionsEnumerator implements Enumeration<Permission> {
    private Iterator<PermissionCollection> perms;
    private Enumeration<Permission> permset;
    PermissionsEnumerator(Iterator<PermissionCollection> e) {
        perms = e;
        permset = getNextEnumWithMore();
    }
    public boolean hasMoreElements() {
        if (permset == null)
            return  false;
        if (permset.hasMoreElements())
            return true;
        permset = getNextEnumWithMore();
        return (permset != null);
    }
    public Permission nextElement() {
        if (hasMoreElements()) {
            return permset.nextElement();
        } else {
            throw new NoSuchElementException("PermissionsEnumerator");
        }
    }
    private Enumeration<Permission> getNextEnumWithMore() {
        while (perms.hasNext()) {
            PermissionCollection pc = perms.next();
            Enumeration<Permission> next =pc.elements();
            if (next.hasMoreElements())
                return next;
        }
        return null;
    }
}
final class PermissionsHash extends PermissionCollection
implements Serializable
{
    private transient Map<Permission, Permission> permsMap;
    PermissionsHash() {
        permsMap = new HashMap<Permission, Permission>(11);
    }
    public void add(Permission permission) {
        synchronized (this) {
            permsMap.put(permission, permission);
        }
    }
    public boolean implies(Permission permission) {
        synchronized (this) {
            Permission p = permsMap.get(permission);
            if (p == null) {
                for (Permission p_ : permsMap.values()) {
                    if (p_.implies(permission))
                        return true;
                }
                return false;
            } else {
                return true;
            }
        }
    }
    public Enumeration<Permission> elements() {
        synchronized (this) {
            return Collections.enumeration(permsMap.values());
        }
    }
    private static final long serialVersionUID = -8491988220802933440L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("perms", Hashtable.class),
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<Permission, Permission> perms =
                new Hashtable<>(permsMap.size()*2);
        synchronized (this) {
            perms.putAll(permsMap);
        }
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("perms", perms);
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        Hashtable<Permission, Permission> perms =
                (Hashtable<Permission, Permission>)gfields.get("perms", null);
        permsMap = new HashMap<Permission, Permission>(perms.size()*2);
        permsMap.putAll(perms);
    }
}
