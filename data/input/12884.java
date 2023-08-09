public abstract class GSSManager {
    public static GSSManager getInstance() {
        return new sun.security.jgss.GSSManagerImpl();
    }
    public  abstract Oid[] getMechs();
    public abstract  Oid[] getNamesForMech(Oid mech)
        throws GSSException;
    public abstract  Oid[] getMechsForName(Oid nameType);
    public abstract GSSName createName(String nameStr, Oid nameType)
        throws GSSException;
    public abstract GSSName createName(byte name[], Oid nameType)
        throws GSSException;
    public abstract GSSName createName(String nameStr, Oid nameType,
                                       Oid mech) throws GSSException;
    public abstract GSSName createName(byte name[], Oid nameType, Oid mech)
        throws GSSException;
    public abstract GSSCredential createCredential (int usage)
        throws GSSException;
    public abstract GSSCredential createCredential (GSSName name,
                                  int lifetime, Oid mech, int usage)
        throws GSSException;
    public abstract GSSCredential createCredential(GSSName name,
                                      int lifetime, Oid mechs[], int usage)
        throws GSSException;
    public abstract GSSContext createContext(GSSName peer, Oid mech,
                                        GSSCredential myCred, int lifetime)
        throws GSSException;
    public abstract GSSContext createContext(GSSCredential myCred)
        throws GSSException;
    public abstract GSSContext createContext(byte [] interProcessToken)
        throws GSSException;
    public abstract void addProviderAtFront(Provider p, Oid mech)
        throws GSSException;
    public abstract void addProviderAtEnd(Provider p, Oid mech)
        throws GSSException;
}
