public class Base64InputStream extends FilterInputStream {
    private final Base64.Coder coder;
    private static byte[] EMPTY = new byte[0];
    private static final int BUFFER_SIZE = 2048;
    private boolean eof;
    private byte[] inputBuffer;
    private int outputStart;
    private int outputEnd;
    public Base64InputStream(InputStream in, int flags) {
        this(in, flags, false);
    }
    public Base64InputStream(InputStream in, int flags, boolean encode) {
        super(in);
        eof = false;
        inputBuffer = new byte[BUFFER_SIZE];
        if (encode) {
            coder = new Base64.Encoder(flags, null);
        } else {
            coder = new Base64.Decoder(flags, null);
        }
        coder.output = new byte[coder.maxOutputSize(BUFFER_SIZE)];
        outputStart = 0;
        outputEnd = 0;
    }
    public boolean markSupported() {
        return false;
    }
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }
    public void reset() {
        throw new UnsupportedOperationException();
    }
    public void close() throws IOException {
        in.close();
        inputBuffer = null;
    }
    public int available() {
        return outputEnd - outputStart;
    }
    public long skip(long n) throws IOException {
        if (outputStart >= outputEnd) {
            refill();
        }
        if (outputStart >= outputEnd) {
            return 0;
        }
        long bytes = Math.min(n, outputEnd-outputStart);
        outputStart += bytes;
        return bytes;
    }
    public int read() throws IOException {
        if (outputStart >= outputEnd) {
            refill();
        }
        if (outputStart >= outputEnd) {
            return -1;
        } else {
            return coder.output[outputStart++];
        }
    }
    public int read(byte[] b, int off, int len) throws IOException {
        if (outputStart >= outputEnd) {
            refill();
        }
        if (outputStart >= outputEnd) {
            return -1;
        }
        int bytes = Math.min(len, outputEnd-outputStart);
        System.arraycopy(coder.output, outputStart, b, off, bytes);
        outputStart += bytes;
        return bytes;
    }
    private void refill() throws IOException {
        if (eof) return;
        int bytesRead = in.read(inputBuffer);
        boolean success;
        if (bytesRead == -1) {
            eof = true;
            success = coder.process(EMPTY, 0, 0, true);
        } else {
            success = coder.process(inputBuffer, 0, bytesRead, false);
        }
        if (!success) {
            throw new IOException("bad base-64");
        }
        outputEnd = coder.op;
        outputStart = 0;
    }
}
