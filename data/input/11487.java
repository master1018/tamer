public class GSSNameElement implements GSSNameSpi {
    long pName = 0; 
    private String printableName;
    private Oid printableType;
    private GSSLibStub cStub;
    static final GSSNameElement DEF_ACCEPTOR = new GSSNameElement();
    private static Oid getNativeNameType(Oid nameType, GSSLibStub stub) {
        if (GSSUtil.NT_GSS_KRB5_PRINCIPAL.equals(nameType)) {
            Oid[] supportedNTs = null;
            try {
                supportedNTs = stub.inquireNamesForMech();
            } catch (GSSException ge) {
                if (ge.getMajor() == GSSException.BAD_MECH &&
                    GSSUtil.isSpNegoMech(stub.getMech())) {
                    try {
                        stub = GSSLibStub.getInstance
                            (GSSUtil.GSS_KRB5_MECH_OID);
                        supportedNTs = stub.inquireNamesForMech();
                    } catch (GSSException ge2) {
                        SunNativeProvider.debug("Name type list unavailable: " +
                            ge2.getMajorString());
                    }
                } else {
                    SunNativeProvider.debug("Name type list unavailable: " +
                        ge.getMajorString());
                }
            }
            if (supportedNTs != null) {
                for (int i = 0; i < supportedNTs.length; i++) {
                    if (supportedNTs[i].equals(nameType)) return nameType;
                }
                SunNativeProvider.debug("Override " + nameType +
                    " with mechanism default(null)");
                return null; 
            }
        }
        return nameType;
    }
    private GSSNameElement() {
        printableName = "<DEFAULT ACCEPTOR>";
    }
    GSSNameElement(long pNativeName, GSSLibStub stub) throws GSSException {
        assert(stub != null);
        if (pNativeName == 0) {
            throw new GSSException(GSSException.BAD_NAME);
        }
        pName = pNativeName;
        cStub = stub;
        setPrintables();
    }
    GSSNameElement(byte[] nameBytes, Oid nameType, GSSLibStub stub)
        throws GSSException {
        assert(stub != null);
        if (nameBytes == null) {
            throw new GSSException(GSSException.BAD_NAME);
        }
        cStub = stub;
        byte[] name = nameBytes;
        if (nameType != null) {
            nameType = getNativeNameType(nameType, stub);
            if (GSSName.NT_EXPORT_NAME.equals(nameType)) {
                byte[] mechBytes = null;
                DerOutputStream dout = new DerOutputStream();
                Oid mech = cStub.getMech();
                try {
                    dout.putOID(new ObjectIdentifier(mech.toString()));
                } catch (IOException e) {
                    throw new GSSExceptionImpl(GSSException.FAILURE, e);
                }
                mechBytes = dout.toByteArray();
                name = new byte[2 + 2 + mechBytes.length + 4 + nameBytes.length];
                int pos = 0;
                name[pos++] = 0x04;
                name[pos++] = 0x01;
                name[pos++] = (byte) (mechBytes.length>>>8);
                name[pos++] = (byte) mechBytes.length;
                System.arraycopy(mechBytes, 0, name, pos, mechBytes.length);
                pos += mechBytes.length;
                name[pos++] = (byte) (nameBytes.length>>>24);
                name[pos++] = (byte) (nameBytes.length>>>16);
                name[pos++] = (byte) (nameBytes.length>>>8);
                name[pos++] = (byte) nameBytes.length;
                System.arraycopy(nameBytes, 0, name, pos, nameBytes.length);
            }
        }
        pName = cStub.importName(name, nameType);
        setPrintables();
        SunNativeProvider.debug("Imported " + printableName + " w/ type " +
                                printableType);
    }
    private void setPrintables() throws GSSException {
        Object[] printables = null;
        printables = cStub.displayName(pName);
        assert((printables != null) && (printables.length == 2));
        printableName = (String) printables[0];
        assert(printableName != null);
        printableType = (Oid) printables[1];
        if (printableType == null) {
            printableType = GSSName.NT_USER_NAME;
        }
    }
    public String getKrbName() throws GSSException {
        long mName = 0;
        GSSLibStub stub = cStub;
        if (!GSSUtil.isKerberosMech(cStub.getMech())) {
            stub = GSSLibStub.getInstance(GSSUtil.GSS_KRB5_MECH_OID);
        }
        mName = stub.canonicalizeName(pName);
        Object[] printables2 = stub.displayName(mName);
        stub.releaseName(mName);
        SunNativeProvider.debug("Got kerberized name: " + printables2[0]);
        return (String) printables2[0];
    }
    public Provider getProvider() {
        return SunNativeProvider.INSTANCE;
    }
    public boolean equals(GSSNameSpi other) throws GSSException {
        if (!(other instanceof GSSNameElement)) {
            return false;
        }
        return cStub.compareName(pName, ((GSSNameElement)other).pName);
    }
    public boolean equals(Object other) {
        if (!(other instanceof GSSNameElement)) {
            return false;
        }
        try {
            return equals((GSSNameElement) other);
        } catch (GSSException ex) {
            return false;
        }
    }
    public int hashCode() {
        return new Long(pName).hashCode();
    }
    public byte[] export() throws GSSException {
        byte[] nameVal = cStub.exportName(pName);
        int pos = 0;
        if ((nameVal[pos++] != 0x04) ||
            (nameVal[pos++] != 0x01))
            throw new GSSException(GSSException.BAD_NAME);
        int mechOidLen  = (((0xFF & nameVal[pos++]) << 8) |
                           (0xFF & nameVal[pos++]));
        ObjectIdentifier temp = null;
        try {
            DerInputStream din = new DerInputStream(nameVal, pos,
                                                    mechOidLen);
            temp = new ObjectIdentifier(din);
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.BAD_NAME, e);
        }
        Oid mech2 = new Oid(temp.toString());
        assert(mech2.equals(getMechanism()));
        pos += mechOidLen;
        int mechPortionLen = (((0xFF & nameVal[pos++]) << 24) |
                              ((0xFF & nameVal[pos++]) << 16) |
                              ((0xFF & nameVal[pos++]) << 8) |
                              (0xFF & nameVal[pos++]));
        byte[] mechPortion = new byte[mechPortionLen];
        System.arraycopy(nameVal, pos, mechPortion, 0, mechPortionLen);
        return mechPortion;
    }
    public Oid getMechanism() {
        return cStub.getMech();
    }
    public String toString() {
        return printableName;
    }
    public Oid getStringNameType() {
        return printableType;
    }
    public boolean isAnonymousName() {
        return (GSSName.NT_ANONYMOUS.equals(printableType));
    }
    public void dispose() {
        if (pName != 0) {
            cStub.releaseName(pName);
            pName = 0;
        }
    }
    protected void finalize() throws Throwable {
        dispose();
    }
}
