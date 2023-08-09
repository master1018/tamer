public class Krb5NameElement
    implements GSSNameSpi {
    private PrincipalName krb5PrincipalName;
    private String gssNameStr = null;
    private Oid gssNameType = null;
    private static String CHAR_ENCODING = "UTF-8";
    private Krb5NameElement(PrincipalName principalName,
                            String gssNameStr,
                            Oid gssNameType) {
        this.krb5PrincipalName = principalName;
        this.gssNameStr = gssNameStr;
        this.gssNameType = gssNameType;
    }
    static Krb5NameElement getInstance(String gssNameStr, Oid gssNameType)
        throws GSSException {
        if (gssNameType == null)
            gssNameType = Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL;
        else
            if (!gssNameType.equals(GSSName.NT_USER_NAME) &&
                !gssNameType.equals(GSSName.NT_HOSTBASED_SERVICE) &&
                !gssNameType.equals(Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL) &&
                !gssNameType.equals(GSSName.NT_EXPORT_NAME))
                throw new GSSException(GSSException.BAD_NAMETYPE, -1,
                                       gssNameType.toString()
                                       +" is an unsupported nametype");
        PrincipalName principalName;
        try {
            if (gssNameType.equals(GSSName.NT_EXPORT_NAME) ||
                gssNameType.equals(Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL)) {
                principalName = new PrincipalName(gssNameStr,
                                  PrincipalName.KRB_NT_PRINCIPAL);
            } else {
                String[] components = getComponents(gssNameStr);
                if (gssNameType.equals(GSSName.NT_USER_NAME))
                    principalName = new PrincipalName(gssNameStr,
                                    PrincipalName.KRB_NT_PRINCIPAL);
                else {
                    String hostName = null;
                    String service = components[0];
                    if (components.length >= 2)
                        hostName = components[1];
                    String principal = getHostBasedInstance(service, hostName);
                    principalName = new ServiceName(principal,
                                            PrincipalName.KRB_NT_SRV_HST);
                }
            }
        } catch (KrbException e) {
            throw new GSSException(GSSException.BAD_NAME, -1, e.getMessage());
        }
        return new Krb5NameElement(principalName, gssNameStr, gssNameType);
    }
    static Krb5NameElement getInstance(PrincipalName principalName) {
        return new Krb5NameElement(principalName,
                                   principalName.getName(),
                                   Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
    }
    private static String[] getComponents(String gssNameStr)
        throws GSSException {
        String[] retVal;
        int separatorPos = gssNameStr.lastIndexOf('@', gssNameStr.length());
        if ((separatorPos > 0) &&
                (gssNameStr.charAt(separatorPos-1) == '\\')) {
            if ((separatorPos - 2 < 0) ||
                (gssNameStr.charAt(separatorPos-2) != '\\'))
                separatorPos = -1;
        }
        if (separatorPos > 0) {
            String serviceName = gssNameStr.substring(0, separatorPos);
            String hostName = gssNameStr.substring(separatorPos+1);
            retVal = new String[] { serviceName, hostName};
        } else {
            retVal = new String[] {gssNameStr};
        }
        return retVal;
    }
    private static String getHostBasedInstance(String serviceName,
                                               String hostName)
        throws GSSException {
            StringBuffer temp = new StringBuffer(serviceName);
            try {
                if (hostName == null)
                    hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
            }
            hostName = hostName.toLowerCase();
            temp = temp.append('/').append(hostName);
            return temp.toString();
    }
    public final PrincipalName getKrb5PrincipalName() {
        return krb5PrincipalName;
    }
    public boolean equals(GSSNameSpi other) throws GSSException {
        if (other == this)
            return true;
        if (other instanceof Krb5NameElement) {
                Krb5NameElement that = (Krb5NameElement) other;
                return (this.krb5PrincipalName.getName().equals(
                            that.krb5PrincipalName.getName()));
        }
        return false;
    }
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        try {
            if (another instanceof Krb5NameElement)
                 return equals((Krb5NameElement) another);
        } catch (GSSException e) {
        }
        return false;
    }
    public int hashCode() {
        return 37 * 17 + krb5PrincipalName.getName().hashCode();
    }
    public byte[] export() throws GSSException {
        byte[] retVal = null;
        try {
            retVal = krb5PrincipalName.getName().getBytes(CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }
        return retVal;
    }
    public Oid getMechanism() {
        return (Krb5MechFactory.GSS_KRB5_MECH_OID);
    }
    public String toString() {
        return (gssNameStr);
    }
    public Oid getGSSNameType() {
        return (gssNameType);
    }
    public Oid getStringNameType() {
        return (gssNameType);
    }
    public boolean isAnonymousName() {
        return (gssNameType.equals(GSSName.NT_ANONYMOUS));
    }
    public Provider getProvider() {
        return Krb5MechFactory.PROVIDER;
    }
}
