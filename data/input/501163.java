public class SigEnumConstant extends SigField implements IEnumConstant {
    private static final int UNKNOWN = -1;
    private int ordinal = UNKNOWN;
    public SigEnumConstant(String name) {
        super(name);
    }
    public int getOrdinal() {
        if (ordinal == UNKNOWN) {
            throw new UnsupportedOperationException();
        }
        return ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
