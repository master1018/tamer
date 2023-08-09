public abstract class EncodedKeySpec implements KeySpec {
    private byte[] encodedKey;
    public EncodedKeySpec(byte[] encodedKey) {
        this.encodedKey = encodedKey.clone();
    }
    public byte[] getEncoded() {
        return this.encodedKey.clone();
    }
    public abstract String getFormat();
}
