public class AssetFileDescriptor implements Parcelable {
    public static final long UNKNOWN_LENGTH = -1;
    private final ParcelFileDescriptor mFd;
    private final long mStartOffset;
    private final long mLength;
    public AssetFileDescriptor(ParcelFileDescriptor fd, long startOffset,
            long length) {
        if (length < 0 && startOffset != 0) {
            throw new IllegalArgumentException(
                    "startOffset must be 0 when using UNKNOWN_LENGTH");
        }
        mFd = fd;
        mStartOffset = startOffset;
        mLength = length;
    }
    public ParcelFileDescriptor getParcelFileDescriptor() {
        return mFd;
    }
    public FileDescriptor getFileDescriptor() {
        return mFd.getFileDescriptor();
    }
    public long getStartOffset() {
        return mStartOffset;
    }
    public long getLength() {
        if (mLength >= 0) {
            return mLength;
        }
        long len = mFd.getStatSize();
        return len >= 0 ? len : UNKNOWN_LENGTH;
    }
    public long getDeclaredLength() {
        return mLength;
    }
    public void close() throws IOException {
        mFd.close();
    }
    private boolean isMemoryFile() throws IOException {
        return MemoryFile.isMemoryFile(mFd.getFileDescriptor());
    }
    public FileInputStream createInputStream() throws IOException {
        if (isMemoryFile()) {
            if (mLength > Integer.MAX_VALUE) {
                throw new IOException("File length too large for a memory file: " + mLength);
            }
            return new AutoCloseMemoryFileInputStream(mFd, (int)mLength);
        }
        if (mLength < 0) {
            return new ParcelFileDescriptor.AutoCloseInputStream(mFd);
        }
        return new AutoCloseInputStream(this);
    }
    public FileOutputStream createOutputStream() throws IOException {
        if (mLength < 0) {
            return new ParcelFileDescriptor.AutoCloseOutputStream(mFd);
        }
        return new AutoCloseOutputStream(this);
    }
    @Override
    public String toString() {
        return "{AssetFileDescriptor: " + mFd
                + " start=" + mStartOffset + " len=" + mLength + "}";
    }
    public static class AutoCloseInputStream
            extends ParcelFileDescriptor.AutoCloseInputStream {
        private long mRemaining;
        public AutoCloseInputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            super.skip(fd.getStartOffset());
            mRemaining = (int)fd.getLength();
        }
        @Override
        public int available() throws IOException {
            return mRemaining >= 0
                    ? (mRemaining < 0x7fffffff ? (int)mRemaining : 0x7fffffff)
                    : super.available();
        }
        @Override
        public int read() throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return -1;
                int res = super.read();
                if (res >= 0) mRemaining--;
                return res;
            }
            return super.read();
        }
        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return -1;
                if (count > mRemaining) count = (int)mRemaining;
                int res = super.read(buffer, offset, count);
                if (res >= 0) mRemaining -= res;
                return res;
            }
            return super.read(buffer, offset, count);
        }
        @Override
        public int read(byte[] buffer) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return -1;
                int count = buffer.length;
                if (count > mRemaining) count = (int)mRemaining;
                int res = super.read(buffer, 0, count);
                if (res >= 0) mRemaining -= res;
                return res;
            }
            return super.read(buffer);
        }
        @Override
        public long skip(long count) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return -1;
                if (count > mRemaining) count = mRemaining;
                long res = super.skip(count);
                if (res >= 0) mRemaining -= res;
                return res;
            }
            return super.skip(count);
        }
        @Override
        public void mark(int readlimit) {
            if (mRemaining >= 0) {
                return;
            }
            super.mark(readlimit);
        }
        @Override
        public boolean markSupported() {
            if (mRemaining >= 0) {
                return false;
            }
            return super.markSupported();
        }
        @Override
        public synchronized void reset() throws IOException {
            if (mRemaining >= 0) {
                return;
            }
            super.reset();
        }
    }
    private static class AutoCloseMemoryFileInputStream extends FileInputStream {
        private ParcelFileDescriptor mParcelFd;
        private MemoryFile mFile;
        private InputStream mStream;
        public AutoCloseMemoryFileInputStream(ParcelFileDescriptor fd, int length)
                throws IOException {
            super(fd.getFileDescriptor());
            mParcelFd = fd;
            mFile = new MemoryFile(fd.getFileDescriptor(), length, "r");
            mStream = mFile.getInputStream();
        }
        @Override
        public int available() throws IOException {
            return mStream.available();
        }
        @Override
        public void close() throws IOException {
            mParcelFd.close();  
            mFile.close();      
            mStream.close();    
        }
        @Override
        public FileChannel getChannel() {
            return null;
        }
        @Override
        public int read() throws IOException {
            return mStream.read();
        }
        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            return mStream.read(buffer, offset, count);
        }
        @Override
        public int read(byte[] buffer) throws IOException {
            return mStream.read(buffer);
        }
        @Override
        public long skip(long count) throws IOException {
            return mStream.skip(count);
        }
    }
    public static class AutoCloseOutputStream
            extends ParcelFileDescriptor.AutoCloseOutputStream {
        private long mRemaining;
        public AutoCloseOutputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            if (fd.getParcelFileDescriptor().seekTo(fd.getStartOffset()) < 0) {
                throw new IOException("Unable to seek");
            }
            mRemaining = (int)fd.getLength();
        }
        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return;
                if (count > mRemaining) count = (int)mRemaining;
                super.write(buffer, offset, count);
                mRemaining -= count;
                return;
            }
            super.write(buffer, offset, count);
        }
        @Override
        public void write(byte[] buffer) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return;
                int count = buffer.length;
                if (count > mRemaining) count = (int)mRemaining;
                super.write(buffer);
                mRemaining -= count;
                return;
            }
            super.write(buffer);
        }
        @Override
        public void write(int oneByte) throws IOException {
            if (mRemaining >= 0) {
                if (mRemaining == 0) return;
                super.write(oneByte);
                mRemaining--;
                return;
            }
            super.write(oneByte);
        }
    }
    public int describeContents() {
        return mFd.describeContents();
    }
    public void writeToParcel(Parcel out, int flags) {
        mFd.writeToParcel(out, flags);
        out.writeLong(mStartOffset);
        out.writeLong(mLength);
    }
    AssetFileDescriptor(Parcel src) {
        mFd = ParcelFileDescriptor.CREATOR.createFromParcel(src);
        mStartOffset = src.readLong();
        mLength = src.readLong();
    }
    public static final Parcelable.Creator<AssetFileDescriptor> CREATOR
            = new Parcelable.Creator<AssetFileDescriptor>() {
        public AssetFileDescriptor createFromParcel(Parcel in) {
            return new AssetFileDescriptor(in);
        }
        public AssetFileDescriptor[] newArray(int size) {
            return new AssetFileDescriptor[size];
        }
    };
    public static AssetFileDescriptor fromMemoryFile(MemoryFile memoryFile)
            throws IOException {
        ParcelFileDescriptor fd = memoryFile.getParcelFileDescriptor();
        return new AssetFileDescriptor(fd, 0, memoryFile.length());
    }
}
