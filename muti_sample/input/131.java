final class PCBC extends FeedbackCipher {
    private final byte[] k;
    private byte[] kSave = null;
    PCBC(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
        k = new byte[blockSize];
    }
    String getFeedback() {
        return "PCBC";
    }
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv)
            throws InvalidKeyException {
        if ((key == null) || (iv == null) || (iv.length != blockSize)) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        reset();
        embeddedCipher.init(decrypting, algorithm, key);
    }
    void reset() {
        System.arraycopy(iv, 0, k, 0, blockSize);
    }
    void save() {
        if (kSave == null) {
            kSave = new byte[blockSize];
        }
        System.arraycopy(k, 0, kSave, 0, blockSize);
    }
    void restore() {
        System.arraycopy(kSave, 0, k, 0, blockSize);
    }
    void encrypt(byte[] plain, int plainOffset, int plainLen,
                        byte[] cipher, int cipherOffset)
    {
        int i;
        int endIndex = plainOffset + plainLen;
        for (; plainOffset < endIndex;
             plainOffset += blockSize, cipherOffset += blockSize) {
            for (i=0; i<blockSize; i++) {
                k[i] ^= (byte)(plain[i+plainOffset]);
            }
            embeddedCipher.encryptBlock(k, 0, cipher, cipherOffset);
            for (i = 0; i < blockSize; i++) {
                k[i] = (byte)(plain[i+plainOffset] ^ cipher[i+cipherOffset]);
            }
        }
    }
    void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                        byte[] plain, int plainOffset)
    {
        int i;
        int endIndex = cipherOffset + cipherLen;
        for (; cipherOffset < endIndex;
             plainOffset += blockSize, cipherOffset += blockSize) {
            embeddedCipher.decryptBlock(cipher, cipherOffset,
                                   plain, plainOffset);
            for (i = 0; i < blockSize; i++) {
                plain[i+plainOffset] ^= k[i];
            }
            for (i = 0; i < blockSize; i++) {
                k[i] = (byte)(plain[i+plainOffset] ^ cipher[i+cipherOffset]);
            }
        }
    }
}
