public abstract class MacSpi {
    public MacSpi() {
    }
    protected abstract int engineGetMacLength();
    protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract void engineUpdate(byte input);
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] bInput;
        if (input.hasArray()) {
            bInput = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            engineUpdate(bInput, offset + position, limit - position);
            input.position(limit);
        } else {
            bInput = new byte[input.limit() - input.position()];
            input.get(bInput);
            engineUpdate(bInput, 0, bInput.length);
        }
    }
    protected abstract byte[] engineDoFinal();
    protected abstract void engineReset();
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}