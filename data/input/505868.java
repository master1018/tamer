public class BackupDataOutput {
    int mBackupWriter;
    public BackupDataOutput(FileDescriptor fd) {
        if (fd == null) throw new NullPointerException();
        mBackupWriter = ctor(fd);
        if (mBackupWriter == 0) {
            throw new RuntimeException("Native initialization failed with fd=" + fd);
        }
    }
    public int writeEntityHeader(String key, int dataSize) throws IOException {
        int result = writeEntityHeader_native(mBackupWriter, key, dataSize);
        if (result >= 0) {
            return result;
        } else {
            throw new IOException("result=0x" + Integer.toHexString(result));
        }
    }
    public int writeEntityData(byte[] data, int size) throws IOException {
        int result = writeEntityData_native(mBackupWriter, data, size);
        if (result >= 0) {
            return result;
        } else {
            throw new IOException("result=0x" + Integer.toHexString(result));
        }
    }
    public void setKeyPrefix(String keyPrefix) {
        setKeyPrefix_native(mBackupWriter, keyPrefix);
    }
    protected void finalize() throws Throwable {
        try {
            dtor(mBackupWriter);
        } finally {
            super.finalize();
        }
    }
    private native static int ctor(FileDescriptor fd);
    private native static void dtor(int mBackupWriter);
    private native static int writeEntityHeader_native(int mBackupWriter, String key, int dataSize);
    private native static int writeEntityData_native(int mBackupWriter, byte[] data, int size);
    private native static void setKeyPrefix_native(int mBackupWriter, String keyPrefix);
}
