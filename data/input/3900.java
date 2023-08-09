public class KDCOptions extends KerberosFlags {
    public final int KDC_OPT_PROXIABLE = 0x10000000;
    public final int KDC_OPT_RENEWABLE_OK = 0x00000010;
    public final int KDC_OPT_FORWARDABLE = 0x40000000;
    public static final int RESERVED        = 0;
    public static final int FORWARDABLE     = 1;
    public static final int FORWARDED       = 2;
    public static final int PROXIABLE       = 3;
    public static final int PROXY           = 4;
    public static final int ALLOW_POSTDATE  = 5;
    public static final int POSTDATED       = 6;
    public static final int UNUSED7         = 7;
    public static final int RENEWABLE       = 8;
    public static final int UNUSED9         = 9;
    public static final int UNUSED10        = 10;
    public static final int UNUSED11        = 11;
    public static final int RENEWABLE_OK    = 27;
    public static final int ENC_TKT_IN_SKEY = 28;
    public static final int RENEW           = 30;
    public static final int VALIDATE        = 31;
    private boolean DEBUG = Krb5.DEBUG;
    public KDCOptions() {
        super(Krb5.KDC_OPTS_MAX + 1);
        setDefault();
    }
    public KDCOptions(int size, byte[] data) throws Asn1Exception {
        super(size, data);
        if ((size > data.length * BITS_PER_UNIT) || (size > Krb5.KDC_OPTS_MAX + 1))
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
    }
    public KDCOptions(boolean[] data) throws Asn1Exception {
        super(data);
        if (data.length > Krb5.KDC_OPTS_MAX + 1) {
            throw new Asn1Exception(Krb5.BITSTRING_BAD_LENGTH);
        }
    }
    public KDCOptions(DerValue encoding) throws Asn1Exception, IOException {
        this(encoding.getUnalignedBitString(true).toBooleanArray());
    }
    public KDCOptions(byte[] options) {
        super(options.length * BITS_PER_UNIT, options);
    }
    public static KDCOptions parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new KDCOptions(subDer);
        }
    }
    public void set(int option, boolean value) throws ArrayIndexOutOfBoundsException {
        super.set(option, value);
    }
    public boolean get(int option) throws ArrayIndexOutOfBoundsException {
        return super.get(option);
    }
    private void setDefault() {
        try {
            Config config = Config.getInstance();
            int options =config.getDefaultIntValue("kdc_default_options",
                    "libdefaults");
            if ((options & RENEWABLE_OK) == RENEWABLE_OK) {
                set(RENEWABLE_OK, true);
            } else {
                if (config.getDefaultBooleanValue("renewable", "libdefaults")) {
                    set(RENEWABLE_OK, true);
                }
            }
            if ((options & PROXIABLE) == PROXIABLE) {
                set(PROXIABLE, true);
            } else {
                if (config.getDefaultBooleanValue("proxiable", "libdefaults")) {
                    set(PROXIABLE, true);
                }
            }
            if ((options & FORWARDABLE) == FORWARDABLE) {
                set(FORWARDABLE, true);
            } else {
                if (config.getDefaultBooleanValue("forwardable", "libdefaults")) {
                    set(FORWARDABLE, true);
                }
            }
        } catch (KrbException e) {
            if (DEBUG) {
                System.out.println("Exception in getting default values for " +
                        "KDC Options from the configuration ");
                e.printStackTrace();
            }
        }
    }
}
