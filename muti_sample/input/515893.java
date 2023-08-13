public final class AttSynthetic extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "Synthetic";
    public AttSynthetic() {
        super(ATTRIBUTE_NAME);
    }
    public int byteLength() {
        return 6;
    }
}
