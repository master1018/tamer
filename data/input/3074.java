public final class AESWrapCipher extends CipherSpi {
    private static final byte[] IV = {
        (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6,
        (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6
    };
    private static final int blksize = AESConstants.AES_BLOCK_SIZE;
    private AESCrypt cipher;
    private boolean decrypting = false;
    public AESWrapCipher() {
        cipher = new AESCrypt();
    }
    protected void engineSetMode(String mode)
        throws NoSuchAlgorithmException {
        if (!mode.equalsIgnoreCase("ECB")) {
            throw new NoSuchAlgorithmException(mode + " cannot be used");
        }
    }
    protected void engineSetPadding(String padding)
        throws NoSuchPaddingException {
        if (!padding.equalsIgnoreCase("NoPadding")) {
            throw new NoSuchPaddingException(padding + " cannot be used");
        }
    }
    protected int engineGetBlockSize() {
        return blksize;
    }
    protected int engineGetOutputSize(int inputLen) {
        int result = 0;
        if (decrypting) {
            result = inputLen - 8;
        } else {
            result = inputLen + 8;
        }
        return (result < 0? 0:result);
    }
    protected byte[] engineGetIV() {
        return null;
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
        throws InvalidKeyException {
        if (opmode == Cipher.WRAP_MODE) {
            decrypting = false;
        } else if (opmode == Cipher.UNWRAP_MODE) {
            decrypting = true;
        } else {
            throw new UnsupportedOperationException("This cipher can " +
                "only be used for key wrapping and unwrapping");
        }
        cipher.init(decrypting, key.getAlgorithm(), key.getEncoded());
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("This cipher " +
                "does not accept any parameters");
        }
        engineInit(opmode, key, random);
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameters params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("This cipher " +
                "does not accept any parameters");
        }
        engineInit(opmode, key, random);
    }
    protected byte[] engineUpdate(byte[] in, int inOffset, int inLen) {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected int engineUpdate(byte[] in, int inOffset, int inLen,
                               byte[] out, int outOffset)
        throws ShortBufferException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected byte[] engineDoFinal(byte[] input, int inputOffset,
                                   int inputLen)
        throws IllegalBlockSizeException, BadPaddingException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected int engineDoFinal(byte[] in, int inOffset, int inLen,
                                byte[] out, int outOffset)
        throws IllegalBlockSizeException, ShortBufferException,
               BadPaddingException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        if (!AESCrypt.isKeySizeValid(encoded.length)) {
            throw new InvalidKeyException("Invalid key length: " +
                                          encoded.length + " bytes");
        }
        return encoded.length * 8;
    }
    protected byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException {
        byte[] keyVal = key.getEncoded();
        if ((keyVal == null) || (keyVal.length == 0)) {
            throw new InvalidKeyException("Cannot get an encoding of " +
                                          "the key to be wrapped");
        }
        byte[] out = new byte[keyVal.length + 8];
        if (keyVal.length == 8) {
            System.arraycopy(IV, 0, out, 0, IV.length);
            System.arraycopy(keyVal, 0, out, IV.length, 8);
            cipher.encryptBlock(out, 0, out, 0);
        } else {
            if (keyVal.length % 8 != 0) {
                throw new IllegalBlockSizeException("length of the " +
                    "to be wrapped key should be multiples of 8 bytes");
            }
            System.arraycopy(IV, 0, out, 0, IV.length);
            System.arraycopy(keyVal, 0, out, IV.length, keyVal.length);
            int N = keyVal.length/8;
            byte[] buffer = new byte[blksize];
            for (int j = 0; j < 6; j++) {
                for (int i = 1; i <= N; i++) {
                    int T = i + j*N;
                    System.arraycopy(out, 0, buffer, 0, IV.length);
                    System.arraycopy(out, i*8, buffer, IV.length, 8);
                    cipher.encryptBlock(buffer, 0, buffer, 0);
                    for (int k = 1; T != 0; k++) {
                        byte v = (byte) T;
                        buffer[IV.length - k] ^= v;
                        T >>>= 8;
                    }
                    System.arraycopy(buffer, 0, out, 0, IV.length);
                    System.arraycopy(buffer, 8, out, 8*i, 8);
                }
            }
        }
        return out;
    }
    protected Key engineUnwrap(byte[] wrappedKey,
                               String wrappedKeyAlgorithm,
                               int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        int wrappedKeyLen = wrappedKey.length;
        if (wrappedKeyLen == 0) {
            throw new InvalidKeyException("The wrapped key is empty");
        }
        if (wrappedKeyLen % 8 != 0) {
            throw new InvalidKeyException
                ("The wrapped key has invalid key length");
        }
        byte[] out = new byte[wrappedKeyLen - 8];
        byte[] buffer = new byte[blksize];
        if (wrappedKeyLen == 16) {
            cipher.decryptBlock(wrappedKey, 0, buffer, 0);
            for (int i = 0; i < IV.length; i++) {
                if (IV[i] != buffer[i]) {
                    throw new InvalidKeyException("Integrity check failed");
                }
            }
            System.arraycopy(buffer, IV.length, out, 0, out.length);
        } else {
            System.arraycopy(wrappedKey, 0, buffer, 0, IV.length);
            System.arraycopy(wrappedKey, IV.length, out, 0, out.length);
            int N = out.length/8;
            for (int j = 5; j >= 0; j--) {
                for (int i = N; i > 0; i--) {
                    int T = i + j*N;
                    System.arraycopy(out, 8*(i-1), buffer, IV.length, 8);
                    for (int k = 1; T != 0; k++) {
                        byte v = (byte) T;
                        buffer[IV.length - k] ^= v;
                        T >>>= 8;
                    }
                    cipher.decryptBlock(buffer, 0, buffer, 0);
                    System.arraycopy(buffer, IV.length, out, 8*(i-1), 8);
                }
            }
            for (int i = 0; i < IV.length; i++) {
                if (IV[i] != buffer[i]) {
                    throw new InvalidKeyException("Integrity check failed");
                }
            }
        }
        return ConstructKeys.constructKey(out, wrappedKeyAlgorithm,
                                          wrappedKeyType);
    }
}
