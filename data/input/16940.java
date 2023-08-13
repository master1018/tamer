class CipherBlockChaining extends FeedbackCipher  {
    protected byte[] r;
    private byte[] k;
    private byte[] rSave = null;
    CipherBlockChaining(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
        k = new byte[blockSize];
        r = new byte[blockSize];
    }
    String getFeedback() {
        return "CBC";
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
        System.arraycopy(iv, 0, r, 0, blockSize);
    }
    void save() {
        if (rSave == null) {
            rSave = new byte[blockSize];
        }
        System.arraycopy(r, 0, rSave, 0, blockSize);
    }
    void restore() {
        System.arraycopy(rSave, 0, r, 0, blockSize);
    }
    void encrypt(byte[] plain, int plainOffset, int plainLen,
                 byte[] cipher, int cipherOffset)
    {
        int i;
        int endIndex = plainOffset + plainLen;
        for (; plainOffset < endIndex;
             plainOffset+=blockSize, cipherOffset += blockSize) {
            for (i=0; i<blockSize; i++) {
                k[i] = (byte)(plain[i+plainOffset] ^ r[i]);
            }
            embeddedCipher.encryptBlock(k, 0, cipher, cipherOffset);
            System.arraycopy(cipher, cipherOffset, r, 0, blockSize);
        }
    }
    void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                 byte[] plain, int plainOffset)
    {
        int i;
        byte[] cipherOrig=null;
        int endIndex = cipherOffset + cipherLen;
        if (cipher==plain && (cipherOffset >= plainOffset)
            && ((cipherOffset - plainOffset) < blockSize)) {
            cipherOrig = (byte[])cipher.clone();
        }
        for (; cipherOffset < endIndex;
             cipherOffset += blockSize, plainOffset += blockSize) {
            embeddedCipher.decryptBlock(cipher, cipherOffset, k, 0);
            for (i = 0; i < blockSize; i++) {
                plain[i+plainOffset] = (byte)(k[i] ^ r[i]);
            }
            if (cipherOrig==null) {
                System.arraycopy(cipher, cipherOffset, r, 0, blockSize);
            } else {
                System.arraycopy(cipherOrig, cipherOffset, r, 0, blockSize);
            }
        }
    }
}
