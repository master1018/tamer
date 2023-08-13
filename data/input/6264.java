public class Krb5AcceptCredential
    implements Krb5CredElement {
    private static final long serialVersionUID = 7714332137352567952L;
    private Krb5NameElement name;
    private Krb5Util.ServiceCreds screds;
    private Krb5AcceptCredential(Krb5NameElement name, Krb5Util.ServiceCreds creds) {
        this.name = name;
        this.screds = creds;
    }
    static Krb5AcceptCredential getInstance(final GSSCaller caller, Krb5NameElement name)
        throws GSSException {
        final String serverPrinc = (name == null? null:
            name.getKrb5PrincipalName().getName());
        final AccessControlContext acc = AccessController.getContext();
        Krb5Util.ServiceCreds creds = null;
        try {
            creds = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Krb5Util.ServiceCreds>() {
                public Krb5Util.ServiceCreds run() throws Exception {
                    return Krb5Util.getServiceCreds(
                        caller == GSSCaller.CALLER_UNKNOWN ? GSSCaller.CALLER_ACCEPT: caller,
                        serverPrinc, acc);
                }});
        } catch (PrivilegedActionException e) {
            GSSException ge =
                new GSSException(GSSException.NO_CRED, -1,
                    "Attempt to obtain new ACCEPT credentials failed!");
            ge.initCause(e.getException());
            throw ge;
        }
        if (creds == null)
            throw new GSSException(GSSException.NO_CRED, -1,
                                   "Failed to find any Kerberos credentails");
        if (name == null) {
            String fullName = creds.getName();
            name = Krb5NameElement.getInstance(fullName,
                                       Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
        }
        return new Krb5AcceptCredential(name, creds);
    }
    public final GSSNameSpi getName() throws GSSException {
        return name;
    }
    public int getInitLifetime() throws GSSException {
        return 0;
    }
    public int getAcceptLifetime() throws GSSException {
        return GSSCredential.INDEFINITE_LIFETIME;
    }
    public boolean isInitiatorCredential() throws GSSException {
        return false;
    }
    public boolean isAcceptorCredential() throws GSSException {
        return true;
    }
    public final Oid getMechanism() {
        return Krb5MechFactory.GSS_KRB5_MECH_OID;
    }
    public final java.security.Provider getProvider() {
        return Krb5MechFactory.PROVIDER;
    }
    EncryptionKey[] getKrb5EncryptionKeys() {
        return screds.getEKeys();
    }
    public void dispose() throws GSSException {
        try {
            destroy();
        } catch (DestroyFailedException e) {
            GSSException gssException =
                new GSSException(GSSException.FAILURE, -1,
                 "Could not destroy credentials - " + e.getMessage());
            gssException.initCause(e);
        }
    }
    public void destroy() throws DestroyFailedException {
        screds.destroy();
    }
}
