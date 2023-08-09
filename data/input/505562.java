public class IllegalFormatFlagsException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 790824L;
    private String flags;
    public IllegalFormatFlagsException(String f) {
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
        buffer.append("Flags = '");
        buffer.append(flags);
        buffer.append("'");
        return buffer.toString();
    }
}
