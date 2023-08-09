public class DESedeKeySpec implements java.security.spec.KeySpec {
    public static final int DES_EDE_KEY_LEN = 24;
    private byte[] key;
    public DESedeKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }
    public DESedeKeySpec(byte[] key, int offset) throws InvalidKeyException {
        if (key.length - offset < 24) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[24];
        System.arraycopy(key, offset, this.key, 0, 24);
    }
    public byte[] getKey() {
        return (byte[])this.key.clone();
    }
    public static boolean isParityAdjusted(byte[] key, int offset)
        throws InvalidKeyException {
            if (key.length - offset < 24) {
                throw new InvalidKeyException("Wrong key size");
            }
            if (DESKeySpec.isParityAdjusted(key, offset) == false
                || DESKeySpec.isParityAdjusted(key, offset + 8) == false
                || DESKeySpec.isParityAdjusted(key, offset + 16) == false) {
                return false;
            }
            return true;
    }
}
