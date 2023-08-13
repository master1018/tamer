final class CipherFeedback extends FeedbackCipher {
    private final byte[] k;
    private final byte[] register;
    private int numBytes;
    private byte[] registerSave = null;
    CipherFeedback(SymmetricCipher embeddedCipher, int numBytes) {
        super(embeddedCipher);
        if (numBytes > blockSize) {
            numBytes = blockSize;
        }
        this.numBytes = numBytes;
        k = new byte[blockSize];
        register = new byte[blockSize];
    }
    String getFeedback() {
        return "CFB";
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
        int i, len;
        len = blockSize - numBytes;
        int loopCount = plainLen / numBytes;
        int oddBytes = plainLen % numBytes;
        if (len == 0) {
            for (; loopCount > 0 ;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i = 0; i < blockSize; i++)
                    register[i] = cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
            }
            if (oddBytes > 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<oddBytes; i++)
                    register[i] = cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
            }
        } else {
            for (; loopCount > 0 ;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                System.arraycopy(register, numBytes, register, 0, len);
                for (i=0; i<numBytes; i++)
                    register[i+len] = cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
            }
            if (oddBytes != 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                System.arraycopy(register, numBytes, register, 0, len);
                for (i=0; i<oddBytes; i++) {
                    register[i+len] = cipher[i+cipherOffset] =
                        (byte)(k[i] ^ plain[i+plainOffset]);
                }
            }
        }
    }
    void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                        byte[] plain, int plainOffset)
    {
        int i, len;
        len = blockSize - numBytes;
        int loopCount = cipherLen / numBytes;
        int oddBytes = cipherLen % numBytes;
        if (len == 0) {
            for (; loopCount > 0;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i = 0; i < blockSize; i++) {
                    register[i] = cipher[i+cipherOffset];
                    plain[i+plainOffset]
                        = (byte)(cipher[i+cipherOffset] ^ k[i]);
                }
            }
            if (oddBytes > 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                for (i=0; i<oddBytes; i++) {
                    register[i] = cipher[i+cipherOffset];
                    plain[i+plainOffset]
                        = (byte)(cipher[i+cipherOffset] ^ k[i]);
                }
            }
        } else {
            for (; loopCount > 0;
                 plainOffset += numBytes, cipherOffset += numBytes,
                 loopCount--) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                System.arraycopy(register, numBytes, register, 0, len);
                for (i=0; i<numBytes; i++) {
                    register[i+len] = cipher[i+cipherOffset];
                    plain[i+plainOffset]
                        = (byte)(cipher[i+cipherOffset] ^ k[i]);
                }
            }
            if (oddBytes != 0) {
                embeddedCipher.encryptBlock(register, 0, k, 0);
                System.arraycopy(register, numBytes, register, 0, len);
                for (i=0; i<oddBytes; i++) {
                    register[i+len] = cipher[i+cipherOffset];
                    plain[i+plainOffset]
                        = (byte)(cipher[i+cipherOffset] ^ k[i]);
                }
            }
        }
    }
}
