final class ExceptionHandlerTable implements IExceptionHandlerTable
{
    public Exception_info get (final int offset)
    {
        return (Exception_info) m_exceptions.get (offset);
    }
    public int size ()
    {
        return m_exceptions.size ();
    }
    public long length ()
    {
        return 2 + (m_exceptions.size () << 3); 
    }
    public Object clone ()
    {
        try
        {
            final ExceptionHandlerTable _clone = (ExceptionHandlerTable) super.clone ();
            final int exceptions_count = m_exceptions.size (); 
            _clone.m_exceptions = new ArrayList (exceptions_count);
            for (int e = 0; e < exceptions_count; ++ e)
            {
                _clone.m_exceptions.add (((Exception_info) m_exceptions.get (e)).clone ());
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
        int exception_table_length = m_exceptions.size (); 
        out.writeU2 (exception_table_length);
        for (int i = 0; i < exception_table_length; i++)
        {
            get (i).writeInClassFormat (out);
        }
    }
    public int add (final Exception_info exception)
    {
        final int newoffset = m_exceptions.size (); 
        m_exceptions.add (exception);
        return newoffset;
    }
    public Exception_info set (final int offset, final Exception_info exception)
    {
        return (Exception_info) m_exceptions.set (offset, exception);
    }
    ExceptionHandlerTable (final int capacity)
    {
        m_exceptions = capacity < 0 ? new ArrayList () : new ArrayList (capacity);
    }
    private List m_exceptions;
} 
