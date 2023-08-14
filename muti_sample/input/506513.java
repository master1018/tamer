public class JournaledFile {
    File mReal;
    File mTemp;
    boolean mWriting;
    public JournaledFile(File real, File temp) {
        mReal = real;
        mTemp = temp;
    }
    public File chooseForRead() {
        File result;
        if (mReal.exists()) {
            result = mReal;
            if (mTemp.exists()) {
                mTemp.delete();
            }
        } else if (mTemp.exists()) {
            result = mTemp;
            mTemp.renameTo(mReal);
        } else {
            return mReal;
        }
        return result;
    }
    public File chooseForWrite() {
        if (mWriting) {
            throw new IllegalStateException("uncommitted write already in progress");
        }
        if (!mReal.exists()) {
            try {
                mReal.createNewFile();
            } catch (IOException e) {
            }
        }
        if (mTemp.exists()) {
            mTemp.delete();
        }
        mWriting = true;
        return mTemp;
    }
    public void commit() {
        if (!mWriting) {
            throw new IllegalStateException("no file to commit");
        }
        mWriting = false;
        mTemp.renameTo(mReal);
    }
    public void rollback() {
        if (!mWriting) {
            throw new IllegalStateException("no file to roll back");
        }
        mWriting = false;
        mTemp.delete();
    }
}
