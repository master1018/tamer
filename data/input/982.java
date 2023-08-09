final class ElectronicCodeBook extends FeedbackCipher {
    ElectronicCodeBook(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
    }
    String getFeedback() {
        return "ECB";
    }
    void reset() {
    }
    void save() {}
    void restore() {}
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv)
            throws InvalidKeyException {
        if ((key == null) || (iv != null)) {
            throw new InvalidKeyException("Internal error");
        }
        embeddedCipher.init(decrypting, algorithm, key);
    }
    void encrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        while (len >= blockSize) {
            embeddedCipher.encryptBlock(in, inOff, out, outOff);
            len -= blockSize;
            inOff += blockSize;
            outOff += blockSize;
        }
    }
    void decrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        while (len >= blockSize) {
            embeddedCipher.decryptBlock(in, inOff, out, outOff);
            len -= blockSize;
            inOff += blockSize;
            outOff += blockSize;
        }
    }
}
