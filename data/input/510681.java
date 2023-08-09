final class CONSTANT_Float_info extends CONSTANT_literal_info
{
    public static final byte TAG = 4;
    public float m_value;
    public CONSTANT_Float_info (final float value)
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
        return Float.toString (m_value);
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeFloat (m_value);
    }
    protected CONSTANT_Float_info (final UDataInputStream bytes) throws IOException
    {
        m_value = bytes.readFloat ();
    }
} 
