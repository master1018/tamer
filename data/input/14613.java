public final class AESCipher extends CipherSpi {
    private CipherCore core = null;
    public AESCipher() {
        core = new CipherCore(new AESCrypt(), AESConstants.AES_BLOCK_SIZE);
    }
    protected void engineSetMode(String mode)
        throws NoSuchAlgorithmException {
        core.setMode(mode);
    }
    protected void engineSetPadding(String paddingScheme)
        throws NoSuchPaddingException {
        core.setPadding(paddingScheme);
    }
    protected int engineGetBlockSize() {
        return AESConstants.AES_BLOCK_SIZE;
    }
    protected int engineGetOutputSize(int inputLen) {
        return core.getOutputSize(inputLen);
    }
    protected byte[] engineGetIV() {
        return core.getIV();
    }
    protected AlgorithmParameters engineGetParameters() {
        return core.getParameters("AES");
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
        throws InvalidKeyException {
        core.init(opmode, key, random);
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        core.init(opmode, key, params, random);
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameters params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        core.init(opmode, key, params, random);
    }
    protected byte[] engineUpdate(byte[] input, int inputOffset,
                                  int inputLen) {
        return core.update(input, inputOffset, inputLen);
    }
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
                               byte[] output, int outputOffset)
        throws ShortBufferException {
        return core.update(input, inputOffset, inputLen, output,
                           outputOffset);
    }
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
        throws IllegalBlockSizeException, BadPaddingException {
        return core.doFinal(input, inputOffset, inputLen);
    }
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
                                byte[] output, int outputOffset)
        throws IllegalBlockSizeException, ShortBufferException,
               BadPaddingException {
        return core.doFinal(input, inputOffset, inputLen, output,
                            outputOffset);
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        if (!AESCrypt.isKeySizeValid(encoded.length)) {
            throw new InvalidKeyException("Invalid AES key length: " +
                                          encoded.length + " bytes");
        }
        return encoded.length * 8;
    }
    protected byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException {
        return core.wrap(key);
    }
    protected Key engineUnwrap(byte[] wrappedKey,
                                     String wrappedKeyAlgorithm,
                                     int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        return core.unwrap(wrappedKey, wrappedKeyAlgorithm,
                           wrappedKeyType);
    }
}
