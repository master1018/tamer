class LogInputStream extends InputStream {
    private InputStream in;
    private int length;
    public LogInputStream(InputStream in, int length) throws IOException {
        this.in = in;
        this.length = length;
    }
    public int read() throws IOException {
        if (length == 0)
            return -1;
        int c = in.read();
        length = (c != -1) ? length - 1 : 0;
        return c;
    }
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte b[], int off, int len) throws IOException {
        if (length == 0)
            return -1;
        len = (length < len) ? length : len;
        int n = in.read(b, off, len);
        length = (n != -1) ? length - n : 0;
        return n;
    }
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE)
            throw new IOException("Too many bytes to skip - " + n);
        if (length == 0)
            return 0;
        n = (length < n) ? length : n;
        n = in.skip(n);
        length -= n;
        return n;
    }
    public int available() throws IOException {
        int avail = in.available();
        return (length < avail) ? length : avail;
    }
    public void close() {
        length = 0;
    }
    protected void finalize() throws IOException {
        close();
    }
}
