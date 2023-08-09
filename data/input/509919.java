public class IllegalFormatPrecisionException extends IllegalFormatException {
    private static final long serialVersionUID = 18711008L;
    private int p;
    public IllegalFormatPrecisionException(int p) {
        this.p = p;
    }
    public int getPrecision() {
        return p;
    }
    @Override
    public String getMessage() {
        return String.valueOf(p);
    }
}
