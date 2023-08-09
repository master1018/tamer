final class LineNumber_info implements Cloneable, IClassFormatOutput
{
    public int m_start_pc, m_line_number;
    public LineNumber_info (final int start_pc, final int line_number)
    {
        m_start_pc = start_pc;
        m_line_number = line_number;
    }
    public String toString ()
    {
        return "line_number_info: [start_pc = " + m_start_pc + ", line_number = " + m_line_number + "]";
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
        out.writeU2 (m_line_number);
    }
    LineNumber_info (final UDataInputStream bytes) throws IOException
    {
        m_start_pc = bytes.readU2 ();
        m_line_number = bytes.readU2 ();
    }
} 
