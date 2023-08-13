public class PAData {
    private int pADataType;
    private byte[] pADataValue = null;
    private static final byte TAG_PATYPE = 1;
    private static final byte TAG_PAVALUE = 2;
    private PAData() {
    }
    public PAData(int new_pADataType, byte[] new_pADataValue) {
        pADataType = new_pADataType;
        if (new_pADataValue != null) {
            pADataValue = new_pADataValue.clone();
        }
    }
    public Object clone() {
        PAData new_pAData = new PAData();
        new_pAData.pADataType = pADataType;
        if (pADataValue != null) {
            new_pAData.pADataValue = new byte[pADataValue.length];
            System.arraycopy(pADataValue, 0, new_pAData.pADataValue,
                             0, pADataValue.length);
        }
        return new_pAData;
    }
    public PAData(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der = null;
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x01) {
            this.pADataType = der.getData().getBigInteger().intValue();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x02) {
            this.pADataValue = der.getData().getOctetString();
        }
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(pADataType);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_PATYPE), temp);
        temp = new DerOutputStream();
        temp.putOctetString(pADataValue);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_PAVALUE), temp);
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public int getType() {
        return pADataType;
    }
    public byte[] getValue() {
        return ((pADataValue == null) ? null : pADataValue.clone());
    }
    public static class SaltAndParams {
        public final String salt;
        public final byte[] params;
        public SaltAndParams(String s, byte[] p) {
            if (s != null && s.isEmpty()) s = null;
            this.salt = s;
            this.params = p;
        }
    }
    public static SaltAndParams getSaltAndParams(int eType, PAData[] pas)
            throws Asn1Exception, KrbException {
        if (pas == null || pas.length == 0) {
            return new SaltAndParams(null, null);
        }
        String paPwSalt = null;
        ETypeInfo2 info2 = null;
        ETypeInfo info = null;
        for (PAData p: pas) {
            if (p.getValue() != null) {
                try {
                    switch (p.getType()) {
                        case Krb5.PA_PW_SALT:
                            paPwSalt = new String(p.getValue(),
                                    KerberosString.MSNAME?"UTF8":"8859_1");
                            break;
                        case Krb5.PA_ETYPE_INFO:
                            DerValue der = new DerValue(p.getValue());
                            while (der.data.available() > 0) {
                                DerValue value = der.data.getDerValue();
                                ETypeInfo tmp = new ETypeInfo(value);
                                if (tmp.getEType() == eType) info = tmp;
                            }
                            break;
                        case Krb5.PA_ETYPE_INFO2:
                            der = new DerValue(p.getValue());
                            while (der.data.available() > 0) {
                                DerValue value = der.data.getDerValue();
                                ETypeInfo2 tmp = new ETypeInfo2(value);
                                if (tmp.getEType() == eType) info2 = tmp;
                            }
                            break;
                    }
                } catch (IOException ioe) {
                }
            }
        }
        if (info2 != null) {
            return new SaltAndParams(info2.getSalt(), info2.getParams());
        } else if (info != null) {
            return new SaltAndParams(info.getSalt(), null);
        }
        return new SaltAndParams(paPwSalt, null);
    }
}
