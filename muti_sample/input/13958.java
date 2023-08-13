public class Des3 {
    private static final Des3DkCrypto CRYPTO = new Des3DkCrypto();
    private Des3() {
    }
    public static byte[] stringToKey(char[] chars)
        throws GeneralSecurityException {
        return CRYPTO.stringToKey(chars);
    }
    public static byte[] parityFix(byte[] value)
        throws GeneralSecurityException {
        return CRYPTO.parityFix(value);
    }
    public static int getChecksumLength() {
        return CRYPTO.getChecksumLength();
    }
    public static byte[] calculateChecksum(byte[] baseKey, int usage,
        byte[] input, int start, int len) throws GeneralSecurityException {
            return CRYPTO.calculateChecksum(baseKey, usage, input, start, len);
    }
    public static byte[] encrypt(byte[] baseKey, int usage,
        byte[] ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
            return CRYPTO.encrypt(baseKey, usage, ivec, null ,
                plaintext, start, len);
    }
    public static byte[] encryptRaw(byte[] baseKey, int usage,
        byte[] ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        return CRYPTO.encryptRaw(baseKey, usage, ivec, plaintext, start, len);
    }
    public static byte[] decrypt(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len)
        throws GeneralSecurityException {
        return CRYPTO.decrypt(baseKey, usage, ivec, ciphertext, start, len);
    }
    public static byte[] decryptRaw(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len)
        throws GeneralSecurityException {
        return CRYPTO.decryptRaw(baseKey, usage, ivec, ciphertext, start, len);
    }
};
