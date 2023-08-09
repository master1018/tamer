public class Support_StringReader extends Reader {
    private String str;
    private int markpos = -1;
    private int pos = 0;
    private int count;
    public Support_StringReader(String str) {
        super(str);
        this.str = str;
        this.count = str.length();
    }
    @Override
    public void close() {
        synchronized (lock) {
            if (isOpen()) {
                str = null;
            }
        }
    }
    private boolean isOpen() {
        return str != null;
    }
    @Override
    public void mark(int readLimit) throws IOException {
        if (readLimit >= 0) {
            synchronized (lock) {
                if (isOpen()) {
                    markpos = pos;
                } else {
                    throw new IOException("StringReader is closed");
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isOpen()) {
                if (pos != count) {
                    return str.charAt(pos++);
                }
                return -1;
            }
            throw new IOException("StringReader is closed");
        }
    }
    @Override
    public int read(char buf[], int offset, int count) throws IOException {
        if (0 <= offset && offset <= buf.length && 0 <= count
                && count <= buf.length - offset) {
            synchronized (lock) {
                if (isOpen()) {
                    if (pos == this.count) {
                        return -1;
                    }
                    int end = pos + count > this.count ? this.count : pos
                            + count;
                    str.getChars(pos, end, buf, offset);
                    int read = end - pos;
                    pos = end;
                    return read;
                }
                throw new IOException("StringReader is closed");
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isOpen()) {
                return true;
            }
            throw new IOException("StringReader is closed");
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isOpen()) {
                pos = markpos != -1 ? markpos : 0;
            } else {
                throw new IOException("StringReader is closed");
            }
        }
    }
    @Override
    public long skip(long count) throws IOException {
        synchronized (lock) {
            if (isOpen()) {
                if (count <= 0) {
                    return 0;
                }
                long skipped = 0;
                if (count < this.count - pos) {
                    pos = pos + (int) count;
                    skipped = count;
                } else {
                    skipped = this.count - pos;
                    pos = this.count;
                }
                return skipped;
            }
            throw new IOException("StringReader is closed");
        }
    }
}
