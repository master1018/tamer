final class CONSTANT_String_info extends CONSTANT_literal_info
{
    public static final byte TAG = 8;
    public int m_string_index;
    public CONSTANT_String_info (final int string_index)
    {
        m_string_index = string_index;
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
        return "CONSTANT_String: [string_index = " + m_string_index + ']';
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_string_index);    
    }
    protected CONSTANT_String_info (final UDataInputStream bytes) throws IOException
    {
        m_string_index = bytes.readU2 ();
    }
} 
