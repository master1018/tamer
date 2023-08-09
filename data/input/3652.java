public class PAEncTSEnc {
    public KerberosTime pATimeStamp;
    public Integer pAUSec; 
    public PAEncTSEnc(
                      KerberosTime new_pATimeStamp,
                      Integer new_pAUSec
                          ) {
        pATimeStamp = new_pATimeStamp;
        pAUSec = new_pAUSec;
    }
    public PAEncTSEnc() {
        KerberosTime now = new KerberosTime(KerberosTime.NOW);
        pATimeStamp = now;
        pAUSec = new Integer(now.getMicroSeconds());
    }
    public PAEncTSEnc(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der;
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        pATimeStamp = KerberosTime.parse(encoding.getData(), (byte)0x00, false);
        if (encoding.getData().available() > 0) {
            der = encoding.getData().getDerValue();
            if ((der.getTag() & 0x1F) == 0x01) {
                pAUSec = new Integer(der.getData().getBigInteger().intValue());
            }
            else throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), pATimeStamp.asn1Encode());
        if (pAUSec != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(pAUSec.intValue()));
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), temp);
        }
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
}
