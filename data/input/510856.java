public class PushbackReader extends FilterReader {
    char[] buf;
    int pos;
    public PushbackReader(Reader in) {
        super(in);
        buf = new char[1];
        pos = 1;
    }
    public PushbackReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        buf = new char[size];
        pos = size;
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            buf = null;
            in.close();
        }
    }
    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException(Msg.getString("K007f")); 
    }
    @Override
    public boolean markSupported() {
        return false;
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (buf == null) {
                throw new IOException(Msg.getString("K0059")); 
            }
            if (pos < buf.length) {
                return buf[pos++];
            }
            return in.read();
        }
    }
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            if (null == buf) {
                throw new IOException(Msg.getString("K0059")); 
            }
            if (buffer == null) {
                throw new NullPointerException(Msg.getString("K0047")); 
            }
            if ((offset | count) < 0 || offset > buffer.length - count) {
                throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
            }
            int copiedChars = 0;
            int copyLength = 0;
            int newOffset = offset;
            if (pos < buf.length) {
                copyLength = (buf.length - pos >= count) ? count : buf.length
                        - pos;
                System.arraycopy(buf, pos, buffer, newOffset, copyLength);
                newOffset += copyLength;
                copiedChars += copyLength;
                pos += copyLength;
            }
            if (copyLength == count) {
                return count;
            }
            int inCopied = in.read(buffer, newOffset, count - copiedChars);
            if (inCopied > 0) {
                return inCopied + copiedChars;
            }
            if (copiedChars == 0) {
                return inCopied;
            }
            return copiedChars;
        }
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (buf == null) {
                throw new IOException(Msg.getString("K0080")); 
            }
            return (buf.length - pos > 0 || in.ready());
        }
    }
    @Override
    public void reset() throws IOException {
        throw new IOException(Msg.getString("K007f")); 
    }
    public void unread(char[] buffer) throws IOException {
        unread(buffer, 0, buffer.length);
    }
    public void unread(char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            if (buf == null) {
                throw new IOException(Msg.getString("K0059")); 
            }
            if (length > pos) {
                throw new IOException(Msg.getString("K007e")); 
            }
            if (offset > buffer.length - length || offset < 0) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
            }
            if (length < 0) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
            }
            for (int i = offset + length - 1; i >= offset; i--) {
                unread(buffer[i]);
            }
        }
    }
    public void unread(int oneChar) throws IOException {
        synchronized (lock) {
            if (buf == null) {
                throw new IOException(Msg.getString("K0059")); 
            }
            if (pos == 0) {
                throw new IOException(Msg.getString("K007e")); 
            }
            buf[--pos] = (char) oneChar;
        }
    }
    @Override
    public long skip(long count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (buf == null) {
                throw new IOException(Msg.getString("K0059")); 
            }
            if (count == 0) {
                return 0;
            }
            long inSkipped;
            int availableFromBuffer = buf.length - pos;
            if (availableFromBuffer > 0) {
                long requiredFromIn = count - availableFromBuffer;
                if (requiredFromIn <= 0) {
                    pos += count;
                    return count;
                }
                pos += availableFromBuffer;
                inSkipped = in.skip(requiredFromIn);
            } else {
                inSkipped = in.skip(count);
            }
            return inSkipped + availableFromBuffer;
        }
    }
}
