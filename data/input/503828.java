public class UnknownFormatFlagsException extends IllegalFormatException {
    private static final long serialVersionUID = 19370506L;
    private String flags;
    public UnknownFormatFlagsException(String f) {
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
        return Msg.getString("K034a", flags); 
    }
}
