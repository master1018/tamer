final class ByteArrayIStream extends InputStream
{
    public ByteArrayIStream (final byte [] buf)
    {
        this (buf, buf.length);
    }
    public ByteArrayIStream (final byte [] buf, final int length)
    {
        if ($assert.ENABLED) $assert.ASSERT ((length >= 0) && (length <= buf.length),
            "invalid length: " + length);
        m_buf = buf;
        m_max = length;
    }    
    public final int read ()
    {
        if (m_pos >= m_max)
            return -1;
        else
            return 0xFF & m_buf [m_pos ++];
    }
    public final int read (final byte [] buf, final int offset, int length)
    {
        if ($assert.ENABLED)
            $assert.ASSERT ((offset >= 0) && (offset <= buf.length) &&
                (length >= 0) && ((offset + length) <= buf.length),
                "invalid input (" + buf.length + ", " + offset + ", " + length + ")");
        final int pos = m_pos;
        final int max = m_max;
        if (pos >= max) return -1;
        if (pos + length > max) length = max - pos;
        if (length <= 0) return 0;
        final byte [] mbuf = m_buf;
        if (length < NATIVE_COPY_THRESHOLD)
            for (int i = 0; i < length; ++ i) buf [offset + i] = mbuf [pos + i];
        else
            System.arraycopy (mbuf, pos, buf, offset, length);
        m_pos += length;
        return length;
    }
    public final int available ()
    {
        return m_max - m_pos;
    }
    public final long skip (long n)
    {
        if (m_pos + n > m_max) n = m_max - m_pos;
        if (n < 0) return 0;        
        m_pos += n;
        return n;
    }
    public final void reset ()
    {
        m_pos = 0;
    }
    public final void close ()
    {
        reset ();
    }
    private final byte [] m_buf;
    private final int m_max;
    private int m_pos;
    private static final int NATIVE_COPY_THRESHOLD  = 9;
} 
