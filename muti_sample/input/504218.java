class FileBackupHelperBase {
    private static final String TAG = "FileBackupHelperBase";
    int mPtr;
    Context mContext;
    boolean mExceptionLogged;
    FileBackupHelperBase(Context context) {
        mPtr = ctor();
        mContext = context;
    }
    protected void finalize() throws Throwable {
        try {
            dtor(mPtr);
        } finally {
            super.finalize();
        }
    }
    static void performBackup_checked(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState, String[] files, String[] keys) {
        if (files.length == 0) {
            return;
        }
        for (String f: files) {
            if (f.charAt(0) != '/') {
                throw new RuntimeException("files must have all absolute paths: " + f);
            }
        }
        if (files.length != keys.length) {
            throw new RuntimeException("files.length=" + files.length
                    + " keys.length=" + keys.length);
        }
        FileDescriptor oldStateFd = oldState != null ? oldState.getFileDescriptor() : null;
        FileDescriptor newStateFd = newState.getFileDescriptor();
        if (newStateFd == null) {
            throw new NullPointerException();
        }
        int err = performBackup_native(oldStateFd, data.mBackupWriter, newStateFd, files, keys);
        if (err != 0) {
            throw new RuntimeException("Backup failed 0x" + Integer.toHexString(err));
        }
    }
    void writeFile(File f, BackupDataInputStream in) {
        int result = -1;
        File parent = f.getParentFile();
        parent.mkdirs();
        result = writeFile_native(mPtr, f.getAbsolutePath(), in.mData.mBackupReader);
        if (result != 0) {
            if (!mExceptionLogged) {
                Log.e(TAG, "Failed restoring file '" + f + "' for app '"
                        + mContext.getPackageName() + "\' result=0x"
                        + Integer.toHexString(result));
                mExceptionLogged = true;
            }
        }
    }
    public void writeNewStateDescription(ParcelFileDescriptor fd) {
        int result = writeSnapshot_native(mPtr, fd.getFileDescriptor());
    }
    boolean isKeyInList(String key, String[] list) {
        for (String s: list) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }
    private static native int ctor();
    private static native void dtor(int ptr);
    native private static int performBackup_native(FileDescriptor oldState,
            int data, FileDescriptor newState, String[] files, String[] keys);
    private static native int writeFile_native(int ptr, String filename, int backupReader);
    private static native int writeSnapshot_native(int ptr, FileDescriptor fd);
}
