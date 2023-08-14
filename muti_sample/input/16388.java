public class SpNegoCredElement implements GSSCredentialSpi {
    private GSSCredentialSpi cred = null;
    SpNegoCredElement(GSSCredentialSpi cred) throws GSSException {
        this.cred = cred;
    }
    Oid getInternalMech() {
        return cred.getMechanism();
    }
    public GSSCredentialSpi getInternalCred() {
        return cred;
    }
    public Provider getProvider() {
        return SpNegoMechFactory.PROVIDER;
    }
    public void dispose() throws GSSException {
        cred.dispose();
    }
    public GSSNameSpi getName() throws GSSException {
        return cred.getName();
    }
    public int getInitLifetime() throws GSSException {
        return cred.getInitLifetime();
    }
    public int getAcceptLifetime() throws GSSException {
        return cred.getAcceptLifetime();
    }
    public boolean isInitiatorCredential() throws GSSException {
        return cred.isInitiatorCredential();
    }
    public boolean isAcceptorCredential() throws GSSException {
        return cred.isAcceptorCredential();
    }
    public Oid getMechanism() {
        return GSSUtil.GSS_SPNEGO_MECH_OID;
    }
}
