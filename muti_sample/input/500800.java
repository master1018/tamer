public class ContentLengthOutputStream extends OutputStream {
    private final SessionOutputBuffer out;
    private final long contentLength;
    private long total = 0;
    private boolean closed = false;
    public ContentLengthOutputStream(final SessionOutputBuffer out, long contentLength) {
        super();
        if (out == null) {
            throw new IllegalArgumentException("Session output buffer may not be null");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("Content length may not be negative");
        }
        this.out = out;
        this.contentLength = contentLength;
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
        if (this.total < this.contentLength) {
            long max = this.contentLength - this.total;
            if (len > max) {
                len = (int) max;
            }
            this.out.write(b, off, len);
            this.total += len;
        }
    }
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    public void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        if (this.total < this.contentLength) {
            this.out.write(b);
            this.total++;
        }
    }
}
