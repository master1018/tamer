public class ETC1 {
    public static final int ENCODED_BLOCK_SIZE = 8;
    public static final int DECODED_BLOCK_SIZE = 48;
    public static final int ETC_PKM_HEADER_SIZE = 16;
    public static final int ETC1_RGB8_OES = 0x8D64;
    public static native void encodeBlock(Buffer in, int validPixelMask, Buffer out);
    public static native void decodeBlock(Buffer in, Buffer out);
    public static native int getEncodedDataSize(int width, int height);
    public static native void encodeImage(Buffer in, int width, int height,
            int pixelSize, int stride, Buffer out);
    public static native void decodeImage(Buffer in, Buffer out,
            int width, int height, int pixelSize, int stride);
    public static native void formatHeader(Buffer header, int width, int height);
    public static native boolean isValid(Buffer header);
    public static native int getWidth(Buffer header);
    public static native int getHeight(Buffer header);
}
