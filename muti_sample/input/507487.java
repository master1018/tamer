final class GenericAttribute_info extends Attribute_info
{
    public byte [] m_info;
    public GenericAttribute_info (final int attribute_name_index, final byte [] info)
    {
        super (attribute_name_index, (info != null ? info.length : 0));
        m_info = (info != null ? info : EMPTY_BYTE_ARRAY);
    }
    public long length ()
    {
        return 6 + m_info.length; 
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "generic attribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + ']';
    }
    public Object clone ()
    {
        final GenericAttribute_info _clone = (GenericAttribute_info) super.clone ();
        _clone.m_info = (m_info.length == 0 ? EMPTY_BYTE_ARRAY : (byte []) m_info.clone ());
        return _clone;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.write (m_info, 0, m_info.length);
    }
    GenericAttribute_info (final int attribute_name_index, final long attribute_length,
                           final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        m_info = new byte [(int) m_attribute_length];
        bytes.readFully (m_info);
    }
    private static final byte [] EMPTY_BYTE_ARRAY = new byte [0];
} 
