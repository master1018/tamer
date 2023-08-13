public class HttpCaptureOutputStream extends FilterOutputStream {
    private HttpCapture capture = null;
    public HttpCaptureOutputStream(OutputStream out, HttpCapture cap) {
        super(out);
        capture = cap;
    }
    @Override
    public void write(int b) throws IOException {
        capture.sent(b);
        out.write(b);
    }
    @Override
    public void write(byte[] ba) throws IOException {
        for (byte b : ba) {
            capture.sent(b);
        }
        out.write(ba);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < len; i++) {
            capture.sent(b[i]);
        }
        out.write(b, off, len);
    }
    @Override
    public void flush() throws IOException {
        try {
            capture.flush();
        } catch (IOException iOException) {
        }
        super.flush();
    }
}
