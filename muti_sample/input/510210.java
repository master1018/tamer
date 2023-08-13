final class BridgeAttribute_info extends Attribute_info
{
    public BridgeAttribute_info (final int attribute_name_index)
    {
        super (attribute_name_index, 0);
    }
    public long length ()
    {
        return 6;
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "BridgeAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + ']';
    }
    public Object clone ()
    {        
        return super.clone ();    
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
    }
    BridgeAttribute_info (final int attribute_name_index, final long attribute_length)
    {
        super (attribute_name_index, attribute_length);
    }
} 
