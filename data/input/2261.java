public class MaxStreamFormatVersionComponentImpl extends TaggedComponentBase
    implements MaxStreamFormatVersionComponent
{
    private byte version;
    public static final MaxStreamFormatVersionComponentImpl singleton
        = new MaxStreamFormatVersionComponentImpl();
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MaxStreamFormatVersionComponentImpl))
            return false ;
        MaxStreamFormatVersionComponentImpl other =
            (MaxStreamFormatVersionComponentImpl)obj ;
        return version == other.version ;
    }
    public int hashCode()
    {
        return version ;
    }
    public String toString()
    {
        return "MaxStreamFormatVersionComponentImpl[version=" + version + "]" ;
    }
    public MaxStreamFormatVersionComponentImpl()
    {
        version = ORBUtility.getMaxStreamFormatVersion();
    }
    public MaxStreamFormatVersionComponentImpl(byte streamFormatVersion) {
        version = streamFormatVersion;
    }
    public byte getMaxStreamFormatVersion()
    {
        return version;
    }
    public void writeContents(OutputStream os)
    {
        os.write_octet(version);
    }
    public int getId()
    {
        return TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value;
    }
}
