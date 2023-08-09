public class EncAPRepPart {
    public KerberosTime ctime;
    public int cusec;
    EncryptionKey subKey; 
    Integer seqNumber; 
    public EncAPRepPart(
            KerberosTime new_ctime,
            int new_cusec,
            EncryptionKey new_subKey,
            Integer new_seqNumber) {
        ctime = new_ctime;
        cusec = new_cusec;
        subKey = new_subKey;
        seqNumber = new_seqNumber;
    }
    public EncAPRepPart(byte[] data)
            throws Asn1Exception, IOException {
        init(new DerValue(data));
    }
    public EncAPRepPart(DerValue encoding)
            throws Asn1Exception, IOException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der, subDer;
        if (((encoding.getTag() & (byte) 0x1F) != (byte) 0x1B)
                || (encoding.isApplication() != true)
                || (encoding.isConstructed() != true)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        ctime = KerberosTime.parse(der.getData(), (byte) 0x00, true);
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte) 0x1F) == (byte) 0x01) {
            cusec = subDer.getData().getBigInteger().intValue();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if (der.getData().available() > 0) {
            subKey = EncryptionKey.parse(der.getData(), (byte) 0x02, true);
        } else {
            subKey = null;
            seqNumber = null;
        }
        if (der.getData().available() > 0) {
            subDer = der.getData().getDerValue();
            if ((subDer.getTag() & 0x1F) != 0x03) {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
            seqNumber = new Integer(subDer.getData().getBigInteger().intValue());
        } else {
            seqNumber = null;
        }
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        Vector<DerValue> v = new Vector<>();
        DerOutputStream temp = new DerOutputStream();
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x00), ctime.asn1Encode()));
        temp.putInteger(BigInteger.valueOf(cusec));
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x01), temp.toByteArray()));
        if (subKey != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x02), subKey.asn1Encode()));
        }
        if (seqNumber != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(seqNumber.longValue()));
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x03), temp.toByteArray()));
        }
        DerValue der[] = new DerValue[v.size()];
        v.copyInto(der);
        temp = new DerOutputStream();
        temp.putSequence(der);
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.createTag(DerValue.TAG_APPLICATION,
                true, (byte) 0x1B), temp);
        return out.toByteArray();
    }
    public final EncryptionKey getSubKey() {
        return subKey;
    }
    public final Integer getSeqNumber() {
        return seqNumber;
    }
}
