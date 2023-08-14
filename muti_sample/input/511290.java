public class Support_StringWriter extends Writer {
    private StringBuffer buf;
    public Support_StringWriter() {
        super();
        buf = new StringBuffer(16);
        lock = buf;
    }
    public Support_StringWriter(int initialSize) {
        if (initialSize >= 0) {
            buf = new StringBuffer(initialSize);
            lock = buf;
        } else {
            throw new IllegalArgumentException();
        }
    }
    @Override
    public void close() throws IOException {
    }
    @Override
    public void flush() {
    }
    public StringBuffer getBuffer() {
        synchronized (lock) {
            return buf;
        }
    }
    @Override
    public String toString() {
        synchronized (lock) {
            return buf.toString();
        }
    }
    @Override
    public void write(char[] buf, int offset, int count) {
        if (0 <= offset && offset <= buf.length && 0 <= count
                && count <= buf.length - offset) {
            synchronized (lock) {
                this.buf.append(buf, offset, count);
            }
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    @Override
    public void write(int oneChar) {
        synchronized (lock) {
            buf.append((char) oneChar);
        }
    }
    @Override
    public void write(String str) {
        synchronized (lock) {
            buf.append(str);
        }
    }
    @Override
    public void write(String str, int offset, int count) {
        String sub = str.substring(offset, offset + count);
        synchronized (lock) {
            buf.append(sub);
        }
    }
}
