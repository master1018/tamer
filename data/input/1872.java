public class MaxStreamFormatVersionServiceContext extends ServiceContext {
    private byte maxStreamFormatVersion;
    public static final MaxStreamFormatVersionServiceContext singleton
        = new MaxStreamFormatVersionServiceContext();
    public MaxStreamFormatVersionServiceContext() {
        maxStreamFormatVersion = ORBUtility.getMaxStreamFormatVersion();
    }
    public MaxStreamFormatVersionServiceContext(byte maxStreamFormatVersion) {
        this.maxStreamFormatVersion = maxStreamFormatVersion;
    }
    public MaxStreamFormatVersionServiceContext(InputStream is,
                                                GIOPVersion gv) {
        super(is, gv) ;
        maxStreamFormatVersion = is.read_octet();
    }
    public static final int SERVICE_CONTEXT_ID = RMICustomMaxStreamFormat.value;
    public int getId() { return SERVICE_CONTEXT_ID; }
    public void writeData(OutputStream os) throws SystemException
    {
        os.write_octet(maxStreamFormatVersion);
    }
    public byte getMaximumStreamFormatVersion()
    {
        return maxStreamFormatVersion;
    }
    public String toString()
    {
        return "MaxStreamFormatVersionServiceContext["
            + maxStreamFormatVersion + "]";
    }
}
