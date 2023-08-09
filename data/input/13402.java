public class FileCacheImageInputStream extends ImageInputStreamImpl {
    private InputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private static final int BUFFER_LENGTH = 1024;
    private byte[] buf = new byte[BUFFER_LENGTH];
    private long length = 0L;
    private boolean foundEOF = false;
    private final Object disposerReferent;
    private final DisposerRecord disposerRecord;
    private final StreamCloser.CloseAction closeAction;
    public FileCacheImageInputStream(InputStream stream, File cacheDir)
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
        disposerRecord = new StreamDisposerRecord(cacheFile, cache);
        if (getClass() == FileCacheImageInputStream.class) {
            disposerReferent = new Object();
            Disposer.addRecord(disposerReferent, disposerRecord);
        } else {
            disposerReferent = new StreamFinalizer(this);
        }
    }
    private long readUntil(long pos) throws IOException {
        if (pos < length) {
            return pos;
        }
        if (foundEOF) {
            return length;
        }
        long len = pos - length;
        cache.seek(length);
        while (len > 0) {
            int nbytes =
                stream.read(buf, 0, (int)Math.min(len, (long)BUFFER_LENGTH));
            if (nbytes == -1) {
                foundEOF = true;
                return length;
            }
            cache.write(buf, 0, nbytes);
            len -= nbytes;
            length += nbytes;
        }
        return pos;
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        long next = streamPos + 1;
        long pos = readUntil(next);
        if (pos >= next) {
            cache.seek(streamPos++);
            return cache.read();
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
        long pos = readUntil(streamPos + len);
        len = (int)Math.min((long)len, pos - streamPos);
        if (len > 0) {
            cache.seek(streamPos);
            cache.readFully(b, off, len);
            streamPos += len;
            return len;
        } else {
            return -1;
        }
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
        super.close();
        disposerRecord.dispose(); 
        stream = null;
        cache = null;
        cacheFile = null;
        StreamCloser.removeFromQueue(closeAction);
    }
    protected void finalize() throws Throwable {
    }
    private static class StreamDisposerRecord implements DisposerRecord {
        private File cacheFile;
        private RandomAccessFile cache;
        public StreamDisposerRecord(File cacheFile, RandomAccessFile cache) {
            this.cacheFile = cacheFile;
            this.cache = cache;
        }
        public synchronized void dispose() {
            if (cache != null) {
                try {
                    cache.close();
                } catch (IOException e) {
                } finally {
                    cache = null;
                }
            }
            if (cacheFile != null) {
                cacheFile.delete();
                cacheFile = null;
            }
        }
    }
}
