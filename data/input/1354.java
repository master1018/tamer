abstract class SymmetricCipher {
    SymmetricCipher() {
    }
    abstract int getBlockSize();
    abstract void init(boolean decrypting, String algorithm, byte[] key)
        throws InvalidKeyException;
    abstract void encryptBlock(byte[] plain, int plainOffset,
                          byte[] cipher, int cipherOffset);
    abstract void decryptBlock(byte[] cipher, int cipherOffset,
                          byte[] plain, int plainOffset);
}
