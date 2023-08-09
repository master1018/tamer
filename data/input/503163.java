public class CloseShieldInputStream extends ProxyInputStream {
    public CloseShieldInputStream(InputStream in) {
        super(in);
    }
    public void close() {
        in = new ClosedInputStream();
    }
}
