public class EDIPartyName {
    private String nameAssigner;
    private String partyName;
    private byte[] encoding;
    public EDIPartyName(String nameAssigner, String partyName) {
        this.nameAssigner = nameAssigner;
        this.partyName = partyName;
    }
    private EDIPartyName(String nameAssigner, String partyName, 
                         byte[] encoding) {
        this.nameAssigner = nameAssigner;
        this.partyName = partyName;
        this.encoding = encoding;
    }
    public String getNameAssigner() {
        return nameAssigner;
    }
    public String getPartyName() {
        return partyName;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(
            new ASN1Type[] {
                new ASN1Explicit(0, DirectoryString.ASN1), 
                new ASN1Explicit(1, DirectoryString.ASN1)
            }) {
        {
            setOptional(0);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new EDIPartyName((String) values[0], (String) values[1],
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            EDIPartyName epn = (EDIPartyName) object;
            values[0] = epn.nameAssigner;
            values[1] = epn.partyName;
        }
    };
}
