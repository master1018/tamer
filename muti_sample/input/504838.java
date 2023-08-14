public abstract class Signer extends Identity {
    private static final long serialVersionUID = -1763464102261361480L;
    private PrivateKey privateKey;
    protected Signer() {
        super();
    }
    public Signer(String name) {
        super(name);
    }
    public Signer(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
    public PrivateKey getPrivateKey() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("getSignerPrivateKey"); 
        }
        return privateKey;
    }
    public final void setKeyPair(KeyPair pair)
            throws InvalidParameterException, KeyException {
        if (pair == null) {
            throw new NullPointerException();
        }
        if ((pair.getPrivate() == null) || (pair.getPublic() == null)) {
            throw new InvalidParameterException();
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setSignerKeyPair"); 
        }
        final PublicKey pk = pair.getPublic();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws KeyManagementException {
                    setPublicKey(pk);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new KeyException(e.getException());
        }
        this.privateKey = pair.getPrivate();
    }
    @Override
    public String toString() {
        String s = "[Signer]" + getName(); 
        if (getScope() != null) {
            s = s + '[' + getScope().toString() + ']';
        }
        return s;
    }
}
