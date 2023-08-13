final class ByteArrayOStream extends OutputStream
{
    public ByteArrayOStream (final int initialCapacity)
    {
        if ($assert.ENABLED)
            $assert.ASSERT (initialCapacity >= 0, "negative initial capacity: " + initialCapacity); 
        m_buf = new byte [initialCapacity];
    }
    public final ByteArrayIStream toByteIStream ()
    {
        return new ByteArrayIStream (m_buf, m_pos);
    }
    public final void write2 (final int b1, final int b2)
    {
        final int pos = m_pos;
        final int capacity = pos + 2;
        byte [] mbuf = m_buf;
        final int mbuflen = mbuf.length;
        if (mbuflen < capacity)
        {
            final byte [] newbuf = new byte [Math.max (mbuflen << 1, capacity)];
            if (pos < NATIVE_COPY_THRESHOLD)
                for (int i = 0; i < pos; ++ i) newbuf [i] = mbuf [i];
            else
                System.arraycopy (mbuf, 0, newbuf, 0, pos);
            m_buf = mbuf = newbuf;
        }
        mbuf [pos] = (byte) b1;
        mbuf [pos + 1] = (byte) b2;
        m_pos = capacity;
    }
    public final void write3 (final int b1, final int b2, final int b3)
    {
        final int pos = m_pos;
        final int capacity = pos + 3;
        byte [] mbuf = m_buf;
        final int mbuflen = mbuf.length;
        if (mbuflen < capacity)
        {
            final byte [] newbuf = new byte [Math.max (mbuflen << 1, capacity)];
            if (pos < NATIVE_COPY_THRESHOLD)
                for (int i = 0; i < pos; ++ i) newbuf [i] = mbuf [i];
            else
                System.arraycopy (mbuf, 0, newbuf, 0, pos);
            m_buf = mbuf = newbuf;
        }
        mbuf [pos] = (byte) b1;
        mbuf [pos + 1] = (byte) b2;
        mbuf [pos + 2] = (byte) b3;
        m_pos = capacity;
    }
    public final void write4 (final int b1, final int b2, final int b3, final int b4)
    {
        final int pos = m_pos;
        final int capacity = pos + 4;
        byte [] mbuf = m_buf;
        final int mbuflen = mbuf.length;
        if (mbuflen < capacity)
        {
            final byte [] newbuf = new byte [Math.max (mbuflen << 1, capacity)];
            if (pos < NATIVE_COPY_THRESHOLD)
                for (int i = 0; i < pos; ++ i) newbuf [i] = mbuf [i];
            else
                System.arraycopy (mbuf, 0, newbuf, 0, pos);
            m_buf = mbuf = newbuf;
        }
        mbuf [pos] = (byte) b1;
        mbuf [pos + 1] = (byte) b2;
        mbuf [pos + 2] = (byte) b3;
        mbuf [pos + 3] = (byte) b4;
        m_pos = capacity;
    }
    public final void writeTo (final OutputStream out)
        throws IOException
    {
        out.write (m_buf, 0, m_pos);
    }
    public final byte [] getByteArray ()
    {
        return m_buf;
    }
    public final byte [] copyByteArray ()
    {
        final int pos = m_pos;
        final byte [] result = new byte [pos];
        final byte [] mbuf = m_buf;
        if (pos < NATIVE_COPY_THRESHOLD)
            for (int i = 0; i < pos; ++ i) result [i] = mbuf [i];
        else
            System.arraycopy (mbuf, 0, result, 0, pos);
        return result;
    }
    public final int size ()
    {
        return m_pos;
    }
    public final int capacity ()
    {
        return m_buf.length;
    }
    public final void reset ()
    {
        m_pos = 0;
    }
    public final void write (final int b)
    {
        final int pos = m_pos;
        final int capacity = pos + 1;
        byte [] mbuf = m_buf;
        final int mbuflen = mbuf.length;
        if (mbuflen < capacity)
        {
            final byte [] newbuf = new byte [Math.max (mbuflen << 1, capacity)];
            if (pos < NATIVE_COPY_THRESHOLD)
                for (int i = 0; i < pos; ++ i) newbuf [i] = mbuf [i];
            else
                System.arraycopy (mbuf, 0, newbuf, 0, pos);
            m_buf = mbuf = newbuf;
        }
        mbuf [pos] = (byte) b;
        m_pos = capacity;
    }
    public final void write (final byte [] buf, final int offset, final int length)
    {
        if ($assert.ENABLED)
            $assert.ASSERT ((offset >= 0) && (offset <= buf.length) &&
                (length >= 0) && ((offset + length) <= buf.length),
                "invalid input (" + buf.length + ", " + offset + ", " + length + ")");
        final int pos = m_pos;
        final int capacity = pos + length;
        byte [] mbuf = m_buf;
        final int mbuflen = mbuf.length;
        if (mbuflen < capacity)
        {
            final byte [] newbuf = new byte [Math.max (mbuflen << 1, capacity)];
            if (pos < NATIVE_COPY_THRESHOLD)
                for (int i = 0; i < pos; ++ i) newbuf [i] = mbuf [i];
            else
                System.arraycopy (mbuf, 0, newbuf, 0, pos);
            m_buf = mbuf = newbuf;
        }
        if (length < NATIVE_COPY_THRESHOLD)
            for (int i = 0; i < length; ++ i) mbuf [pos + i] = buf [offset + i];
        else
            System.arraycopy (buf, offset, mbuf, pos, length);
        m_pos = capacity; 
    }
    public final void close ()
    {
        reset ();
    }
    private byte [] m_buf;
    private int m_pos;
    private static final int NATIVE_COPY_THRESHOLD  = 9;
} 
