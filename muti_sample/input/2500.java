public class GSSCredElement implements GSSCredentialSpi {
    private int usage;
    long pCred; 
    private GSSNameElement name = null;
    private GSSLibStub cStub;
    void doServicePermCheck() throws GSSException {
        if (GSSUtil.isKerberosMech(cStub.getMech())) {
            if (System.getSecurityManager() != null) {
                if (isInitiatorCredential()) {
                    String tgsName = Krb5Util.getTGSName(name);
                    Krb5Util.checkServicePermission(tgsName, "initiate");
                }
                if (isAcceptorCredential() &&
                    name != GSSNameElement.DEF_ACCEPTOR) {
                    String krbName = name.getKrbName();
                    Krb5Util.checkServicePermission(krbName, "accept");
                }
            }
        }
    }
    GSSCredElement(long pCredentials, GSSNameElement srcName, Oid mech)
        throws GSSException {
        pCred = pCredentials;
        cStub = GSSLibStub.getInstance(mech);
        usage = GSSCredential.INITIATE_ONLY;
        name = srcName;
    }
    GSSCredElement(GSSNameElement name, int lifetime, int usage,
                   GSSLibStub stub) throws GSSException {
        cStub = stub;
        this.usage = usage;
        if (name != null) { 
            this.name = name;
            doServicePermCheck();
            pCred = cStub.acquireCred(this.name.pName, lifetime, usage);
        } else {
            pCred = cStub.acquireCred(0, lifetime, usage);
            this.name = new GSSNameElement(cStub.getCredName(pCred), cStub);
            doServicePermCheck();
        }
    }
    public Provider getProvider() {
        return SunNativeProvider.INSTANCE;
    }
    public void dispose() throws GSSException {
        name = null;
        if (pCred != 0) {
            pCred = cStub.releaseCred(pCred);
        }
    }
    public GSSNameElement getName() throws GSSException {
        return (name == GSSNameElement.DEF_ACCEPTOR ?
            null : name);
    }
    public int getInitLifetime() throws GSSException {
        if (isInitiatorCredential()) {
            return cStub.getCredTime(pCred);
        } else return 0;
    }
    public int getAcceptLifetime() throws GSSException {
        if (isAcceptorCredential()) {
            return cStub.getCredTime(pCred);
        } else return 0;
    }
    public boolean isInitiatorCredential() {
        return (usage != GSSCredential.ACCEPT_ONLY);
    }
    public boolean isAcceptorCredential() {
        return (usage != GSSCredential.INITIATE_ONLY);
    }
    public Oid getMechanism() {
        return cStub.getMech();
    }
    public String toString() {
        return "N/A";
    }
    protected void finalize() throws Throwable {
        dispose();
    }
}
