public class DuplicateFormatFlagsException extends IllegalFormatException {
    private static final long serialVersionUID = 18890531L;
    private String flags;
    public DuplicateFormatFlagsException(String f) {
        if (null == f) {
            throw new NullPointerException();
        }
        flags = f;
    }
    public String getFlags() {
        return flags;
    }
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Flags of the DuplicateFormatFlagsException is'");
        buffer.append(flags);
        buffer.append("'");
        return buffer.toString();
    }
}
