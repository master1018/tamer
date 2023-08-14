public class BackupDataInputStream extends InputStream {
    String key;
    int dataSize;
    BackupDataInput mData;
    byte[] mOneByte;
    BackupDataInputStream(BackupDataInput data) {
        mData = data;
    }
    public int read() throws IOException {
        byte[] one = mOneByte;
        if (mOneByte == null) {
            one = mOneByte = new byte[1];
        }
        mData.readEntityData(one, 0, 1);
        return one[0];
    }
    public int read(byte[] b, int offset, int size) throws IOException {
        return mData.readEntityData(b, offset, size);
    }
    public int read(byte[] b) throws IOException {
        return mData.readEntityData(b, 0, b.length);
    }
    public String getKey() {
        return this.key;
    }
    public int size() {
        return this.dataSize;
    }
}
