public class NullInputStream extends InputStream {
    private long size;
    private long position;
    private long mark = -1;
    private long readlimit;
    private boolean eof;
    private boolean throwEofException;
    private boolean markSupported;
    public NullInputStream(long size) {
       this(size, true, false);
    }
    public NullInputStream(long size, boolean markSupported, boolean throwEofException) {
       this.size = size;
       this.markSupported = markSupported;
       this.throwEofException = throwEofException;
    }
    public long getPosition() {
        return position;
    }
    public long getSize() {
        return size;
    }
    public int available() {
        long avail = size - position;
        if (avail <= 0) {
            return 0;
        } else if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int)avail;
        }
    }
    public void close() throws IOException {
        eof = false;
        position = 0;
        mark = -1;
    }
    public synchronized void mark(int readlimit) {
        if (!markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        mark = position;
        this.readlimit = readlimit;
    }
    public boolean markSupported() {
        return markSupported;
    }
    public int read() throws IOException {
        if (eof) {
            throw new IOException("Read after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position++;
        return processByte();
    }
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (eof) {
            throw new IOException("Read after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position += length;
        int returnLength = length;
        if (position > size) {
            returnLength = length - (int)(position - size);
            position = size;
        }
        processBytes(bytes, offset, returnLength);
        return returnLength;
    }
    public synchronized void reset() throws IOException {
        if (!markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        if (mark < 0) {
            throw new IOException("No position has been marked");
        }
        if (position > (mark + readlimit)) {
            throw new IOException("Marked position [" + mark +
                    "] is no longer valid - passed the read limit [" +
                    readlimit + "]");
        }
        position = mark;
        eof = false;
    }
    public long skip(long numberOfBytes) throws IOException {
        if (eof) {
            throw new IOException("Skip after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position += numberOfBytes;
        long returnLength = numberOfBytes;
        if (position > size) {
            returnLength = numberOfBytes - (position - size);
            position = size;
        }
        return returnLength;
    }
    protected int processByte() {
        return 0;
    }
    protected void processBytes(byte[] bytes, int offset, int length) {
    }
    private int doEndOfFile() throws EOFException {
        eof = true;
        if (throwEofException) {
            throw new EOFException();
        }
        return -1;
    }
}
