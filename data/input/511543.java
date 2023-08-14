public class MyMacSpi extends MacSpi {
    private int length = 0;
    @Override
    protected int engineGetMacLength() {
        return length;
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params == null) {
            if (!(key instanceof SecretKeySpec)) {
                throw new IllegalArgumentException("params is null and key is SecretKeySpec");
            }
        }
    }
    @Override
    protected void engineUpdate(byte input) {
    }
    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        if (offset >= 0 && len >= 0) {
            length = len;
        }
    }
    @Override
    protected byte[] engineDoFinal() {
        return new byte[length];
    }
    @Override
    protected void engineReset() {
        length++;
    }  
}
