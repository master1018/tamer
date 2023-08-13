final public class SortControl extends BasicControl {
    public static final String OID = "1.2.840.113556.1.4.473";
    private static final long serialVersionUID = -1965961680233330744L;
    public SortControl(String sortBy, boolean criticality) throws IOException {
        super(OID, criticality, null);
        super.value = setEncodedValue(new SortKey[]{ new SortKey(sortBy) });
    }
    public SortControl(String[] sortBy, boolean criticality)
        throws IOException {
        super(OID, criticality, null);
        SortKey[] sortKeys = new SortKey[sortBy.length];
        for (int i = 0; i < sortBy.length; i++) {
            sortKeys[i] = new SortKey(sortBy[i]);
        }
        super.value = setEncodedValue(sortKeys);
    }
    public SortControl(SortKey[] sortBy, boolean criticality)
        throws IOException {
        super(OID, criticality, null);
        super.value = setEncodedValue(sortBy);
    }
    private byte[] setEncodedValue(SortKey[] sortKeys) throws IOException {
        BerEncoder ber = new BerEncoder(30 * sortKeys.length + 10);
        String matchingRule;
        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
        for (int i = 0; i < sortKeys.length; i++) {
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeString(sortKeys[i].getAttributeID(), true); 
            if ((matchingRule = sortKeys[i].getMatchingRuleID()) != null) {
                ber.encodeString(matchingRule, (Ber.ASN_CONTEXT | 0), true);
            }
            if (! sortKeys[i].isAscending()) {
                ber.encodeBoolean(true, (Ber.ASN_CONTEXT | 1));
            }
            ber.endSeq();
        }
        ber.endSeq();
        return ber.getTrimmedBuf();
    }
}
