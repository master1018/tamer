final public class SortResponseControl extends BasicControl {
    public static final String OID = "1.2.840.113556.1.4.474";
    private static final long serialVersionUID = 5142939176006310877L;
    private int resultCode = 0;
    private String badAttrId = null;
    public SortResponseControl(String id, boolean criticality, byte[] value)
        throws IOException {
        super(id, criticality, value);
        BerDecoder ber = new BerDecoder(value, 0, value.length);
        ber.parseSeq(null);
        resultCode = ber.parseEnumeration();
        if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_CONTEXT)) {
            badAttrId = ber.parseStringWithTag(Ber.ASN_CONTEXT, true, null);
        }
    }
    public boolean isSorted() {
        return (resultCode == 0); 
    }
    public int getResultCode() {
        return resultCode;
    }
    public String getAttributeID() {
        return badAttrId;
    }
    public NamingException getException() {
        return LdapCtx.mapErrorCode(resultCode, null);
    }
}
