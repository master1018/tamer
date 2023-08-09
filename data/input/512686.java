public final class AttAnnotationDefault extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "AnnotationDefault";
    private final Constant value;
    private final int byteLength;
    public AttAnnotationDefault(Constant value, int byteLength) {
        super(ATTRIBUTE_NAME);
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        this.value = value;
        this.byteLength = byteLength;
    }
    public int byteLength() {
        return byteLength + 6;
    }
    public Constant getValue() {
        return value;
    }
}
