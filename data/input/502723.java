public class SecretKeySpec implements SecretKey, KeySpec, Serializable {
    private static final long serialVersionUID = 6577238317307289933L;
    private final byte[] key;
    private final String algorithm;
    private final String format = "RAW"; 
    public SecretKeySpec(byte[] key, String algorithm) {
        if (key == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.05")); 
        }
        if (key.length == 0) {
            throw new IllegalArgumentException(Messages.getString("crypto.35")); 
        }
        if (algorithm == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.02")); 
        }
        this.algorithm = algorithm;
        this.key = new byte[key.length];
        System.arraycopy(key, 0, this.key, 0, key.length);
    }
    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) {
        if (key == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.05")); 
        }
        if (key.length == 0) {
            throw new IllegalArgumentException(Messages.getString("crypto.35")); 
        }
        if (len < 0 || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("crypto.36")); 
        }
        if ((key.length - offset < len)) {
            throw new IllegalArgumentException(Messages.getString("crypto.37")); 
        }
        if (algorithm == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.02")); 
        }
        this.algorithm = algorithm;
        this.key = new byte[len];
        System.arraycopy(key, offset, this.key, 0, len);
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getFormat() {
        return format;
    }
    public byte[] getEncoded() {
        byte[] result = new byte[key.length];
        System.arraycopy(key, 0, result, 0, key.length);
        return result;
    }
    @Override
    public int hashCode() {
        int result = algorithm.length();
        for (byte element : key) {
            result += element;
        }
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecretKeySpec)) {
            return false;
        }
        SecretKeySpec ks = (SecretKeySpec) obj;
        return (algorithm.equalsIgnoreCase(ks.algorithm))
            && (Arrays.equals(key, ks.key));
    }
}
