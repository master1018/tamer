public class Deflater {
    public static final int BEST_COMPRESSION = 9;
    public static final int BEST_SPEED = 1;
    public static final int DEFAULT_COMPRESSION = -1;
    public static final int DEFAULT_STRATEGY = 0;
    public static final int DEFLATED = 8;
    public static final int FILTERED = 1;
    public static final int HUFFMAN_ONLY = 2;
    public static final int NO_COMPRESSION = 0;
    public static final int NO_FLUSH = 0;
    public static final int SYNC_FLUSH = 2;
    public static final int FULL_FLUSH = 3;
    private static final int FINISH = 4;
    private static native void oneTimeInitialization();
    private static final byte[] STUB_INPUT_BUFFER = new byte[0];
    static {
        oneTimeInitialization();
    }
    private int flushParm = NO_FLUSH;
    private boolean finished;
    private int compressLevel = DEFAULT_COMPRESSION;
    private int strategy = DEFAULT_STRATEGY;
    private long streamHandle = -1;
    private byte[] inputBuffer;
    private int inRead;
    private int inLength;
    public Deflater() {
        this(DEFAULT_COMPRESSION, false);
    }
    public Deflater(int level) {
        this(level, false);
    }
    public Deflater(int level, boolean noHeader) {
        super();
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException();
        }
        compressLevel = level;
        streamHandle = createStream(compressLevel, strategy, noHeader);
    }
    public int deflate(byte[] buf) {
        return deflate(buf, 0, buf.length);
    }
    public synchronized int deflate(byte[] buf, int off, int nbytes) {
        return deflateImpl(buf, off, nbytes, flushParm);
    }
    public synchronized int deflate(byte[] buf, int off, int nbytes, int flush) {
        if (flush != NO_FLUSH && flush != SYNC_FLUSH && flush != FULL_FLUSH) {
            throw new IllegalArgumentException();
        }
        return deflateImpl(buf, off, nbytes, flush);
    }
    private synchronized int deflateImpl(
            byte[] buf, int off, int nbytes, int flush) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        if (off > buf.length || nbytes < 0 || off < 0 || buf.length - off < nbytes) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (inputBuffer == null) {
            setInput(STUB_INPUT_BUFFER);
        }
        return deflateImpl(buf, off, nbytes, streamHandle, flush);
    }
    private synchronized native int deflateImpl(byte[] buf, int off,
            int nbytes, long handle, int flushParm1);
    private synchronized native void endImpl(long handle);
    public synchronized void end() {
        endImpl();
    }
    private void endImpl() {
        if (streamHandle != -1) {
            endImpl(streamHandle);
            inputBuffer = null;
            streamHandle = -1;
        }
    }
    @Override
    protected void finalize() {
        synchronized (this) {
            end(); 
            endImpl(); 
        }
    }
    public synchronized void finish() {
        flushParm = FINISH;
    }
    public synchronized boolean finished() {
        return finished;
    }
    public synchronized int getAdler() {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        return getAdlerImpl(streamHandle);
    }
    private synchronized native int getAdlerImpl(long handle);
    public synchronized int getTotalIn() {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        return (int) getTotalInImpl(streamHandle);
    }
    private synchronized native long getTotalInImpl(long handle);
    public synchronized int getTotalOut() {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        return (int) getTotalOutImpl(streamHandle);
    }
    private synchronized native long getTotalOutImpl(long handle);
    public synchronized boolean needsInput() {
        if (inputBuffer == null) {
            return true;
        }
        return inRead == inLength;
    }
    public synchronized void reset() {
        if (streamHandle == -1) {
            throw new NullPointerException();
        }
        flushParm = NO_FLUSH;
        finished = false;
        resetImpl(streamHandle);
        inputBuffer = null;
    }
    private synchronized native void resetImpl(long handle);
    public void setDictionary(byte[] buf) {
        setDictionary(buf, 0, buf.length);
    }
    public synchronized void setDictionary(byte[] buf, int off, int nbytes) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            setDictionaryImpl(buf, off, nbytes, streamHandle);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    private synchronized native void setDictionaryImpl(byte[] buf, int off,
            int nbytes, long handle);
    public void setInput(byte[] buf) {
        setInput(buf, 0, buf.length);
    }
    public synchronized void setInput(byte[] buf, int off, int nbytes) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            inLength = nbytes;
            inRead = 0;
            if (inputBuffer == null) {
                setLevelsImpl(compressLevel, strategy, streamHandle);
            }
            inputBuffer = buf;
            setInputImpl(buf, off, nbytes, streamHandle);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    private synchronized native void setLevelsImpl(int level, int strategy,
            long handle);
    private synchronized native void setInputImpl(byte[] buf, int off,
            int nbytes, long handle);
    public synchronized void setLevel(int level) {
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException();
        }
        if (inputBuffer != null) {
            throw new IllegalStateException();
        }
        compressLevel = level;
    }
    public synchronized void setStrategy(int strategy) {
        if (strategy < DEFAULT_STRATEGY || strategy > HUFFMAN_ONLY) {
            throw new IllegalArgumentException();
        }
        if (inputBuffer != null) {
            throw new IllegalStateException();
        }
        this.strategy = strategy;
    }
    public synchronized long getBytesRead() {
        if (streamHandle == -1) {
            throw new NullPointerException();
        }
        return getTotalInImpl(streamHandle);
    }
    public synchronized long getBytesWritten() {
        if (streamHandle == -1) {
            throw new NullPointerException();
        }
        return getTotalOutImpl(streamHandle);
    }
    private native long createStream(int level, int strategy1, boolean noHeader1);
}
