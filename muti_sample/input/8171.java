public final class SpNegoMechFactory implements MechanismFactory {
    static final Provider PROVIDER =
        new sun.security.jgss.SunProvider();
    static final Oid GSS_SPNEGO_MECH_OID =
        GSSUtil.createOid("1.3.6.1.5.5.2");
    private static Oid[] nameTypes =
        new Oid[] { GSSName.NT_USER_NAME,
                        GSSName.NT_HOSTBASED_SERVICE,
                        GSSName.NT_EXPORT_NAME};
    private static final Oid DEFAULT_SPNEGO_MECH_OID =
            ProviderList.DEFAULT_MECH_OID.equals(GSS_SPNEGO_MECH_OID)?
                GSSUtil.GSS_KRB5_MECH_OID:
                ProviderList.DEFAULT_MECH_OID;
    final GSSManagerImpl manager;
    final Oid[] availableMechs;
    private static SpNegoCredElement getCredFromSubject(GSSNameSpi name,
                                                        boolean initiate)
        throws GSSException {
        Vector<SpNegoCredElement> creds =
            GSSUtil.searchSubject(name, GSS_SPNEGO_MECH_OID,
                initiate, SpNegoCredElement.class);
        SpNegoCredElement result = ((creds == null || creds.isEmpty()) ?
                                    null : creds.firstElement());
        if (result != null) {
            GSSCredentialSpi cred = result.getInternalCred();
            if (GSSUtil.isKerberosMech(cred.getMechanism())) {
                if (initiate) {
                    Krb5InitCredential krbCred = (Krb5InitCredential) cred;
                    Krb5MechFactory.checkInitCredPermission
                        ((Krb5NameElement) krbCred.getName());
                } else {
                    Krb5AcceptCredential krbCred = (Krb5AcceptCredential) cred;
                    Krb5MechFactory.checkAcceptCredPermission
                        ((Krb5NameElement) krbCred.getName(), name);
                }
            }
        }
        return result;
    }
    public SpNegoMechFactory(GSSCaller caller) {
        manager = new GSSManagerImpl(caller, false);
        Oid[] mechs = manager.getMechs();
        availableMechs = new Oid[mechs.length-1];
        for (int i = 0, j = 0; i < mechs.length; i++) {
            if (!mechs[i].equals(GSS_SPNEGO_MECH_OID)) {
                availableMechs[j++] = mechs[i];
            }
        }
        for (int i=0; i<availableMechs.length; i++) {
            if (availableMechs[i].equals(DEFAULT_SPNEGO_MECH_OID)) {
                if (i != 0) {
                    availableMechs[i] = availableMechs[0];
                    availableMechs[0] = DEFAULT_SPNEGO_MECH_OID;
                }
                break;
            }
        }
    }
    public GSSNameSpi getNameElement(String nameStr, Oid nameType)
            throws GSSException {
        return manager.getNameElement(
                nameStr, nameType, DEFAULT_SPNEGO_MECH_OID);
    }
    public GSSNameSpi getNameElement(byte[] name, Oid nameType)
            throws GSSException {
        return manager.getNameElement(name, nameType, DEFAULT_SPNEGO_MECH_OID);
    }
    public GSSCredentialSpi getCredentialElement(GSSNameSpi name,
           int initLifetime, int acceptLifetime,
           int usage) throws GSSException {
        SpNegoCredElement credElement = getCredFromSubject
            (name, (usage != GSSCredential.ACCEPT_ONLY));
        if (credElement == null) {
            credElement = new SpNegoCredElement
                (manager.getCredentialElement(name, initLifetime,
                acceptLifetime, null, usage));
        }
        return credElement;
    }
    public GSSContextSpi getMechanismContext(GSSNameSpi peer,
                             GSSCredentialSpi myInitiatorCred, int lifetime)
        throws GSSException {
        if (myInitiatorCred == null) {
            myInitiatorCred = getCredFromSubject(null, true);
        } else if (!(myInitiatorCred instanceof SpNegoCredElement)) {
            SpNegoCredElement cred = new SpNegoCredElement(myInitiatorCred);
            return new SpNegoContext(this, peer, cred, lifetime);
        }
        return new SpNegoContext(this, peer, myInitiatorCred, lifetime);
    }
    public GSSContextSpi getMechanismContext(GSSCredentialSpi myAcceptorCred)
        throws GSSException {
        if (myAcceptorCred == null) {
            myAcceptorCred = getCredFromSubject(null, false);
        } else if (!(myAcceptorCred instanceof SpNegoCredElement)) {
            SpNegoCredElement cred = new SpNegoCredElement(myAcceptorCred);
            return new SpNegoContext(this, cred);
        }
        return new SpNegoContext(this, myAcceptorCred);
    }
    public GSSContextSpi getMechanismContext(byte[] exportedContext)
        throws GSSException {
        return new SpNegoContext(this, exportedContext);
    }
    public final Oid getMechanismOid() {
        return GSS_SPNEGO_MECH_OID;
    }
    public Provider getProvider() {
        return PROVIDER;
    }
    public Oid[] getNameTypes() {
        return nameTypes;
    }
}
