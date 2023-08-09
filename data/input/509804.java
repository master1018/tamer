final class AllPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = -4023755556366636806L;
    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
        "all_allowed", Boolean.TYPE), }; 
    private transient Permission all;
    @Override
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new SecurityException(Messages.getString("security.15")); 
        }
        if (!(permission instanceof AllPermission)) {
            throw new IllegalArgumentException(Messages.getString("security.16", 
                permission));
        }
        all = permission;
    }
    @Override
    public Enumeration<Permission> elements() {
        return new SingletonEnumeration<Permission>(all);
    }
    final static class SingletonEnumeration<E> implements Enumeration<E> {
        private E element;
        public SingletonEnumeration(E single) {
            element = single;
        }
        public boolean hasMoreElements() {
            return element != null;
        }
        public E nextElement() {
            if (element == null) {
                throw new NoSuchElementException(Messages.getString("security.17")); 
            }
            E last = element;
            element = null;
            return last;
        }
    }
    @Override
    public boolean implies(Permission permission) {
        return all != null;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("all_allowed", all != null); 
        out.writeFields();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        if (fields.get("all_allowed", false)) { 
            all = new AllPermission();
        }
    }
}
