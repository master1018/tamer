public class DemuxOutputStream
    extends OutputStream
{
    private InheritableThreadLocal m_streams = new InheritableThreadLocal();
    public OutputStream bindStream( OutputStream output )
    {
        OutputStream stream = getStream();
        m_streams.set( output );
        return stream;
    }
    public void close()
        throws IOException
    {
        OutputStream output = getStream();
        if( null != output )
        {
            output.close();
        }
    }
    public void flush()
        throws IOException
    {
        OutputStream output = getStream();
        if( null != output )
        {
            output.flush();
        }
    }
    public void write( int ch )
        throws IOException
    {
        OutputStream output = getStream();
        if( null != output )
        {
            output.write( ch );
        }
    }
    private OutputStream getStream()
    {
        return (OutputStream)m_streams.get();
    }
}
