final public class EntryChangeResponseControl extends BasicControl {
    public static final String OID = "2.16.840.1.113730.3.4.7";
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int MODIFY = 4;
    public static final int RENAME = 8;
    private int changeType;
    private String previousDN = null;
    private long changeNumber = -1L;
    private static final long serialVersionUID = -2087354136750180511L;
    public EntryChangeResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {
        super(id, criticality, value);
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);
            ber.parseSeq(null);
            changeType = ber.parseEnumeration();
            if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_OCTET_STR)){
                previousDN = ber.parseString(true);
            }
            if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_INTEGER)) {
                changeNumber = ber.parseInt();
            }
        }
    }
    public int getChangeType() {
        return changeType;
    }
    public String getPreviousDN() {
        return previousDN;
    }
    public long getChangeNumber() {
        return changeNumber;
    }
}
