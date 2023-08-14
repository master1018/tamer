public abstract class ProxyReader extends FilterReader {
    public ProxyReader(Reader proxy) {
        super(proxy);
    }
    public int read() throws IOException {
        return in.read();
    }
    public int read(char[] chr) throws IOException {
        return in.read(chr);
    }
    public int read(char[] chr, int st, int end) throws IOException {
        return in.read(chr, st, end);
    }
    public long skip(long ln) throws IOException {
        return in.skip(ln);
    }
    public boolean ready() throws IOException {
        return in.ready();
    }
    public void close() throws IOException {
        in.close();
    }
    public synchronized void mark(int idx) throws IOException {
        in.mark(idx);
    }
    public synchronized void reset() throws IOException {
        in.reset();
    }
    public boolean markSupported() {
        return in.markSupported();
    }
}
