public class Inflater {
    static {
        oneTimeInitialization();
    }
    private static native void oneTimeInitialization();
    private boolean finished; 
    int inLength;
    int inRead;
    private boolean needsDictionary; 
    private long streamHandle = -1;
    public Inflater() {
        this(false);
    }
    public Inflater(boolean noHeader) {
        streamHandle = createStream(noHeader);
    }
    private native long createStream(boolean noHeader1);
    public synchronized void end() {
        if (streamHandle != -1) {
            endImpl(streamHandle);
            inRead = 0;
            inLength = 0;
            streamHandle = -1;
        }
    }
    private native synchronized void endImpl(long handle);
    @Override
    protected void finalize() {
        end();
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
    private native synchronized int getAdlerImpl(long handle);
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
    public synchronized int getRemaining() {
        return inLength - inRead;
    }
    public synchronized int getTotalIn() {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        long totalIn = getTotalInImpl(streamHandle);
        return (totalIn <= Integer.MAX_VALUE ? (int) totalIn
                : Integer.MAX_VALUE);
    }
    private synchronized native long getTotalInImpl(long handle);
    public synchronized int getTotalOut() {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        long totalOut = getTotalOutImpl(streamHandle);
        return (totalOut <= Integer.MAX_VALUE ? (int) totalOut
                : Integer.MAX_VALUE);
    }
    private native synchronized long getTotalOutImpl(long handle);
    public int inflate(byte[] buf) throws DataFormatException {
        return inflate(buf, 0, buf.length);
    }
    public synchronized int inflate(byte[] buf, int off, int nbytes)
            throws DataFormatException {
        if (off > buf.length || nbytes < 0 || off < 0
                || buf.length - off < nbytes) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (nbytes == 0) {
            return 0;
        }
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        if (needsInput()) {
            return 0;
        }
        boolean neededDict = needsDictionary;
        needsDictionary = false;
        int result = inflateImpl(buf, off, nbytes, streamHandle);
        if (needsDictionary && neededDict) {
            throw new DataFormatException(
                    Messages.getString("archive.27")); 
        }
        return result;
    }
    private native synchronized int inflateImpl(byte[] buf, int off,
            int nbytes, long handle);
    public synchronized boolean needsDictionary() {
        return needsDictionary;
    }
    public synchronized boolean needsInput() {
        return inRead == inLength;
    }
    public synchronized void reset() {
        if (streamHandle == -1) {
            throw new NullPointerException();
        }
        finished = false;
        needsDictionary = false;
        inLength = inRead = 0;
        resetImpl(streamHandle);
    }
    private native synchronized void resetImpl(long handle);
    public synchronized void setDictionary(byte[] buf) {
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
    private native synchronized void setDictionaryImpl(byte[] buf, int off,
            int nbytes, long handle);
    public synchronized void setInput(byte[] buf) {
        setInput(buf, 0, buf.length);
    }
    public synchronized void setInput(byte[] buf, int off, int nbytes) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            inRead = 0;
            inLength = nbytes;
            setInputImpl(buf, off, nbytes, streamHandle);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    synchronized int setFileInput(FileDescriptor fd, long off, int nbytes) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        inRead = 0;
        inLength = setFileInputImpl(fd, off, nbytes, streamHandle);
        return inLength;
    }
    private native synchronized void setInputImpl(byte[] buf, int off,
            int nbytes, long handle);
    private native synchronized int setFileInputImpl(FileDescriptor fd, long off,
            int nbytes, long handle);
}
