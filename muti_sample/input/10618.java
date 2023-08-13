abstract class FeedbackCipher {
    final SymmetricCipher embeddedCipher;
    final int blockSize;
    byte[] iv;
    FeedbackCipher(SymmetricCipher embeddedCipher) {
        this.embeddedCipher = embeddedCipher;
        blockSize = embeddedCipher.getBlockSize();
    }
    final SymmetricCipher getEmbeddedCipher() {
        return embeddedCipher;
    }
    final int getBlockSize() {
        return blockSize;
    }
    abstract String getFeedback();
    abstract void save();
    abstract void restore();
    abstract void init(boolean decrypting, String algorithm, byte[] key,
                       byte[] iv) throws InvalidKeyException;
    final byte[] getIV() {
        return iv;
    }
    abstract void reset();
    abstract void encrypt(byte[] plain, int plainOffset, int plainLen,
                          byte[] cipher, int cipherOffset);
     void encryptFinal(byte[] plain, int plainOffset, int plainLen,
                       byte[] cipher, int cipherOffset)
         throws IllegalBlockSizeException {
         encrypt(plain, plainOffset, plainLen, cipher, cipherOffset);
     }
    abstract void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                          byte[] plain, int plainOffset);
     void decryptFinal(byte[] cipher, int cipherOffset, int cipherLen,
                       byte[] plain, int plainOffset)
         throws IllegalBlockSizeException {
         decrypt(cipher, cipherOffset, cipherLen, plain, plainOffset);
     }
}
