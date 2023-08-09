public class LoggingSessionOutputBuffer implements SessionOutputBuffer {
    private final SessionOutputBuffer out;
    private final Wire wire;
    public LoggingSessionOutputBuffer(final SessionOutputBuffer out, final Wire wire) {
        super();
        this.out = out;
        this.wire = wire;
    }
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b,  off,  len);
        if (this.wire.enabled()) {
            this.wire.output(b, off, len);
        }
    }
    public void write(int b) throws IOException {
        this.out.write(b);
        if (this.wire.enabled()) {
            this.wire.output(b);
        }
    }
    public void write(byte[] b) throws IOException {
        this.out.write(b);
        if (this.wire.enabled()) {
            this.wire.output(b);
        }
    }
    public void flush() throws IOException {
        this.out.flush();
    }
    public void writeLine(final CharArrayBuffer buffer) throws IOException {
        this.out.writeLine(buffer);
        if (this.wire.enabled()) {
            String s = new String(buffer.buffer(), 0, buffer.length());
            this.wire.output(s + "[EOL]");
        }
    }
    public void writeLine(final String s) throws IOException {
        this.out.writeLine(s);
        if (this.wire.enabled()) {
            this.wire.output(s + "[EOL]");
        }
    }
    public HttpTransportMetrics getMetrics() {
        return this.out.getMetrics();
    }
}
