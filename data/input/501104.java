public abstract class AbstractSessionOutputBuffer implements SessionOutputBuffer {
    private static final byte[] CRLF = new byte[] {HTTP.CR, HTTP.LF};
    private static final int MAX_CHUNK = 256;
    private OutputStream outstream;
    private ByteArrayBuffer buffer;
    private String charset = HTTP.US_ASCII;
    private boolean ascii = true;
    private HttpTransportMetricsImpl metrics;
    protected void init(final OutputStream outstream, int buffersize, final HttpParams params) {
        if (outstream == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        if (buffersize <= 0) {
            throw new IllegalArgumentException("Buffer size may not be negative or zero");
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.outstream = outstream;
        this.buffer = new ByteArrayBuffer(buffersize);
        this.charset = HttpProtocolParams.getHttpElementCharset(params); 
        this.ascii = this.charset.equalsIgnoreCase(HTTP.US_ASCII)
                     || this.charset.equalsIgnoreCase(HTTP.ASCII);
        this.metrics = new HttpTransportMetricsImpl();
    }
    protected void flushBuffer() throws IOException {
        int len = this.buffer.length();
        if (len > 0) {
            this.outstream.write(this.buffer.buffer(), 0, len);
            this.buffer.clear();
            this.metrics.incrementBytesTransferred(len);
        }
    }
    public void flush() throws IOException {
        flushBuffer();
        this.outstream.flush();
    }
    public void write(final byte[] b, int off, int len) throws IOException {
        if (b == null) {
            return;
        }
        if (len > MAX_CHUNK || len > this.buffer.capacity()) {
            flushBuffer();
            this.outstream.write(b, off, len);
            this.metrics.incrementBytesTransferred(len);
        } else {
            int freecapacity = this.buffer.capacity() - this.buffer.length();
            if (len > freecapacity) {
                flushBuffer();
            }
            this.buffer.append(b, off, len);
        }
    }
    public void write(final byte[] b) throws IOException {
        if (b == null) {
            return;
        }
        write(b, 0, b.length);
    }
    public void write(int b) throws IOException {
        if (this.buffer.isFull()) {
            flushBuffer();
        }
        this.buffer.append(b);
    }
    public void writeLine(final String s) throws IOException {
        if (s == null) {
            return;
        }
        if (s.length() > 0) {
            write(s.getBytes(this.charset));
        }
        write(CRLF);
    }
    public void writeLine(final CharArrayBuffer s) throws IOException {
        if (s == null) {
            return;
        }
        if (this.ascii) {
            int off = 0;
            int remaining = s.length();
            while (remaining > 0) {
                int chunk = this.buffer.capacity() - this.buffer.length();
                chunk = Math.min(chunk, remaining);
                if (chunk > 0) {
                    this.buffer.append(s, off, chunk);
                }
                if (this.buffer.isFull()) {
                    flushBuffer();
                }
                off += chunk;
                remaining -= chunk;
            }
        } else {
            byte[] tmp = s.toString().getBytes(this.charset);
            write(tmp);
        }
        write(CRLF);
    }
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}
