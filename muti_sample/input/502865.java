final class Exception_info implements Cloneable, IClassFormatOutput
{
    public int m_start_pc, m_end_pc, m_handler_pc, m_catch_type;
    public Exception_info (final int start_pc, final int end_pc,
                           final int handler_pc, final int catch_type)
    {
        m_start_pc = start_pc;
        m_end_pc = end_pc;
        m_handler_pc = handler_pc;
        m_catch_type = catch_type;
    }
    public String toString ()
    {
        return "exception_info: [start_pc/end_pc = " + m_start_pc + '/' + m_end_pc +
               ", handler_pc = " + m_handler_pc +
               ", catch_type = " + m_catch_type + ']';
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
        out.writeU2 (m_start_pc);
        out.writeU2 (m_end_pc);
        out.writeU2 (m_handler_pc);
        out.writeU2 (m_catch_type);
    }
    Exception_info (final UDataInputStream bytes) throws IOException
    {
        m_start_pc = bytes.readU2 ();
        m_end_pc = bytes.readU2 ();
        m_handler_pc = bytes.readU2 ();
        m_catch_type = bytes.readU2 ();
    }
} 
