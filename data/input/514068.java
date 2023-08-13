public class FormatFlagsConversionMismatchException extends
        IllegalFormatException implements Serializable {
    private static final long serialVersionUID = 19120414L;
    private String f;
    private char c;
    public FormatFlagsConversionMismatchException(String f, char c) {
        if (null == f) {
            throw new NullPointerException();
        }
        this.f = f;
        this.c = c;
    }
    public String getFlags() {
        return f;
    }
    public char getConversion() {
        return c;
    }
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Mismatched Convertor =");
        buffer.append(c);
        buffer.append(", Flags= ");
        buffer.append(f);
        return buffer.toString();
    }
}
