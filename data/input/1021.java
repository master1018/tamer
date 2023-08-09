public class ReasonFlags {
    public static final String UNUSED = "unused";
    public static final String KEY_COMPROMISE = "key_compromise";
    public static final String CA_COMPROMISE = "ca_compromise";
    public static final String AFFILIATION_CHANGED = "affiliation_changed";
    public static final String SUPERSEDED = "superseded";
    public static final String CESSATION_OF_OPERATION
                                   = "cessation_of_operation";
    public static final String CERTIFICATE_HOLD = "certificate_hold";
    public static final String PRIVILEGE_WITHDRAWN = "privilege_withdrawn";
    public static final String AA_COMPROMISE = "aa_compromise";
    private final static String[] NAMES = {
        UNUSED,
        KEY_COMPROMISE,
        CA_COMPROMISE,
        AFFILIATION_CHANGED,
        SUPERSEDED,
        CESSATION_OF_OPERATION,
        CERTIFICATE_HOLD,
        PRIVILEGE_WITHDRAWN,
        AA_COMPROMISE,
    };
    private static int name2Index(String name) throws IOException {
        for( int i=0; i<NAMES.length; i++ ) {
            if( NAMES[i].equalsIgnoreCase(name) ) {
                return i;
            }
        }
        throw new IOException("Name not recognized by ReasonFlags");
    }
    private boolean[] bitString;
    private boolean isSet(int position) {
        return bitString[position];
    }
    private void set(int position, boolean val) {
        if (position >= bitString.length) {
            boolean[] tmp = new boolean[position+1];
            System.arraycopy(bitString, 0, tmp, 0, bitString.length);
            bitString = tmp;
        }
        bitString[position] = val;
    }
    public ReasonFlags(byte[] reasons) {
        bitString = new BitArray(reasons.length*8, reasons).toBooleanArray();
    }
    public ReasonFlags(boolean[] reasons) {
        this.bitString = reasons;
    }
    public ReasonFlags(BitArray reasons) {
        this.bitString = reasons.toBooleanArray();
    }
    public ReasonFlags(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        this.bitString = derVal.getUnalignedBitString(true).toBooleanArray();
    }
    public ReasonFlags(DerValue derVal) throws IOException {
        this.bitString = derVal.getUnalignedBitString(true).toBooleanArray();
    }
    public boolean[] getFlags() {
        return bitString;
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Boolean)) {
            throw new IOException("Attribute must be of type Boolean.");
        }
        boolean val = ((Boolean)obj).booleanValue();
        set(name2Index(name), val);
    }
    public Object get(String name) throws IOException {
        return Boolean.valueOf(isSet(name2Index(name)));
    }
    public void delete(String name) throws IOException {
        set(name, Boolean.FALSE);
    }
    public String toString() {
        String s = "Reason Flags [\n";
        try {
            if (isSet(0)) s += "  Unused\n";
            if (isSet(1)) s += "  Key Compromise\n";
            if (isSet(2)) s += "  CA Compromise\n";
            if (isSet(3)) s += "  Affiliation_Changed\n";
            if (isSet(4)) s += "  Superseded\n";
            if (isSet(5)) s += "  Cessation Of Operation\n";
            if (isSet(6)) s += "  Certificate Hold\n";
            if (isSet(7)) s += "  Privilege Withdrawn\n";
            if (isSet(8)) s += "  AA Compromise\n";
        } catch (ArrayIndexOutOfBoundsException ex) {}
        s += "]\n";
        return (s);
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putTruncatedUnalignedBitString(new BitArray(this.bitString));
    }
    public Enumeration<String> getElements () {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        for( int i=0; i<NAMES.length; i++ ) {
            elements.addElement(NAMES[i]);
        }
        return (elements.elements());
    }
}
