public class SignerStub extends Signer {
    public SignerStub() {
        super();
    }
    public SignerStub(String name) {
        super(name);
    }
    public SignerStub(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
}
