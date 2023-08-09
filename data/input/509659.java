public abstract class VCardParser {
    protected final int mParseType;
    protected boolean mCanceled;
    public VCardParser() {
        this(VCardConfig.PARSE_TYPE_UNKNOWN);
    }
    public VCardParser(int parseType) {
        mParseType = parseType;
    }
    public abstract boolean parse(InputStream is, VCardInterpreter interepreter)
            throws IOException, VCardException;
    public abstract boolean parse(InputStream is, String charset, VCardInterpreter builder)
            throws IOException, VCardException;
    public abstract void parse(InputStream is, String charset,
            VCardInterpreter builder, boolean canceled)
        throws IOException, VCardException;
    public void cancel() {
        mCanceled = true;
    }
}
