public abstract class FilterReader extends Reader {
    protected Reader in;
    protected FilterReader(Reader in) {
        super(in);
        this.in = in;
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            in.close();
        }
    }
    @Override
    public synchronized void mark(int readlimit) throws IOException {
        synchronized (lock) {
            in.mark(readlimit);
        }
    }
    @Override
    public boolean markSupported() {
        synchronized (lock) {
            return in.markSupported();
        }
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            return in.read();
        }
    }
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            return in.read(buffer, offset, count);
        }
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            return in.ready();
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            in.reset();
        }
    }
    @Override
    public long skip(long count) throws IOException {
        synchronized (lock) {
            return in.skip(count);
        }
    }
}
