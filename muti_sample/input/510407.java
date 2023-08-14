final class InnerClassesAttribute_info extends Attribute_info
{
    public boolean makesClassNested (final int class_index, final int [] nestedAccessFlags)
    {
        if (class_index > 0)
        {
            for (int i = 0, iLimit = size (); i < iLimit; ++ i)
            {
                final InnerClass_info info = get (i);
                if (info.m_inner_class_index == class_index)
                {
                    nestedAccessFlags [0] = info.m_inner_access_flags;
                    return true;
                }
            }
        }
        return false;
    }
    public final InnerClass_info get (final int offset)
    {
        return (InnerClass_info) m_classes.get (offset);
    }
    public final int size ()
    {
        return m_classes.size ();
    }
    public final long length ()
    {
        return 8 + (m_classes.size () << 3); 
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ("InnerClassesAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + length () + "]\n");
        for (int l = 0; l < size (); ++ l)
        {
            s.append ("            " + get (l));
            s.append ("\n"); 
        }
        return s.toString ();
    }
    public Object clone ()
    {
        final InnerClassesAttribute_info _clone = (InnerClassesAttribute_info) super.clone ();
        final List classes = m_classes;
        final int class_count = classes.size (); 
        _clone.m_classes = new ArrayList (class_count);
        for (int e = 0; e < class_count; ++ e)
        {
            _clone.m_classes.add (((InnerClass_info) classes.get (e)).clone ());
        }
        return _clone;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        final List classes = m_classes;
        final int class_count = classes.size (); 
        out.writeU2 (class_count);
        for (int l = 0; l < class_count; ++ l)
        {
            ((InnerClass_info) classes.get (l)).writeInClassFormat (out);
        }
    }
    InnerClassesAttribute_info (final int attribute_name_index, final long attribute_length,
                                final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        final int class_count = bytes.readU2 ();
        final List classes = new ArrayList (class_count);
        for (int i = 0; i < class_count; ++ i)
        {
            classes.add (new InnerClass_info (bytes));
        }
        m_classes = classes;
    }
    private List m_classes; 
} 
