public class MemoryCacheImageOutputStream extends ImageOutputStreamImpl {
    private OutputStream stream;
    private MemoryCache cache = new MemoryCache();
    public MemoryCacheImageOutputStream(OutputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        this.stream = stream;
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        int val = cache.read(streamPos);
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
        long bytesLeftInCache = cache.getLength() - streamPos;
        if (bytesLeftInCache <= 0) {
            return -1; 
        }
        len = (int)Math.min(bytesLeftInCache, (long)len);
        cache.read(b, off, len, streamPos);
        streamPos += len;
        return len;
    }
    public void write(int b) throws IOException {
        flushBits(); 
        cache.write(b, streamPos);
        ++streamPos;
    }
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits(); 
        cache.write(b, off, len, streamPos);
        streamPos += len;
    }
    public long length() {
        try {
            checkClosed();
            return cache.getLength();
        } catch (IOException e) {
            return -1L;
        }
    }
    public boolean isCached() {
        return true;
    }
    public boolean isCachedFile() {
        return false;
    }
    public boolean isCachedMemory() {
        return true;
    }
    public void close() throws IOException {
        long length = cache.getLength();
        seek(length);
        flushBefore(length);
        super.close();
        cache.reset();
        cache = null;
        stream = null;
    }
    public void flushBefore(long pos) throws IOException {
        long oFlushedPos = flushedPos;
        super.flushBefore(pos); 
        long flushBytes = flushedPos - oFlushedPos;
        cache.writeToStream(stream, oFlushedPos, flushBytes);
        cache.disposeBefore(flushedPos);
        stream.flush();
    }
}
