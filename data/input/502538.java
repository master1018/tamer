final class BasicPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = 739301742472979399L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("all_allowed", Boolean.TYPE), 
        new ObjectStreamField("permissions", Hashtable.class), 
        new ObjectStreamField("permClass", Class.class), }; 
    private transient Map<String, Permission> items = new HashMap<String, Permission>();
    private transient boolean allEnabled; 
    private Class<? extends Permission> permClass;
    @Override
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new SecurityException(Messages.getString("security.15")); 
        }
        if (permission == null) {
            throw new IllegalArgumentException(Messages.getString("security.20")); 
        }
        Class<? extends Permission> inClass = permission.getClass();
        if (permClass != null) {
            if (permClass != inClass) {
                throw new IllegalArgumentException(Messages.getString("security.16", 
                    permission));
            }
        } else if( !(permission instanceof BasicPermission)) {
            throw new IllegalArgumentException(Messages.getString("security.16", 
                permission));
        } else { 
            synchronized (this) {
                if (permClass != null && inClass != permClass) {
                    throw new IllegalArgumentException(Messages.getString("security.16", 
                        permission));
                }
                permClass = inClass;
            }
        }
        String name = permission.getName();
        items.put(name, permission);
        allEnabled = allEnabled || (name.length() == 1 && '*' == name.charAt(0));
    }
    @Override
    public Enumeration<Permission> elements() {
        return Collections.enumeration(items.values());
    }
    @Override
    public boolean implies(Permission permission) {
        if (permission == null || permission.getClass() != permClass) {
            return false;
        }
        if (allEnabled) {
            return true;
        }
        String checkName = permission.getName();
        if (items.containsKey(checkName)) {
            return true;
        }
        char[] name = checkName.toCharArray();
        int pos = name.length - 2; 
        for (; pos >= 0; pos--) {
            if (name[pos] == '.') {
                break;
            }
        }
        while (pos >= 0) {
            name[pos + 1] = '*'; 
            if (items.containsKey(new String(name, 0, pos + 2))) {
                return true;
            }
            for (--pos; pos >= 0; pos--) {
                if (name[pos] == '.') {
                    break;
                }
            }
        }
        return false;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("all_allowed", allEnabled); 
        fields.put("permissions", new Hashtable<String, Permission>(items)); 
        fields.put("permClass", permClass); 
        out.writeFields();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        items = new HashMap<String, Permission>();
        synchronized (this) {
            permClass = (Class<? extends Permission>)fields.get("permClass", null); 
            items.putAll((Hashtable<String, Permission>) fields.get(
                    "permissions", new Hashtable<String, Permission>())); 
            for (Iterator<Permission> iter = items.values().iterator(); iter.hasNext();) {
                if (iter.next().getClass() != permClass) {
                    throw new InvalidObjectException(Messages.getString("security.24")); 
                }
            }
            allEnabled = fields.get("all_allowed", false); 
            if (allEnabled && !items.containsKey("*")) { 
                throw new InvalidObjectException(Messages.getString("security.25")); 
            }
        }
    }
}
