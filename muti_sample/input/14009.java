public abstract class MacSpi {
    protected abstract int engineGetMacLength();
    protected abstract void engineInit(Key key,
                                       AlgorithmParameterSpec params)
        throws InvalidKeyException, InvalidAlgorithmParameterException ;
    protected abstract void engineUpdate(byte input);
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    protected void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining() == false) {
            return;
        }
        if (input.hasArray()) {
            byte[] b = input.array();
            int ofs = input.arrayOffset();
            int pos = input.position();
            int lim = input.limit();
            engineUpdate(b, ofs + pos, lim - pos);
            input.position(lim);
        } else {
            int len = input.remaining();
            byte[] b = new byte[CipherSpi.getTempArraySize(len)];
            while (len > 0) {
                int chunk = Math.min(len, b.length);
                input.get(b, 0, chunk);
                engineUpdate(b, 0, chunk);
                len -= chunk;
            }
        }
    }
    protected abstract byte[] engineDoFinal();
    protected abstract void engineReset();
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
