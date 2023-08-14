final class FieldCollection implements IFieldCollection
{
    public Field_info get (final int offset)
    {
        return (Field_info) m_fields.get (offset);
    }
    public int [] get (final ClassDef cls, final String name)
    {
        if (cls == null) throw new IllegalArgumentException  ("null input: cls");
        final int count = m_fields.size (); 
        final IntVector result = new IntVector (count);
        for (int f = 0; f < count; ++ f)
        {
            final Field_info field = (Field_info) m_fields.get (f);
            if (field.getName (cls).equals (name))  
                result.add (f);
        }
        return result.values (); 
    }
    public int size ()
    {
        return m_fields.size ();
    }
    public Object clone ()
    {
        try
        {
            final FieldCollection _clone = (FieldCollection) super.clone ();
            final int fields_count = m_fields.size (); 
            _clone.m_fields = new ArrayList (fields_count);
            for (int f = 0; f < fields_count; ++ f)
            {
                _clone.m_fields.add (((Field_info) m_fields.get (f)).clone ());
            }
            return _clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        final int fields_count = m_fields.size (); 
        out.writeU2 (fields_count);
        for (int i = 0; i < fields_count; i++)
        {
            get (i).writeInClassFormat (out);
        }
    }
    public void accept (final IClassDefVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public int add (final Field_info field)
    {
        final int newoffset = m_fields.size (); 
        m_fields.add (field);
        return newoffset;
    }
    public Field_info set (final int offset, final Field_info field)
    {
        return (Field_info) m_fields.set (offset, field);
    }
    FieldCollection (final int capacity)
    {
        m_fields = capacity < 0 ? new ArrayList () : new ArrayList (capacity);
    }
    private List m_fields; 
} 
