public final class PrivateOutputStream extends OutputStream {
    private BaseStream mParent;
    private ByteArrayOutputStream mArray;
    private boolean mOpen;
    private int mMaxPacketSize;
    public PrivateOutputStream(BaseStream p, int maxSize) {
        mParent = p;
        mArray = new ByteArrayOutputStream();
        mMaxPacketSize = maxSize;
        mOpen = true;
    }
    public int size() {
        return mArray.size();
    }
    @Override
    public synchronized void write(int b) throws IOException {
        ensureOpen();
        mParent.ensureNotDone();
        mArray.write(b);
        if (mArray.size() == mMaxPacketSize) {
            mParent.continueOperation(true, false);
        }
    }
    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    @Override
    public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
        int offset1 = offset;
        int remainLength = count;
        if (buffer == null) {
            throw new IOException("buffer is null");
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException("index outof bound");
        }
        ensureOpen();
        mParent.ensureNotDone();
        if (count < mMaxPacketSize) {
            mArray.write(buffer, offset, count);
        } else {
            while (remainLength >= mMaxPacketSize) {
                mArray.write(buffer, offset1, mMaxPacketSize);
                offset1 += mMaxPacketSize;
                remainLength = count - offset1;
                mParent.continueOperation(true, false);
            }
            if (remainLength > 0) {
                mArray.write(buffer, offset1, remainLength);
            }
        }
    }
    public synchronized byte[] readBytes(int size) {
        if (mArray.size() > 0) {
            byte[] temp = mArray.toByteArray();
            mArray.reset();
            byte[] result = new byte[size];
            System.arraycopy(temp, 0, result, 0, size);
            if (temp.length != size) {
                mArray.write(temp, size, temp.length - size);
            }
            return result;
        } else {
            return null;
        }
    }
    private void ensureOpen() throws IOException {
        mParent.ensureOpen();
        if (!mOpen) {
            throw new IOException("Output stream is closed");
        }
    }
    @Override
    public void close() throws IOException {
        mOpen = false;
        mParent.streamClosed(false);
    }
    public boolean isClosed() {
        return !mOpen;
    }
}
