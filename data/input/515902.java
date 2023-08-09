public class Base64OutputStream extends FilterOutputStream {
    private final Base64.Coder coder;
    private final int flags;
    private byte[] buffer = null;
    private int bpos = 0;
    private static byte[] EMPTY = new byte[0];
    public Base64OutputStream(OutputStream out, int flags) {
        this(out, flags, true);
    }
    public Base64OutputStream(OutputStream out, int flags, boolean encode) {
        super(out);
        this.flags = flags;
        if (encode) {
            coder = new Base64.Encoder(flags, null);
        } else {
            coder = new Base64.Decoder(flags, null);
        }
    }
    public void write(int b) throws IOException {
        if (buffer == null) {
            buffer = new byte[1024];
        }
        if (bpos >= buffer.length) {
            internalWrite(buffer, 0, bpos, false);
            bpos = 0;
        }
        buffer[bpos++] = (byte) b;
    }
    private void flushBuffer() throws IOException {
        if (bpos > 0) {
            internalWrite(buffer, 0, bpos, false);
            bpos = 0;
        }
    }
    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) return;
        flushBuffer();
        internalWrite(b, off, len, false);
    }
    public void close() throws IOException {
        IOException thrown = null;
        try {
            flushBuffer();
            internalWrite(EMPTY, 0, 0, true);
        } catch (IOException e) {
            thrown = e;
        }
        try {
            if ((flags & Base64.NO_CLOSE) == 0) {
                out.close();
            } else {
                out.flush();
            }
        } catch (IOException e) {
            if (thrown != null) {
                thrown = e;
            }
        }
        if (thrown != null) {
            throw thrown;
        }
    }
    private void internalWrite(byte[] b, int off, int len, boolean finish) throws IOException {
        coder.output = embiggen(coder.output, coder.maxOutputSize(len));
        if (!coder.process(b, off, len, finish)) {
            throw new IOException("bad base-64");
        }
        out.write(coder.output, 0, coder.op);
    }
    private byte[] embiggen(byte[] b, int len) {
        if (b == null || b.length < len) {
            return new byte[len];
        } else {
            return b;
        }
    }
}
