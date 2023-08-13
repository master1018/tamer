public final class AmrInputStream extends InputStream
{    
    static {
        System.loadLibrary("media_jni");
    }
    private final static String TAG = "AmrInputStream";
    private final static int SAMPLES_PER_FRAME = 8000 * 20 / 1000;
    private InputStream mInputStream;
    private int mGae;
    private byte[] mBuf = new byte[SAMPLES_PER_FRAME * 2];
    private int mBufIn = 0;
    private int mBufOut = 0;
    private byte[] mOneByte = new byte[1];
    public AmrInputStream(InputStream inputStream) {
        mInputStream = inputStream;
        mGae = GsmAmrEncoderNew();
        GsmAmrEncoderInitialize(mGae);
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
        if (mGae == 0) throw new IllegalStateException("not open");
        if (mBufOut >= mBufIn) {
            mBufOut = 0;
            mBufIn = 0;
            for (int i = 0; i < SAMPLES_PER_FRAME * 2; ) {
                int n = mInputStream.read(mBuf, i, SAMPLES_PER_FRAME * 2 - i);
                if (n == -1) return -1;
                i += n;
            }
            mBufIn = GsmAmrEncoderEncode(mGae, mBuf, 0, mBuf, 0);
        }
        if (length > mBufIn - mBufOut) length = mBufIn - mBufOut;
        System.arraycopy(mBuf, mBufOut, b, offset, length);
        mBufOut += length;
        return length;
    }
    @Override
    public void close() throws IOException {
        try {
            if (mInputStream != null) mInputStream.close();
        } finally {
            mInputStream = null;
            try {
                if (mGae != 0) GsmAmrEncoderCleanup(mGae);
            } finally {
                try {
                    if (mGae != 0) GsmAmrEncoderDelete(mGae);
                } finally {
                    mGae = 0;
                }
            }
        }
    }
    @Override
    protected void finalize() throws Throwable {
        if (mGae != 0) {
            close();
            throw new IllegalStateException("someone forgot to close AmrInputStream");
        }
    }
    private static native int GsmAmrEncoderNew();
    private static native void GsmAmrEncoderInitialize(int gae);
    private static native int GsmAmrEncoderEncode(int gae,
            byte[] pcm, int pcmOffset, byte[] amr, int amrOffset) throws IOException;
    private static native void GsmAmrEncoderCleanup(int gae);
    private static native void GsmAmrEncoderDelete(int gae);
}
