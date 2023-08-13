public class GSSNameImpl implements GSSName {
    final static Oid oldHostbasedServiceName;
    static {
        Oid tmp = null;
        try {
            tmp = new Oid("1.3.6.1.5.6.2");
        } catch (Exception e) {
        }
        oldHostbasedServiceName = tmp;
    }
    private GSSManagerImpl gssManager = null;
    private String appNameStr = null;
    private byte[] appNameBytes = null;
    private Oid appNameType = null;
    private String printableName = null;
    private Oid printableNameType = null;
    private HashMap<Oid, GSSNameSpi> elements = null;
    private GSSNameSpi mechElement = null;
    static GSSNameImpl wrapElement(GSSManagerImpl gssManager,
        GSSNameSpi mechElement) throws GSSException {
        return (mechElement == null ?
            null : new GSSNameImpl(gssManager, mechElement));
    }
    GSSNameImpl(GSSManagerImpl gssManager, GSSNameSpi mechElement) {
        this.gssManager = gssManager;
        appNameStr = printableName = mechElement.toString();
        appNameType = printableNameType = mechElement.getStringNameType();
        this.mechElement = mechElement;
        elements = new HashMap<Oid, GSSNameSpi>(1);
        elements.put(mechElement.getMechanism(), this.mechElement);
    }
    GSSNameImpl(GSSManagerImpl gssManager,
                       Object appName,
                       Oid appNameType)
        throws GSSException {
        this(gssManager, appName, appNameType, null);
    }
    GSSNameImpl(GSSManagerImpl gssManager,
                        Object appName,
                        Oid appNameType,
                        Oid mech)
        throws GSSException {
        if (oldHostbasedServiceName.equals(appNameType)) {
            appNameType = GSSName.NT_HOSTBASED_SERVICE;
        }
        if (appName == null)
            throw new GSSExceptionImpl(GSSException.BAD_NAME,
                                   "Cannot import null name");
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        if (NT_EXPORT_NAME.equals(appNameType)) {
            importName(gssManager, appName);
        } else {
            init(gssManager, appName, appNameType, mech);
        }
    }
    private void init(GSSManagerImpl gssManager,
                      Object appName, Oid appNameType,
                      Oid mech)
        throws GSSException {
        this.gssManager = gssManager;
        this.elements =
                new HashMap<Oid, GSSNameSpi>(gssManager.getMechs().length);
        if (appName instanceof String) {
            this.appNameStr = (String) appName;
            if (appNameType != null) {
                printableName = appNameStr;
                printableNameType = appNameType;
            }
        } else {
            this.appNameBytes = (byte[]) appName;
        }
        this.appNameType = appNameType;
        mechElement = getElement(mech);
        if (printableName == null) {
            printableName = mechElement.toString();
            printableNameType = mechElement.getStringNameType();
        }
    }
    private void importName(GSSManagerImpl gssManager,
                            Object appName)
        throws GSSException {
        int pos = 0;
        byte[] bytes = null;
        if (appName instanceof String) {
            try {
                bytes = ((String) appName).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        } else
            bytes = (byte[]) appName;
        if ((bytes[pos++] != 0x04) ||
            (bytes[pos++] != 0x01))
            throw new GSSExceptionImpl(GSSException.BAD_NAME,
                                   "Exported name token id is corrupted!");
        int oidLen  = (((0xFF & bytes[pos++]) << 8) |
                       (0xFF & bytes[pos++]));
        ObjectIdentifier temp = null;
        try {
            DerInputStream din = new DerInputStream(bytes, pos,
                                                    oidLen);
            temp = new ObjectIdentifier(din);
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.BAD_NAME,
                       "Exported name Object identifier is corrupted!");
        }
        Oid oid = new Oid(temp.toString());
        pos += oidLen;
        int mechPortionLen = (((0xFF & bytes[pos++]) << 24) |
                              ((0xFF & bytes[pos++]) << 16) |
                              ((0xFF & bytes[pos++]) << 8) |
                              (0xFF & bytes[pos++]));
        byte[] mechPortion = new byte[mechPortionLen];
        System.arraycopy(bytes, pos, mechPortion, 0, mechPortionLen);
        init(gssManager, mechPortion, NT_EXPORT_NAME, oid);
    }
    public GSSName canonicalize(Oid mech) throws GSSException {
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        return wrapElement(gssManager, getElement(mech));
    }
    public boolean equals(GSSName other) throws GSSException {
        if (this.isAnonymous() || other.isAnonymous())
            return false;
        if (other == this)
            return true;
        if (! (other instanceof GSSNameImpl))
            return equals(gssManager.createName(other.toString(),
                                                other.getStringNameType()));
        GSSNameImpl that = (GSSNameImpl) other;
        GSSNameSpi myElement = this.mechElement;
        GSSNameSpi element = that.mechElement;
        if ((myElement == null) && (element != null)) {
            myElement = this.getElement(element.getMechanism());
        } else if ((myElement != null) && (element == null)) {
            element = that.getElement(myElement.getMechanism());
        }
        if (myElement != null && element != null) {
            return myElement.equals(element);
        }
        if ((this.appNameType != null) &&
            (that.appNameType != null)) {
            if (!this.appNameType.equals(that.appNameType)) {
                return false;
            }
            byte[] myBytes = null;
            byte[] bytes = null;
            try {
                myBytes =
                    (this.appNameStr != null ?
                     this.appNameStr.getBytes("UTF-8") :
                     this.appNameBytes);
                bytes =
                    (that.appNameStr != null ?
                     that.appNameStr.getBytes("UTF-8") :
                     that.appNameBytes);
            } catch (UnsupportedEncodingException e) {
            }
            return Arrays.equals(myBytes, bytes);
        }
        return false;
    }
    public int hashCode() {
        return 1;
    }
    public boolean equals(Object another) {
        try {
            if (another instanceof GSSName)
                return equals((GSSName) another);
        } catch (GSSException e) {
        }
            return false;
    }
    public byte[] export() throws GSSException {
        if (mechElement == null) {
            mechElement = getElement(ProviderList.DEFAULT_MECH_OID);
        }
        byte[] mechPortion = mechElement.export();
        byte[] oidBytes = null;
        ObjectIdentifier oid = null;
        try {
            oid = new ObjectIdentifier
                (mechElement.getMechanism().toString());
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.FAILURE,
                                       "Invalid OID String ");
        }
        DerOutputStream dout = new DerOutputStream();
        try {
            dout.putOID(oid);
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.FAILURE,
                                   "Could not ASN.1 Encode "
                                   + oid.toString());
        }
        oidBytes = dout.toByteArray();
        byte[] retVal = new byte[2
                                + 2 + oidBytes.length
                                + 4 + mechPortion.length];
        int pos = 0;
        retVal[pos++] = 0x04;
        retVal[pos++] = 0x01;
        retVal[pos++] = (byte) (oidBytes.length>>>8);
        retVal[pos++] = (byte) oidBytes.length;
        System.arraycopy(oidBytes, 0, retVal, pos, oidBytes.length);
        pos += oidBytes.length;
        retVal[pos++] = (byte) (mechPortion.length>>>24);
        retVal[pos++] = (byte) (mechPortion.length>>>16);
        retVal[pos++] = (byte) (mechPortion.length>>>8);
        retVal[pos++] = (byte)  mechPortion.length;
        System.arraycopy(mechPortion, 0, retVal, pos, mechPortion.length);
        return retVal;
    }
    public String toString() {
         return printableName;
    }
    public Oid getStringNameType() throws GSSException {
        return printableNameType;
    }
    public boolean isAnonymous() {
        if (printableNameType == null) {
            return false;
        } else {
            return GSSName.NT_ANONYMOUS.equals(printableNameType);
        }
    }
    public boolean isMN() {
        return true; 
    }
    public synchronized GSSNameSpi getElement(Oid mechOid)
        throws GSSException {
        GSSNameSpi retVal = elements.get(mechOid);
        if (retVal == null) {
            if (appNameStr != null) {
                retVal = gssManager.getNameElement
                    (appNameStr, appNameType, mechOid);
            } else {
                retVal = gssManager.getNameElement
                    (appNameBytes, appNameType, mechOid);
            }
            elements.put(mechOid, retVal);
        }
        return retVal;
    }
    Set<GSSNameSpi> getElements() {
        return new HashSet<GSSNameSpi>(elements.values());
    }
    private static String getNameTypeStr(Oid nameTypeOid) {
        if (nameTypeOid == null)
            return "(NT is null)";
        if (nameTypeOid.equals(NT_USER_NAME))
            return "NT_USER_NAME";
        if (nameTypeOid.equals(NT_HOSTBASED_SERVICE))
            return "NT_HOSTBASED_SERVICE";
        if (nameTypeOid.equals(NT_EXPORT_NAME))
            return "NT_EXPORT_NAME";
        if (nameTypeOid.equals(GSSUtil.NT_GSS_KRB5_PRINCIPAL))
            return "NT_GSS_KRB5_PRINCIPAL";
        else
            return "Unknown";
    }
}
