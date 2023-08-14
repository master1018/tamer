public abstract class ProxyInputStream extends FilterInputStream {
    public ProxyInputStream(InputStream proxy) {
        super(proxy);
    }
    public int read() throws IOException {
        return in.read();
    }
    public int read(byte[] bts) throws IOException {
        return in.read(bts);
    }
    public int read(byte[] bts, int st, int end) throws IOException {
        return in.read(bts, st, end);
    }
    public long skip(long ln) throws IOException {
        return in.skip(ln);
    }
    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
        in.close();
    }
    public synchronized void mark(int idx) {
        in.mark(idx);
    }
    public synchronized void reset() throws IOException {
        in.reset();
    }
    public boolean markSupported() {
        return in.markSupported();
    }
}
