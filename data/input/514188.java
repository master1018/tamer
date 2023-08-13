public final class Permissions extends PermissionCollection implements
    Serializable {
    private static final long serialVersionUID = 4858622370623524688L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("perms", Hashtable.class), 
        new ObjectStreamField("allPermission", PermissionCollection.class), }; 
    private transient Map klasses = new HashMap();
    private boolean allEnabled;  
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new SecurityException(Messages.getString("security.15")); 
        }
        if (permission == null) {
            throw new NullPointerException(Messages.getString("security.20")); 
        }
        Class klass = permission.getClass();
        PermissionCollection klassMates = (PermissionCollection)klasses
            .get(klass);
        if (klassMates == null) {
            synchronized (klasses) {
                klassMates = (PermissionCollection)klasses.get(klass);
                if (klassMates == null) {
                    klassMates = permission.newPermissionCollection();
                    if (klassMates == null) {
                        klassMates = new PermissionsHash();
                    }
                    klasses.put(klass, klassMates);
                }
            }
        }
        klassMates.add(permission);
        if (klass == AllPermission.class) {
            allEnabled = true;
        }
    }
    public Enumeration<Permission> elements() {
        return new MetaEnumeration(klasses.values().iterator());
    }
    final static class MetaEnumeration implements Enumeration {
        private Iterator pcIter;
        private Enumeration current;
        public MetaEnumeration(Iterator outer) {
            pcIter = outer;
            current = getNextEnumeration();
        }
        private Enumeration getNextEnumeration() {
            while (pcIter.hasNext()) {
                Enumeration en = ((PermissionCollection)pcIter.next())
                    .elements();
                if (en.hasMoreElements()) {
                    return en;
                }
            }
            return null;
        }
        public boolean hasMoreElements() {
            return current != null ;
        }
        public Object nextElement() {
            if (current != null) {
                Object next = current.nextElement();
                if (!current.hasMoreElements()) {
                    current = getNextEnumeration();
                }
                return next;
            }
            throw new NoSuchElementException(Messages.getString("security.17")); 
        }
    }
    public boolean implies(Permission permission) {
        if (permission == null) {
            throw new NullPointerException(Messages.getString("security.21")); 
        }
        if (allEnabled) {
            return true;
        }
        Class klass = permission.getClass();
        PermissionCollection klassMates = null;
        UnresolvedPermissionCollection billets = (UnresolvedPermissionCollection)klasses
            .get(UnresolvedPermission.class);
        if (billets != null && billets.hasUnresolved(permission)) {
            synchronized (klasses) {
                klassMates = (PermissionCollection)klasses.get(klass);
                try {
                    klassMates = billets.resolveCollection(permission,
                                                           klassMates);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                if (klassMates != null) {
                    klasses.put(klass, klassMates);
                    if (klass == AllPermission.class) {
                        allEnabled = true;
                    }
                }
            }
        } else {
            klassMates = (PermissionCollection)klasses.get(klass);
        }
        if (klassMates != null) {
            return klassMates.implies(permission);
        }
        return false;
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        Map perms = (Map)fields.get("perms", null); 
        klasses = new HashMap();
        synchronized (klasses) {
            for (Iterator iter = perms.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry)  iter.next();
                Class key = (Class) entry.getKey();
                PermissionCollection pc = (PermissionCollection) entry.getValue();
                if (key != pc.elements().nextElement().getClass()) {
                    throw new InvalidObjectException(Messages.getString("security.22")); 
                }
                klasses.put(key, pc);
            }
        }
        allEnabled = fields.get("allPermission", null) != null; 
        if (allEnabled && !klasses.containsKey(AllPermission.class)) {
            throw new InvalidObjectException(Messages.getString("security.23")); 
        }
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("perms", new Hashtable(klasses)); 
        fields.put("allPermission", allEnabled ? klasses 
            .get(AllPermission.class) : null);
        out.writeFields();
    }
}
