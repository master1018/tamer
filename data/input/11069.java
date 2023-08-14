public abstract class Writer implements Appendable, Closeable, Flushable {
    private char[] writeBuffer;
    private final int writeBufferSize = 1024;
    protected Object lock;
    protected Writer() {
        this.lock = this;
    }
    protected Writer(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }
    public void write(int c) throws IOException {
        synchronized (lock) {
            if (writeBuffer == null){
                writeBuffer = new char[writeBufferSize];
            }
            writeBuffer[0] = (char) c;
            write(writeBuffer, 0, 1);
        }
    }
    public void write(char cbuf[]) throws IOException {
        write(cbuf, 0, cbuf.length);
    }
    abstract public void write(char cbuf[], int off, int len) throws IOException;
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }
    public void write(String str, int off, int len) throws IOException {
        synchronized (lock) {
            char cbuf[];
            if (len <= writeBufferSize) {
                if (writeBuffer == null) {
                    writeBuffer = new char[writeBufferSize];
                }
                cbuf = writeBuffer;
            } else {    
                cbuf = new char[len];
            }
            str.getChars(off, (off + len), cbuf, 0);
            write(cbuf, 0, len);
        }
    }
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }
    abstract public void flush() throws IOException;
    abstract public void close() throws IOException;
}
