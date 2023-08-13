public class MissingFormatWidthException extends IllegalFormatException {
    private static final long serialVersionUID = 15560123L;
    private String s;
    public MissingFormatWidthException(String s) {
        if (null == s) {
            throw new NullPointerException();
        }
        this.s = s;
    }
    public String getFormatSpecifier() {
        return s;
    }
    @Override
    public String getMessage() {
        return s;
    }
}
