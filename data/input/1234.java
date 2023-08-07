public class DSSPrivateKey extends DSSKey implements PrivateKey, DSAPrivateKey {
    private final BigInteger x;
    private transient String str;
    public DSSPrivateKey(BigInteger p, BigInteger q, BigInteger g, BigInteger x) {
        this(Registry.RAW_ENCODING_ID, p, q, g, x);
    }
    public DSSPrivateKey(int preferredFormat, BigInteger p, BigInteger q, BigInteger g, BigInteger x) {
        super(preferredFormat == Registry.ASN1_ENCODING_ID ? Registry.PKCS8_ENCODING_ID : preferredFormat, p, q, g);
        this.x = x;
    }
    public static DSSPrivateKey valueOf(byte[] k) {
        if (k[0] == Registry.MAGIC_RAW_DSS_PRIVATE_KEY[0]) try {
            return (DSSPrivateKey) new DSSKeyPairRawCodec().decodePrivateKey(k);
        } catch (IllegalArgumentException ignored) {
        }
        return (DSSPrivateKey) new DSSKeyPairPKCS8Codec().decodePrivateKey(k);
    }
    public BigInteger getX() {
        return x;
    }
    public byte[] getEncoded(int format) {
        byte[] result;
        switch(format) {
            case IKeyPairCodec.RAW_FORMAT:
                result = new DSSKeyPairRawCodec().encodePrivateKey(this);
                break;
            case IKeyPairCodec.PKCS8_FORMAT:
                result = new DSSKeyPairPKCS8Codec().encodePrivateKey(this);
                break;
            default:
                throw new IllegalArgumentException("Unsupported encoding format: " + format);
        }
        return result;
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof DSAPrivateKey)) return false;
        DSAPrivateKey that = (DSAPrivateKey) obj;
        return super.equals(that) && x.equals(that.getX());
    }
    public String toString() {
        if (str == null) {
            String ls = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
            str = new StringBuilder(this.getClass().getName()).append("(").append(super.toString()).append(",").append(ls).append("x=0x").append(Configuration.DEBUG ? x.toString(16) : "**...*").append(ls).append(")").toString();
        }
        return str;
    }
}
