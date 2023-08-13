public abstract class InputStream extends Object implements Closeable {
    private static byte[] skipBuf;
    public InputStream() {
    }
    public int available() throws IOException {
        return 0;
    }
    public void close() throws IOException {
    }
    public void mark(int readlimit) {
    }
    public boolean markSupported() {
        return false;
    }
    public abstract int read() throws IOException;
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte[] b, int offset, int length) throws IOException {
        if (offset > b.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        } 
        if (length < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        for (int i = 0; i < length; i++) {
            int c;
            try {
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            b[offset + i] = (byte) c;
        }
        return length;
    }
    public synchronized void reset() throws IOException {
        throw new IOException();
    }
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long skipped = 0;
        int toRead = n < 4096 ? (int) n : 4096;
        byte[] localBuf = skipBuf;
        if (localBuf == null || localBuf.length < toRead) {
            skipBuf = localBuf = new byte[toRead];
        }
        while (skipped < n) {
            int read = read(localBuf, 0, toRead);
            if (read == -1) {
                return skipped;
            }
            skipped += read;
            if (read < toRead) {
                return skipped;
            }
            if (n - skipped < toRead) {
                toRead = (int) (n - skipped);
            }
        }
        return skipped;
    }
}
