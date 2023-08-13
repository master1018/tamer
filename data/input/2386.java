public class GSSManagerImpl extends GSSManager {
    private static final String USE_NATIVE_PROP =
        "sun.security.jgss.native";
    private static final Boolean USE_NATIVE;
    static {
        USE_NATIVE =
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                            String osname = System.getProperty("os.name");
                            if (osname.startsWith("SunOS") ||
                                osname.startsWith("Linux")) {
                                return new Boolean(System.getProperty
                                    (USE_NATIVE_PROP));
                            }
                            return Boolean.FALSE;
                    }
            });
    }
    private ProviderList list;
    public GSSManagerImpl(GSSCaller caller, boolean useNative) {
        list = new ProviderList(caller, useNative);
    }
    public GSSManagerImpl(GSSCaller caller) {
        list = new ProviderList(caller, USE_NATIVE);
    }
    public GSSManagerImpl() {
        list = new ProviderList(GSSCaller.CALLER_UNKNOWN, USE_NATIVE);
    }
    public Oid[] getMechs(){
        return list.getMechs();
    }
    public Oid[] getNamesForMech(Oid mech)
        throws GSSException {
        MechanismFactory factory = list.getMechFactory(mech);
        return factory.getNameTypes().clone();
    }
    public Oid[] getMechsForName(Oid nameType){
        Oid[] mechs = list.getMechs();
        Oid[] retVal = new Oid[mechs.length];
        int pos = 0;
        if (nameType.equals(GSSNameImpl.oldHostbasedServiceName)) {
            nameType = GSSName.NT_HOSTBASED_SERVICE;
        }
        for (int i = 0; i < mechs.length; i++) {
            Oid mech = mechs[i];
            try {
                Oid[] namesForMech = getNamesForMech(mech);
                if (nameType.containedIn(namesForMech)) {
                    retVal[pos++] = mech;
                }
            } catch (GSSException e) {
                GSSUtil.debug("Skip " + mech +
                              ": error retrieving supported name types");
            }
        }
        if (pos < retVal.length) {
            Oid[] temp = new Oid[pos];
            for (int i = 0; i < pos; i++)
                temp[i] = retVal[i];
            retVal = temp;
        }
        return retVal;
    }
    public GSSName createName(String nameStr, Oid nameType)
        throws GSSException {
        return new GSSNameImpl(this, nameStr, nameType);
    }
    public GSSName createName(byte name[], Oid nameType)
        throws GSSException {
        return new GSSNameImpl(this, name, nameType);
    }
    public GSSName createName(String nameStr, Oid nameType,
                              Oid mech) throws GSSException {
        return new GSSNameImpl(this, nameStr, nameType, mech);
    }
    public GSSName createName(byte name[], Oid nameType, Oid mech)
        throws GSSException {
        return new GSSNameImpl(this, name, nameType, mech);
    }
    public GSSCredential createCredential(int usage)
        throws GSSException {
        return new GSSCredentialImpl(this, usage);
    }
    public GSSCredential createCredential(GSSName aName,
                                          int lifetime, Oid mech, int usage)
        throws GSSException {
        return new GSSCredentialImpl(this, aName, lifetime, mech, usage);
    }
    public GSSCredential createCredential(GSSName aName,
                                          int lifetime, Oid mechs[], int usage)
        throws GSSException {
        return new GSSCredentialImpl(this, aName, lifetime, mechs, usage);
    }
    public GSSContext createContext(GSSName peer, Oid mech,
                                    GSSCredential myCred, int lifetime)
        throws GSSException {
        return new GSSContextImpl(this, peer, mech, myCred, lifetime);
    }
    public GSSContext createContext(GSSCredential myCred)
        throws GSSException {
        return new GSSContextImpl(this, myCred);
    }
    public GSSContext createContext(byte[] interProcessToken)
        throws GSSException {
        return new GSSContextImpl(this, interProcessToken);
    }
    public void addProviderAtFront(Provider p, Oid mech)
        throws GSSException {
        list.addProviderAtFront(p, mech);
    }
    public void addProviderAtEnd(Provider p, Oid mech)
        throws GSSException {
        list.addProviderAtEnd(p, mech);
    }
    public GSSCredentialSpi getCredentialElement(GSSNameSpi name, int initLifetime,
                                          int acceptLifetime, Oid mech, int usage)
        throws GSSException {
        MechanismFactory factory = list.getMechFactory(mech);
        return factory.getCredentialElement(name, initLifetime,
                                            acceptLifetime, usage);
    }
    public GSSNameSpi getNameElement(String name, Oid nameType, Oid mech)
        throws GSSException {
        MechanismFactory factory = list.getMechFactory(mech);
        return factory.getNameElement(name, nameType);
    }
    public GSSNameSpi getNameElement(byte[] name, Oid nameType, Oid mech)
        throws GSSException {
        MechanismFactory factory = list.getMechFactory(mech);
        return factory.getNameElement(name, nameType);
    }
    GSSContextSpi getMechanismContext(GSSNameSpi peer,
                                      GSSCredentialSpi myInitiatorCred,
                                      int lifetime, Oid mech)
        throws GSSException {
        Provider p = null;
        if (myInitiatorCred != null) {
            p = myInitiatorCred.getProvider();
        }
        MechanismFactory factory = list.getMechFactory(mech, p);
        return factory.getMechanismContext(peer, myInitiatorCred, lifetime);
    }
    GSSContextSpi getMechanismContext(GSSCredentialSpi myAcceptorCred,
                                      Oid mech)
        throws GSSException {
        Provider p = null;
        if (myAcceptorCred != null) {
            p = myAcceptorCred.getProvider();
        }
        MechanismFactory factory = list.getMechFactory(mech, p);
        return factory.getMechanismContext(myAcceptorCred);
    }
    GSSContextSpi getMechanismContext(byte[] exportedContext)
        throws GSSException {
        if ((exportedContext == null) || (exportedContext.length == 0)) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }
        GSSContextSpi result = null;
        Oid[] mechs = list.getMechs();
        for (int i = 0; i < mechs.length; i++) {
            MechanismFactory factory = list.getMechFactory(mechs[i]);
            if (factory.getProvider().getName().equals("SunNativeGSS")) {
                result = factory.getMechanismContext(exportedContext);
                if (result != null) break;
            }
        }
        if (result == null) {
            throw new GSSException(GSSException.UNAVAILABLE);
        }
        return result;
    }
}
