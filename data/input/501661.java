public class DESKeySpec implements KeySpec {
    public static final int DES_KEY_LEN = 8;
    private final byte[] key;
    private static final byte[][] SEMIWEAKS = {
                {(byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01,
                 (byte) 0xF1, (byte) 0x01, (byte) 0xF1, (byte) 0x01},
                {(byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xE0,
                 (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1},
                {(byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x1F,
                 (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE,
                 (byte) 0x0E, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE},
                {(byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x1F,
                 (byte) 0xF1, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xE0,
                 (byte) 0x0E, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1},
                {(byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE,
                 (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE},
                {(byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01,
                 (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01},
                {(byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x1F,
                 (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01,
                 (byte) 0x0E, (byte) 0x01, (byte) 0x0E, (byte) 0x01},
                {(byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE,
                 (byte) 0xF1, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE},
                {(byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0,
                 (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1},
                {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, 
                 (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01},
                {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                 (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE},
                {(byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xE0,
                 (byte) 0xF1, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1},
                {(byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x1F,
                 (byte) 0x0E, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E},
                };
    public DESKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }
    public DESKeySpec(byte[] key, int offset)
                throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException(Messages.getString("crypto.2F")); 
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.40")); 
        }
        this.key = new byte[DES_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, DES_KEY_LEN);
    }
    public byte[] getKey() {
        byte[] result = new byte[DES_KEY_LEN];
        System.arraycopy(this.key, 0, result, 0, DES_KEY_LEN);
        return result;
    }
    public static boolean isParityAdjusted(byte[] key, int offset)
            throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException(Messages.getString("crypto.2F")); 
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.40")); 
        }
        int byteKey = 0;
        for (int i = offset; i < DES_KEY_LEN; i++) {
            byteKey = key[i];
            byteKey ^= byteKey >> 1;
            byteKey ^= byteKey >> 2;
            byteKey ^= byteKey >> 4;
            if ((byteKey & 1) == 0) {
                return false;
            }
        }
        return true;
    }
    public static boolean isWeak(byte[] key, int offset)
              throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException(Messages.getString("crypto.2F")); 
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.40")); 
        }
        I:
        for (int i=0; i<SEMIWEAKS.length; i++) {
            for (int j=0; j<DES_KEY_LEN; j++) {
                if (SEMIWEAKS[i][j] != key[offset+j]) {
                    continue I;
                }
            }
            return true;
        }
        return false;
    }
}
