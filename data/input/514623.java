final class HTMLWriter
{
    public HTMLWriter (final Writer out)
    {
        if (out == null) throw new IllegalArgumentException ("null input: out");
        m_out = out;
    }
    public void write (final String s)
    {
        if ($assert.ENABLED) $assert.ASSERT (s != null, "s = null");
        if (m_out != null)
        {
            try
            {
                m_out.write (s);
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
        }
    }
    public void write (final char c)
    {
        if (m_out != null)
        {
            try
            {
                m_out.write (c);
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
        }
    }
    public void eol ()
    {
        if (m_out != null)
        {
            try
            {
                m_out.write (IConstants.EOL);
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
        }
    }
    public void flush ()
    {
        if (m_out != null)
        {
            try
            {
                m_out.flush ();
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
        }
    }
    public void close ()
    {
        if (m_out != null)
        {
            try { m_out.close (); } catch (IOException ignore) {}
            m_out = null;
        }
    }
    private Writer m_out;
} 
