class AppInputStream extends InputStream {
    private final static byte[] SKIP_ARRAY = new byte[1024];
    private SSLSocketImpl c;
    InputRecord r;
    private final byte[] oneByte = new byte[1];
    AppInputStream(SSLSocketImpl conn) {
        r = new InputRecord();
        c = conn;
    }
    public int available() throws IOException {
        if (c.checkEOF() || (r.isAppDataValid() == false)) {
            return 0;
        }
        return r.available();
    }
    public synchronized int read() throws IOException {
        int n = read(oneByte, 0, 1);
        if (n <= 0) { 
            return -1;
        }
        return oneByte[0] & 0xff;
    }
    public synchronized int read(byte b[], int off, int len)
            throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (c.checkEOF()) {
            return -1;
        }
        try {
            while (r.available() == 0) {
                c.readDataRecord(r);
                if (c.checkEOF()) {
                    return -1;
                }
            }
            int howmany = Math.min(len, r.available());
            howmany = r.read(b, off, howmany);
            return howmany;
        } catch (Exception e) {
            c.handleException(e);
            return -1;
        }
    }
    public synchronized long skip(long n) throws IOException {
        long skipped = 0;
        while (n > 0) {
            int len = (int)Math.min(n, SKIP_ARRAY.length);
            int r = read(SKIP_ARRAY, 0, len);
            if (r <= 0) {
                break;
            }
            n -= r;
            skipped += r;
        }
        return skipped;
    }
    public void close() throws IOException {
        c.close();
    }
}
