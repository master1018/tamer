public class PipedWriter extends Writer {
    private PipedReader dest;
    public PipedWriter() {
        super();
    }
    public PipedWriter(PipedReader dest) throws IOException {
        super(dest);
        connect(dest);
    }
    @Override
    public void close() throws IOException {
        PipedReader reader = dest;
        if (reader != null) {
            reader.done();
            dest = null;
        }
    }
    public void connect(PipedReader reader) throws IOException {
        if (reader == null) {
            throw new NullPointerException();
        }
        synchronized (reader) {
            if (this.dest != null) {
                throw new IOException(Msg.getString("K0079")); 
            }
            if (reader.isConnected) {
                throw new IOException(Msg.getString("K0078")); 
            }
            reader.establishConnection();
            this.lock = reader;
            this.dest = reader;
        }
    }
    @Override
    public void flush() throws IOException {
        PipedReader reader = dest;
        if (reader == null) {
            return;
        }
        synchronized (reader) {
            reader.notifyAll();
        }
    }
    @Override
    public void write(char[] buffer, int offset, int count) throws IOException {
        PipedReader reader = dest;
        if (reader == null) {
            throw new IOException(Msg.getString("K007b")); 
        }
        reader.receive(buffer, offset, count);
    }
    @Override
    public void write(int c) throws IOException {
        PipedReader reader = dest;
        if (reader == null) {
            throw new IOException(Msg.getString("K007b")); 
        }
        reader.receive((char) c);
    }
}
