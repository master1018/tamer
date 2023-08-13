public class BitwiseInputStream {
    private byte[] mBuf;
    private int mPos;
    private int mEnd;
    public static class AccessException extends Exception {
        public AccessException(String s) {
            super("BitwiseInputStream access failed: " + s);
        }
    }
    public BitwiseInputStream(byte buf[]) {
        mBuf = buf;
        mEnd = buf.length << 3;
        mPos = 0;
    }
    public int available() {
        return mEnd - mPos;
    }
    public int read(int bits) throws AccessException {
        int index = mPos >>> 3;
        int offset = 16 - (mPos & 0x07) - bits;  
        if ((bits < 0) || (bits > 8) || ((mPos + bits) > mEnd)) {
            throw new AccessException("illegal read " +
                "(pos " + mPos + ", end " + mEnd + ", bits " + bits + ")");
        }
        int data = (mBuf[index] & 0xFF) << 8;
        if (offset < 8) data |= mBuf[index + 1] & 0xFF;
        data >>>= offset;
        data &= (-1 >>> (32 - bits));
        mPos += bits;
        return data;
    }
    public byte[] readByteArray(int bits) throws AccessException {
        int bytes = (bits >>> 3) + ((bits & 0x07) > 0 ? 1 : 0);  
        byte[] arr = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            int increment = Math.min(8, bits - (i << 3));
            arr[i] = (byte)(read(increment) << (8 - increment));
        }
        return arr;
    }
    public void skip(int bits) throws AccessException {
        if ((mPos + bits) > mEnd) {
            throw new AccessException("illegal skip " +
                "(pos " + mPos + ", end " + mEnd + ", bits " + bits + ")");
        }
        mPos += bits;
    }
}
