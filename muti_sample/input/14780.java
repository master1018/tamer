class UndefLengthOutputStream extends FilterOutputStream
{
    private boolean closed = false;
    ExchangeImpl t;
    UndefLengthOutputStream (ExchangeImpl t, OutputStream src) {
        super (src);
        this.t = t;
    }
    public void write (int b) throws IOException {
        if (closed) {
            throw new IOException ("stream closed");
        }
        out.write(b);
    }
    public void write (byte[]b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException ("stream closed");
        }
        out.write(b, off, len);
    }
    public void close () throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        flush();
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
