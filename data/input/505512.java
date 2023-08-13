public class DESedeKeySpec implements KeySpec {
    public static final int DES_EDE_KEY_LEN = 24;
    private final byte[] key;
    public DESedeKeySpec(byte[] key)
                throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException(Messages.getString("crypto.2F")); 
        }
        if (key.length < DES_EDE_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.30")); 
        }
        this.key = new byte[DES_EDE_KEY_LEN];
        System.arraycopy(key, 0, this.key, 0, DES_EDE_KEY_LEN);
    }
    public DESedeKeySpec(byte[] key, int offset)
                throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException(Messages.getString("crypto.2F")); 
        }
        if (key.length - offset < DES_EDE_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.30")); 
        }
        this.key = new byte[DES_EDE_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, DES_EDE_KEY_LEN);
    }
    public byte[] getKey() {
        byte[] result = new byte [DES_EDE_KEY_LEN];
        System.arraycopy(this.key, 0, result, 0, DES_EDE_KEY_LEN);
        return result;
    }
    public static boolean isParityAdjusted(byte[] key, int offset)
                throws InvalidKeyException {
        if (key.length - offset < DES_EDE_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.30")); 
        }
        for (int i=offset; i<DES_EDE_KEY_LEN+offset; i++) {
            int b = key[i];
            if ((((b & 1) + ((b & 2) >> 1) + ((b & 4) >> 2)
                + ((b & 8) >> 3) + ((b & 16) >> 4) + ((b & 32) >> 5)
                + ((b & 64) >> 6)) & 1) == ((b & 128) >> 7)) {
                return false;
            }
        }
        return true;
    }
}
