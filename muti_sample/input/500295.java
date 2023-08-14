public class PushbackInputStream extends FilterInputStream {
    protected byte[] buf;
    protected int pos;
    public PushbackInputStream(InputStream in) {
        super(in);
        buf = (in == null) ? null : new byte[1];
        pos = 1;
    }
    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        buf = (in == null) ? null : new byte[size];
        pos = size;
    }
    @Override
    public int available() throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        return buf.length - pos + in.available();
    }
    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
            buf = null;
        }
    }
    @Override
    public boolean markSupported() {
        return false;
    }
    @Override
    public int read() throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        if (pos < buf.length) {
            return (buf[pos++] & 0xFF);
        }
        return in.read();
    }
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buf == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        int copiedBytes = 0, copyLength = 0, newOffset = offset;
        if (pos < buf.length) {
            copyLength = (buf.length - pos >= length) ? length : buf.length
                    - pos;
            System.arraycopy(buf, pos, buffer, newOffset, copyLength);
            newOffset += copyLength;
            copiedBytes += copyLength;
            pos += copyLength;
        }
        if (copyLength == length) {
            return length;
        }
        int inCopied = in.read(buffer, newOffset, length - copiedBytes);
        if (inCopied > 0) {
            return inCopied + copiedBytes;
        }
        if (copiedBytes == 0) {
            return inCopied;
        }
        return copiedBytes;
    }
    @Override
    public long skip(long count) throws IOException {
        if (in == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (count <= 0) {
            return 0;
        }
        int numSkipped = 0;
        if (pos < buf.length) {
            numSkipped += (count < buf.length - pos) ? count : buf.length - pos;
            pos += numSkipped;
        }
        if (numSkipped < count) {
            numSkipped += in.skip(count - numSkipped);
        }
        return numSkipped;
    }
    public void unread(byte[] buffer) throws IOException {
        unread(buffer, 0, buffer.length);
    }
    public void unread(byte[] buffer, int offset, int length)
            throws IOException {
        if (length > pos) {
            throw new IOException(Msg.getString("K007e")); 
        }
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        if (buf == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        System.arraycopy(buffer, offset, buf, pos - length, length);
        pos = pos - length;
    }
    public void unread(int oneByte) throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        if (pos == 0) {
            throw new IOException(Msg.getString("K007e")); 
        }
        buf[--pos] = (byte) oneByte;
    }
    @Override
    public void mark(int readlimit) {
        return;
    }
    @Override
    public void reset() throws IOException {
        throw new IOException();
    }
}
