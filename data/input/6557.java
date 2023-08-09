public class Des3DkCrypto extends DkCrypto {
    private static final byte[] ZERO_IV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};
    public Des3DkCrypto() {
    }
    protected int getKeySeedLength() {
        return 168;   
    }
    public byte[] stringToKey(char[] salt) throws GeneralSecurityException {
        byte[] saltUtf8 = null;
        try {
            saltUtf8 = charToUtf8(salt);
            return stringToKey(saltUtf8, null);
        } finally {
            if (saltUtf8 != null) {
                Arrays.fill(saltUtf8, (byte)0);
            }
        }
    }
    private byte[] stringToKey(byte[] secretAndSalt, byte[] opaque)
        throws GeneralSecurityException {
        if (opaque != null && opaque.length > 0) {
            throw new RuntimeException("Invalid parameter to stringToKey");
        }
        byte[] tmpKey = randomToKey(nfold(secretAndSalt, getKeySeedLength()));
        return dk(tmpKey, KERBEROS_CONSTANT);
    }
    public byte[] parityFix(byte[] value)
        throws GeneralSecurityException {
        setParityBit(value);
        return value;
    }
    protected byte[] randomToKey(byte[] in) {
        if (in.length != 21) {
            throw new IllegalArgumentException("input must be 168 bits");
        }
        byte[] one = keyCorrection(des3Expand(in, 0, 7));
        byte[] two = keyCorrection(des3Expand(in, 7, 14));
        byte[] three = keyCorrection(des3Expand(in, 14, 21));
        byte[] key = new byte[24];
        System.arraycopy(one, 0, key, 0, 8);
        System.arraycopy(two, 0, key, 8, 8);
        System.arraycopy(three, 0, key, 16, 8);
        return key;
    }
    private static byte[] keyCorrection(byte[] key) {
        try {
            if (DESKeySpec.isWeak(key, 0)) {
                key[7] = (byte)(key[7] ^ 0xF0);
            }
        } catch (InvalidKeyException ex) {
        }
        return key;
    }
    private static byte[] des3Expand(byte[] input, int start, int end) {
        if ((end - start) != 7)
            throw new IllegalArgumentException(
                "Invalid length of DES Key Value:" + start + "," + end);
        byte[] result = new byte[8];
        byte last = 0;
        System.arraycopy(input, start, result, 0, 7);
        byte posn = 0;
        for (int i = start; i < end; i++) {
            byte bit = (byte) (input[i]&0x01);
            if (debug) {
                System.out.println(i + ": " + Integer.toHexString(input[i]) +
                    " bit= " + Integer.toHexString(bit));
            }
            ++posn;
            if (bit != 0) {
                last |= (bit<<posn);
            }
        }
        if (debug) {
            System.out.println("last: " + Integer.toHexString(last));
        }
        result[7] = last;
        setParityBit(result);
        return result;
    }
    private static void setParityBit(byte[] key) {
        for (int i = 0; i < key.length; i++) {
            int b = key[i] & 0xfe;
            b |= (Integer.bitCount(b) & 1) ^ 1;
            key[i] = (byte) b;
        }
    }
    protected Cipher getCipher(byte[] key, byte[] ivec, int mode)
        throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("desede");
        KeySpec spec = new DESedeKeySpec(key, 0);
        SecretKey secretKey = factory.generateSecret(spec);
        if (ivec == null) {
            ivec = ZERO_IV;
        }
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        IvParameterSpec encIv = new IvParameterSpec(ivec, 0, ivec.length);
        cipher.init(mode, secretKey, encIv);
        return cipher;
    }
    public int getChecksumLength() {
        return 20;  
    }
    protected byte[] getHmac(byte[] key, byte[] msg)
        throws GeneralSecurityException {
        SecretKey keyKi = new SecretKeySpec(key, "HmacSHA1");
        Mac m = Mac.getInstance("HmacSHA1");
        m.init(keyKi);
        return m.doFinal(msg);
    }
}
