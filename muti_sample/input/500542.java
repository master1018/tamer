public final class AttEnclosingMethod extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "EnclosingMethod";
    private final CstType type;
    private final CstNat method;
    public AttEnclosingMethod(CstType type, CstNat method) {
        super(ATTRIBUTE_NAME);
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.type = type;
        this.method = method;
    }
    public int byteLength() {
        return 10;
    }
    public CstType getEnclosingClass() {
        return type;
    }
    public CstNat getMethod() {
        return method;
    }
}
