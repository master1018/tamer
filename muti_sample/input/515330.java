final class CONSTANT_Long_info extends CONSTANT_literal_info
{
    public static final byte TAG = 5;
    public long m_value;
    public CONSTANT_Long_info (final long value)
    {
        m_value = value;
    }
    public final byte tag ()
    {
        return TAG;
    }
    public Object accept (final ICONSTANTVisitor visitor, final Object ctx)
    {
        return visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return Long.toString (m_value);
    }
    public int width ()
    {
        return 2;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeLong (m_value);
    }
    protected CONSTANT_Long_info (final UDataInputStream bytes) throws IOException
    {
        m_value = bytes.readLong ();
    }
} 
