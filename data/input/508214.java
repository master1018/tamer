public class BackupDataInput {
    int mBackupReader;
    private EntityHeader mHeader = new EntityHeader();
    private boolean mHeaderReady;
    private static class EntityHeader {
        String key;
        int dataSize;
    }
    public BackupDataInput(FileDescriptor fd) {
        if (fd == null) throw new NullPointerException();
        mBackupReader = ctor(fd);
        if (mBackupReader == 0) {
            throw new RuntimeException("Native initialization failed with fd=" + fd);
        }
    }
    protected void finalize() throws Throwable {
        try {
            dtor(mBackupReader);
        } finally {
            super.finalize();
        }
    }
    public boolean readNextHeader() throws IOException {
        int result = readNextHeader_native(mBackupReader, mHeader);
        if (result == 0) {
            mHeaderReady = true;
            return true;
        } else if (result > 0) {
            mHeaderReady = false;
            return false;
        } else {
            mHeaderReady = false;
            throw new IOException("failed: 0x" + Integer.toHexString(result));
        }
    }
    public String getKey() {
        if (mHeaderReady) {
            return mHeader.key;
        } else {
            throw new IllegalStateException("Entity header not read");
        }
    }
    public int getDataSize() {
        if (mHeaderReady) {
            return mHeader.dataSize;
        } else {
            throw new IllegalStateException("Entity header not read");
        }
    }
    public int readEntityData(byte[] data, int offset, int size) throws IOException {
        if (mHeaderReady) {
            int result = readEntityData_native(mBackupReader, data, offset, size);
            if (result >= 0) {
                return result;
            } else {
                throw new IOException("result=0x" + Integer.toHexString(result));
            }
        } else {
            throw new IllegalStateException("Entity header not read");
        }
    }
    public void skipEntityData() throws IOException {
        if (mHeaderReady) {
            skipEntityData_native(mBackupReader);
        } else {
            throw new IllegalStateException("Entity header not read");
        }
    }
    private native static int ctor(FileDescriptor fd);
    private native static void dtor(int mBackupReader);
    private native int readNextHeader_native(int mBackupReader, EntityHeader entity);
    private native int readEntityData_native(int mBackupReader, byte[] data, int offset, int size);
    private native int skipEntityData_native(int mBackupReader);
}
