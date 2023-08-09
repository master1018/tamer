final class CONSTANT_Double_info extends CONSTANT_literal_info
{
    public static final byte TAG = 6;
    public double m_value;
    public CONSTANT_Double_info (final double value)
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
        return Double.toString (m_value);
    }
    public int width ()
    {
        return 2;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeDouble (m_value);
    }
    protected CONSTANT_Double_info (final UDataInputStream bytes) throws IOException
    {
        m_value = bytes.readDouble ();    
    }
} 
