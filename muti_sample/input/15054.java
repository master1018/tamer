class FileOutputStream extends OutputStream
{
    private final FileDescriptor fd;
    private final boolean append;
    private FileChannel channel;
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
    public FileOutputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null, false);
    }
    public FileOutputStream(String name, boolean append)
        throws FileNotFoundException
    {
        this(name != null ? new File(name) : null, append);
    }
    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }
    public FileOutputStream(File file, boolean append)
        throws FileNotFoundException
    {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        this.fd = new FileDescriptor();
        this.append = append;
        fd.incrementAndGetUseCount();
        open(name, append);
    }
    public FileOutputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkWrite(fdObj);
        }
        this.fd = fdObj;
        this.append = false;
        fd.incrementAndGetUseCount();
    }
    private native void open(String name, boolean append)
        throws FileNotFoundException;
    private native void write(int b, boolean append) throws IOException;
    public void write(int b) throws IOException {
        write(b, append);
    }
    private native void writeBytes(byte b[], int off, int len, boolean append)
        throws IOException;
    public void write(byte b[]) throws IOException {
        writeBytes(b, 0, b.length, append);
    }
    public void write(byte b[], int off, int len) throws IOException {
        writeBytes(b, off, len, append);
    }
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
     public final FileDescriptor getFD()  throws IOException {
        if (fd != null) return fd;
        throw new IOException();
     }
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, false, true, append, this);
                fd.incrementAndGetUseCount();
            }
            return channel;
        }
    }
    protected void finalize() throws IOException {
        if (fd != null) {
            if (fd == FileDescriptor.out || fd == FileDescriptor.err) {
                flush();
            } else {
                runningFinalize.set(Boolean.TRUE);
                try {
                    close();
                } finally {
                    runningFinalize.set(Boolean.FALSE);
                }
            }
        }
    }
    private native void close0() throws IOException;
    private static native void initIDs();
    static {
        initIDs();
    }
}
