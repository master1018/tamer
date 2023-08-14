public final class PropertyPermission extends BasicPermission {
    private static final long serialVersionUID = 885438825399942851L;
    transient private boolean read, write;
    public PropertyPermission(String name, String actions) {
        super(name);
        decodeActions(actions);
    }
    private void decodeActions(String actions) {
        StringTokenizer tokenizer = new StringTokenizer(Util.toASCIILowerCase(actions),
                " \t\n\r,"); 
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("read")) { 
                read = true;
            } else if (token.equals("write")) { 
                write = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (!read && !write) {
            throw new IllegalArgumentException();
        }
    }
    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            PropertyPermission pp = (PropertyPermission) o;
            return read == pp.read && write == pp.write;
        }
        return false;
    }
    @Override
    public String getActions() {
        return read ? (write ? "read,write" : "read") : "write"; 
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean implies(Permission permission) {
        if (super.implies(permission)) {
            PropertyPermission pp = (PropertyPermission) permission;
            return (read || !pp.read) && (write || !pp.write);
        }
        return false;
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new PropertyPermissionCollection();
    }
    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
            "actions", String.class) }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("actions", getActions()); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        String actions = (String) fields.get("actions", ""); 
        decodeActions(actions);
    }
}
