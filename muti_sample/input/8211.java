public final class RC2Cipher extends CipherSpi {
    private final CipherCore core;
    private final RC2Crypt embeddedCipher;
    public RC2Cipher() {
        embeddedCipher = new RC2Crypt();
        core = new CipherCore(embeddedCipher, 8);
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
        return 8;
    }
    protected int engineGetOutputSize(int inputLen) {
        return core.getOutputSize(inputLen);
    }
    protected byte[] engineGetIV() {
        return core.getIV();
    }
    protected AlgorithmParameters engineGetParameters() {
        return core.getParameters("RC2");
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        embeddedCipher.initEffectiveKeyBits(0);
        core.init(opmode, key, random);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null && params instanceof RC2ParameterSpec) {
            embeddedCipher.initEffectiveKeyBits
                (((RC2ParameterSpec)params).getEffectiveKeyBits());
        } else {
            embeddedCipher.initEffectiveKeyBits(0);
        }
        core.init(opmode, key, params, random);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null && params.getAlgorithm().equals("RC2")) {
            try {
                RC2ParameterSpec rc2Params = (RC2ParameterSpec)
                    params.getParameterSpec(RC2ParameterSpec.class);
                engineInit(opmode, key, rc2Params, random);
            } catch (InvalidParameterSpecException ipse) {
                throw new InvalidAlgorithmParameterException
                            ("Wrong parameter type: RC2 expected");
            }
        } else {
            embeddedCipher.initEffectiveKeyBits(0);
            core.init(opmode, key, params, random);
        }
    }
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        return core.update(in, inOfs, inLen);
    }
    protected int engineUpdate(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException {
        return core.update(in, inOfs, inLen, out, outOfs);
    }
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen)
            throws IllegalBlockSizeException, BadPaddingException {
        return core.doFinal(in, inOfs, inLen);
    }
    protected int engineDoFinal(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws IllegalBlockSizeException,
            ShortBufferException, BadPaddingException {
        return core.doFinal(in, inOfs, inLen, out, outOfs);
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] keyBytes = CipherCore.getKeyBytes(key);
        RC2Crypt.checkKey(key.getAlgorithm(), keyBytes.length);
        return keyBytes.length << 3;
    }
    protected byte[] engineWrap(Key key)
            throws IllegalBlockSizeException, InvalidKeyException {
        return core.wrap(key);
    }
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm,
            int wrappedKeyType) throws InvalidKeyException,
            NoSuchAlgorithmException {
        return core.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
    }
}
