final class ExceptionsAttribute_info extends Attribute_info
{
    public ExceptionsAttribute_info (final int attribute_name_index,
                                     final IDeclaredExceptionTable exceptions)
    {
        super (attribute_name_index, exceptions.length ());
        m_exceptions = exceptions;
    }
    public IDeclaredExceptionTable getDeclaredExceptions ()
    {
        return m_exceptions;
    }
    public long length ()
    {
        return 6 + m_exceptions.length ();
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "ExceptionsAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + ']';
    }
    public Object clone ()
    {
        final ExceptionsAttribute_info _clone = (ExceptionsAttribute_info) super.clone ();
        _clone.m_exceptions = (IDeclaredExceptionTable) m_exceptions.clone ();
        return _clone;        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        m_exceptions.writeInClassFormat (out);
    }
    ExceptionsAttribute_info (final int attribute_name_index, final long attribute_length,
                              final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        final int number_of_exceptions = bytes.readU2 ();
        m_exceptions = new DeclaredExceptionTable (number_of_exceptions);
        for (int i = 0; i < number_of_exceptions; i++)
        {
            final int exception_index = bytes.readU2 ();
            m_exceptions.add (exception_index);
        }
    }
    private IDeclaredExceptionTable m_exceptions;
} 
