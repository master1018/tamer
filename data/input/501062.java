public final class Bitmap implements Parcelable {
    public static final int DENSITY_NONE = 0;
    private final int mNativeBitmap;
    private final boolean mIsMutable;
    private byte[] mNinePatchChunk;   
    private int mWidth = -1;
    private int mHeight = -1;
    private boolean mRecycled;
     int mDensity = sDefaultDensity = getDefaultDensity();
    private static volatile Matrix sScaleMatrix;
    private static volatile int sDefaultDensity = -1;
    public static void setDefaultDensity(int density) {
        sDefaultDensity = density;
    }
     static int getDefaultDensity() {
        if (sDefaultDensity >= 0) {
            return sDefaultDensity;
        }
        sDefaultDensity = DisplayMetrics.DENSITY_DEVICE;
        return sDefaultDensity;
    }
    private Bitmap(int nativeBitmap, boolean isMutable, byte[] ninePatchChunk,
            int density) {
        if (nativeBitmap == 0) {
            throw new RuntimeException("internal error: native bitmap is 0");
        }
        mNativeBitmap = nativeBitmap;
        mIsMutable = isMutable;
        mNinePatchChunk = ninePatchChunk;
        if (density >= 0) {
            mDensity = density;
        }
    }
    public int getDensity() {
        return mDensity;
    }
    public void setDensity(int density) {
        mDensity = density;
    }
    public void setNinePatchChunk(byte[] chunk) {
        mNinePatchChunk = chunk;
    }
    public void recycle() {
        if (!mRecycled) {
            nativeRecycle(mNativeBitmap);
            mNinePatchChunk = null;
            mRecycled = true;
        }
    }
    public final boolean isRecycled() {
        return mRecycled;
    }
    private void checkRecycled(String errorMessage) {
        if (mRecycled) {
            throw new IllegalStateException(errorMessage);
        }
    }
    private static void checkXYSign(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
    }
    private static void checkWidthHeight(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
    }
    public enum Config {
        ALPHA_8     (2),
        RGB_565     (4),
        ARGB_4444   (5),
        ARGB_8888   (6);
        Config(int ni) {
            this.nativeInt = ni;
        }
        final int nativeInt;
         static Config nativeToConfig(int ni) {
            return sConfigs[ni];
        }
        private static Config sConfigs[] = {
            null, null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888
        };
    }
    public void copyPixelsToBuffer(Buffer dst) {
        int elements = dst.remaining();
        int shift;
        if (dst instanceof ByteBuffer) {
            shift = 0;
        } else if (dst instanceof ShortBuffer) {
            shift = 1;
        } else if (dst instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long bufferSize = (long)elements << shift;
        long pixelSize = (long)getRowBytes() * getHeight();
        if (bufferSize < pixelSize) {
            throw new RuntimeException("Buffer not large enough for pixels");
        }
        nativeCopyPixelsToBuffer(mNativeBitmap, dst);
        int position = dst.position();
        position += pixelSize >> shift;
        dst.position(position);
    }
    public void copyPixelsFromBuffer(Buffer src) {
        checkRecycled("copyPixelsFromBuffer called on recycled bitmap");
        int elements = src.remaining();
        int shift;
        if (src instanceof ByteBuffer) {
            shift = 0;
        } else if (src instanceof ShortBuffer) {
            shift = 1;
        } else if (src instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long bufferBytes = (long)elements << shift;
        long bitmapBytes = (long)getRowBytes() * getHeight();
        if (bufferBytes < bitmapBytes) {
            throw new RuntimeException("Buffer not large enough for pixels");
        }
        nativeCopyPixelsFromBuffer(mNativeBitmap, src);
    }
    public Bitmap copy(Config config, boolean isMutable) {
        checkRecycled("Can't copy a recycled bitmap");
        Bitmap b = nativeCopy(mNativeBitmap, config.nativeInt, isMutable);
        if (b != null) {
            b.mDensity = mDensity;
        }
        return b;
    }
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth,
            int dstHeight, boolean filter) {
        Matrix m;
        synchronized (Bitmap.class) {
            m = sScaleMatrix;
            sScaleMatrix = null;
        }
        if (m == null) {
            m = new Matrix();
        }
        final int width = src.getWidth();
        final int height = src.getHeight();
        final float sx = dstWidth  / (float)width;
        final float sy = dstHeight / (float)height;
        m.setScale(sx, sy);
        Bitmap b = Bitmap.createBitmap(src, 0, 0, width, height, m, filter);
        synchronized (Bitmap.class) {
            if (sScaleMatrix == null) {
                sScaleMatrix = m;
            }
        }
        return b;
    }
    public static Bitmap createBitmap(Bitmap src) {
        return createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
    }
    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        return createBitmap(source, x, y, width, height, null, false);
    }
    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height,
            Matrix m, boolean filter) {
        checkXYSign(x, y);
        checkWidthHeight(width, height);
        if (x + width > source.getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        }
        if (y + height > source.getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        }
        if (!source.isMutable() && x == 0 && y == 0 && width == source.getWidth() &&
                height == source.getHeight() && (m == null || m.isIdentity())) {
            return source;
        }
        int neww = width;
        int newh = height;
        Canvas canvas = new Canvas();
        Bitmap bitmap;
        Paint paint;
        Rect srcR = new Rect(x, y, x + width, y + height);
        RectF dstR = new RectF(0, 0, width, height);
        if (m == null || m.isIdentity()) {
            bitmap = createBitmap(neww, newh,
                    source.hasAlpha() ? Config.ARGB_8888 : Config.RGB_565);
            paint = null;   
        } else {
            boolean hasAlpha = source.hasAlpha() || !m.rectStaysRect();
            RectF deviceR = new RectF();
            m.mapRect(deviceR, dstR);
            neww = Math.round(deviceR.width());
            newh = Math.round(deviceR.height());
            bitmap = createBitmap(neww, newh, hasAlpha ? Config.ARGB_8888 : Config.RGB_565);
            if (hasAlpha) {
                bitmap.eraseColor(0);
            }
            canvas.translate(-deviceR.left, -deviceR.top);
            canvas.concat(m);
            paint = new Paint();
            paint.setFilterBitmap(filter);
            if (!m.rectStaysRect()) {
                paint.setAntiAlias(true);
            }
        }
        bitmap.mDensity = source.mDensity;
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(source, srcR, dstR, paint);
        return bitmap;
    }
    public static Bitmap createBitmap(int width, int height, Config config) {
        Bitmap bm = nativeCreate(null, 0, width, width, height, config.nativeInt, true);
        bm.eraseColor(0);    
        return bm;
    }
    public static Bitmap createBitmap(int colors[], int offset, int stride,
            int width, int height, Config config) {
        checkWidthHeight(width, height);
        if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
        int lastScanline = offset + (height - 1) * stride;
        int length = colors.length;
        if (offset < 0 || (offset + width > length) || lastScanline < 0 ||
                (lastScanline + width > length)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return nativeCreate(colors, offset, stride, width, height,
                            config.nativeInt, false);
    }
    public static Bitmap createBitmap(int colors[], int width, int height, Config config) {
        return createBitmap(colors, 0, width, width, height, config);
    }
    public byte[] getNinePatchChunk() {
        return mNinePatchChunk;
    }
    public enum CompressFormat {
        JPEG    (0),
        PNG     (1);
        CompressFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    private final static int WORKING_COMPRESS_STORAGE = 4096;
    public boolean compress(CompressFormat format, int quality, OutputStream stream) {
        checkRecycled("Can't compress a recycled bitmap");
        if (stream == null) {
            throw new NullPointerException();
        }
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        }
        return nativeCompress(mNativeBitmap, format.nativeInt, quality,
                              stream, new byte[WORKING_COMPRESS_STORAGE]);
    }
    public final boolean isMutable() {
        return mIsMutable;
    }
    public final int getWidth() {
        return mWidth == -1 ? mWidth = nativeWidth(mNativeBitmap) : mWidth;
    }
    public final int getHeight() {
        return mHeight == -1 ? mHeight = nativeHeight(mNativeBitmap) : mHeight;
    }
    public int getScaledWidth(Canvas canvas) {
        return scaleFromDensity(getWidth(), mDensity, canvas.mDensity);
    }
    public int getScaledHeight(Canvas canvas) {
        return scaleFromDensity(getHeight(), mDensity, canvas.mDensity);
    }
    public int getScaledWidth(DisplayMetrics metrics) {
        return scaleFromDensity(getWidth(), mDensity, metrics.densityDpi);
    }
    public int getScaledHeight(DisplayMetrics metrics) {
        return scaleFromDensity(getHeight(), mDensity, metrics.densityDpi);
    }
    public int getScaledWidth(int targetDensity) {
        return scaleFromDensity(getWidth(), mDensity, targetDensity);
    }
    public int getScaledHeight(int targetDensity) {
        return scaleFromDensity(getHeight(), mDensity, targetDensity);
    }
    static public int scaleFromDensity(int size, int sdensity, int tdensity) {
        if (sdensity == DENSITY_NONE || sdensity == tdensity) {
            return size;
        }
        return ( (size * tdensity) + (sdensity >> 1) ) / sdensity;
    }
    public final int getRowBytes() {
        return nativeRowBytes(mNativeBitmap);
    }
    public final Config getConfig() {
        return Config.nativeToConfig(nativeConfig(mNativeBitmap));
    }
    public final boolean hasAlpha() {
        return nativeHasAlpha(mNativeBitmap);
    }
    public void setHasAlpha(boolean hasAlpha) {
        nativeSetHasAlpha(mNativeBitmap, hasAlpha);
    }
    public void eraseColor(int c) {
        checkRecycled("Can't erase a recycled bitmap");
        if (!isMutable()) {
            throw new IllegalStateException("cannot erase immutable bitmaps");
        }
        nativeErase(mNativeBitmap, c);
    }
    public int getPixel(int x, int y) {
        checkRecycled("Can't call getPixel() on a recycled bitmap");
        checkPixelAccess(x, y);
        return nativeGetPixel(mNativeBitmap, x, y);
    }
    public void getPixels(int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        checkRecycled("Can't call getPixels() on a recycled bitmap");
        if (width == 0 || height == 0) {
            return; 
        }
        checkPixelsAccess(x, y, width, height, offset, stride, pixels);
        nativeGetPixels(mNativeBitmap, pixels, offset, stride,
                        x, y, width, height);
    }
    private void checkPixelAccess(int x, int y) {
        checkXYSign(x, y);
        if (x >= getWidth()) {
            throw new IllegalArgumentException("x must be < bitmap.width()");
        }
        if (y >= getHeight()) {
            throw new IllegalArgumentException("y must be < bitmap.height()");
        }
    }
    private void checkPixelsAccess(int x, int y, int width, int height,
                                   int offset, int stride, int pixels[]) {
        checkXYSign(x, y);
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        if (x + width > getWidth()) {
            throw new IllegalArgumentException(
                    "x + width must be <= bitmap.width()");
        }
        if (y + height > getHeight()) {
            throw new IllegalArgumentException(
                    "y + height must be <= bitmap.height()");
        }
        if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
        int lastScanline = offset + (height - 1) * stride;
        int length = pixels.length;
        if (offset < 0 || (offset + width > length)
                || lastScanline < 0
                || (lastScanline + width > length)) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    public void setPixel(int x, int y, int color) {
        checkRecycled("Can't call setPixel() on a recycled bitmap");
        if (!isMutable()) {
            throw new IllegalStateException();
        }
        checkPixelAccess(x, y);
        nativeSetPixel(mNativeBitmap, x, y, color);
    }
    public void setPixels(int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        checkRecycled("Can't call setPixels() on a recycled bitmap");
        if (!isMutable()) {
            throw new IllegalStateException();
        }
        if (width == 0 || height == 0) {
            return; 
        }
        checkPixelsAccess(x, y, width, height, offset, stride, pixels);
        nativeSetPixels(mNativeBitmap, pixels, offset, stride,
                        x, y, width, height);
    }
    public static final Parcelable.Creator<Bitmap> CREATOR
            = new Parcelable.Creator<Bitmap>() {
        public Bitmap createFromParcel(Parcel p) {
            Bitmap bm = nativeCreateFromParcel(p);
            if (bm == null) {
                throw new RuntimeException("Failed to unparcel Bitmap");
            }
            return bm;
        }
        public Bitmap[] newArray(int size) {
            return new Bitmap[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel p, int flags) {
        checkRecycled("Can't parcel a recycled bitmap");
        if (!nativeWriteToParcel(mNativeBitmap, mIsMutable, mDensity, p)) {
            throw new RuntimeException("native writeToParcel failed");
        }
    }
    public Bitmap extractAlpha() {
        return extractAlpha(null, null);
    }
    public Bitmap extractAlpha(Paint paint, int[] offsetXY) {
        checkRecycled("Can't extractAlpha on a recycled bitmap");
        int nativePaint = paint != null ? paint.mNativePaint : 0;
        Bitmap bm = nativeExtractAlpha(mNativeBitmap, nativePaint, offsetXY);
        if (bm == null) {
            throw new RuntimeException("Failed to extractAlpha on Bitmap");
        }
        bm.mDensity = mDensity;
        return bm;
    }
    public boolean sameAs(Bitmap other) {
        return this == other ||
              (other != null &&
               nativeSameAs(mNativeBitmap, other.mNativeBitmap));
    }
    public void prepareToDraw() {
        nativePrepareToDraw(mNativeBitmap);
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            nativeDestructor(mNativeBitmap);
        } finally {
            super.finalize();
        }
    }
    private static native Bitmap nativeCreate(int[] colors, int offset,
                                              int stride, int width, int height,
                                            int nativeConfig, boolean mutable);
    private static native Bitmap nativeCopy(int srcBitmap, int nativeConfig,
                                            boolean isMutable);
    private static native void nativeDestructor(int nativeBitmap);
    private static native void nativeRecycle(int nativeBitmap);
    private static native boolean nativeCompress(int nativeBitmap, int format,
                                            int quality, OutputStream stream,
                                            byte[] tempStorage);
    private static native void nativeErase(int nativeBitmap, int color);
    private static native int nativeWidth(int nativeBitmap);
    private static native int nativeHeight(int nativeBitmap);
    private static native int nativeRowBytes(int nativeBitmap);
    private static native int nativeConfig(int nativeBitmap);
    private static native boolean nativeHasAlpha(int nativeBitmap);
    private static native int nativeGetPixel(int nativeBitmap, int x, int y);
    private static native void nativeGetPixels(int nativeBitmap, int[] pixels,
                                               int offset, int stride, int x,
                                               int y, int width, int height);
    private static native void nativeSetPixel(int nativeBitmap, int x, int y,
                                              int color);
    private static native void nativeSetPixels(int nativeBitmap, int[] colors,
                                               int offset, int stride, int x,
                                               int y, int width, int height);
    private static native void nativeCopyPixelsToBuffer(int nativeBitmap,
                                                        Buffer dst);
    private static native void nativeCopyPixelsFromBuffer(int nb, Buffer src);
    private static native Bitmap nativeCreateFromParcel(Parcel p);
    private static native boolean nativeWriteToParcel(int nativeBitmap,
                                                      boolean isMutable,
                                                      int density,
                                                      Parcel p);
    private static native Bitmap nativeExtractAlpha(int nativeBitmap,
                                                    int nativePaint,
                                                    int[] offsetXY);
    private static native void nativePrepareToDraw(int nativeBitmap);
    private static native void nativeSetHasAlpha(int nBitmap, boolean hasAlpha);
    private static native boolean nativeSameAs(int nb0, int nb1);
     final int ni() {
        return mNativeBitmap;
    }
}
