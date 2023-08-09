final class ConstantValueAttribute_info extends Attribute_info
{
    public int m_value_index;
    public ConstantValueAttribute_info (final int attribute_name_index, final int value_index)
    {
        super (attribute_name_index, 2);
        m_value_index = value_index;
    }
    public CONSTANT_literal_info getValue (final ClassDef cls)
    {
        return (CONSTANT_literal_info) cls.getConstants ().get (m_value_index);
    }
    public long length ()
    {
        return 8;
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "ConstantValueAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + ']';
    }
    public Object clone ()
    {        
        return super.clone ();    
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_value_index);
    }
    ConstantValueAttribute_info (final int attribute_name_index, final long attribute_length,
                                 final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        m_value_index = bytes.readU2 ();
        if (DEBUG) System.out.println ("\tconstantvalue_index: " + m_value_index);
    }
    private static final boolean DEBUG = false;
} 
