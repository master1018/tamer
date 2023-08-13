public class AutoCloseInputStream extends ProxyInputStream {
    public AutoCloseInputStream(InputStream in) {
        super(in);
    }
    public void close() throws IOException {
        in.close();
        in = new ClosedInputStream();
    }
    public int read() throws IOException {
        int n = in.read();
        if (n == -1) {
            close();
        }
        return n;
    }
    public int read(byte[] b) throws IOException {
        int n = in.read(b);
        if (n == -1) {
            close();
        }
        return n;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        if (n == -1) {
            close();
        }
        return n;
    }
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
