abstract class LeftOverInputStream extends FilterInputStream {
    ExchangeImpl t;
    ServerImpl server;
    protected boolean closed = false;
    protected boolean eof = false;
    byte[] one = new byte [1];
    public LeftOverInputStream (ExchangeImpl t, InputStream src) {
        super (src);
        this.t = t;
        this.server = t.getServerImpl();
    }
    public boolean isDataBuffered () throws IOException {
        assert eof;
        return super.available() > 0;
    }
    public void close () throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (!eof) {
            eof = drain (ServerConfig.getDrainAmount());
        }
    }
    public boolean isClosed () {
        return closed;
    }
    public boolean isEOF () {
        return eof;
    }
    protected abstract int readImpl (byte[]b, int off, int len) throws IOException;
    public synchronized int read () throws IOException {
        if (closed) {
            throw new IOException ("Stream is closed");
        }
        int c = readImpl (one, 0, 1);
        if (c == -1 || c == 0) {
            return c;
        } else {
            return one[0] & 0xFF;
        }
    }
    public synchronized int read (byte[]b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException ("Stream is closed");
        }
        return readImpl (b, off, len);
    }
    public boolean drain (long l) throws IOException {
        int bufSize = 2048;
        byte[] db = new byte [bufSize];
        while (l > 0) {
            long len = readImpl (db, 0, bufSize);
            if (len == -1) {
                eof = true;
                return true;
            } else {
                l = l - len;
            }
        }
        return false;
    }
}
