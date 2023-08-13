public class ETypeInfo {
    private int etype;
    private String salt = null;
    private static final byte TAG_TYPE = 0;
    private static final byte TAG_VALUE = 1;
    private ETypeInfo() {
    }
    public ETypeInfo(int etype, String salt) {
        this.etype = etype;
        this.salt = salt;
    }
    public Object clone() {
        return new ETypeInfo(etype, salt);
    }
    public ETypeInfo(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der = null;
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x00) {
            this.etype = der.getData().getBigInteger().intValue();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        if (encoding.getData().available() > 0) {
            der = encoding.getData().getDerValue();
            if ((der.getTag() & 0x1F) == 0x01) {
                byte[] saltBytes = der.getData().getOctetString();
                if (KerberosString.MSNAME) {
                    this.salt = new String(saltBytes, "UTF8");
                } else {
                    this.salt = new String(saltBytes);
                }
            }
        }
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(etype);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true,
                                        TAG_TYPE), temp);
        if (salt != null) {
            temp = new DerOutputStream();
            if (KerberosString.MSNAME) {
                temp.putOctetString(salt.getBytes("UTF8"));
            } else {
                temp.putOctetString(salt.getBytes());
            }
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true,
                                        TAG_VALUE), temp);
        }
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public int getEType() {
        return etype;
    }
    public String getSalt() {
        return salt;
    }
}
