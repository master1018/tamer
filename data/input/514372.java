public final class AttLineNumberTable extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "LineNumberTable";
    private final LineNumberList lineNumbers;
    public AttLineNumberTable(LineNumberList lineNumbers) {
        super(ATTRIBUTE_NAME);
        try {
            if (lineNumbers.isMutable()) {
                throw new MutabilityException("lineNumbers.isMutable()");
            }
        } catch (NullPointerException ex) {
            throw new NullPointerException("lineNumbers == null");
        }
        this.lineNumbers = lineNumbers;
    }
    public int byteLength() {
        return 8 + 4 * lineNumbers.size();
    }
    public LineNumberList getLineNumbers() {
        return lineNumbers;
    }
}
