final public class PersistentSearchControl extends BasicControl {
    public static final String OID = "2.16.840.1.113730.3.4.3";
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int MODIFY = 4;
    public static final int RENAME = 8;
    public static final int ANY = ADD | DELETE | MODIFY | RENAME;
    private int changeTypes = ANY;
    private boolean changesOnly = false;
    private boolean returnControls = true;
    private static final long serialVersionUID = 6335140491154854116L;
    public PersistentSearchControl() throws IOException {
        super(OID);
        super.value = setEncodedValue();
    }
    public PersistentSearchControl(int changeTypes, boolean changesOnly,
        boolean returnControls, boolean criticality) throws IOException {
        super(OID, criticality, null);
        this.changeTypes = changeTypes;
        this.changesOnly = changesOnly;
        this.returnControls = returnControls;
        super.value = setEncodedValue();
    }
    private byte[] setEncodedValue() throws IOException {
        BerEncoder ber = new BerEncoder(32);
        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeInt(changeTypes);
            ber.encodeBoolean(changesOnly);
            ber.encodeBoolean(returnControls);
        ber.endSeq();
        return ber.getTrimmedBuf();
    }
}
