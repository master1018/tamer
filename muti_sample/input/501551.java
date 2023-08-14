public class CharArrayReader extends Reader {
    protected char buf[];
    protected int pos;
    protected int markedPos = -1;
    protected int count;
    public CharArrayReader(char[] buf) {
        this.buf = buf;
        this.count = buf.length;
    }
    public CharArrayReader(char[] buf, int offset, int length) {
        if (offset < 0 || offset > buf.length || length < 0 || offset + length < 0) {
            throw new IllegalArgumentException();
        }
        this.buf = buf;
        this.pos = offset;
        this.markedPos = offset;
        int bufferLength = buf.length;
        this.count = offset + length < bufferLength ? length : bufferLength;
    }
    @Override
    public void close() {
        synchronized (lock) {
            if (isOpen()) {
                buf = null;
            }
        }
    }
    private boolean isOpen() {
        return buf != null;
    }
    private boolean isClosed() {
        return buf == null;
    }
    @Override
    public void mark(int readLimit) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            markedPos = pos;
        }
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            if (pos == count) {
                return -1;
            }
            return buf[pos++];
        }
    }
    @Override
    public int read(char[] buffer, int offset, int len) throws IOException {
        if (offset < 0 || offset > buffer.length) {
            throw new ArrayIndexOutOfBoundsException(
                    Msg.getString("K002e", offset)); 
        }
        if (len < 0 || len > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(
                    Msg.getString("K0031", len)); 
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            if (pos < this.count) {
                int bytesRead = pos + len > this.count ? this.count - pos : len;
                System.arraycopy(this.buf, pos, buffer, offset, bytesRead);
                pos += bytesRead;
                return bytesRead;
            }
            return -1;
        }
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            return pos != count;
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            pos = markedPos != -1 ? markedPos : 0;
        }
    }
    @Override
    public long skip(long n) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0060")); 
            }
            if (n <= 0) {
                return 0;
            }
            long skipped = 0;
            if (n < this.count - pos) {
                pos = pos + (int) n;
                skipped = n;
            } else {
                skipped = this.count - pos;
                pos = this.count;
            }
            return skipped;
        }
    }
}
