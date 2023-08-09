public class StringReader extends Reader {
    private String str;
    private int markpos = -1;
    private int pos;
    private int count;
    public StringReader(String str) {
        super();
        this.str = str;
        this.count = str.length();
    }
    @Override
    public void close() {
        str = null;
    }
    private boolean isClosed() {
        return str == null;
    }
    @Override
    public void mark(int readLimit) throws IOException {
        if (readLimit < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); 
            }
            markpos = pos;
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
                throw new IOException(Msg.getString("K0083")); 
            }
            if (pos != count) {
                return str.charAt(pos++);
            }
            return -1;
        }
    }
    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); 
            }
            if (offset < 0 || offset > buf.length) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
            }
            if (len < 0 || len > buf.length - offset) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", len)); 
            }
            if (len == 0) {
                return 0;
            }
            if (pos == this.count) {
                return -1;
            }
            int end = pos + len > this.count ? this.count : pos + len;
            str.getChars(pos, end, buf, offset);
            int read = end - pos;
            pos = end;
            return read;
        }
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); 
            }
            return true;
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); 
            }
            pos = markpos != -1 ? markpos : 0;
        }
    }
    @Override
    public long skip(long ns) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); 
            }
            int minSkip = -pos;
            int maxSkip = count - pos;
            if (maxSkip == 0 || ns > maxSkip) {
                ns = maxSkip; 
            } else if (ns < minSkip) {
                ns = minSkip;
            }
            pos += ns;
            return ns;
        }
    }
}
