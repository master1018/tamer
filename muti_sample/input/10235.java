public class APRep {
    public int pvno;
    public int msgType;
    public EncryptedData encPart;
    public APRep(EncryptedData new_encPart) {
        pvno = Krb5.PVNO;
        msgType = Krb5.KRB_AP_REP;
        encPart = new_encPart;
    }
    public APRep(byte[] data) throws Asn1Exception,
            KrbApErrException, IOException {
        init(new DerValue(data));
    }
    public APRep(DerValue encoding) throws Asn1Exception,
            KrbApErrException, IOException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception,
            KrbApErrException, IOException {
        if (((encoding.getTag() & (byte) (0x1F)) != Krb5.KRB_AP_REP)
                || (encoding.isApplication() != true)
                || (encoding.isConstructed() != true)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        DerValue der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        DerValue subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte) 0x1F) != (byte) 0x00) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        pvno = subDer.getData().getBigInteger().intValue();
        if (pvno != Krb5.PVNO) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_BADVERSION);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte) 0x1F) != (byte) 0x01) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        msgType = subDer.getData().getBigInteger().intValue();
        if (msgType != Krb5.KRB_AP_REP) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MSG_TYPE);
        }
        encPart = EncryptedData.parse(der.getData(), (byte) 0x02, false);
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(pvno));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x00), temp);
        temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(msgType));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x01), temp);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x02), encPart.asn1Encode());
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        DerOutputStream aprep = new DerOutputStream();
        aprep.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte) 0x0F), temp);
        return aprep.toByteArray();
    }
}
