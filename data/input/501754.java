public final class AttSignature extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "Signature";
    private final CstUtf8 signature;
    public AttSignature(CstUtf8 signature) {
        super(ATTRIBUTE_NAME);
        if (signature == null) {
            throw new NullPointerException("signature == null");
        }
        this.signature = signature;
    }
    public int byteLength() {
        return 8;
    }
    public CstUtf8 getSignature() {
        return signature;
    }
}
