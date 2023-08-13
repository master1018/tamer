public class SecretKeySpec implements KeySpec, SecretKey {
    private static final long serialVersionUID = 6577238317307289933L;
    private byte[] key;
    private String algorithm;
    public SecretKeySpec(byte[] key, String algorithm) {
        if (key == null || algorithm == null) {
            throw new IllegalArgumentException("Missing argument");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("Empty key");
        }
        this.key = (byte[])key.clone();
        this.algorithm = algorithm;
    }
    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) {
        if (key == null || algorithm == null) {
            throw new IllegalArgumentException("Missing argument");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("Empty key");
        }
        if (key.length-offset < len) {
            throw new IllegalArgumentException
                ("Invalid offset/length combination");
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("len is negative");
        }
        this.key = new byte[len];
        System.arraycopy(key, offset, this.key, 0, len);
        this.algorithm = algorithm;
    }
    public String getAlgorithm() {
        return this.algorithm;
    }
    public String getFormat() {
        return "RAW";
    }
    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        if (this.algorithm.equalsIgnoreCase("TripleDES"))
            return (retval ^= "desede".hashCode());
        else
            return (retval ^= this.algorithm.toLowerCase().hashCode());
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SecretKey))
            return false;
        String thatAlg = ((SecretKey)obj).getAlgorithm();
        if (!(thatAlg.equalsIgnoreCase(this.algorithm))) {
            if ((!(thatAlg.equalsIgnoreCase("DESede"))
                 || !(this.algorithm.equalsIgnoreCase("TripleDES")))
                && (!(thatAlg.equalsIgnoreCase("TripleDES"))
                    || !(this.algorithm.equalsIgnoreCase("DESede"))))
            return false;
        }
        byte[] thatKey = ((SecretKey)obj).getEncoded();
        return java.util.Arrays.equals(this.key, thatKey);
    }
}
