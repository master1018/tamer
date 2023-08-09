class HttpSendInputStream extends FilterInputStream {
    HttpSendSocket owner;
    public HttpSendInputStream(InputStream in, HttpSendSocket owner)
        throws IOException
    {
        super(in);
        this.owner = owner;
    }
    public void deactivate()
    {
        in = null;
    }
    public int read() throws IOException
    {
        if (in == null)
            in = owner.readNotify();
        return in.read();
    }
    public int read(byte b[], int off, int len) throws IOException
    {
        if (len == 0)
            return 0;
        if (in == null)
            in = owner.readNotify();
        return in.read(b, off, len);
    }
    public long skip(long n) throws IOException
    {
        if (n == 0)
            return 0;
        if (in == null)
            in = owner.readNotify();
        return in.skip(n);
    }
    public int available() throws IOException
    {
        if (in == null)
            in = owner.readNotify();
        return in.available();
    }
    public void close() throws IOException
    {
        owner.close();
    }
    public synchronized void mark(int readlimit)
    {
        if (in == null) {
            try {
                in = owner.readNotify();
            }
            catch (IOException e) {
                return;
            }
        }
        in.mark(readlimit);
    }
    public synchronized void reset() throws IOException
    {
        if (in == null)
            in = owner.readNotify();
        in.reset();
    }
    public boolean markSupported()
    {
        if (in == null) {
            try {
                in = owner.readNotify();
            }
            catch (IOException e) {
                return false;
            }
        }
        return in.markSupported();
    }
}
