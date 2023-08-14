public class FilterInputStream extends InputStream {
    protected volatile InputStream in;
    protected FilterInputStream(InputStream in) {
        super();
        this.in = in;
    }
    @Override
    public int available() throws IOException {
        return in.available();
    }
    @Override
    public void close() throws IOException {
        in.close();
    }
    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
    @Override
    public int read() throws IOException {
        return in.read();
    }
    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        return in.read(buffer, offset, count);
    }
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }
    @Override
    public long skip(long count) throws IOException {
        return in.skip(count);
    }
}
