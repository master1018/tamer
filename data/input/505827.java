public class IdentityScopeStub extends IdentityScope {
    public IdentityScopeStub() {
        super();
    }
    public IdentityScopeStub(String name) {
        super(name);
    }
    public IdentityScopeStub(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
    public int size() {
        return 0;
    }
    public Identity getIdentity(String name) {
        return this;
    }
    public Identity getIdentity(PublicKey key) {
        return this;
    }
    public void addIdentity(Identity identity) throws KeyManagementException {
    }
    public void removeIdentity(Identity identity) throws KeyManagementException {
    }
    public Enumeration identities() {
        return null;
    }
    public static void mySetSystemScope(IdentityScope scope) {
        IdentityScope.setSystemScope(scope);
    }
}
