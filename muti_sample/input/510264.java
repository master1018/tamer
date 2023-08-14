public class MemoryFile
{
    private static String TAG = "MemoryFile";
    private static final int PROT_READ = 0x1;
    private static final int PROT_WRITE = 0x2;
    private static native FileDescriptor native_open(String name, int length) throws IOException;
    private static native int native_mmap(FileDescriptor fd, int length, int mode)
            throws IOException;
    private static native void native_munmap(int addr, int length) throws IOException;
    private static native void native_close(FileDescriptor fd);
    private static native int native_read(FileDescriptor fd, int address, byte[] buffer,
            int srcOffset, int destOffset, int count, boolean isUnpinned) throws IOException;
    private static native void native_write(FileDescriptor fd, int address, byte[] buffer,
            int srcOffset, int destOffset, int count, boolean isUnpinned) throws IOException;
    private static native void native_pin(FileDescriptor fd, boolean pin) throws IOException;
    private static native int native_get_size(FileDescriptor fd) throws IOException;
    private FileDescriptor mFD;        
    private int mAddress;   
    private int mLength;    
    private boolean mAllowPurging = false;  
    private final boolean mOwnsRegion;  
    public MemoryFile(String name, int length) throws IOException {
        mLength = length;
        mFD = native_open(name, length);
        mAddress = native_mmap(mFD, length, PROT_READ | PROT_WRITE);
        mOwnsRegion = true;
    }
    public MemoryFile(FileDescriptor fd, int length, String mode) throws IOException {
        if (fd == null) {
            throw new NullPointerException("File descriptor is null.");
        }
        if (!isMemoryFile(fd)) {
            throw new IllegalArgumentException("Not a memory file.");
        }
        mLength = length;
        mFD = fd;
        mAddress = native_mmap(mFD, length, modeToProt(mode));
        mOwnsRegion = false;
    }
    public void close() {
        deactivate();
        if (!isClosed()) {
            native_close(mFD);
        }
    }
    public void deactivate() {
        if (!isDeactivated()) {
            try {
                native_munmap(mAddress, mLength);
                mAddress = 0;
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }
    private boolean isDeactivated() {
        return mAddress == 0;
    }
    private boolean isClosed() {
        return !mFD.valid();
    }
    @Override
    protected void finalize() {
        if (!isClosed()) {
            Log.e(TAG, "MemoryFile.finalize() called while ashmem still open");
            close();
        }
    }
    public int length() {
        return mLength;
    }
    public boolean isPurgingAllowed() {
        return mAllowPurging;
    }
    synchronized public boolean allowPurging(boolean allowPurging) throws IOException {
        if (!mOwnsRegion) {
            throw new IOException("Only the owner can make ashmem regions purgable.");
        }
        boolean oldValue = mAllowPurging;
        if (oldValue != allowPurging) {
            native_pin(mFD, !allowPurging);
            mAllowPurging = allowPurging;
        }
        return oldValue;
    }
    public InputStream getInputStream() {
        return new MemoryInputStream();
    }
     public OutputStream getOutputStream() {
        return new MemoryOutputStream();
    }
    public int readBytes(byte[] buffer, int srcOffset, int destOffset, int count) 
            throws IOException {
        if (isDeactivated()) {
            throw new IOException("Can't read from deactivated memory file.");
        }
        if (destOffset < 0 || destOffset > buffer.length || count < 0
                || count > buffer.length - destOffset
                || srcOffset < 0 || srcOffset > mLength
                || count > mLength - srcOffset) {
            throw new IndexOutOfBoundsException();
        }
        return native_read(mFD, mAddress, buffer, srcOffset, destOffset, count, mAllowPurging);
    }
    public void writeBytes(byte[] buffer, int srcOffset, int destOffset, int count)
            throws IOException {
        if (isDeactivated()) {
            throw new IOException("Can't write to deactivated memory file.");
        }
        if (srcOffset < 0 || srcOffset > buffer.length || count < 0
                || count > buffer.length - srcOffset
                || destOffset < 0 || destOffset > mLength
                || count > mLength - destOffset) {
            throw new IndexOutOfBoundsException();
        }
        native_write(mFD, mAddress, buffer, srcOffset, destOffset, count, mAllowPurging);
    }
    public ParcelFileDescriptor getParcelFileDescriptor() throws IOException {
        FileDescriptor fd = getFileDescriptor();
        return fd != null ? new ParcelFileDescriptor(fd) : null;
    }
    public FileDescriptor getFileDescriptor() throws IOException {
        return mFD;
    }
    public static boolean isMemoryFile(FileDescriptor fd) throws IOException {
        return (native_get_size(fd) >= 0);
    }
    public static int getSize(FileDescriptor fd) throws IOException {
        return native_get_size(fd);
    }
    private static int modeToProt(String mode) {
        if ("r".equals(mode)) {
            return PROT_READ;
        } else {
            throw new IllegalArgumentException("Unsupported file mode: '" + mode + "'");
        }
    }
    private class MemoryInputStream extends InputStream {
        private int mMark = 0;
        private int mOffset = 0;
        private byte[] mSingleByte;
        @Override
        public int available() throws IOException {
            if (mOffset >= mLength) {
                return 0;
            }
            return mLength - mOffset;
        }
        @Override
        public boolean markSupported() {
            return true;
        }
        @Override
        public void mark(int readlimit) {
            mMark = mOffset;
        }
        @Override
        public void reset() throws IOException {
            mOffset = mMark;
        }
        @Override
        public int read() throws IOException {
            if (mSingleByte == null) {
                mSingleByte = new byte[1];
            }
            int result = read(mSingleByte, 0, 1);
            if (result != 1) {
                return -1;
            }
            return mSingleByte[0];
        }
        @Override
        public int read(byte buffer[], int offset, int count) throws IOException {
            if (offset < 0 || count < 0 || offset + count > buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, available());
            if (count < 1) {
                return -1;
            }
            int result = readBytes(buffer, mOffset, offset, count);
            if (result > 0) {
                mOffset += result;
            }
            return result;
        }
        @Override
        public long skip(long n) throws IOException {
            if (mOffset + n > mLength) {
                n = mLength - mOffset;
            }
            mOffset += n;
            return n;
        }
    }
    private class MemoryOutputStream extends OutputStream {
        private int mOffset = 0;
        private byte[] mSingleByte;
        @Override
        public void write(byte buffer[], int offset, int count) throws IOException {
            writeBytes(buffer, offset, mOffset, count);
        }
        @Override
        public void write(int oneByte) throws IOException {
            if (mSingleByte == null) {
                mSingleByte = new byte[1];
            }
            mSingleByte[0] = (byte)oneByte;
            write(mSingleByte, 0, 1);
        }
    }
}
