public abstract class Writer implements Appendable, Closeable, Flushable {
    static final String TOKEN_NULL = "null"; 
    protected Object lock;
    protected Writer() {
        super();
        lock = this;
    }
    protected Writer(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }
    public abstract void close() throws IOException;
    public abstract void flush() throws IOException;
    public void write(char[] buf) throws IOException {
        write(buf, 0, buf.length);
    }
    public abstract void write(char[] buf, int offset, int count)
            throws IOException;
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            char oneCharArray[] = new char[1];
            oneCharArray[0] = (char) oneChar;
            write(oneCharArray);
        }
    }
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }
    public void write(String str, int offset, int count) throws IOException {
        if (count < 0) { 
            throw new StringIndexOutOfBoundsException();
        }
        char buf[] = new char[count];
        str.getChars(offset, offset + count, buf, 0);
        synchronized (lock) {
            write(buf, 0, buf.length);
        }
    }
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }
    public Writer append(CharSequence csq) throws IOException {
        if (null == csq) {
            write(TOKEN_NULL);
        } else {
            write(csq.toString());
        }
        return this;
    }
    public Writer append(CharSequence csq, int start, int end)
            throws IOException {
        if (null == csq) {
            write(TOKEN_NULL.substring(start, end));
        } else {
            write(csq.subSequence(start, end).toString());
        }
        return this;
    }
    boolean checkError() {
        return false;
    }
}
