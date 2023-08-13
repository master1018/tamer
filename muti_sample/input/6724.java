final class CipherTextStealing extends CipherBlockChaining {
    CipherTextStealing(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
    }
    String getFeedback() {
        return "CTS";
    }
    void encryptFinal(byte[] plain, int plainOffset, int plainLen,
                      byte[] cipher, int cipherOffset)
        throws IllegalBlockSizeException {
        if (plainLen < blockSize) {
            throw new IllegalBlockSizeException("input is too short!");
        } else if (plainLen == blockSize) {
            encrypt(plain, plainOffset, plainLen, cipher, cipherOffset);
        } else {
            int nLeft = plainLen % blockSize;
            if (nLeft == 0) {
                encrypt(plain, plainOffset, plainLen, cipher, cipherOffset);
                int lastBlkIndex = cipherOffset + plainLen - blockSize;
                int nextToLastBlkIndex = lastBlkIndex - blockSize;
                byte[] tmp = new byte[blockSize];
                System.arraycopy(cipher, lastBlkIndex, tmp, 0, blockSize);
                System.arraycopy(cipher, nextToLastBlkIndex,
                                 cipher, lastBlkIndex, blockSize);
                System.arraycopy(tmp, 0, cipher, nextToLastBlkIndex,
                                 blockSize);
            } else {
                int newPlainLen = plainLen - (blockSize + nLeft);
                if (newPlainLen > 0) {
                    encrypt(plain, plainOffset, newPlainLen, cipher,
                            cipherOffset);
                    plainOffset += newPlainLen;
                    cipherOffset += newPlainLen;
                }
                byte[] tmp = new byte[blockSize];
                for (int i = 0; i < blockSize; i++) {
                    tmp[i] = (byte) (plain[plainOffset+i] ^ r[i]);
                }
                byte[] tmp2 = new byte[blockSize];
                embeddedCipher.encryptBlock(tmp, 0, tmp2, 0);
                System.arraycopy(tmp2, 0, cipher,
                                 cipherOffset+blockSize, nLeft);
                for (int i=0; i<nLeft; i++) {
                    tmp2[i] = (byte)
                        (plain[plainOffset+blockSize+i] ^ tmp2[i]);
                }
                embeddedCipher.encryptBlock(tmp2, 0, cipher, cipherOffset);
            }
        }
    }
    void decryptFinal(byte[] cipher, int cipherOffset, int cipherLen,
                      byte[] plain, int plainOffset)
        throws IllegalBlockSizeException {
        if (cipherLen < blockSize) {
            throw new IllegalBlockSizeException("input is too short!");
        } else if (cipherLen == blockSize) {
            decrypt(cipher, cipherOffset, cipherLen, plain, plainOffset);
        } else {
            int nLeft = cipherLen % blockSize;
            if (nLeft == 0) {
                int lastBlkIndex = cipherOffset + cipherLen - blockSize;
                int nextToLastBlkIndex =
                    cipherOffset + cipherLen - 2*blockSize;
                byte[] tmp = new byte[2*blockSize];
                System.arraycopy(cipher, lastBlkIndex, tmp, 0, blockSize);
                System.arraycopy(cipher, nextToLastBlkIndex,
                                 tmp, blockSize, blockSize);
                int cipherLen2 = cipherLen-2*blockSize;
                decrypt(cipher, cipherOffset, cipherLen2, plain, plainOffset);
                decrypt(tmp, 0, 2*blockSize, plain, plainOffset+cipherLen2);
            } else {
                int newCipherLen = cipherLen-(blockSize+nLeft);
                if (newCipherLen > 0) {
                    decrypt(cipher, cipherOffset, newCipherLen, plain,
                            plainOffset);
                    cipherOffset += newCipherLen;
                    plainOffset += newCipherLen;
                }
                byte[] tmp = new byte[blockSize];
                embeddedCipher.decryptBlock(cipher, cipherOffset, tmp, 0);
                for (int i = 0; i < nLeft; i++) {
                    plain[plainOffset+blockSize+i] =
                        (byte) (cipher[cipherOffset+blockSize+i] ^ tmp[i]);
                }
                System.arraycopy(cipher, cipherOffset+blockSize, tmp, 0,
                                 nLeft);
                embeddedCipher.decryptBlock(tmp, 0, plain, plainOffset);
                for (int i=0; i<blockSize; i++) {
                    plain[plainOffset+i] = (byte)
                        (plain[plainOffset+i]^r[i]);
                }
            }
        }
    }
}
