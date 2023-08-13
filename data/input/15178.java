public class EncKrbPrivPart {
    public byte[] userData = null;
    public KerberosTime timestamp; 
    public Integer usec; 
    public Integer seqNumber; 
    public HostAddress sAddress; 
    public HostAddress rAddress; 
    public EncKrbPrivPart(
            byte[] new_userData,
            KerberosTime new_timestamp,
            Integer new_usec,
            Integer new_seqNumber,
            HostAddress new_sAddress,
            HostAddress new_rAddress) {
        if (new_userData != null) {
            userData = new_userData.clone();
        }
        timestamp = new_timestamp;
        usec = new_usec;
        seqNumber = new_seqNumber;
        sAddress = new_sAddress;
        rAddress = new_rAddress;
    }
    public EncKrbPrivPart(byte[] data) throws Asn1Exception, IOException {
        init(new DerValue(data));
    }
    public EncKrbPrivPart(DerValue encoding) throws Asn1Exception, IOException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der, subDer;
        if (((encoding.getTag() & (byte) 0x1F) != (byte) 0x1C)
                || (encoding.isApplication() != true)
                || (encoding.isConstructed() != true)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte) 0x1F) == (byte) 0x00) {
            userData = subDer.getData().getOctetString();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        timestamp = KerberosTime.parse(der.getData(), (byte) 0x01, true);
        if ((der.getData().peekByte() & 0x1F) == 0x02) {
            subDer = der.getData().getDerValue();
            usec = new Integer(subDer.getData().getBigInteger().intValue());
        } else {
            usec = null;
        }
        if ((der.getData().peekByte() & 0x1F) == 0x03) {
            subDer = der.getData().getDerValue();
            seqNumber = new Integer(subDer.getData().getBigInteger().intValue());
        } else {
            seqNumber = null;
        }
        sAddress = HostAddress.parse(der.getData(), (byte) 0x04, false);
        if (der.getData().available() > 0) {
            rAddress = HostAddress.parse(der.getData(), (byte) 0x05, true);
        }
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream temp = new DerOutputStream();
        DerOutputStream bytes = new DerOutputStream();
        temp.putOctetString(userData);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x00), temp);
        if (timestamp != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x01), timestamp.asn1Encode());
        }
        if (usec != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(usec.intValue()));
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x02), temp);
        }
        if (seqNumber != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(seqNumber.longValue()));
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x03), temp);
        }
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x04), sAddress.asn1Encode());
        if (rAddress != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x05), rAddress.asn1Encode());
        }
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        bytes = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte) 0x1C), temp);
        return bytes.toByteArray();
    }
}
