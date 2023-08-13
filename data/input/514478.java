final class UnresolvedPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = -7176153071733132400L;
    private static final ObjectStreamField[] serialPersistentFields = { 
        new ObjectStreamField("permissions", Hashtable.class), }; 
    private transient Map klasses = new HashMap();
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new SecurityException(Messages.getString("security.15")); 
        }
        if (permission == null
            || permission.getClass() != UnresolvedPermission.class) {
            throw new IllegalArgumentException(Messages.getString("security.16", 
                permission));
        }
        synchronized (klasses) {
            String klass = permission.getName();
            Collection klassMates = (Collection)klasses.get(klass);
            if (klassMates == null) {
                klassMates = new HashSet();
                klasses.put(klass, klassMates);
            }
            klassMates.add(permission);
        }
    }
    public Enumeration elements() {
        Collection all = new ArrayList();
        for (Iterator iter = klasses.values().iterator(); iter.hasNext();) {
            all.addAll((Collection)iter.next());
        }
        return Collections.enumeration(all);
    }
    public boolean implies(Permission permission) {
        return false;
    }
    boolean hasUnresolved(Permission permission) {
        return klasses.containsKey(permission.getClass().getName());
    }
    PermissionCollection resolveCollection(Permission target,
                                           PermissionCollection holder) {
        String klass = target.getClass().getName();
        if (klasses.containsKey(klass)) {
            synchronized (klasses) {
                Collection klassMates = (Collection)klasses.get(klass);
                for (Iterator iter = klassMates.iterator(); iter.hasNext();) {
                    UnresolvedPermission element = (UnresolvedPermission)iter
                        .next();
                    Permission resolved = element.resolve(target.getClass());
                    if (resolved != null) {
                        if (holder == null) {
                            holder = target.newPermissionCollection();
                            if (holder == null) {
                                holder = new PermissionsHash();
                            }
                        }
                        holder.add(resolved);
                        iter.remove();
                    }
                }
                if (klassMates.size() == 0) {
                    klasses.remove(klass);
                }
            }
        }
        return holder;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        Hashtable permissions = new Hashtable();
        for (Iterator iter = klasses.entrySet().iterator(); iter.hasNext();) {
        	Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            permissions.put(key, new Vector(((Collection) entry.getValue())));
        }
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("permissions", permissions); 
        out.writeFields();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        Map permissions = (Map)fields.get("permissions", null); 
        klasses = new HashMap();
        synchronized (klasses) {
            for (Iterator iter = permissions.entrySet().iterator(); iter
            	.hasNext();) {
            	Map.Entry entry = (Map.Entry) iter.next();
	            String key = (String) entry.getKey();
	            Collection values = (Collection) entry.getValue();
	            for (Iterator iterator = values.iterator(); iterator.hasNext();) {
	                UnresolvedPermission element =
	                        (UnresolvedPermission) iterator.next();
	                if (!element.getName().equals(key)) {
	                    throw new InvalidObjectException(
	                        Messages.getString("security.22")); 
	                }
	            }
	            klasses.put(key, new HashSet(values));
	        }
        }
    }
}