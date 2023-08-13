public class ByteArrayInputStream extends InputStream {
    protected byte[] buf;
    protected int pos;
    protected int mark;
    protected int count;
    public ByteArrayInputStream(byte buf[]) {
        this.mark = 0;
        this.buf = buf;
        this.count = buf.length;
    }
    public ByteArrayInputStream(byte[] buf, int offset, int length) {
        this.buf = buf;
        pos = offset;
        mark = offset;
        count = offset + length > buf.length ? buf.length : offset + length;
    }
    @Override
    public synchronized int available() {
        return count - pos;
    }
    @Override
    public void close() throws IOException {
    }
    @Override
    public synchronized void mark(int readlimit) {
        mark = pos;
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public synchronized int read() {
        return pos < count ? buf[pos++] & 0xFF : -1;
    }
    @Override
    public synchronized int read(byte[] b, int offset, int length) {
        if (b == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | length) < 0 || length > b.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (this.pos >= this.count) {
            return -1;
        }
        if (length == 0) {
            return 0;
        }
        int copylen = this.count - pos < length ? this.count - pos : length;
        System.arraycopy(buf, pos, b, offset, copylen);
        pos += copylen;
        return copylen;
    }
    @Override
    public synchronized void reset() {
        pos = mark;
    }
    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }
        int temp = pos;
        pos = this.count - pos < n ? this.count : (int) (pos + n);
        return pos - temp;
    }
}
