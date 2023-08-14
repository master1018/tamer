final class SourceFileAttribute_info extends Attribute_info
{
    public int m_sourcefile_index;
    public SourceFileAttribute_info (final int attribute_name_index)
    {
        super (attribute_name_index, 0);
    }
    public long length ()
    {
        return 8;
    }
    public CONSTANT_Utf8_info getSourceFile (final ClassDef cls)
    {
        return (CONSTANT_Utf8_info) cls.getConstants ().get (m_sourcefile_index);
    }  
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "SourceFileAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + ']';
    }
    public Object clone ()
    {        
        return super.clone ();    
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_sourcefile_index);
    }
    SourceFileAttribute_info (final int attribute_name_index, final long attribute_length,
                              final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        m_sourcefile_index = bytes.readU2 ();
    }
} 
