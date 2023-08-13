class PropertyPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = 7015263904581634791L;
    Hashtable<String, Permission> permissions = new Hashtable<String, Permission>(
            30);
    @Override
    public void add(Permission perm) {
        if (!isReadOnly()) {
            Permission prev = permissions.put(perm.getName(), perm);
            if (prev != null && !prev.getActions().equals(perm.getActions())) {
                Permission np = new PropertyPermission(perm.getName(),
                        "read,write"); 
                permissions.put(perm.getName(), np);
            }
        } else {
            throw new IllegalStateException();
        }
    }
    @Override
    public Enumeration<Permission> elements() {
        return permissions.elements();
    }
    @Override
    public boolean implies(Permission perm) {
        Enumeration<Permission> elemEnum = elements();
        while (elemEnum.hasMoreElements()) {
            if ((elemEnum.nextElement()).implies(perm)) {
                return true;
            }
        }
        return perm.getActions().equals("read,write") 
                && implies(new PropertyPermission(perm.getName(), "read")) 
                && implies(new PropertyPermission(perm.getName(), "write")); 
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("permissions", Hashtable.class), 
            new ObjectStreamField("all_allowed", Boolean.TYPE) }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("permissions", permissions); 
        fields.put("all_allowed", false); 
        stream.writeFields();
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        permissions = (Hashtable<String, Permission>) fields.get(
                "permissions", null); 
    }
}
