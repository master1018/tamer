public class YuvImage {
    private final static int WORKING_COMPRESS_STORAGE = 4096;
    private int mFormat;
    private byte[] mData;
    private int[] mStrides;
    private int mWidth;
    private int mHeight;
    public YuvImage(byte[] yuv, int format, int width, int height, int[] strides) {
        if (format != ImageFormat.NV21 &&
                format != ImageFormat.YUY2) {
            throw new IllegalArgumentException(
                    "only support ImageFormat.NV21 " +
                    "and ImageFormat.YUY2 for now");
        }
        if (width <= 0  || height <= 0) {
            throw new IllegalArgumentException(
                    "width and height must large than 0");
        }
        if (yuv == null) {
            throw new IllegalArgumentException("yuv cannot be null");
        }
        if (strides == null) {
            mStrides = calculateStrides(width, format);
        } else {
            mStrides = strides;
        }
        mData = yuv;
        mFormat = format;
        mWidth = width;
        mHeight = height;
    }
    public boolean compressToJpeg(Rect rectangle, int quality, OutputStream stream) {
        Rect wholeImage = new Rect(0, 0, mWidth, mHeight);
        if (!wholeImage.contains(rectangle)) {
            throw new IllegalArgumentException(
                    "rectangle is not inside the image");
        }
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        }
        if (stream == null) {
            throw new IllegalArgumentException("stream cannot be null");
        }
        adjustRectangle(rectangle);
        int[] offsets = calculateOffsets(rectangle.left, rectangle.top);
        return nativeCompressToJpeg(mData, mFormat, rectangle.width(),
                rectangle.height(), offsets, mStrides, quality, stream,
                new byte[WORKING_COMPRESS_STORAGE]);
    }
    public byte[] getYuvData() {
        return mData;
    }
    public int getYuvFormat() {
        return mFormat;
    }
    public int[] getStrides() {
        return mStrides;
    }
    public int getWidth() {
        return mWidth;
    }
    public int getHeight() {
        return mHeight;
    }
    int[] calculateOffsets(int left, int top) {
        int[] offsets = null;
        if (mFormat == ImageFormat.NV21) {
            offsets = new int[] {top * mStrides[0] + left,
                  mHeight * mStrides[0] + top / 2 * mStrides[1]
                  + left / 2 * 2 };
            return offsets;
        }
        if (mFormat == ImageFormat.YUY2) {
            offsets = new int[] {top * mStrides[0] + left / 2 * 4};
            return offsets;
        }
        return offsets;
    }
    private int[] calculateStrides(int width, int format) {
        int[] strides = null;
        if (format == ImageFormat.NV21) {
            strides = new int[] {width, width};
            return strides;
        }
        if (format == ImageFormat.YUY2) {
            strides = new int[] {width * 2};
            return strides;
        }
        return strides;
    }
   private void adjustRectangle(Rect rect) {
       int width = rect.width();
       int height = rect.height();
       if (mFormat == ImageFormat.NV21) {
           width &= ~1;
           height &= ~1;
           rect.left &= ~1;
           rect.top &= ~1;
           rect.right = rect.left + width;
           rect.bottom = rect.top + height;
        }
        if (mFormat == ImageFormat.YUY2) {
            width &= ~1;
            rect.left &= ~1;
            rect.right = rect.left + width;
        }
    }
    private static native boolean nativeCompressToJpeg(byte[] oriYuv,
            int format, int width, int height, int[] offsets, int[] strides,
            int quality, OutputStream stream, byte[] tempStorage);
}
