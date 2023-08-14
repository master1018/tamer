class FixedLengthOutputStream extends FilterOutputStream
{
    private long remaining;
    private boolean eof = false;
    private boolean closed = false;
    ExchangeImpl t;
    FixedLengthOutputStream (ExchangeImpl t, OutputStream src, long len) {
        super (src);
        this.t = t;
        this.remaining = len;
    }
    public void write (int b) throws IOException {
        if (closed) {
            throw new IOException ("stream closed");
        }
        eof = (remaining == 0);
        if (eof) {
            throw new StreamClosedException();
        }
        out.write(b);
        remaining --;
    }
    public void write (byte[]b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException ("stream closed");
        }
        eof = (remaining == 0);
        if (eof) {
            throw new StreamClosedException();
        }
        if (len > remaining) {
            throw new IOException ("too many bytes to write to stream");
        }
        out.write(b, off, len);
        remaining -= len;
    }
    public void close () throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (remaining > 0) {
            t.close();
            throw new IOException ("insufficient bytes written to stream");
        }
        flush();
        eof = true;
        LeftOverInputStream is = t.getOriginalInputStream();
        if (!is.isClosed()) {
            try {
                is.close();
            } catch (IOException e) {}
        }
        WriteFinishedEvent e = new WriteFinishedEvent (t);
        t.getHttpContext().getServerImpl().addEvent (e);
    }
}
