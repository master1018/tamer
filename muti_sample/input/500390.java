public class IdentityOutputStream extends OutputStream {
    private final SessionOutputBuffer out;
    private boolean closed = false;
    public IdentityOutputStream(final SessionOutputBuffer out) {
        super();
        if (out == null) {
            throw new IllegalArgumentException("Session output buffer may not be null");
        }
        this.out = out;
    }
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.out.flush();
        }
    }
    public void flush() throws IOException {
        this.out.flush();
    }
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        this.out.write(b, off, len);
    }
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    public void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        this.out.write(b);
    }
}
