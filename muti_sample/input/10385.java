public final class Krb5MechFactory implements MechanismFactory {
    private static final boolean DEBUG = Krb5Util.DEBUG;
    static final Provider PROVIDER =
        new sun.security.jgss.SunProvider();
    static final Oid GSS_KRB5_MECH_OID =
        createOid("1.2.840.113554.1.2.2");
    static final Oid NT_GSS_KRB5_PRINCIPAL =
        createOid("1.2.840.113554.1.2.2.1");
    private static Oid[] nameTypes =
        new Oid[] { GSSName.NT_USER_NAME,
                        GSSName.NT_HOSTBASED_SERVICE,
                        GSSName.NT_EXPORT_NAME,
                        NT_GSS_KRB5_PRINCIPAL};
    final private GSSCaller caller;
    private static Krb5CredElement getCredFromSubject(GSSNameSpi name,
                                                      boolean initiate)
        throws GSSException {
        Vector<Krb5CredElement> creds =
            GSSUtil.searchSubject(name, GSS_KRB5_MECH_OID, initiate,
                                  (initiate ?
                                   Krb5InitCredential.class :
                                   Krb5AcceptCredential.class));
        Krb5CredElement result = ((creds == null || creds.isEmpty()) ?
                                  null : creds.firstElement());
        if (result != null) {
            if (initiate) {
                checkInitCredPermission((Krb5NameElement) result.getName());
            } else {
                checkAcceptCredPermission
                    ((Krb5NameElement) result.getName(), name);
            }
        }
        return result;
    }
    public Krb5MechFactory(GSSCaller caller) {
        this.caller = caller;
    }
    public GSSNameSpi getNameElement(String nameStr, Oid nameType)
        throws GSSException {
        return Krb5NameElement.getInstance(nameStr, nameType);
    }
    public GSSNameSpi getNameElement(byte[] name, Oid nameType)
        throws GSSException {
        return Krb5NameElement.getInstance(new String(name), nameType);
    }
    public GSSCredentialSpi getCredentialElement(GSSNameSpi name,
           int initLifetime, int acceptLifetime,
           int usage) throws GSSException {
        if (name != null && !(name instanceof Krb5NameElement)) {
            name = Krb5NameElement.getInstance(name.toString(),
                                       name.getStringNameType());
        }
        Krb5CredElement credElement = getCredFromSubject
            (name, (usage != GSSCredential.ACCEPT_ONLY));
        if (credElement == null) {
            if (usage == GSSCredential.INITIATE_ONLY ||
                usage == GSSCredential.INITIATE_AND_ACCEPT) {
                credElement = Krb5InitCredential.getInstance
                    (caller, (Krb5NameElement) name, initLifetime);
                checkInitCredPermission
                    ((Krb5NameElement) credElement.getName());
            } else if (usage == GSSCredential.ACCEPT_ONLY) {
                credElement =
                    Krb5AcceptCredential.getInstance(caller,
                                                     (Krb5NameElement) name);
                checkAcceptCredPermission
                    ((Krb5NameElement) credElement.getName(), name);
            } else
                throw new GSSException(GSSException.FAILURE, -1,
                                       "Unknown usage mode requested");
        }
        return credElement;
    }
    public static void checkInitCredPermission(Krb5NameElement name) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String realm = (name.getKrb5PrincipalName()).getRealmAsString();
            String tgsPrincipal =
                new String("krbtgt/" + realm + '@' + realm);
            ServicePermission perm =
                new ServicePermission(tgsPrincipal, "initiate");
            try {
                sm.checkPermission(perm);
            } catch (SecurityException e) {
                if (DEBUG) {
                    System.out.println("Permission to initiate" +
                        "kerberos init credential" + e.getMessage());
                }
                throw e;
            }
        }
    }
    public static void checkAcceptCredPermission(Krb5NameElement name,
                                           GSSNameSpi originalName) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ServicePermission perm = new ServicePermission
                (name.getKrb5PrincipalName().getName(), "accept");
            try {
                sm.checkPermission(perm);
            } catch (SecurityException e) {
                if (originalName == null) {
                    e = new SecurityException("No permission to acquire "
                                      + "Kerberos accept credential");
                }
                throw e;
            }
        }
    }
    public GSSContextSpi getMechanismContext(GSSNameSpi peer,
                             GSSCredentialSpi myInitiatorCred, int lifetime)
        throws GSSException {
        if (peer != null && !(peer instanceof Krb5NameElement)) {
            peer = Krb5NameElement.getInstance(peer.toString(),
                                       peer.getStringNameType());
        }
        if (myInitiatorCred == null) {
            myInitiatorCred = getCredentialElement(null, lifetime, 0,
                GSSCredential.INITIATE_ONLY);
        }
        return new Krb5Context(caller, (Krb5NameElement)peer,
                               (Krb5CredElement)myInitiatorCred, lifetime);
    }
    public GSSContextSpi getMechanismContext(GSSCredentialSpi myAcceptorCred)
        throws GSSException {
        if (myAcceptorCred == null) {
            myAcceptorCred = getCredentialElement(null, 0,
                GSSCredential.INDEFINITE_LIFETIME, GSSCredential.ACCEPT_ONLY);
        }
        return new Krb5Context(caller, (Krb5CredElement)myAcceptorCred);
    }
    public GSSContextSpi getMechanismContext(byte[] exportedContext)
        throws GSSException {
        return new Krb5Context(caller, exportedContext);
    }
    public final Oid getMechanismOid() {
        return GSS_KRB5_MECH_OID;
    }
    public Provider getProvider() {
        return PROVIDER;
    }
    public Oid[] getNameTypes() {
        return nameTypes;
    }
    private static Oid createOid(String oidStr) {
        Oid retVal = null;
        try {
            retVal = new Oid(oidStr);
        } catch (GSSException e) {
        }
        return retVal;
    }
}
