public class PosterOutputStream extends ByteArrayOutputStream {
    private boolean closed;
    public PosterOutputStream () {
        super (256);
    }
    public synchronized void write(int b) {
        if (closed) {
            return;
        }
        super.write (b);
    }
    public synchronized void write(byte b[], int off, int len) {
        if (closed) {
            return;
        }
        super.write (b, off, len);
    }
    public synchronized void reset() {
        if (closed) {
            return;
        }
        super.reset ();
    }
    public synchronized void close() throws IOException {
        closed = true;
        super.close ();
    }
}
