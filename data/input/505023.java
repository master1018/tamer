public class SmsSplitter {
    private static final int MAX_SEGMENT_COUNT = 26;
    private ByteBuffer mOutBuffer;
    private int mMaxSegmentLen;
    private byte[] mData;
    private int mPreambleEnd;
    private int mCurrentSegment;
    private int mSegmentCount;
    public SmsSplitter(int maxLen) {
        mMaxSegmentLen = maxLen;
        mOutBuffer = ByteBuffer.allocate(maxLen);
    }
    public int split(byte[] data) {
        mData = data;
        mCurrentSegment = 0;
        calculateSegments();
        if (mSegmentCount > MAX_SEGMENT_COUNT) {
            mSegmentCount = -1;
        }
        return mSegmentCount;
    }
    public boolean hasNext() {
        return mCurrentSegment < mSegmentCount;
    }
    public byte[] getNext() {
        if (mCurrentSegment >= mSegmentCount) {
            throw new IndexOutOfBoundsException();
        }
        byte[] segment;
        if (mSegmentCount == 1) {
            segment = mData;
        } else {
            mOutBuffer.clear();
            mOutBuffer.put(mData, 0, mPreambleEnd);
            mOutBuffer.put((byte) ('a' + mCurrentSegment));
            mOutBuffer.put((byte) ('a' + mSegmentCount - 1));
            mOutBuffer.put((byte) ' ');
            int segmentPayload = mMaxSegmentLen - mPreambleEnd - 3;
            int offset = mPreambleEnd + 1 + segmentPayload * mCurrentSegment;
            int len = (offset + segmentPayload > mData.length) ?
                    mData.length - offset : segmentPayload;
            mOutBuffer.put(mData, offset, len);
            mOutBuffer.flip();
            segment = new byte[mOutBuffer.limit()];
            mOutBuffer.get(segment);
        }
        mCurrentSegment++;
        return segment;
    }
    private void calculateSegments() {
        int totalLen = mData.length;
        if (totalLen < mMaxSegmentLen) {
            mSegmentCount = 1;
        } else {
            searchPreambleEnd();
            int newPreambleLen = mPreambleEnd + 2;
            int segmentPayload = mMaxSegmentLen - newPreambleLen - 1;
            int totalPayload = totalLen - mPreambleEnd - 1;
            mSegmentCount = (totalPayload + segmentPayload -1) / segmentPayload;
        }
    }
    private void searchPreambleEnd() {
        byte[] data = mData;
        int index = 0;
        while(index < data.length && data[index] != ' ') {
            index++;
        }
        mPreambleEnd = index;
    }
}