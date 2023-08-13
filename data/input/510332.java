public class NinePatch {
    public NinePatch(Bitmap bitmap, byte[] chunk, String srcName) {
        mBitmap = bitmap;
        mChunk = chunk;
        mSrcName = srcName;
        validateNinePatchChunk(mBitmap.ni(), chunk);
    }
    public NinePatch(NinePatch patch) {
        mBitmap = patch.mBitmap;
        mChunk = patch.mChunk;
        mSrcName = patch.mSrcName;
        if (patch.mPaint != null) {
            mPaint = new Paint(patch.mPaint);
        }
        validateNinePatchChunk(mBitmap.ni(), mChunk);
    }
    public void setPaint(Paint p) {
        mPaint = p;
    }
    public void draw(Canvas canvas, RectF location) {
        nativeDraw(canvas.mNativeCanvas, location,
                   mBitmap.ni(), mChunk,
                   mPaint != null ? mPaint.mNativePaint : 0,
                   canvas.mDensity, mBitmap.mDensity);
    }
    public void draw(Canvas canvas, Rect location) {
        nativeDraw(canvas.mNativeCanvas, location,
                mBitmap.ni(), mChunk,
                mPaint != null ? mPaint.mNativePaint : 0,
                canvas.mDensity, mBitmap.mDensity);
    }
    public void draw(Canvas canvas, Rect location, Paint paint) {
        nativeDraw(canvas.mNativeCanvas, location,
                mBitmap.ni(), mChunk, paint != null ? paint.mNativePaint : 0,
                canvas.mDensity, mBitmap.mDensity);
    }
    public int getDensity() {
        return mBitmap.mDensity;
    }
    public int getWidth() {
        return mBitmap.getWidth();
    }
    public int getHeight() {
        return mBitmap.getHeight();
    }
    public final boolean hasAlpha() {
        return mBitmap.hasAlpha();
    }
    public final Region getTransparentRegion(Rect location) {
        int r = nativeGetTransparentRegion(mBitmap.ni(), mChunk, location);
        return r != 0 ? new Region(r) : null;
    }
    public native static boolean isNinePatchChunk(byte[] chunk);
    private final Bitmap mBitmap;
    private final byte[] mChunk;
    private Paint        mPaint;
    private String       mSrcName;  
    private static native void validateNinePatchChunk(int bitmap, byte[] chunk);
    private static native void nativeDraw(int canvas_instance, RectF loc, int bitmap_instance,
                                          byte[] c, int paint_instance_or_null,
                                          int destDensity, int srcDensity);
    private static native void nativeDraw(int canvas_instance, Rect loc, int bitmap_instance,
                                          byte[] c, int paint_instance_or_null,
                                          int destDensity, int srcDensity);
    private static native int nativeGetTransparentRegion(
            int bitmap, byte[] chunk, Rect location);
}
