class ChunkedOutputStream extends FilterOutputStream
{
    private boolean closed = false;
    final static int CHUNK_SIZE = 4096;
    final static int OFFSET = 6; 
    private int pos = OFFSET;
    private int count = 0;
    private byte[] buf = new byte [CHUNK_SIZE+OFFSET+2];
    ExchangeImpl t;
    ChunkedOutputStream (ExchangeImpl t, OutputStream src) {
        super (src);
        this.t = t;
    }
    public void write (int b) throws IOException {
        if (closed) {
            throw new StreamClosedException ();
        }
        buf [pos++] = (byte)b;
        count ++;
        if (count == CHUNK_SIZE) {
            writeChunk();
        }
        assert count < CHUNK_SIZE;
    }
    public void write (byte[]b, int off, int len) throws IOException {
        if (closed) {
            throw new StreamClosedException ();
        }
        int remain = CHUNK_SIZE - count;
        if (len > remain) {
            System.arraycopy (b,off,buf,pos,remain);
            count = CHUNK_SIZE;
            writeChunk();
            len -= remain;
            off += remain;
            while (len >= CHUNK_SIZE) {
                System.arraycopy (b,off,buf,OFFSET,CHUNK_SIZE);
                len -= CHUNK_SIZE;
                off += CHUNK_SIZE;
                count = CHUNK_SIZE;
                writeChunk();
            }
        }
        if (len > 0) {
            System.arraycopy (b,off,buf,pos,len);
            count += len;
            pos += len;
        }
        if (count == CHUNK_SIZE) {
            writeChunk();
        }
    }
    private void writeChunk () throws IOException {
        char[] c = Integer.toHexString (count).toCharArray();
        int clen = c.length;
        int startByte = 4 - clen;
        int i;
        for (i=0; i<clen; i++) {
            buf[startByte+i] = (byte)c[i];
        }
        buf[startByte + (i++)] = '\r';
        buf[startByte + (i++)] = '\n';
        buf[startByte + (i++) + count] = '\r';
        buf[startByte + (i++) + count] = '\n';
        out.write (buf, startByte, i+count);
        count = 0;
        pos = OFFSET;
    }
    public void close () throws IOException {
        if (closed) {
            return;
        }
        flush();
        try {
            writeChunk();
            out.flush();
            LeftOverInputStream is = t.getOriginalInputStream();
            if (!is.isClosed()) {
                is.close();
            }
        } catch (IOException e) {
        } finally {
            closed = true;
        }
        WriteFinishedEvent e = new WriteFinishedEvent (t);
        t.getHttpContext().getServerImpl().addEvent (e);
    }
    public void flush () throws IOException {
        if (closed) {
            throw new StreamClosedException ();
        }
        if (count > 0) {
            writeChunk();
        }
        out.flush();
    }
}
