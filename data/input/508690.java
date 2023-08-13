public abstract class Reader implements Readable, Closeable {
    protected Object lock;
    protected Reader() {
        super();
        lock = this;
    }
    protected Reader(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }
    public abstract void close() throws IOException;
    public void mark(int readLimit) throws IOException {
        throw new IOException();
    }
    public boolean markSupported() {
        return false;
    }
    public int read() throws IOException {
        synchronized (lock) {
            char charArray[] = new char[1];
            if (read(charArray, 0, 1) != -1) {
                return charArray[0];
            }
            return -1;
        }
    }
    public int read(char[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }
    public abstract int read(char[] buf, int offset, int count)
            throws IOException;
    public boolean ready() throws IOException {
        return false;
    }
    public void reset() throws IOException {
        throw new IOException();
    }
    public long skip(long count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            long skipped = 0;
            int toRead = count < 512 ? (int) count : 512;
            char charsSkipped[] = new char[toRead];
            while (skipped < count) {
                int read = read(charsSkipped, 0, toRead);
                if (read == -1) {
                    return skipped;
                }
                skipped += read;
                if (read < toRead) {
                    return skipped;
                }
                if (count - skipped < toRead) {
                    toRead = (int) (count - skipped);
                }
            }
            return skipped;
        }
    }
    public int read(CharBuffer target) throws IOException {
        if (null == target) {
            throw new NullPointerException();
        }
        int length = target.length();
        char[] buf = new char[length];
        length = Math.min(length, read(buf));
        if (length > 0) {
            target.put(buf, 0, length);
        }
        return length;
    }
}
