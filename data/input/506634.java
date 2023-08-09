public class ProxyOutputStream extends FilterOutputStream {
    public ProxyOutputStream(OutputStream proxy) {
        super(proxy);
    }
    public void write(int idx) throws IOException {
        out.write(idx);
    }
    public void write(byte[] bts) throws IOException {
        out.write(bts);
    }
    public void write(byte[] bts, int st, int end) throws IOException {
        out.write(bts, st, end);
    }
    public void flush() throws IOException {
        out.flush();
    }
    public void close() throws IOException {
        out.close();
    }
}
