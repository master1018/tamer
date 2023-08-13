abstract class Krb5Token extends GSSToken {
    public static final int AP_REQ_ID = 0x0100;
    public static final int AP_REP_ID = 0x0200;
    public static final int ERR_ID    = 0x0300;
    public static final int MIC_ID    = 0x0101;
    public static final int WRAP_ID   = 0x0201;
    public static final int MIC_ID_v2  = 0x0404;
    public static final int WRAP_ID_v2 = 0x0504;
    public static ObjectIdentifier OID;
    static {
        try {
            OID = new ObjectIdentifier(Krb5MechFactory.
                                       GSS_KRB5_MECH_OID.toString());
        } catch (IOException ioe) {
        }
    }
    public static String getTokenName(int tokenId) {
        String retVal = null;
        switch (tokenId) {
            case AP_REQ_ID:
            case AP_REP_ID:
                retVal = "Context Establishment Token";
                break;
            case MIC_ID:
                retVal = "MIC Token";
                break;
            case MIC_ID_v2:
                retVal = "MIC Token (new format)";
                break;
            case WRAP_ID:
                retVal = "Wrap Token";
                break;
            case WRAP_ID_v2:
                retVal = "Wrap Token (new format)";
                break;
            default:
                retVal = "Kerberos GSS-API Mechanism Token";
                break;
        }
        return retVal;
    }
}
