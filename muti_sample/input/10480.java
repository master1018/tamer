public final class PBEWithMD5AndDESCipher extends CipherSpi {
    private PBECipherCore core;
    public PBEWithMD5AndDESCipher()
        throws NoSuchAlgorithmException, NoSuchPaddingException {
        core = new PBECipherCore("DES");
    }
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if ((mode != null) && (!mode.equalsIgnoreCase("CBC"))) {
            throw new NoSuchAlgorithmException("Invalid cipher mode: " + mode);
        }
    }
    protected void engineSetPadding(String paddingScheme)
        throws NoSuchPaddingException
    {
        if ((paddingScheme != null) &&
            (!paddingScheme.equalsIgnoreCase("PKCS5Padding"))) {
            throw new NoSuchPaddingException("Invalid padding scheme: " +
                                             paddingScheme);
        }
    }
    protected int engineGetBlockSize() {
        return core.getBlockSize();
    }
    protected int engineGetOutputSize(int inputLen) {
        return core.getOutputSize(inputLen);
    }
    protected byte[] engineGetIV() {
        return core.getIV();
    }
    protected AlgorithmParameters engineGetParameters() {
        return core.getParameters();
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
        throws InvalidKeyException {
        try {
            engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
        } catch (InvalidAlgorithmParameterException ie) {
            InvalidKeyException ike =
                new InvalidKeyException("requires PBE parameters");
            ike.initCause(ie);
            throw ike;
        }
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
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen)
    {
        return core.update(input, inputOffset, inputLen);
    }
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
                               byte[] output, int outputOffset)
        throws ShortBufferException
    {
        return core.update(input, inputOffset, inputLen,
                           output, outputOffset);
    }
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
        throws IllegalBlockSizeException, BadPaddingException
    {
        return core.doFinal(input, inputOffset, inputLen);
    }
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
                                byte[] output, int outputOffset)
        throws ShortBufferException, IllegalBlockSizeException,
               BadPaddingException
    {
        return core.doFinal(input, inputOffset, inputLen,
                            output, outputOffset);
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        return 56;
    }
    protected byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException {
        return core.wrap(key);
    }
    protected Key engineUnwrap(byte[] wrappedKey,
                               String wrappedKeyAlgorithm,
                               int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encodedKey;
        return core.unwrap(wrappedKey, wrappedKeyAlgorithm,
                           wrappedKeyType);
    }
}
