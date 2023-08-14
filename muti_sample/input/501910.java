public class Canvas {
    final int mNativeCanvas;
    private Bitmap  mBitmap;    
    private GL      mGL;        
    private DrawFilter  mDrawFilter;
     int mDensity = Bitmap.DENSITY_NONE;
    private int mScreenDensity = Bitmap.DENSITY_NONE;
    @SuppressWarnings({"UnusedDeclaration"})
    private int         mSurfaceFormat;
    public Canvas() {
        mNativeCanvas = initRaster(0);
    }
    public Canvas(Bitmap bitmap) {
        if (!bitmap.isMutable()) {
            throw new IllegalStateException(
                            "Immutable bitmap passed to Canvas constructor");
        }
        throwIfRecycled(bitmap);
        mNativeCanvas = initRaster(bitmap.ni());
        mBitmap = bitmap;
        mDensity = bitmap.mDensity;
    }
     Canvas(int nativeCanvas) {
        if (nativeCanvas == 0) {
            throw new IllegalStateException();
        }
        mNativeCanvas = nativeCanvas;
        mDensity = Bitmap.getDefaultDensity();
    }
    public Canvas(GL gl) {
        mNativeCanvas = initGL();
        mGL = gl;
        mDensity = Bitmap.getDefaultDensity();
    }
    public GL getGL() {
        return mGL;
    }
    public static void freeGlCaches() {
        freeCaches();
    }
    public void setBitmap(Bitmap bitmap) {
        if (!bitmap.isMutable()) {
            throw new IllegalStateException();
        }
        if (mGL != null) {
            throw new RuntimeException("Can't set a bitmap device on a GL canvas");
        }
        throwIfRecycled(bitmap);
        native_setBitmap(mNativeCanvas, bitmap.ni());
        mBitmap = bitmap;
        mDensity = bitmap.mDensity;
    }
    public void setViewport(int width, int height) {
        if (mGL != null) {
            nativeSetViewport(mNativeCanvas, width, height);
        }
    }
    public native boolean isOpaque();
    public native int getWidth();
    public native int getHeight();
    public int getDensity() {
        return mDensity;
    }
    public void setDensity(int density) {
        if (mBitmap != null) {
            mBitmap.setDensity(density);
        }
        mDensity = density;
    }
    public void setScreenDensity(int density) {
        mScreenDensity = density;
    }
    public static final int MATRIX_SAVE_FLAG = 0x01;
    public static final int CLIP_SAVE_FLAG = 0x02;
    public static final int HAS_ALPHA_LAYER_SAVE_FLAG = 0x04;
    public static final int FULL_COLOR_LAYER_SAVE_FLAG = 0x08;
    public static final int CLIP_TO_LAYER_SAVE_FLAG = 0x10;
    public static final int ALL_SAVE_FLAG = 0x1F; 
    public native int save();
    public native int save(int saveFlags);
    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        return native_saveLayer(mNativeCanvas, bounds,
                                paint != null ? paint.mNativePaint : 0,
                                saveFlags);
    }
    public int saveLayer(float left, float top, float right, float bottom,
                         Paint paint, int saveFlags) {
        return native_saveLayer(mNativeCanvas, left, top, right, bottom,
                                paint != null ? paint.mNativePaint : 0,
                                saveFlags);
    }
    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        alpha = Math.min(255, Math.max(0, alpha));
        return native_saveLayerAlpha(mNativeCanvas, bounds, alpha, saveFlags);
    }
    public int saveLayerAlpha(float left, float top, float right, float bottom,
                              int alpha, int saveFlags) {
        return native_saveLayerAlpha(mNativeCanvas, left, top, right, bottom,
                                     alpha, saveFlags);
    }
    public native void restore();
    public native int getSaveCount();
    public native void restoreToCount(int saveCount);
    public native void translate(float dx, float dy);
    public native void scale(float sx, float sy);
    public final void scale(float sx, float sy, float px, float py) {
        translate(px, py);
        scale(sx, sy);
        translate(-px, -py);
    }
    public native void rotate(float degrees);
    public final void rotate(float degrees, float px, float py) {
        translate(px, py);
        rotate(degrees);
        translate(-px, -py);
    }
    public native void skew(float sx, float sy);
    public void concat(Matrix matrix) {
        native_concat(mNativeCanvas, matrix.native_instance);
    }
    public void setMatrix(Matrix matrix) {
        native_setMatrix(mNativeCanvas,
                         matrix == null ? 0 : matrix.native_instance);
    }
    public void getMatrix(Matrix ctm) {
        native_getCTM(mNativeCanvas, ctm.native_instance);
    }
    public final Matrix getMatrix() {
        Matrix m = new Matrix();
        getMatrix(m);
        return m;
    }
    public boolean clipRect(RectF rect, Region.Op op) {
        return native_clipRect(mNativeCanvas,
                               rect.left, rect.top, rect.right, rect.bottom,
                               op.nativeInt);
    }
    public boolean clipRect(Rect rect, Region.Op op) {
        return native_clipRect(mNativeCanvas,
                               rect.left, rect.top, rect.right, rect.bottom,
                               op.nativeInt);
    }
    public native boolean clipRect(RectF rect);
    public native boolean clipRect(Rect rect);
    public boolean clipRect(float left, float top, float right, float bottom,
                            Region.Op op) {
        return native_clipRect(mNativeCanvas, left, top, right, bottom,
                               op.nativeInt);
    }
    public native boolean clipRect(float left, float top,
                                   float right, float bottom);
    public native boolean clipRect(int left, int top,
                                   int right, int bottom);
    public boolean clipPath(Path path, Region.Op op) {
        return native_clipPath(mNativeCanvas, path.ni(), op.nativeInt);
    }
    public boolean clipPath(Path path) {
        return clipPath(path, Region.Op.INTERSECT);
    }
    public boolean clipRegion(Region region, Region.Op op) {
        return native_clipRegion(mNativeCanvas, region.ni(), op.nativeInt);
    }
    public boolean clipRegion(Region region) {
        return clipRegion(region, Region.Op.INTERSECT);
    }
    public DrawFilter getDrawFilter() {
        return mDrawFilter;
    }
    public void setDrawFilter(DrawFilter filter) {
        int nativeFilter = 0;
        if (filter != null) {
            nativeFilter = filter.mNativeInt;
        }
        mDrawFilter = filter;
        nativeSetDrawFilter(mNativeCanvas, nativeFilter);
    }
    public enum EdgeType {
        BW(0),  
        AA(1);  
        EdgeType(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public boolean quickReject(RectF rect, EdgeType type) {
        return native_quickReject(mNativeCanvas, rect, type.nativeInt);
    }
    public boolean quickReject(Path path, EdgeType type) {
        return native_quickReject(mNativeCanvas, path.ni(), type.nativeInt);
    }
    public boolean quickReject(float left, float top, float right, float bottom,
                               EdgeType type) {
        return native_quickReject(mNativeCanvas, left, top, right, bottom,
                                  type.nativeInt);
    }
    public boolean getClipBounds(Rect bounds) {
        return native_getClipBounds(mNativeCanvas, bounds);
    }
    public final Rect getClipBounds() {
        Rect r = new Rect();
        getClipBounds(r);
        return r;
    }
    public void drawRGB(int r, int g, int b) {
        native_drawRGB(mNativeCanvas, r, g, b);
    }
    public void drawARGB(int a, int r, int g, int b) {
        native_drawARGB(mNativeCanvas, a, r, g, b);
    }
    public void drawColor(int color) {
        native_drawColor(mNativeCanvas, color);
    }
    public void drawColor(int color, PorterDuff.Mode mode) {
        native_drawColor(mNativeCanvas, color, mode.nativeInt);
    }
    public void drawPaint(Paint paint) {
        native_drawPaint(mNativeCanvas, paint.mNativePaint);
    }
    public native void drawPoints(float[] pts, int offset, int count,
                                  Paint paint);
    public void drawPoints(float[] pts, Paint paint) {
        drawPoints(pts, 0, pts.length, paint);
    }
    public native void drawPoint(float x, float y, Paint paint);
    public void drawLine(float startX, float startY, float stopX, float stopY,
                         Paint paint) {
        native_drawLine(mNativeCanvas, startX, startY, stopX, stopY,
                        paint.mNativePaint);
    }
    public native void drawLines(float[] pts, int offset, int count,
                                 Paint paint);
    public void drawLines(float[] pts, Paint paint) {
        drawLines(pts, 0, pts.length, paint);
    }
    public void drawRect(RectF rect, Paint paint) {
        native_drawRect(mNativeCanvas, rect, paint.mNativePaint);
    }
    public void drawRect(Rect r, Paint paint) {
        drawRect(r.left, r.top, r.right, r.bottom, paint);
    }
    public void drawRect(float left, float top, float right, float bottom,
                         Paint paint) {
        native_drawRect(mNativeCanvas, left, top, right, bottom,
                        paint.mNativePaint);
    }
    public void drawOval(RectF oval, Paint paint) {
        if (oval == null) {
            throw new NullPointerException();
        }
        native_drawOval(mNativeCanvas, oval, paint.mNativePaint);
    }
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        native_drawCircle(mNativeCanvas, cx, cy, radius,
                          paint.mNativePaint);
    }
    public void drawArc(RectF oval, float startAngle, float sweepAngle,
                        boolean useCenter, Paint paint) {
        if (oval == null) {
            throw new NullPointerException();
        }
        native_drawArc(mNativeCanvas, oval, startAngle, sweepAngle,
                       useCenter, paint.mNativePaint);
    }
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        if (rect == null) {
            throw new NullPointerException();
        }
        native_drawRoundRect(mNativeCanvas, rect, rx, ry,
                             paint.mNativePaint);
    }
    public void drawPath(Path path, Paint paint) {
        native_drawPath(mNativeCanvas, path.ni(), paint.mNativePaint);
    }
    private static void throwIfRecycled(Bitmap bitmap) {
        if (bitmap.isRecycled()) {
            throw new RuntimeException(
                        "Canvas: trying to use a recycled bitmap " + bitmap);
        }
    }
    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        throwIfRecycled(bitmap);
        native_drawBitmap(mNativeCanvas, bitmap.ni(), left, top,
                paint != null ? paint.mNativePaint : 0, mDensity, mScreenDensity,
                bitmap.mDensity);
    }
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        if (dst == null) {
            throw new NullPointerException();
        }
        throwIfRecycled(bitmap);
        native_drawBitmap(mNativeCanvas, bitmap.ni(), src, dst,
                          paint != null ? paint.mNativePaint : 0,
                          mScreenDensity, bitmap.mDensity);
    }
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        if (dst == null) {
            throw new NullPointerException();
        }
        throwIfRecycled(bitmap);
        native_drawBitmap(mNativeCanvas, bitmap.ni(), src, dst,
                          paint != null ? paint.mNativePaint : 0,
                          mScreenDensity, bitmap.mDensity);
    }
    public void drawBitmap(int[] colors, int offset, int stride, float x,
                           float y, int width, int height, boolean hasAlpha,
                           Paint paint) {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
        int lastScanline = offset + (height - 1) * stride;
        int length = colors.length;
        if (offset < 0 || (offset + width > length) || lastScanline < 0
                || (lastScanline + width > length)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (width == 0 || height == 0) {
            return;
        }
        native_drawBitmap(mNativeCanvas, colors, offset, stride, x, y, width, height, hasAlpha,
                paint != null ? paint.mNativePaint : 0);
    }
    public void drawBitmap(int[] colors, int offset, int stride, int x, int y,
                           int width, int height, boolean hasAlpha,
                           Paint paint) {
        drawBitmap(colors, offset, stride, (float)x, (float)y, width, height,
                   hasAlpha, paint);
    }
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        nativeDrawBitmapMatrix(mNativeCanvas, bitmap.ni(), matrix.ni(),
                paint != null ? paint.mNativePaint : 0);
    }
    private static void checkRange(int length, int offset, int count) {
        if ((offset | count) < 0 || offset + count > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight,
                               float[] verts, int vertOffset,
                               int[] colors, int colorOffset, Paint paint) {
        if ((meshWidth | meshHeight | vertOffset | colorOffset) < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (meshWidth == 0 || meshHeight == 0) {
            return;
        }
        int count = (meshWidth + 1) * (meshHeight + 1);
        checkRange(verts.length, vertOffset, count * 2);
        if (colors != null) {
            checkRange(colors.length, colorOffset, count);
        }
        nativeDrawBitmapMesh(mNativeCanvas, bitmap.ni(), meshWidth, meshHeight,
                             verts, vertOffset, colors, colorOffset,
                             paint != null ? paint.mNativePaint : 0);
    }
    public enum VertexMode {
        TRIANGLES(0),
        TRIANGLE_STRIP(1),
        TRIANGLE_FAN(2);
        VertexMode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public void drawVertices(VertexMode mode, int vertexCount,
                             float[] verts, int vertOffset,
                             float[] texs, int texOffset,
                             int[] colors, int colorOffset,
                             short[] indices, int indexOffset,
                             int indexCount, Paint paint) {
        checkRange(verts.length, vertOffset, vertexCount);
        if (texs != null) {
            checkRange(texs.length, texOffset, vertexCount);
        }
        if (colors != null) {
            checkRange(colors.length, colorOffset, vertexCount);
        }
        if (indices != null) {
            checkRange(indices.length, indexOffset, indexCount);
        }
        nativeDrawVertices(mNativeCanvas, mode.nativeInt, vertexCount, verts,
                           vertOffset, texs, texOffset, colors, colorOffset,
                          indices, indexOffset, indexCount, paint.mNativePaint);
    }
    public void drawText(char[] text, int index, int count, float x, float y,
                         Paint paint) {
        if ((index | count | (index + count) |
            (text.length - index - count)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        native_drawText(mNativeCanvas, text, index, count, x, y,
                        paint.mNativePaint);
    }
    public native void drawText(String text, float x, float y, Paint paint);
    public void drawText(String text, int start, int end, float x, float y,
                         Paint paint) {
        if ((start | end | (end - start) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        native_drawText(mNativeCanvas, text, start, end, x, y,
                        paint.mNativePaint);
    }
    public void drawText(CharSequence text, int start, int end, float x,
                         float y, Paint paint) {
        if (text instanceof String || text instanceof SpannedString ||
            text instanceof SpannableString) {
            native_drawText(mNativeCanvas, text.toString(), start, end, x, y,
                            paint.mNativePaint);
        }
        else if (text instanceof GraphicsOperations) {
            ((GraphicsOperations) text).drawText(this, start, end, x, y,
                                                     paint);
        }
        else {
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            drawText(buf, 0, end - start, x, y, paint);
            TemporaryBuffer.recycle(buf);
        }
    }
    public void drawPosText(char[] text, int index, int count, float[] pos,
                            Paint paint) {
        if (index < 0 || index + count > text.length || count*2 > pos.length) {
            throw new IndexOutOfBoundsException();
        }
        native_drawPosText(mNativeCanvas, text, index, count, pos,
                           paint.mNativePaint);
    }
    public void drawPosText(String text, float[] pos, Paint paint) {
        if (text.length()*2 > pos.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_drawPosText(mNativeCanvas, text, pos, paint.mNativePaint);
    }
    public void drawTextOnPath(char[] text, int index, int count, Path path,
                               float hOffset, float vOffset, Paint paint) {
        if (index < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_drawTextOnPath(mNativeCanvas, text, index, count,
                              path.ni(), hOffset, vOffset,
                              paint.mNativePaint);
    }
    public void drawTextOnPath(String text, Path path, float hOffset,
                               float vOffset, Paint paint) {
        if (text.length() > 0) {
            native_drawTextOnPath(mNativeCanvas, text, path.ni(),
                                  hOffset, vOffset, paint.mNativePaint);
        }
    }
    public void drawPicture(Picture picture) {
        picture.endRecording();
        native_drawPicture(mNativeCanvas, picture.ni());
    }
    public void drawPicture(Picture picture, RectF dst) {
        save();
        translate(dst.left, dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(dst.width() / picture.getWidth(),
                  dst.height() / picture.getHeight());
        }
        drawPicture(picture);
        restore();
    }
    public void drawPicture(Picture picture, Rect dst) {
        save();
        translate(dst.left, dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale((float)dst.width() / picture.getWidth(),
                  (float)dst.height() / picture.getHeight());
        }
        drawPicture(picture);
        restore();
    }
    protected void finalize() throws Throwable {
        super.finalize();
        if (mNativeCanvas != 0) {
            finalizer(mNativeCanvas);
        }
    }
    public static native void freeCaches();
    private static native int initRaster(int nativeBitmapOrZero);
    private static native int initGL();
    private static native void native_setBitmap(int nativeCanvas, int bitmap);
    private static native void nativeSetViewport(int nCanvas, int w, int h);
    private static native int native_saveLayer(int nativeCanvas, RectF bounds,
                                               int paint, int layerFlags);
    private static native int native_saveLayer(int nativeCanvas, float l,
                                               float t, float r, float b,
                                               int paint, int layerFlags);
    private static native int native_saveLayerAlpha(int nativeCanvas,
                                                    RectF bounds, int alpha,
                                                    int layerFlags);
    private static native int native_saveLayerAlpha(int nativeCanvas, float l,
                                                    float t, float r, float b,
                                                    int alpha, int layerFlags);
    private static native void native_concat(int nCanvas, int nMatrix);
    private static native void native_setMatrix(int nCanvas, int nMatrix);
    private static native boolean native_clipRect(int nCanvas,
                                                  float left, float top,
                                                  float right, float bottom,
                                                  int regionOp);
    private static native boolean native_clipPath(int nativeCanvas,
                                                  int nativePath,
                                                  int regionOp);
    private static native boolean native_clipRegion(int nativeCanvas,
                                                    int nativeRegion,
                                                    int regionOp);
    private static native void nativeSetDrawFilter(int nativeCanvas,
                                                   int nativeFilter);
    private static native boolean native_getClipBounds(int nativeCanvas,
                                                       Rect bounds);
    private static native void native_getCTM(int canvas, int matrix);
    private static native boolean native_quickReject(int nativeCanvas,
                                                     RectF rect,
                                                     int native_edgeType);
    private static native boolean native_quickReject(int nativeCanvas,
                                                     int path,
                                                     int native_edgeType);
    private static native boolean native_quickReject(int nativeCanvas,
                                                     float left, float top,
                                                     float right, float bottom,
                                                     int native_edgeType);
    private static native void native_drawRGB(int nativeCanvas, int r, int g,
                                              int b);
    private static native void native_drawARGB(int nativeCanvas, int a, int r,
                                               int g, int b);
    private static native void native_drawColor(int nativeCanvas, int color);
    private static native void native_drawColor(int nativeCanvas, int color,
                                                int mode);
    private static native void native_drawPaint(int nativeCanvas, int paint);
    private static native void native_drawLine(int nativeCanvas, float startX,
                                               float startY, float stopX,
                                               float stopY, int paint);
    private static native void native_drawRect(int nativeCanvas, RectF rect,
                                               int paint);
    private static native void native_drawRect(int nativeCanvas, float left,
                                               float top, float right,
                                               float bottom, int paint);
    private static native void native_drawOval(int nativeCanvas, RectF oval,
                                               int paint);
    private static native void native_drawCircle(int nativeCanvas, float cx,
                                                 float cy, float radius,
                                                 int paint);
    private static native void native_drawArc(int nativeCanvas, RectF oval,
                                              float startAngle, float sweep,
                                              boolean useCenter, int paint);
    private static native void native_drawRoundRect(int nativeCanvas,
                                                    RectF rect, float rx,
                                                    float ry, int paint);
    private static native void native_drawPath(int nativeCanvas, int path,
                                               int paint);
    private native void native_drawBitmap(int nativeCanvas, int bitmap,
                                                 float left, float top,
                                                 int nativePaintOrZero,
                                                 int canvasDensity,
                                                 int screenDensity,
                                                 int bitmapDensity);
    private native void native_drawBitmap(int nativeCanvas, int bitmap,
                                                 Rect src, RectF dst,
                                                 int nativePaintOrZero,
                                                 int screenDensity,
                                                 int bitmapDensity);
    private static native void native_drawBitmap(int nativeCanvas, int bitmap,
                                                 Rect src, Rect dst,
                                                 int nativePaintOrZero,
                                                 int screenDensity,
                                                 int bitmapDensity);
    private static native void native_drawBitmap(int nativeCanvas, int[] colors,
                                                int offset, int stride, float x,
                                                 float y, int width, int height,
                                                 boolean hasAlpha,
                                                 int nativePaintOrZero);
    private static native void nativeDrawBitmapMatrix(int nCanvas, int nBitmap,
                                                      int nMatrix, int nPaint);
    private static native void nativeDrawBitmapMesh(int nCanvas, int nBitmap,
                                                    int meshWidth, int meshHeight,
                                                    float[] verts, int vertOffset,
                                                    int[] colors, int colorOffset, int nPaint);
    private static native void nativeDrawVertices(int nCanvas, int mode, int n,
                   float[] verts, int vertOffset, float[] texs, int texOffset,
                   int[] colors, int colorOffset, short[] indices,
                   int indexOffset, int indexCount, int nPaint);
    private static native void native_drawText(int nativeCanvas, char[] text,
                                               int index, int count, float x,
                                               float y, int paint);
    private static native void native_drawText(int nativeCanvas, String text,
                                               int start, int end, float x,
                                               float y, int paint);
    private static native void native_drawPosText(int nativeCanvas,
                                                  char[] text, int index,
                                                  int count, float[] pos,
                                                  int paint);
    private static native void native_drawPosText(int nativeCanvas,
                                                  String text, float[] pos,
                                                  int paint);
    private static native void native_drawTextOnPath(int nativeCanvas,
                                                     char[] text, int index,
                                                     int count, int path,
                                                     float hOffset,
                                                     float vOffset, int paint);
    private static native void native_drawTextOnPath(int nativeCanvas,
                                                     String text, int path,
                                                     float hOffset,
                                                     float vOffset, int paint);
    private static native void native_drawPicture(int nativeCanvas,
                                                  int nativePicture);
    private static native void finalizer(int nativeCanvas);
}
