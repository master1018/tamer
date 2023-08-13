class OSFileSystem implements IFileSystem {
    private static final OSFileSystem singleton = new OSFileSystem();
    public static OSFileSystem getOSFileSystem() {
        return singleton;
    }
    private OSFileSystem() {
        super();
    }
    private final void validateLockArgs(int type, long start, long length) {
        if ((type != IFileSystem.SHARED_LOCK_TYPE)
                && (type != IFileSystem.EXCLUSIVE_LOCK_TYPE)) {
            throw new IllegalArgumentException("Illegal lock type requested."); 
        }
        if (start < 0) {
            throw new IllegalArgumentException(
                    "Lock start position must be non-negative"); 
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Lock length must be non-negative"); 
        }
    }
    private native int lockImpl(int fileDescriptor, long start, long length,
            int type, boolean wait);
    public native int getAllocGranularity();
    public boolean lock(int fileDescriptor, long start, long length, int type,
            boolean waitFlag) throws IOException {
        validateLockArgs(type, start, length);
        int result = lockImpl(fileDescriptor, start, length, type, waitFlag);
        return result != -1;
    }
    private native void unlockImpl(int fileDescriptor, long start, long length) throws IOException;
    public void unlock(int fileDescriptor, long start, long length)
            throws IOException {
        validateLockArgs(IFileSystem.SHARED_LOCK_TYPE, start, length);
        unlockImpl(fileDescriptor, start, length);
    }
    public native void fflush(int fileDescriptor, boolean metadata) throws IOException;
    public native long seek(int fd, long offset, int whence) throws IOException;
    public native long readDirect(int fileDescriptor, int address, int offset, int length);
    public native long writeDirect(int fileDescriptor, int address, int offset, int length)
            throws IOException;
    private native long readImpl(int fileDescriptor, byte[] bytes, int offset,
            int length) throws IOException;
    public long read(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        return readImpl(fileDescriptor, bytes, offset, length);
    }
    private native long writeImpl(int fileDescriptor, byte[] bytes,
            int offset, int length) throws IOException;
    public long write(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        return writeImpl(fileDescriptor, bytes, offset, length);
    }
    public native long readv(int fileDescriptor, int[] addresses,
            int[] offsets, int[] lengths, int size) throws IOException;
    public native long writev(int fileDescriptor, int[] addresses, int[] offsets,
            int[] lengths, int size) throws IOException;
    public native void close(int fileDescriptor) throws IOException;
    public native void truncate(int fileDescriptor, long size) throws IOException;
    public int open(byte[] utfPathBytes, int mode) throws FileNotFoundException {
        if (utfPathBytes == null) {
            throw new NullPointerException();
        }
        return openImpl(utfPathBytes, mode);
    }
    private native int openImpl(byte[] fileName, int mode);
    public native long transfer(int fd, FileDescriptor sd, long offset, long count)
            throws IOException;
    public native int ioctlAvailable(int fileDescriptor) throws IOException;
}
