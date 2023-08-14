class ByteBufferInputStream extends InputStream {
    ByteBuffer bb;
    ByteBufferInputStream(ByteBuffer bb) {
        this.bb = bb;
    }
    public int read() throws IOException {
        if (bb == null) {
            throw new IOException("read on a closed InputStream");
        }
        if (bb.remaining() == 0) {
            return -1;
        }
        return bb.get();
    }
    public int read(byte b[]) throws IOException {
        if (bb == null) {
            throw new IOException("read on a closed InputStream");
        }
        return read(b, 0, b.length);
    }
    public int read(byte b[], int off, int len) throws IOException {
        if (bb == null) {
            throw new IOException("read on a closed InputStream");
        }
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        int length = Math.min(bb.remaining(), len);
        if (length == 0) {
            return -1;
        }
        bb.get(b, off, length);
        return length;
    }
    public long skip(long n) throws IOException {
        if (bb == null) {
            throw new IOException("skip on a closed InputStream");
        }
        if (n <= 0) {
            return 0;
        }
        int nInt = (int) n;
        int skip = Math.min(bb.remaining(), nInt);
        bb.position(bb.position() + skip);
        return nInt;
    }
    public int available() throws IOException {
        if (bb == null) {
            throw new IOException("available on a closed InputStream");
        }
        return bb.remaining();
    }
    public void close() throws IOException {
        bb = null;
    }
    public synchronized void mark(int readlimit) {}
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
    public boolean markSupported() {
        return false;
    }
}
