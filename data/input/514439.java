public class ParcelFileDescriptor implements Parcelable {
    private final FileDescriptor mFileDescriptor;
    private boolean mClosed;
    private final ParcelFileDescriptor mParcelDescriptor;
    public static final int MODE_WORLD_READABLE = 0x00000001;
    public static final int MODE_WORLD_WRITEABLE = 0x00000002;
    public static final int MODE_READ_ONLY = 0x10000000;
    public static final int MODE_WRITE_ONLY = 0x20000000;
    public static final int MODE_READ_WRITE = 0x30000000;
    public static final int MODE_CREATE = 0x08000000;
    public static final int MODE_TRUNCATE = 0x04000000;
    public static final int MODE_APPEND = 0x02000000;
    public static ParcelFileDescriptor open(File file, int mode)
            throws FileNotFoundException {
        String path = file.getPath();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
            if ((mode&MODE_WRITE_ONLY) != 0) {
                security.checkWrite(path);
            }
        }
        if ((mode&MODE_READ_WRITE) == 0) {
            throw new IllegalArgumentException(
                    "Must specify MODE_READ_ONLY, MODE_WRITE_ONLY, or MODE_READ_WRITE");
        }
        FileDescriptor fd = Parcel.openFileDescriptor(path, mode);
        return fd != null ? new ParcelFileDescriptor(fd) : null;
    }
    public static ParcelFileDescriptor fromSocket(Socket socket) {
        FileDescriptor fd = getFileDescriptorFromSocket(socket);
        return fd != null ? new ParcelFileDescriptor(fd) : null;
    }
    private static native FileDescriptor getFileDescriptorFromSocket(Socket socket);
    public FileDescriptor getFileDescriptor() {
        return mFileDescriptor;
    }
    public native long getStatSize();
    public native long seekTo(long pos);
    public void close() throws IOException {
        synchronized (this) {
            if (mClosed) return;
            mClosed = true;
        }
        if (mParcelDescriptor != null) {
            mParcelDescriptor.close();
        } else {
            Parcel.closeFileDescriptor(mFileDescriptor);
        }
    }
    public static class AutoCloseInputStream extends FileInputStream {
        private final ParcelFileDescriptor mFd;
        public AutoCloseInputStream(ParcelFileDescriptor fd) {
            super(fd.getFileDescriptor());
            mFd = fd;
        }
        @Override
        public void close() throws IOException {
            mFd.close();
        }
    }
    public static class AutoCloseOutputStream extends FileOutputStream {
        private final ParcelFileDescriptor mFd;
        public AutoCloseOutputStream(ParcelFileDescriptor fd) {
            super(fd.getFileDescriptor());
            mFd = fd;
        }
        @Override
        public void close() throws IOException {
            mFd.close();
        }
    }
    @Override
    public String toString() {
        return "{ParcelFileDescriptor: " + mFileDescriptor + "}";
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (!mClosed) {
                close();
            }
        } finally {
            super.finalize();
        }
    }
    public ParcelFileDescriptor(ParcelFileDescriptor descriptor) {
        super();
        mParcelDescriptor = descriptor;
        mFileDescriptor = mParcelDescriptor.mFileDescriptor;
    }
    ParcelFileDescriptor(FileDescriptor descriptor) {
        super();
        if (descriptor == null) {
            throw new NullPointerException("descriptor must not be null");
        }
        mFileDescriptor = descriptor;
        mParcelDescriptor = null;
    }
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeFileDescriptor(mFileDescriptor);
        if ((flags&PARCELABLE_WRITE_RETURN_VALUE) != 0 && !mClosed) {
            try {
                close();
            } catch (IOException e) {
            }
        }
    }
    public static final Parcelable.Creator<ParcelFileDescriptor> CREATOR
            = new Parcelable.Creator<ParcelFileDescriptor>() {
        public ParcelFileDescriptor createFromParcel(Parcel in) {
            return in.readFileDescriptor();
        }
        public ParcelFileDescriptor[] newArray(int size) {
            return new ParcelFileDescriptor[size];
        }
    };
}
