class FileInputStream extends InputStream
{
    private final FileDescriptor fd;
    private FileChannel channel = null;
    private final Object closeLock = new Object();
    private volatile boolean closed = false;
    private static final ThreadLocal<Boolean> runningFinalize =
        new ThreadLocal<>();
    private static boolean isRunningFinalize() {
        Boolean val;
        if ((val = runningFinalize.get()) != null)
            return val.booleanValue();
        return false;
    }
    public FileInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }
    public FileInputStream(File file) throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        fd = new FileDescriptor();
        fd.incrementAndGetUseCount();
        open(name);
    }
    public FileInputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkRead(fdObj);
        }
        fd = fdObj;
        fd.incrementAndGetUseCount();
    }
    private native void open(String name) throws FileNotFoundException;
    public native int read() throws IOException;
    private native int readBytes(byte b[], int off, int len) throws IOException;
    public int read(byte b[]) throws IOException {
        return readBytes(b, 0, b.length);
    }
    public int read(byte b[], int off, int len) throws IOException {
        return readBytes(b, off, len);
    }
    public native long skip(long n) throws IOException;
    public native int available() throws IOException;
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        if (channel != null) {
           fd.decrementAndGetUseCount();
           channel.close();
        }
        int useCount = fd.decrementAndGetUseCount();
        if ((useCount <= 0) || !isRunningFinalize()) {
            close0();
        }
    }
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) return fd;
        throw new IOException();
    }
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, true, false, this);
                fd.incrementAndGetUseCount();
            }
            return channel;
        }
    }
    private static native void initIDs();
    private native void close0() throws IOException;
    static {
        initIDs();
    }
    protected void finalize() throws IOException {
        if ((fd != null) &&  (fd != FileDescriptor.in)) {
            runningFinalize.set(Boolean.TRUE);
            try {
                close();
            } finally {
                runningFinalize.set(Boolean.FALSE);
            }
        }
    }
}
