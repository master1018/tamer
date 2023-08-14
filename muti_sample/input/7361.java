public final class HmacMD5 extends MacSpi implements Cloneable {
    private HmacCore hmac;
    private static final int MD5_BLOCK_LENGTH = 64;
    public HmacMD5() throws NoSuchAlgorithmException {
        hmac = new HmacCore(MessageDigest.getInstance("MD5"),
                            MD5_BLOCK_LENGTH);
    }
    protected int engineGetMacLength() {
        return hmac.getDigestLength();
    }
    protected void engineInit(Key key, AlgorithmParameterSpec params)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        hmac.init(key, params);
    }
    protected void engineUpdate(byte input) {
        hmac.update(input);
    }
    protected void engineUpdate(byte input[], int offset, int len) {
        hmac.update(input, offset, len);
    }
    protected void engineUpdate(ByteBuffer input) {
        hmac.update(input);
    }
    protected byte[] engineDoFinal() {
        return hmac.doFinal();
    }
    protected void engineReset() {
        hmac.reset();
    }
    public Object clone() {
        HmacMD5 that = null;
        try {
            that = (HmacMD5) super.clone();
            that.hmac = (HmacCore) this.hmac.clone();
        } catch (CloneNotSupportedException e) {
        }
        return that;
    }
}
