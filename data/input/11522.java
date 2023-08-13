class FixedLengthInputStream extends LeftOverInputStream {
    private long remaining;
    FixedLengthInputStream (ExchangeImpl t, InputStream src, long len) {
        super (t, src);
        this.remaining = len;
    }
    protected int readImpl (byte[]b, int off, int len) throws IOException {
        eof = (remaining == 0L);
        if (eof) {
            return -1;
        }
        if (len > remaining) {
            len = (int)remaining;
        }
        int n = in.read(b, off, len);
        if (n > -1) {
            remaining -= n;
            if (remaining == 0) {
                t.getServerImpl().requestCompleted (t.getConnection());
            }
        }
        return n;
    }
    public int available () throws IOException {
        if (eof) {
            return 0;
        }
        int n = in.available();
        return n < remaining? n: (int)remaining;
    }
    public boolean markSupported () {return false;}
    public void mark (int l) {
    }
    public void reset () throws IOException {
        throw new IOException ("mark/reset not supported");
    }
}
