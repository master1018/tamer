public class StringWriter extends Writer {
    private StringBuffer buf;
    public StringWriter() {
        super();
        buf = new StringBuffer(16);
        lock = buf;
    }
    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException();
        }
        buf = new StringBuffer(initialSize);
        lock = buf;
    }
    @Override
    public void close() throws IOException {
    }
    @Override
    public void flush() {
    }
    public StringBuffer getBuffer() {
        return buf;
    }
    @Override
    public String toString() {
        return buf.toString();
    }
    @Override
    public void write(char[] cbuf, int offset, int count) {
        if (cbuf == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > cbuf.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (count == 0) {
            return;
        }
        buf.append(cbuf, offset, count);
    }
    @Override
    public void write(int oneChar) {
        buf.append((char) oneChar);
    }
    @Override
    public void write(String str) {
        buf.append(str);
    }
    @Override
    public void write(String str, int offset, int count) {
        String sub = str.substring(offset, offset + count);
        buf.append(sub);
    }
    @Override
    public StringWriter append(char c) {
        write(c);
        return this;
    }
    @Override
    public StringWriter append(CharSequence csq) {
        if (null == csq) {
            write(TOKEN_NULL);
        } else {
            write(csq.toString());
        }
        return this;
    }
    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = TOKEN_NULL;
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
