public final class FileDescriptor {
    public static final FileDescriptor in = new FileDescriptor();
    public static final FileDescriptor out = new FileDescriptor();
    public static final FileDescriptor err = new FileDescriptor();
    int descriptor = -1;
    boolean readOnly = false;
    private static native void oneTimeInitialization();
    static {
        in.descriptor = 0;
        out.descriptor = 1;
        err.descriptor = 2;
        oneTimeInitialization();
    }
    public FileDescriptor() {
        super();
    }
    public void sync() throws SyncFailedException {
        if (!readOnly) {
            syncImpl();
        }
    }
    private native void syncImpl() throws SyncFailedException;
    public boolean valid() {
        return descriptor != -1;
    }
}
