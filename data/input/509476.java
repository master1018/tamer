public class IdentityStub extends Identity {
    @SuppressWarnings("deprecation")
    public IdentityStub() {
        super();        
    }
    @SuppressWarnings("deprecation")
    public IdentityStub(String name) {
        super(name);        
    }
    @SuppressWarnings("deprecation")
    public IdentityStub(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
    @SuppressWarnings("deprecation")
    public IdentityStub(String name, PublicKey key) throws KeyManagementException{
        this(name);
        setPublicKey(key);
    }
    @SuppressWarnings("deprecation")
    public boolean identityEquals(Identity identity) {
        return super.identityEquals(identity);
    }
}
