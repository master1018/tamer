public class LastReqEntry {
    private int lrType;
    private KerberosTime lrValue;
    private LastReqEntry() {
    }
    public LastReqEntry(int Type, KerberosTime time){
        lrType = Type;
        lrValue = time;
    }
    public LastReqEntry(DerValue encoding) throws Asn1Exception, IOException {
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        DerValue der;
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x00){
            lrType = der.getData().getBigInteger().intValue();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        lrValue = KerberosTime.parse(encoding.getData(), (byte)0x01, false);
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(lrType);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), temp);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), lrValue.asn1Encode());
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public Object clone() {
        LastReqEntry newEntry = new LastReqEntry();
        newEntry.lrType = lrType;
        newEntry.lrValue = (KerberosTime)lrValue.clone();
        return newEntry;
    }
}
