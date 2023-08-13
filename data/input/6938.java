public final class FileDescriptor {
    private int fd;
    private AtomicInteger useCount;
    public  FileDescriptor() {
        fd = -1;
        useCount = new AtomicInteger();
    }
    private  FileDescriptor(int fd) {
        this.fd = fd;
        useCount = new AtomicInteger();
    }
    public static final FileDescriptor in = new FileDescriptor(0);
    public static final FileDescriptor out = new FileDescriptor(1);
    public static final FileDescriptor err = new FileDescriptor(2);
    public boolean valid() {
        return fd != -1;
    }
    public native void sync() throws SyncFailedException;
    private static native void initIDs();
    static {
        initIDs();
    }
    static {
        sun.misc.SharedSecrets.setJavaIOFileDescriptorAccess(
            new sun.misc.JavaIOFileDescriptorAccess() {
                public void set(FileDescriptor obj, int fd) {
                    obj.fd = fd;
                }
                public int get(FileDescriptor obj) {
                    return obj.fd;
                }
                public void setHandle(FileDescriptor obj, long handle) {
                    throw new UnsupportedOperationException();
                }
                public long getHandle(FileDescriptor obj) {
                    throw new UnsupportedOperationException();
                }
            }
        );
    }
    int incrementAndGetUseCount() {
        return useCount.incrementAndGet();
    }
    int decrementAndGetUseCount() {
        return useCount.decrementAndGet();
    }
}
