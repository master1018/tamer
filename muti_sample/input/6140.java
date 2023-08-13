public class StreamBuffer {
    private class StreamBufferOutputStream extends OutputStream {
        private int pos;
        public void write(int b) throws IOException {
            if (mode != WRITE_MODE)
                throw new IOException();
            while (pos >= buf.length)
                grow();
            buf[pos++] = (byte) b;
        }
        public void write(byte[] b, int off, int len) throws IOException {
            if (mode != WRITE_MODE)
                throw new IOException();
            while (pos + len > buf.length)
                grow();
            System.arraycopy(b, off, buf, pos, len);
            pos += len;
        }
        public void close() throws IOException {
            if (mode != WRITE_MODE)
                throw new IOException();
            mode = READ_MODE;
        }
    }
    private class StreamBufferInputStream extends InputStream {
        private int pos;
        public int read() throws IOException {
            if (mode == CLOSED_MODE)
                throw new IOException();
            mode = READ_MODE;
            return (pos < out.pos) ? (buf[pos++] & 0xFF) : -1;
        }
        public int read(byte[] b, int off, int len) throws IOException {
            if (mode == CLOSED_MODE)
                throw new IOException();
            mode = READ_MODE;
            int avail = out.pos - pos;
            int rlen = (avail < len) ? avail : len;
            System.arraycopy(buf, pos, b, off, rlen);
            pos += rlen;
            return rlen;
        }
        public long skip(long len) throws IOException {
            if (mode == CLOSED_MODE)
                throw new IOException();
            mode = READ_MODE;
            int avail = out.pos - pos;
            long slen = (avail < len) ? avail : len;
            pos += slen;
            return slen;
        }
        public int available() throws IOException {
            if (mode == CLOSED_MODE)
                throw new IOException();
            mode = READ_MODE;
            return out.pos - pos;
        }
        public void close() throws IOException {
            if (mode == CLOSED_MODE)
                throw new IOException();
            mode = CLOSED_MODE;
        }
    }
    private static final int START_BUFSIZE = 256;
    private static final int GROW_FACTOR = 2;
    private static final int CLOSED_MODE = 0;
    private static final int WRITE_MODE = 1;
    private static final int READ_MODE = 2;
    private byte[] buf;
    private StreamBufferOutputStream out = new StreamBufferOutputStream();
    private StreamBufferInputStream in = new StreamBufferInputStream();
    private int mode = WRITE_MODE;
    public StreamBuffer() {
        this(START_BUFSIZE);
    }
    public StreamBuffer(int size) {
        buf = new byte[size];
    }
    public OutputStream getOutputStream() {
        return out;
    }
    public InputStream getInputStream() {
        return in;
    }
    public void reset() {
        in.pos = out.pos = 0;
        mode = WRITE_MODE;
    }
    private void grow() {
        byte[] newbuf = new byte[buf.length * GROW_FACTOR];
        System.arraycopy(buf, 0, newbuf, 0, buf.length);
        buf = newbuf;
    }
}
