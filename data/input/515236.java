public class FieldPacker {
    public FieldPacker(int len) {
        mPos = 0;
        mData = new byte[len];
    }
    public void align(int v) {
        while ((mPos & (v - 1)) != 0) {
            mData[mPos++] = 0;
        }
    }
    void reset() {
        mPos = 0;
    }
    void addI8(byte v) {
        mData[mPos++] = v;
    }
    void addI16(short v) {
        align(2);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)(v >> 8);
    }
    void addI32(int v) {
        align(4);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)((v >> 8) & 0xff);
        mData[mPos++] = (byte)((v >> 16) & 0xff);
        mData[mPos++] = (byte)((v >> 24) & 0xff);
    }
    void addI64(long v) {
        align(8);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)((v >> 8) & 0xff);
        mData[mPos++] = (byte)((v >> 16) & 0xff);
        mData[mPos++] = (byte)((v >> 24) & 0xff);
        mData[mPos++] = (byte)((v >> 32) & 0xff);
        mData[mPos++] = (byte)((v >> 40) & 0xff);
        mData[mPos++] = (byte)((v >> 48) & 0xff);
        mData[mPos++] = (byte)((v >> 56) & 0xff);
    }
    void addU8(short v) {
        if ((v < 0) || (v > 0xff)) {
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        mData[mPos++] = (byte)v;
    }
    void addU16(int v) {
        if ((v < 0) || (v > 0xffff)) {
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(2);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)(v >> 8);
    }
    void addU32(long v) {
        if ((v < 0) || (v > 0xffffffff)) {
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(4);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)((v >> 8) & 0xff);
        mData[mPos++] = (byte)((v >> 16) & 0xff);
        mData[mPos++] = (byte)((v >> 24) & 0xff);
    }
    void addU64(long v) {
        if (v < 0) {
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(8);
        mData[mPos++] = (byte)(v & 0xff);
        mData[mPos++] = (byte)((v >> 8) & 0xff);
        mData[mPos++] = (byte)((v >> 16) & 0xff);
        mData[mPos++] = (byte)((v >> 24) & 0xff);
        mData[mPos++] = (byte)((v >> 32) & 0xff);
        mData[mPos++] = (byte)((v >> 40) & 0xff);
        mData[mPos++] = (byte)((v >> 48) & 0xff);
        mData[mPos++] = (byte)((v >> 56) & 0xff);
    }
    void addF32(float v) {
        addI32(Float.floatToRawIntBits(v));
    }
    void addF64(float v) {
        addI64(Double.doubleToRawLongBits(v));
    }
    final byte[] getData() {
        return mData;
    }
    private final byte mData[];
    private int mPos;
}
