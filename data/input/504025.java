public class BufferedWriter extends Writer {
    private Writer out;
    private char buf[];
    private int pos;
    private final String lineSeparator = AccessController
            .doPrivileged(new PriviAction<String>("line.separator")); 
    public BufferedWriter(Writer out) {
        super(out);
        this.out = out;
        buf = new char[8192];
        Logger.global.info(
                "Default buffer size used in BufferedWriter " +
                "constructor. It would be " +
                "better to be explicit if an 8k-char buffer is required.");
    }
    public BufferedWriter(Writer out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        this.out = out;
        this.buf = new char[size];
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                return;
            }
            Throwable thrown = null;
            try {
                flushInternal();
            } catch (Throwable e) {
                thrown = e;
            }
            buf = null;
            try {
                out.close();
            } catch (Throwable e) {
                if (thrown == null) {
                    thrown = e;
                }
            }
            out = null;
            if (thrown != null) {
                SneakyThrow.sneakyThrow(thrown);
            }
        }
    }
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); 
            }
            flushInternal();
            out.flush();
        }
    }
    private void flushInternal() throws IOException {
        if (pos > 0) {
            out.write(buf, 0, pos);
        }
        pos = 0;
    }
    private boolean isClosed() {
        return out == null;
    }
    public void newLine() throws IOException {
        write(lineSeparator, 0, lineSeparator.length());
    }
    @Override
    public void write(char[] cbuf, int offset, int count) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); 
            }
            if (cbuf == null) {
                throw new NullPointerException(Msg.getString("K0047")); 
            }
            if ((offset | count) < 0 || offset > cbuf.length - count) {
                throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
            }
            if (pos == 0 && count >= this.buf.length) {
                out.write(cbuf, offset, count);
                return;
            }
            int available = this.buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                System.arraycopy(cbuf, offset, this.buf, pos, available);
                pos += available;
            }
            if (pos == this.buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= this.buf.length) {
                        out.write(cbuf, offset, available);
                        return;
                    }
                    System.arraycopy(cbuf, offset, this.buf, pos, available);
                    pos += available;
                }
            }
        }
    }
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); 
            }
            if (pos >= buf.length) {
                out.write(buf, 0, buf.length);
                pos = 0;
            }
            buf[pos++] = (char) oneChar;
        }
    }
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); 
            }
            if (count <= 0) {
                return;
            }
            if (offset > str.length() - count || offset < 0) {
                throw new StringIndexOutOfBoundsException();
            }
            if (pos == 0 && count >= buf.length) {
                char[] chars = new char[count];
                str.getChars(offset, offset + count, chars, 0);
                out.write(chars, 0, count);
                return;
            }
            int available = buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                str.getChars(offset, offset + available, buf, pos);
                pos += available;
            }
            if (pos == buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= buf.length) {
                        char[] chars = new char[count];
                        str.getChars(offset, offset + available, chars, 0);
                        out.write(chars, 0, available);
                        return;
                    }
                    str.getChars(offset, offset + available, buf, pos);
                    pos += available;
                }
            }
        }
    }
}
