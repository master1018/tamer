public final class HmacSHA1 extends MacSpi implements Cloneable {
    private HmacCore hmac = null;
    private static final int SHA1_BLOCK_LENGTH = 64;
    public HmacSHA1() throws NoSuchAlgorithmException {
        this.hmac = new HmacCore(MessageDigest.getInstance("SHA1"),
                                 SHA1_BLOCK_LENGTH);
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
        HmacSHA1 that = null;
        try {
            that = (HmacSHA1)super.clone();
            that.hmac = (HmacCore)this.hmac.clone();
        } catch (CloneNotSupportedException e) {
        }
        return that;
    }
}
