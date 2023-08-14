public class CharArrayWriter extends Writer {
    protected char[] buf;
    protected int count;
    public CharArrayWriter() {
        super();
        buf = new char[32];
        lock = buf;
    }
    public CharArrayWriter(int initialSize) {
        super();
        if (initialSize < 0) {
            throw new IllegalArgumentException(Msg.getString("K005e")); 
        }
        buf = new char[initialSize];
        lock = buf;
    }
    @Override
    public void close() {
    }
    private void expand(int i) {
        if (count + i <= buf.length) {
            return;
        }
        int newLen = Math.max(2 * buf.length, count + i);
        char[] newbuf = new char[newLen];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
    }
    @Override
    public void flush() {
    }
    public void reset() {
        synchronized (lock) {
            count = 0;
        }
    }
    public int size() {
        synchronized (lock) {
            return count;
        }
    }
    public char[] toCharArray() {
        synchronized (lock) {
            char[] result = new char[count];
            System.arraycopy(buf, 0, result, 0, count);
            return result;
        }
    }
    @Override
    public String toString() {
        synchronized (lock) {
            return new String(buf, 0, count);
        }
    }
    @Override
    public void write(char[] c, int offset, int len) {
        if (c == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | len) < 0 || len > c.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        synchronized (lock) {
            expand(len);
            System.arraycopy(c, offset, this.buf, this.count, len);
            this.count += len;
        }
    }
    @Override
    public void write(int oneChar) {
        synchronized (lock) {
            expand(1);
            buf[count++] = (char) oneChar;
        }
    }
    @Override
    public void write(String str, int offset, int len) {
        if (str == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | len) < 0 || len > str.length() - offset) {
            throw new StringIndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        synchronized (lock) {
            expand(len);
            str.getChars(offset, offset + len, buf, this.count);
            this.count += len;
        }
    }
    public void writeTo(Writer out) throws IOException {
        synchronized (lock) {
            out.write(buf, 0, count);
        }
    }
    @Override
    public CharArrayWriter append(char c) {
        write(c);
        return this;
    }
    @Override
    public CharArrayWriter append(CharSequence csq) {
        if (null == csq) {
            append(TOKEN_NULL, 0, TOKEN_NULL.length());
        } else {
            append(csq, 0, csq.length());
        }
        return this;
    }
    @Override
    public CharArrayWriter append(CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = TOKEN_NULL;
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
