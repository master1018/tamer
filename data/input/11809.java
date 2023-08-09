public class APReq {
    public int pvno;
    public int msgType;
    public APOptions apOptions;
    public Ticket ticket;
    public EncryptedData authenticator;
    public APReq(
            APOptions new_apOptions,
            Ticket new_ticket,
            EncryptedData new_authenticator) {
        pvno = Krb5.PVNO;
        msgType = Krb5.KRB_AP_REQ;
        apOptions = new_apOptions;
        ticket = new_ticket;
        authenticator = new_authenticator;
    }
    public APReq(byte[] data) throws Asn1Exception, IOException, KrbApErrException, RealmException {
        init(new DerValue(data));
    }
    public APReq(DerValue encoding) throws Asn1Exception, IOException, KrbApErrException, RealmException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception,
            IOException, KrbApErrException, RealmException {
        DerValue der, subDer;
        if (((encoding.getTag() & (byte) 0x1F) != Krb5.KRB_AP_REQ)
                || (encoding.isApplication() != true)
                || (encoding.isConstructed() != true)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
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
        if (msgType != Krb5.KRB_AP_REQ) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MSG_TYPE);
        }
        apOptions = APOptions.parse(der.getData(), (byte) 0x02, false);
        ticket = Ticket.parse(der.getData(), (byte) 0x03, false);
        authenticator = EncryptedData.parse(der.getData(), (byte) 0x04, false);
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
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x02), apOptions.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x03), ticket.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x04), authenticator.asn1Encode());
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        DerOutputStream apreq = new DerOutputStream();
        apreq.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte) 0x0E), temp);
        return apreq.toByteArray();
    }
}
