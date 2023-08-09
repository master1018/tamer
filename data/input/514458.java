public final class LogReceiver {
    private final static int ENTRY_HEADER_SIZE = 20; 
    public final static class LogEntry {
        public int  len; 
        public int   pid;
        public int   tid;
        public int   sec;
        public int   nsec;
        public byte[] data;
    };
    public interface ILogListener {
        public void newEntry(LogEntry entry);
        public void newData(byte[] data, int offset, int length);
    }
    private LogEntry mCurrentEntry;
    private byte[] mEntryHeaderBuffer = new byte[ENTRY_HEADER_SIZE];
    private int mEntryHeaderOffset = 0;
    private int mEntryDataOffset = 0;
    private ILogListener mListener;
    private boolean mIsCancelled = false;
    public LogReceiver(ILogListener listener) {
        mListener = listener;
    }
    public void parseNewData(byte[] data, int offset, int length) {
        if (mListener != null) {
            mListener.newData(data, offset, length);
        }
        while (length > 0 && mIsCancelled == false) {
            if (mCurrentEntry == null) {
                if (mEntryHeaderOffset + length < ENTRY_HEADER_SIZE) {
                    System.arraycopy(data, offset, mEntryHeaderBuffer, mEntryHeaderOffset, length);
                    mEntryHeaderOffset += length;
                    return;
                } else {
                    if (mEntryHeaderOffset != 0) {
                        int size = ENTRY_HEADER_SIZE - mEntryHeaderOffset; 
                        System.arraycopy(data, offset, mEntryHeaderBuffer, mEntryHeaderOffset,
                                size);
                        mCurrentEntry = createEntry(mEntryHeaderBuffer, 0);
                        mEntryHeaderOffset = 0;
                        offset += size;
                        length -= size;
                    } else {
                        mCurrentEntry = createEntry(data, offset);
                        offset += ENTRY_HEADER_SIZE;
                        length -= ENTRY_HEADER_SIZE;
                    }
                }
            }
            if (length >= mCurrentEntry.len - mEntryDataOffset) {
                int dataSize = mCurrentEntry.len - mEntryDataOffset;  
                System.arraycopy(data, offset, mCurrentEntry.data, mEntryDataOffset, dataSize);
                if (mListener != null) {
                    mListener.newEntry(mCurrentEntry);
                }
                mEntryDataOffset = 0;
                mCurrentEntry = null;
                offset += dataSize;
                length -= dataSize;
            } else {
                System.arraycopy(data, offset, mCurrentEntry.data, mEntryDataOffset, length);
                mEntryDataOffset += length;
                return;
            }
        }
    }
    public boolean isCancelled() {
        return mIsCancelled;
    }
    public void cancel() {
        mIsCancelled = true;
    }
    private LogEntry createEntry(byte[] data, int offset) {
        if (data.length < offset + ENTRY_HEADER_SIZE) {
            throw new InvalidParameterException(
                    "Buffer not big enough to hold full LoggerEntry header");
        }
        LogEntry entry = new LogEntry();
        entry.len = ArrayHelper.swapU16bitFromArray(data, offset);
        offset += 4;
        entry.pid = ArrayHelper.swap32bitFromArray(data, offset);
        offset += 4;
        entry.tid = ArrayHelper.swap32bitFromArray(data, offset);
        offset += 4;
        entry.sec = ArrayHelper.swap32bitFromArray(data, offset);
        offset += 4;
        entry.nsec = ArrayHelper.swap32bitFromArray(data, offset);
        offset += 4;
        entry.data = new byte[entry.len];
        return entry;
    }
}
