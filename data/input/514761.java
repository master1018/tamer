final class InnerClass_info implements Cloneable, IClassFormatOutput
{
    public int m_outer_class_index, m_inner_class_index;
    public int m_inner_name_index;
    public int m_inner_access_flags;
    public InnerClass_info (final int outer_class_index, final int inner_class_index,
                            final int inner_name_index, final int inner_access_flags) 
    {
        m_outer_class_index = outer_class_index;
        m_inner_class_index = inner_class_index;
        m_inner_name_index = inner_name_index;
        m_inner_access_flags = inner_access_flags;
    }
    public String toString ()
    {
        return "innerclass_info: [m_outer_class_index = " + m_outer_class_index + ", m_inner_class_index = " + m_inner_class_index +
            ", m_inner_name_index = " + m_inner_name_index + ", m_inner_access_flags = " + m_inner_access_flags + "]"; 
    }
    public Object clone ()
    {
        try
        {    
            return super.clone ();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        out.writeU2 (m_inner_class_index);
        out.writeU2 (m_outer_class_index);
        out.writeU2 (m_inner_name_index);
        out.writeU2 (m_inner_access_flags);
    }
    InnerClass_info (final UDataInputStream bytes) throws IOException
    {
        m_inner_class_index = bytes.readU2 ();
        m_outer_class_index = bytes.readU2 ();
        m_inner_name_index = bytes.readU2 ();
        m_inner_access_flags = bytes.readU2 ();
    }
} 
