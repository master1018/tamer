public final class PrivateInputStream extends InputStream {
    private BaseStream mParent;
    private byte[] mData;
    private int mIndex;
    private boolean mOpen;
    public PrivateInputStream(BaseStream p) {
        mParent = p;
        mData = new byte[0];
        mIndex = 0;
        mOpen = true;
    }
    @Override
    public synchronized int available() throws IOException {
        ensureOpen();
        return mData.length - mIndex;
    }
    @Override
    public synchronized int read() throws IOException {
        ensureOpen();
        while (mData.length == mIndex) {
            if (!mParent.continueOperation(true, true)) {
                return -1;
            }
        }
        return (mData[mIndex++] & 0xFF);
    }
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public synchronized int read(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new IOException("buffer is null");
        }
        if ((offset | length) < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException("index outof bound");
        }
        ensureOpen();
        int currentDataLength = mData.length - mIndex;
        int remainReadLength = length;
        int offset1 = offset;
        int result = 0;
        while (currentDataLength <= remainReadLength) {
            System.arraycopy(mData, mIndex, b, offset1, currentDataLength);
            mIndex += currentDataLength;
            offset1 += currentDataLength;
            result += currentDataLength;
            remainReadLength -= currentDataLength;
            if (!mParent.continueOperation(true, true)) {
                return result == 0 ? -1 : result;
            }
            currentDataLength = mData.length - mIndex;
        }
        if (remainReadLength > 0) {
            System.arraycopy(mData, mIndex, b, offset1, remainReadLength);
            mIndex += remainReadLength;
            result += remainReadLength;
        }
        return result;
    }
    public synchronized void writeBytes(byte[] body, int start) {
        int length = (body.length - start) + (mData.length - mIndex);
        byte[] temp = new byte[length];
        System.arraycopy(mData, mIndex, temp, 0, mData.length - mIndex);
        System.arraycopy(body, start, temp, mData.length - mIndex, body.length - start);
        mData = temp;
        mIndex = 0;
        notifyAll();
    }
    private void ensureOpen() throws IOException {
        mParent.ensureOpen();
        if (!mOpen) {
            throw new IOException("Input stream is closed");
        }
    }
    @Override
    public void close() throws IOException {
        mOpen = false;
        mParent.streamClosed(true);
    }
}
