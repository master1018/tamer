public final class ResampleInputStream extends InputStream
{    
    static {
        System.loadLibrary("media_jni");
    }
    private final static String TAG = "ResampleInputStream";
    private InputStream mInputStream;
    private final int mRateIn;
    private final int mRateOut;
    private byte[] mBuf;
    private int mBufCount;
    private static final int mFirLength = 29;
    private final byte[] mOneByte = new byte[1];
    public ResampleInputStream(InputStream inputStream, int rateIn, int rateOut) {
        if (rateIn != 2 * rateOut) throw new IllegalArgumentException("only support 2:1 at the moment");
        rateIn = 2;
        rateOut = 1;
        mInputStream = inputStream;
        mRateIn = rateIn;
        mRateOut = rateOut;
    }
    @Override
    public int read() throws IOException {
        int rtn = read(mOneByte, 0, 1);
        return rtn == 1 ? (0xff & mOneByte[0]) : -1;
    }
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (mInputStream == null) throw new IllegalStateException("not open");
        int nIn = ((length / 2) * mRateIn / mRateOut + mFirLength) * 2;
        if (mBuf == null) {
            mBuf = new byte[nIn];
        } else if (nIn > mBuf.length) {
            byte[] bf = new byte[nIn];
            System.arraycopy(mBuf, 0, bf, 0, mBufCount);
            mBuf = bf;
        }
        while (true) {
            int len = ((mBufCount / 2 - mFirLength) * mRateOut / mRateIn) * 2;
            if (len > 0) {
                length = len < length ? len : (length / 2) * 2;
                break;
            }
            int n = mInputStream.read(mBuf, mBufCount, mBuf.length - mBufCount);
            if (n == -1) return -1;
            mBufCount += n;
        }
        fir21(mBuf, 0, b, offset, length / 2);
        int nFwd = length * mRateIn / mRateOut;
        mBufCount -= nFwd;
        if (mBufCount > 0) System.arraycopy(mBuf, nFwd, mBuf, 0, mBufCount);
        return length;
    }
    @Override
    public void close() throws IOException {
        try {
            if (mInputStream != null) mInputStream.close();
        } finally {
            mInputStream = null;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        if (mInputStream != null) {
            close();
            throw new IllegalStateException("someone forgot to close ResampleInputStream");
        }
    }
    private static native void fir21(byte[] in, int inOffset,
            byte[] out, int outOffset, int npoints);
}
