public class PipedOutputStream extends OutputStream {
    private PipedInputStream dest;
    public PipedOutputStream() {
        super();
    }
    public PipedOutputStream(PipedInputStream dest) throws IOException {
        super();
        connect(dest);
    }
    @Override
    public void close() throws IOException {
        PipedInputStream stream = dest;
        if (stream != null) {
            stream.done();
            dest = null;
        }
    }
    public void connect(PipedInputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        }
        synchronized (stream) {
            if (this.dest != null) {
                throw new IOException(Msg.getString("K0079")); 
            }
            if (stream.isConnected) {
                throw new IOException(Msg.getString("K007a")); 
            }
            stream.establishConnection();
            this.dest = stream;
        }
    }
    @Override
    public void flush() throws IOException {
        PipedInputStream stream = dest;
        if (stream == null) {
            return;
        }
        synchronized (stream) {
            stream.notifyAll();
        }
    }
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        super.write(buffer, offset, count);
    }
    @Override
    public void write(int oneByte) throws IOException {
        PipedInputStream stream = dest;
        if (stream == null) {
            throw new IOException(Msg.getString("K007b")); 
        }
        stream.receive(oneByte);
    }
}
