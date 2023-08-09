public class KeyRep implements Serializable {
    private static final long serialVersionUID = -4757683898830641853L;
    public static enum Type {
        SECRET,
        PUBLIC,
        PRIVATE,
    }
    private static final String PKCS8 = "PKCS#8";
    private static final String X509 = "X.509";
    private static final String RAW = "RAW";
    private Type type;
    private String algorithm;
    private String format;
    private byte[] encoded;
    public KeyRep(Type type, String algorithm,
                String format, byte[] encoded) {
        if (type == null || algorithm == null ||
            format == null || encoded == null) {
            throw new NullPointerException("invalid null input(s)");
        }
        this.type = type;
        this.algorithm = algorithm;
        this.format = format.toUpperCase();
        this.encoded = encoded.clone();
    }
    protected Object readResolve() throws ObjectStreamException {
        try {
            if (type == Type.SECRET && RAW.equals(format)) {
                return new SecretKeySpec(encoded, algorithm);
            } else if (type == Type.PUBLIC && X509.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePublic(new X509EncodedKeySpec(encoded));
            } else if (type == Type.PRIVATE && PKCS8.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            } else {
                throw new NotSerializableException
                        ("unrecognized type/format combination: " +
                        type + "/" + format);
            }
        } catch (NotSerializableException nse) {
            throw nse;
        } catch (Exception e) {
            NotSerializableException nse = new NotSerializableException
                                        ("java.security.Key: " +
                                        "[" + type + "] " +
                                        "[" + algorithm + "] " +
                                        "[" + format + "]");
            nse.initCause(e);
            throw nse;
        }
    }
}
