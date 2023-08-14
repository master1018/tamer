public class DESKeySpec implements java.security.spec.KeySpec {
    public static final int DES_KEY_LEN = 8;
    private byte[] key;
    private static final byte[][] WEAK_KEYS = {
        { (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
          (byte)0x01, (byte)0x01, (byte)0x01 },
        { (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE,
          (byte)0xFE, (byte)0xFE, (byte)0xFE },
        { (byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x0E,
          (byte)0x0E, (byte)0x0E, (byte)0x0E },
        { (byte)0xE0, (byte)0xE0, (byte)0xE0, (byte)0xE0, (byte)0xF1,
          (byte)0xF1, (byte)0xF1, (byte)0xF1 },
        { (byte)0x01, (byte)0xFE, (byte)0x01, (byte)0xFE, (byte)0x01,
          (byte)0xFE, (byte)0x01, (byte)0xFE },
        { (byte)0x1F, (byte)0xE0, (byte)0x1F, (byte)0xE0, (byte)0x0E,
          (byte)0xF1, (byte)0x0E, (byte)0xF1 },
        { (byte)0x01, (byte)0xE0, (byte)0x01, (byte)0xE0, (byte)0x01,
          (byte)0xF1, (byte)0x01, (byte)0xF1 },
        { (byte)0x1F, (byte)0xFE, (byte)0x1F, (byte)0xFE, (byte)0x0E,
          (byte)0xFE, (byte)0x0E, (byte)0xFE },
        { (byte)0x01, (byte)0x1F, (byte)0x01, (byte)0x1F, (byte)0x01,
          (byte)0x0E, (byte)0x01, (byte)0x0E },
        { (byte)0xE0, (byte)0xFE, (byte)0xE0, (byte)0xFE, (byte)0xF1,
          (byte)0xFE, (byte)0xF1, (byte)0xFE },
        { (byte)0xFE, (byte)0x01, (byte)0xFE, (byte)0x01, (byte)0xFE,
          (byte)0x01, (byte)0xFE, (byte)0x01 },
        { (byte)0xE0, (byte)0x1F, (byte)0xE0, (byte)0x1F, (byte)0xF1,
          (byte)0x0E, (byte)0xF1, (byte)0x0E },
        { (byte)0xE0, (byte)0x01, (byte)0xE0, (byte)0x01, (byte)0xF1,
          (byte)0x01, (byte)0xF1, (byte)0x01 },
        { (byte)0xFE, (byte)0x1F, (byte)0xFE, (byte)0x1F, (byte)0xFE,
          (byte)0x0E, (byte)0xFE, (byte)0x0E },
        { (byte)0x1F, (byte)0x01, (byte)0x1F, (byte)0x01, (byte)0x0E,
          (byte)0x01, (byte)0x0E, (byte)0x01 },
        { (byte)0xFE, (byte)0xE0, (byte)0xFE, (byte)0xE0, (byte)0xFE,
          (byte)0xF1, (byte)0xFE, (byte)0xF1 }
    };
    public DESKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }
    public DESKeySpec(byte[] key, int offset) throws InvalidKeyException {
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[DES_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, DES_KEY_LEN);
    }
    public byte[] getKey() {
        return (byte[])this.key.clone();
    }
    public static boolean isParityAdjusted(byte[] key, int offset)
        throws InvalidKeyException {
            if (key == null) {
                throw new InvalidKeyException("null key");
            }
            if (key.length - offset < DES_KEY_LEN) {
                throw new InvalidKeyException("Wrong key size");
            }
            for (int i = 0; i < DES_KEY_LEN; i++) {
                int k = Integer.bitCount(key[offset++] & 0xff);
                if ((k & 1) == 0) {
                    return false;
                }
            }
            return true;
    }
    public static boolean isWeak(byte[] key, int offset)
        throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("null key");
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        }
        for (int i = 0; i < WEAK_KEYS.length; i++) {
            boolean found = true;
            for (int j = 0; j < DES_KEY_LEN && found == true; j++) {
                if (WEAK_KEYS[i][j] != key[j+offset]) {
                    found = false;
                }
            }
            if (found == true) {
                return found;
            }
        }
        return false;
    }
}
