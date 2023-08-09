public class EncKrbCredPart {
    public KrbCredInfo[] ticketInfo = null;
    public KerberosTime timeStamp; 
    private Integer nonce; 
    private Integer usec; 
    private HostAddress sAddress; 
    private HostAddresses rAddress; 
    public EncKrbCredPart(
            KrbCredInfo[] new_ticketInfo,
            KerberosTime new_timeStamp,
            Integer new_usec,
            Integer new_nonce,
            HostAddress new_sAddress,
            HostAddresses new_rAddress) throws IOException {
        if (new_ticketInfo != null) {
            ticketInfo = new KrbCredInfo[new_ticketInfo.length];
            for (int i = 0; i < new_ticketInfo.length; i++) {
                if (new_ticketInfo[i] == null) {
                    throw new IOException("Cannot create a EncKrbCredPart");
                } else {
                    ticketInfo[i] = (KrbCredInfo) new_ticketInfo[i].clone();
                }
            }
        }
        timeStamp = new_timeStamp;
        usec = new_usec;
        nonce = new_nonce;
        sAddress = new_sAddress;
        rAddress = new_rAddress;
    }
    public EncKrbCredPart(byte[] data) throws Asn1Exception,
            IOException, RealmException {
        init(new DerValue(data));
    }
    public EncKrbCredPart(DerValue encoding) throws Asn1Exception,
            IOException, RealmException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception,
            IOException, RealmException {
        DerValue der, subDer;
        nonce = null;
        timeStamp = null;
        usec = null;
        sAddress = null;
        rAddress = null;
        if (((encoding.getTag() & (byte) 0x1F) != (byte) 0x1D)
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
            DerValue derValues[] = subDer.getData().getSequence(1);
            ticketInfo = new KrbCredInfo[derValues.length];
            for (int i = 0; i < derValues.length; i++) {
                ticketInfo[i] = new KrbCredInfo(derValues[i]);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if (der.getData().available() > 0) {
            if (((byte) (der.getData().peekByte()) & (byte) 0x1F) == (byte) 0x01) {
                subDer = der.getData().getDerValue();
                nonce = new Integer(subDer.getData().getBigInteger().intValue());
            }
        }
        if (der.getData().available() > 0) {
            timeStamp = KerberosTime.parse(der.getData(), (byte) 0x02, true);
        }
        if (der.getData().available() > 0) {
            if (((byte) (der.getData().peekByte()) & (byte) 0x1F) == (byte) 0x03) {
                subDer = der.getData().getDerValue();
                usec = new Integer(subDer.getData().getBigInteger().intValue());
            }
        }
        if (der.getData().available() > 0) {
            sAddress = HostAddress.parse(der.getData(), (byte) 0x04, true);
        }
        if (der.getData().available() > 0) {
            rAddress = HostAddresses.parse(der.getData(), (byte) 0x05, true);
        }
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        DerValue[] tickets = new DerValue[ticketInfo.length];
        for (int i = 0; i < ticketInfo.length; i++) {
            tickets[i] = new DerValue(ticketInfo[i].asn1Encode());
        }
        temp.putSequence(tickets);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x00), temp);
        if (nonce != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(nonce.intValue()));
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x01), temp);
        }
        if (timeStamp != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x02), timeStamp.asn1Encode());
        }
        if (usec != null) {
            temp = new DerOutputStream();
            temp.putInteger(BigInteger.valueOf(usec.intValue()));
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x03), temp);
        }
        if (sAddress != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x04), sAddress.asn1Encode());
        }
        if (rAddress != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x05), rAddress.asn1Encode());
        }
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        bytes = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_APPLICATION,
                true, (byte) 0x1D), temp);
        return bytes.toByteArray();
    }
}
