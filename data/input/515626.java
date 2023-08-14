public class MyCipher extends CipherSpi {
    public MyCipher() {
        super();
    }
    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    }
    @Override
    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
        if (!"PKCS5Padding".equals(padding)) {
            throw new  NoSuchPaddingException(padding);
        }
    }
    @Override
    protected int engineGetBlockSize() {
        return 111;
    }
    @Override
    protected int engineGetOutputSize(int inputLen) {
        return inputLen + 10;
    }
    @Override
    protected byte[] engineGetIV() {
        byte[] b = {1,2,3};
        return b;
    }
    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }
    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
    }
    @Override
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
    }
    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
    }
    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        return null;
    }
    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException {
        return 0;
    }
    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        return null;
    }
    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        return 0;
    }
}
