final class LineNumberTableAttribute_info extends Attribute_info
{
    public LineNumber_info get (final int offset)
    {
        return (LineNumber_info) m_lines.get (offset);
    }
    public int size ()
    {
        return m_lines.size ();
    }
    public long length ()
    {
        return 8 + (m_lines.size () << 2); 
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ("LineNumberTableAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + length () + "]\n");
        for (int l = 0; l < size (); ++ l)
        {
            s.append ("            " + get (l));
            s.append ("\n"); 
        }
        return s.toString ();
    }
    public Object clone ()
    {
        final LineNumberTableAttribute_info _clone = (LineNumberTableAttribute_info) super.clone ();
        final int lines_count = m_lines.size (); 
        _clone.m_lines = new ArrayList (lines_count);
        for (int e = 0; e < lines_count; ++ e)
        {
            _clone.m_lines.add (((LineNumber_info) m_lines.get (e)).clone ());
        }
        return _clone;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        final int lines_count = m_lines.size (); 
        out.writeU2 (lines_count);
        for (int l = 0; l < lines_count; ++ l)
        {
            ((LineNumber_info) m_lines.get (l)).writeInClassFormat (out);
        }
    }
    LineNumberTableAttribute_info (final int attribute_name_index, final long attribute_length,
                                   final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        final int lines_count = bytes.readU2 ();
        m_lines = new ArrayList (lines_count);
        for (int i = 0; i < lines_count; i++)
        {
            m_lines.add (new LineNumber_info (bytes));
        }
    }
    private List m_lines; 
} 
