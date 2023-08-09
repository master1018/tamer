public class SystemScope extends IdentityScope {
    private static final long serialVersionUID = -4810285697932522607L;
    private Hashtable names = new Hashtable();
    private Hashtable keys = new Hashtable();
    public SystemScope() {
        super();
    }
    public SystemScope(String name) {
        super(name);
    }
    public SystemScope(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
    public int size() {
        return names.size();
    }
    public synchronized Identity getIdentity(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        return (Identity) names.get(name);
    }
    public synchronized Identity getIdentity(PublicKey key) {
        if (key == null) {
            return null;
        }
        return (Identity) keys.get(key);
    }
    public synchronized void addIdentity(Identity identity)
            throws KeyManagementException {
        if (identity == null) {
            throw new NullPointerException(Messages.getString("security.92")); 
        }
        String name = identity.getName();
        if (names.containsKey(name)) {
            throw new KeyManagementException(Messages.getString("security.93", name)); 
        }
        PublicKey key = identity.getPublicKey();
        if (key != null && keys.containsKey(key)) {
            throw new KeyManagementException(Messages.getString("security.94", key)); 
        }
        names.put(name, identity);
        if (key != null) {
            keys.put(key, identity);
        }
    }
    public synchronized void removeIdentity(Identity identity)
            throws KeyManagementException {
        if (identity == null) {
            throw new NullPointerException(Messages.getString("security.92")); 
        }
        String name = identity.getName();
        if (name == null) {
            throw new NullPointerException(Messages.getString("security.95")); 
        }
        boolean contains = names.containsKey(name);
        names.remove(name);
        PublicKey key = identity.getPublicKey();
        if (key != null) {
            contains = contains || keys.containsKey(key);
            keys.remove(key);
        }
        if (!contains) {
            throw new KeyManagementException(Messages.getString("security.96")); 
        }
    }
    public Enumeration identities() {
        return names.elements();
    }
}
