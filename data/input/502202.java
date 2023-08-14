public class PipedInputStream extends InputStream {
    private Thread lastReader;
    private Thread lastWriter;
    private boolean isClosed;
    protected byte buffer[];
    protected int in = -1;
    protected int out;
    protected static final int PIPE_SIZE = 1024;
    boolean isConnected;
    public PipedInputStream() {}
    public PipedInputStream(PipedOutputStream out) throws IOException {
        connect(out);
    }
    @Override
    public synchronized int available() throws IOException {
        if (buffer == null || in == -1) {
            return 0;
        }
        return in <= out ? buffer.length - out + in : in - out;
    }
    @Override
    public synchronized void close() throws IOException {
        buffer = null;
        notifyAll();
    }
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }
    synchronized void establishConnection() throws IOException {
        if (isConnected) {
            throw new IOException(Msg.getString("K007a")); 
        }
        buffer = new byte[PipedInputStream.PIPE_SIZE];
        isConnected = true;
    }
    @Override
    public synchronized int read() throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K0074")); 
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0075")); 
        }
        lastReader = Thread.currentThread();
        try {
            int attempts = 3;
            while (in == -1) {
                if (isClosed) {
                    return -1;
                }
                if ((attempts-- <= 0) && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); 
                }
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        int result = buffer[out++] & 0xff;
        if (out == buffer.length) {
            out = 0;
        }
        if (out == in) {
            in = -1;
            out = 0;
        }
        notifyAll();
        return result;
    }
    @Override
    public synchronized int read(byte[] bytes, int offset, int count)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > bytes.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (count == 0) {
            return 0;
        }
        if (!isConnected) {
            throw new IOException(Msg.getString("K0074")); 
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0075")); 
        }
        lastReader = Thread.currentThread();
        try {
            int attempts = 3;
            while (in == -1) {
                if (isClosed) {
                    return -1;
                }
                if ((attempts-- <= 0) && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); 
                }
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        int totalCopied = 0;
        if (out >= in) {
            int leftInBuffer = buffer.length - out;
            int length = leftInBuffer < count ? leftInBuffer : count;
            System.arraycopy(buffer, out, bytes, offset, length);
            out += length;
            if (out == buffer.length) {
                out = 0;
            }
            if (out == in) {
                in = -1;
                out = 0;
            }
            totalCopied += length;
        }
        if (totalCopied < count && in != -1) {
            int leftInBuffer = in - out;
            int leftToCopy = count - totalCopied;
            int length = leftToCopy < leftInBuffer ? leftToCopy : leftInBuffer;
            System.arraycopy(buffer, out, bytes, offset + totalCopied, length);
            out += length;
            if (out == in) {
                in = -1;
                out = 0;
            }
            totalCopied += length;
        }
        notifyAll();
        return totalCopied;
    }
    protected synchronized void receive(int oneByte) throws IOException {
        if (buffer == null || isClosed) {
            throw new IOException(Msg.getString("K0078")); 
        }
        lastWriter = Thread.currentThread();
        try {
            while (buffer != null && out == in) {
                if (lastReader != null && !lastReader.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); 
                }
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); 
        }
        if (in == -1) {
            in = 0;
        }
        buffer[in++] = (byte) oneByte;
        if (in == buffer.length) {
            in = 0;
        }
        notifyAll();
    }
    synchronized void done() {
        isClosed = true;
        notifyAll();
    }
}
