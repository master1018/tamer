public class PositionInputStream extends FilterInputStream {
    private long position = 0L;
    public PositionInputStream(InputStream in) {
        super(in);
    }
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) position++;
        return res;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res != -1) position += res;
        return res;
    }
    public long skip(long n) throws IOException {
        long res = super.skip(n);
        position += res;
        return res;
    }
    public boolean markSupported() {
        return false;
    }
    public void mark(int readLimit) {
        throw new UnsupportedOperationException("mark");
    }
    public void reset() {
        throw new UnsupportedOperationException("reset");
    }
    public long position() {
        return position;
    }
}
