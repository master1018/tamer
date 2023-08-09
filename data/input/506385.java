final class DeclaredExceptionTable implements IDeclaredExceptionTable
{
    public int get (final int offset)
    {
        return m_exceptions.get (offset);
    }
    public int size ()
    {
        return m_exceptions.size ();
    }
    public long length ()
    {
        return (1 + m_exceptions.size ()) << 1; 
    }
    public Object clone ()
    {
        try
        {
            final DeclaredExceptionTable _clone = (DeclaredExceptionTable) super.clone ();
            _clone.m_exceptions = (IntVector) m_exceptions.clone ();
            return _clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        int number_of_exceptions = m_exceptions.size (); 
        out.writeU2 (number_of_exceptions);
        for (int i = 0; i < number_of_exceptions; i++)
        {
            out.writeU2 (get (i));
        }
    }
    public int add (final int exception_index)
    {
        final int newoffset = m_exceptions.size (); 
        m_exceptions.add (exception_index);
        return newoffset;
    }
    public int set (final int offset, final int exception_index)
    {
        return m_exceptions.set (offset, exception_index);
    }
    DeclaredExceptionTable (final int capacity)
    {
         m_exceptions = capacity < 0 ? new IntVector () : new IntVector (capacity);
    }
    private IntVector m_exceptions;
} 
