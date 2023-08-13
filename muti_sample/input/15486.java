public class FileCacheImageOutputStream extends ImageOutputStreamImpl {
    private OutputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private long maxStreamPos = 0L;
    private final StreamCloser.CloseAction closeAction;
    public FileCacheImageOutputStream(OutputStream stream, File cacheDir)
        throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        if ((cacheDir != null) && !(cacheDir.isDirectory())) {
            throw new IllegalArgumentException("Not a directory!");
        }
        this.stream = stream;
        this.cacheFile =
            File.createTempFile("imageio", ".tmp", cacheDir);
        this.cache = new RandomAccessFile(cacheFile, "rw");
        this.closeAction = StreamCloser.createCloseAction(this);
        StreamCloser.addToQueue(closeAction);
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        int val =  cache.read();
        if (val != -1) {
            ++streamPos;
        }
        return val;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (b == null) {
            throw new NullPointerException("b == null!");
        }
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }
        bitOffset = 0;
        if (len == 0) {
            return 0;
        }
        int nbytes = cache.read(b, off, len);
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }
    public void write(int b) throws IOException {
        flushBits(); 
        cache.write(b);
        ++streamPos;
        maxStreamPos = Math.max(maxStreamPos, streamPos);
    }
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits(); 
        cache.write(b, off, len);
        streamPos += len;
        maxStreamPos = Math.max(maxStreamPos, streamPos);
    }
    public long length() {
        try {
            checkClosed();
            return cache.length();
        } catch (IOException e) {
            return -1L;
        }
    }
    public void seek(long pos) throws IOException {
        checkClosed();
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException();
        }
        cache.seek(pos);
        this.streamPos = cache.getFilePointer();
        maxStreamPos = Math.max(maxStreamPos, streamPos);
        this.bitOffset = 0;
    }
    public boolean isCached() {
        return true;
    }
    public boolean isCachedFile() {
        return true;
    }
    public boolean isCachedMemory() {
        return false;
    }
    public void close() throws IOException {
        maxStreamPos = cache.length();
        seek(maxStreamPos);
        flushBefore(maxStreamPos);
        super.close();
        cache.close();
        cache = null;
        cacheFile.delete();
        cacheFile = null;
        stream.flush();
        stream = null;
        StreamCloser.removeFromQueue(closeAction);
    }
    public void flushBefore(long pos) throws IOException {
        long oFlushedPos = flushedPos;
        super.flushBefore(pos); 
        long flushBytes = flushedPos - oFlushedPos;
        if (flushBytes > 0) {
            int bufLen = 512;
            byte[] buf = new byte[bufLen];
            cache.seek(oFlushedPos);
            while (flushBytes > 0) {
                int len = (int)Math.min(flushBytes, bufLen);
                cache.readFully(buf, 0, len);
                stream.write(buf, 0, len);
                flushBytes -= len;
            }
            stream.flush();
        }
    }
}
