public class MemoryCacheImageInputStream extends ImageInputStreamImpl {
    private InputStream stream;
    private MemoryCache cache = new MemoryCache();
    private final Object disposerReferent;
    private final DisposerRecord disposerRecord;
    public MemoryCacheImageInputStream(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        this.stream = stream;
        disposerRecord = new StreamDisposerRecord(cache);
        if (getClass() == MemoryCacheImageInputStream.class) {
            disposerReferent = new Object();
            Disposer.addRecord(disposerReferent, disposerRecord);
        } else {
            disposerReferent = new StreamFinalizer(this);
        }
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        long pos = cache.loadFromStream(stream, streamPos+1);
        if (pos >= streamPos+1) {
            return cache.read(streamPos++);
        } else {
            return -1;
        }
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
        long pos = cache.loadFromStream(stream, streamPos+len);
        len = (int)(pos - streamPos);  
        if (len > 0) {
            cache.read(b, off, len, streamPos);
            streamPos += len;
            return len;
        } else {
            return -1;
        }
    }
    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos); 
        cache.disposeBefore(pos);
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
        super.close();
        disposerRecord.dispose(); 
        stream = null;
        cache = null;
    }
    protected void finalize() throws Throwable {
    }
    private static class StreamDisposerRecord implements DisposerRecord {
        private MemoryCache cache;
        public StreamDisposerRecord(MemoryCache cache) {
            this.cache = cache;
        }
        public synchronized void dispose() {
            if (cache != null) {
                cache.reset();
                cache = null;
            }
        }
    }
}
