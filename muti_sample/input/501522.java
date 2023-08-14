public class BitwiseOutputStream {
    private byte[] mBuf;
    private int mPos;
    private int mEnd;
    public static class AccessException extends Exception {
        public AccessException(String s) {
            super("BitwiseOutputStream access failed: " + s);
        }
    }
    public BitwiseOutputStream(int startingLength) {
        mBuf = new byte[startingLength];
        mEnd = startingLength << 3;
        mPos = 0;
    }
    public byte[] toByteArray() {
        int len = (mPos >>> 3) + ((mPos & 0x07) > 0 ? 1 : 0);  
        byte[] newBuf = new byte[len];
        System.arraycopy(mBuf, 0, newBuf, 0, len);
        return newBuf;
    }
    private void possExpand(int bits) {
        if ((mPos + bits) < mEnd) return;
        byte[] newBuf = new byte[(mPos + bits) >>> 2];
        System.arraycopy(mBuf, 0, newBuf, 0, mEnd >>> 3);
        mBuf = newBuf;
    }
    public void write(int bits, int data) throws AccessException {
        if ((bits < 0) || (bits > 8)) {
            throw new AccessException("illegal write (" + bits + " bits)");
        }
        possExpand(bits);
        data &= (-1 >>> (32 - bits));
        int index = mPos >>> 3;
        int offset = 16 - (mPos & 0x07) - bits;  
        data <<= offset;
        mPos += bits;
        mBuf[index] |= data >>> 8;
        if (offset < 8) mBuf[index + 1] |= data & 0xFF;
    }
    public void writeByteArray(int bits, byte[] arr) throws AccessException {
        for (int i = 0; i < arr.length; i++) {
            int increment = Math.min(8, bits - (i << 3));
            if (increment > 0) {
                write(increment, (byte)(arr[i] >>> (8 - increment)));
            }
        }
    }
    public void skip(int bits) {
        possExpand(bits);
        mPos += bits;
    }
}
