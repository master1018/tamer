public class PipedReader extends Reader {
    private Thread lastReader;
    private Thread lastWriter;
    private boolean isClosed;
    private char[] buffer;
    private int in = -1;
    private int out;
    private static final int PIPE_SIZE = 1024;
    boolean isConnected;
    public PipedReader() {}
    public PipedReader(PipedWriter out) throws IOException {
        connect(out);
    }
    @Override
    public synchronized void close() throws IOException {
        buffer = null;
        notifyAll();
    }
    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }
    synchronized void establishConnection() throws IOException {
        if (isConnected) {
            throw new IOException(Msg.getString("K007a")); 
        }
        buffer = new char[PIPE_SIZE];
        isConnected = true;
    }
    @Override
    public int read() throws IOException {
        char[] carray = new char[1];
        int result = read(carray, 0, 1);
        return result != -1 ? carray[0] : result;
    }
    @Override
    public synchronized int read(char[] buffer, int offset, int count) throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K007b")); 
        }
        if (this.buffer == null) {
            throw new IOException(Msg.getString("K0078")); 
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (count == 0) {
            return 0;
        }
        lastReader = Thread.currentThread();
        try {
            boolean first = true;
            while (in == -1) {
                if (isClosed) {
                    return -1;
                }
                if (!first && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); 
                }
                first = false;
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        int copyLength = 0;
        if (out >= in) {
            copyLength = count > this.buffer.length - out ? this.buffer.length - out
                    : count;
            System.arraycopy(this.buffer, out, buffer, offset, copyLength);
            out += copyLength;
            if (out == this.buffer.length) {
                out = 0;
            }
            if (out == in) {
                in = -1;
                out = 0;
            }
        }
        if (copyLength == count || in == -1) {
            return copyLength;
        }
        int charsCopied = copyLength;
        copyLength = in - out > count - copyLength ? count - copyLength
                : in - out;
        System.arraycopy(this.buffer, out, buffer, offset + charsCopied,
                copyLength);
        out += copyLength;
        if (out == in) {
            in = -1;
            out = 0;
        }
        return charsCopied + copyLength;
    }
    @Override
    public synchronized boolean ready() throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K007b")); 
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); 
        }
        return in != -1;
    }
    synchronized void receive(char oneChar) throws IOException {
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); 
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException(Msg.getString("K0076")); 
        }
        lastWriter = Thread.currentThread();
        try {
            while (buffer != null && out == in) {
                notifyAll();
                wait(1000);
                if (lastReader != null && !lastReader.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); 
                }
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
        buffer[in++] = oneChar;
        if (in == buffer.length) {
            in = 0;
        }
    }
    synchronized void receive(char[] chars, int offset, int count) throws IOException {
        if (chars == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > chars.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); 
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException(Msg.getString("K0076")); 
        }
        lastWriter = Thread.currentThread();
        while (count > 0) {
            try {
                while (buffer != null && out == in) {
                    notifyAll();
                    wait(1000);
                    if (lastReader != null && !lastReader.isAlive()) {
                        throw new IOException(Msg.getString("K0076")); 
                    }
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
            if (in >= out) {
                int length = buffer.length - in;
                if (count < length) {
                    length = count;
                }
                System.arraycopy(chars, offset, buffer, in, length);
                offset += length;
                count -= length;
                in += length;
                if (in == buffer.length) {
                    in = 0;
                }
            }
            if (count > 0 && in != out) {
                int length = out - in;
                if (count < length) {
                    length = count;
                }
                System.arraycopy(chars, offset, buffer, in, length);
                offset += length;
                count -= length;
                in += length;
            }
        }
    }
    synchronized void done() {
        isClosed = true;
        notifyAll();
    }
}
