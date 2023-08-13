final class CONSTANT_Utf8_info extends CONSTANT_info
{
    public static final byte TAG = 1;
    public String m_value;
    public CONSTANT_Utf8_info (final String value)
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
        return "CONSTANT_Utf8: [" + m_value + ']';
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeUTF (m_value);
    }
    protected CONSTANT_Utf8_info (final UDataInputStream bytes) throws IOException
    {
        m_value = bytes.readUTF ();
    }
} 
