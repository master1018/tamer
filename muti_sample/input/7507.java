public class TicketFlags extends KerberosFlags {
    public TicketFlags() {
        super(Krb5.TKT_OPTS_MAX + 1);
    }
    public TicketFlags (boolean[] flags) throws Asn1Exception {
        super(flags);
        if (flags.length > Krb5.TKT_OPTS_MAX + 1) {
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
        }
    }
    public TicketFlags(int size, byte[] data) throws Asn1Exception {
        super(size, data);
        if ((size > data.length * BITS_PER_UNIT) || (size > Krb5.TKT_OPTS_MAX + 1))
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
    }
    public TicketFlags(DerValue encoding) throws IOException, Asn1Exception {
        this(encoding.getUnalignedBitString(true).toBooleanArray());
    }
    public static TicketFlags parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        else {
            DerValue subDer = der.getData().getDerValue();
            return new TicketFlags(subDer);
        }
    }
    public Object clone() {
        try {
            return new TicketFlags(this.toBooleanArray());
        }
        catch (Exception e) {
            return null;
        }
    }
    public boolean match(LoginOptions options) {
        boolean matched = false;
        if (this.get(Krb5.TKT_OPTS_FORWARDABLE) == (options.get(KDCOptions.FORWARDABLE))) {
            if (this.get(Krb5.TKT_OPTS_PROXIABLE) == (options.get(KDCOptions.PROXIABLE))) {
                if (this.get(Krb5.TKT_OPTS_RENEWABLE) == (options.get(KDCOptions.RENEWABLE))) {
                    matched = true;
                }
            }
        }
        return matched;
    }
    public boolean match(TicketFlags flags) {
        boolean matched = true;
        for (int i = 0; i <= Krb5.TKT_OPTS_MAX; i++) {
            if (this.get(i) != flags.get(i)) {
                return false;
            }
        }
        return matched;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean[] flags = toBooleanArray();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] == true) {
                switch (i) {
                case 0:
                    sb.append("RESERVED;");
                    break;
                case 1:
                    sb.append("FORWARDABLE;");
                    break;
                case 2:
                    sb.append("FORWARDED;");
                    break;
                case 3:
                    sb.append("PROXIABLE;");
                    break;
                case 4:
                    sb.append("PROXY;");
                    break;
                case 5:
                    sb.append("MAY-POSTDATE;");
                    break;
                case 6:
                    sb.append("POSTDATED;");
                    break;
                case 7:
                    sb.append("INVALID;");
                    break;
                case 8:
                    sb.append("RENEWABLE;");
                    break;
                case 9:
                    sb.append("INITIAL;");
                    break;
                case 10:
                    sb.append("PRE-AUTHENT;");
                    break;
                case 11:
                    sb.append("HW-AUTHENT;");
                    break;
                }
            }
        }
        String result = sb.toString();
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
