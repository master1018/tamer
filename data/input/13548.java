public class APOptions extends KerberosFlags {
    public APOptions() {
        super(Krb5.AP_OPTS_MAX + 1);
    }
    public APOptions(int oneBit) throws Asn1Exception {
        super(Krb5.AP_OPTS_MAX + 1);
        set(oneBit, true);
    }
    public APOptions(int size, byte[] data) throws Asn1Exception {
        super(size, data);
        if ((size > data.length * BITS_PER_UNIT) || (size > Krb5.AP_OPTS_MAX + 1)) {
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
        }
    }
    public APOptions(boolean[] data) throws Asn1Exception {
        super(data);
        if (data.length > Krb5.AP_OPTS_MAX + 1) {
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
        }
    }
    public APOptions(DerValue encoding) throws IOException, Asn1Exception {
        this(encoding.getUnalignedBitString(true).toBooleanArray());
    }
    public static APOptions parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new APOptions(subDer);
        }
    }
}
