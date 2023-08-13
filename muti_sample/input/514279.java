final class CONSTANT_Integer_info extends CONSTANT_literal_info
{
    public static final byte TAG = 3;
    public int m_value;
    public CONSTANT_Integer_info (final int value)
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
        return Integer.toString (m_value);
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeInt (m_value);
    }
    protected CONSTANT_Integer_info (final UDataInputStream bytes) throws IOException
    {
        m_value = bytes.readInt ();
    }
} 
