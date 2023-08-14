final class OutputFeedback extends FeedbackCipher {
    private byte[] k = null;
    private byte[] register = null;
    private int numBytes;
    private byte[] registerSave = null;
    OutputFeedback(SymmetricCipher embeddedCipher, int numBytes) {
        super(embeddedCipher);
        if (numBytes > blockSize) {
            numBytes = blockSize;
        }
        this.numBytes = numBytes;
        k = new byte[blockSize];
        register = new byte[blockSize];
    }
    String getFeedback() {
        return "OFB";
    }
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv)
            throws InvalidKeyException {
        if ((key == null) || (iv == null) || (iv.length != blockSize)) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        reset();
        embeddedCipher.init(false, algorithm, key);
    }
    void reset() {
        System.arraycopy(iv, 0, register, 0, blockSize);
    }
    void save() {
        if (registerSave == null) {
            registerSave = new byte[blockSize];
        }
        System.arraycopy(register, 0, registerSave, 0, blockSize);
    }
    void restore() {
        System.arraycopy(registerSave, 0, register, 0, blockSize);
    }
    void encrypt(byte[] plain, int plainOffset, int plainLen,
                        byte[] cipher, int cipherOffset)
    {
        int i;
        int len = blockSize - numBytes;
        int loopCount = plainLen / numBytes;
        int oddBytes = plainLen % numBytes;
        if (len == 0) {
            for (; loopCount > 0;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<numBytes; i++)
                    cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
                System.arraycopy(k, 0, register, 0, numBytes);
            }
            if (oddBytes > 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<oddBytes; i++)
                    cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
                System.arraycopy(k, 0, register, 0, numBytes);
            }
        } else {
            for (; loopCount > 0;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<numBytes; i++)
                    cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
                System.arraycopy(register, numBytes, register, 0, len);
                System.arraycopy(k, 0, register, len, numBytes);
            }
            if (oddBytes > 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<oddBytes; i++)
                    cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
                System.arraycopy(register, numBytes, register, 0, len);
                System.arraycopy(k, 0, register, len, numBytes);
            }
        }
    }
    void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                        byte[] plain, int plainOffset)
    {
        encrypt(cipher, cipherOffset, cipherLen, plain, plainOffset);
    }
}
