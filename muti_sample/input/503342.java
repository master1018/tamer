public class CloseShieldOutputStream extends ProxyOutputStream {
    public CloseShieldOutputStream(OutputStream out) {
        super(out);
    }
    public void close() {
        out = new ClosedOutputStream();
    }
}
