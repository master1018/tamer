public class Path {
    public Path() {
        mNativePath = init1();
    }
    public Path(Path src) {
        int valNative = 0;
        if (src != null) {
            valNative = src.mNativePath;
        }
        mNativePath = init2(valNative);
    }
    public void reset() {
        native_reset(mNativePath);
    }
    public void rewind() {
        native_rewind(mNativePath);
    }
    public void set(Path src) {
        if (this != src) {
            native_set(mNativePath, src.mNativePath);
        }
    }
    public enum FillType {
        WINDING         (0),
        EVEN_ODD        (1),
        INVERSE_WINDING (2),
        INVERSE_EVEN_ODD(3);
        FillType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }
    private static final FillType[] sFillTypeArray = {
        FillType.WINDING,
        FillType.EVEN_ODD,
        FillType.INVERSE_WINDING,
        FillType.INVERSE_EVEN_ODD
    };
    public FillType getFillType() {
        return sFillTypeArray[native_getFillType(mNativePath)];
    }
    public void setFillType(FillType ft) {
        native_setFillType(mNativePath, ft.nativeInt);
    }
    public boolean isInverseFillType() {
        final int ft = native_getFillType(mNativePath);
        return (ft & 2) != 0;
    }
    public void toggleInverseFillType() {
        int ft = native_getFillType(mNativePath);
        ft ^= 2;
        native_setFillType(mNativePath, ft);
    }
    public boolean isEmpty() {
        return native_isEmpty(mNativePath);
    }
    public boolean isRect(RectF rect) {
        return native_isRect(mNativePath, rect);
    }
    public void computeBounds(RectF bounds, boolean exact) {
        native_computeBounds(mNativePath, bounds);
    }
    public void incReserve(int extraPtCount) {
        native_incReserve(mNativePath, extraPtCount);
    }
    public void moveTo(float x, float y) {
        native_moveTo(mNativePath, x, y);
    }
    public void rMoveTo(float dx, float dy) {
        native_rMoveTo(mNativePath, dx, dy);
    }
    public void lineTo(float x, float y) {
        native_lineTo(mNativePath, x, y);
    }
    public void rLineTo(float dx, float dy) {
        native_rLineTo(mNativePath, dx, dy);
    }
    public void quadTo(float x1, float y1, float x2, float y2) {
        native_quadTo(mNativePath, x1, y1, x2, y2);
    }
    public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
        native_rQuadTo(mNativePath, dx1, dy1, dx2, dy2);
    }
    public void cubicTo(float x1, float y1, float x2, float y2,
                        float x3, float y3) {
        native_cubicTo(mNativePath, x1, y1, x2, y2, x3, y3);
    }
    public void rCubicTo(float x1, float y1, float x2, float y2,
                         float x3, float y3) {
        native_rCubicTo(mNativePath, x1, y1, x2, y2, x3, y3);
    }
    public void arcTo(RectF oval, float startAngle, float sweepAngle,
                      boolean forceMoveTo) {
        native_arcTo(mNativePath, oval, startAngle, sweepAngle, forceMoveTo);
    }
    public void arcTo(RectF oval, float startAngle, float sweepAngle) {
        native_arcTo(mNativePath, oval, startAngle, sweepAngle, false);
    }
    public void close() {
        native_close(mNativePath);
    }
    public enum Direction {
        CW  (0),    
        CCW (1);    
        Direction(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }
    public void addRect(RectF rect, Direction dir) {
        if (rect == null) {
            throw new NullPointerException("need rect parameter");
        }
        native_addRect(mNativePath, rect, dir.nativeInt);
    }
    public void addRect(float left, float top, float right, float bottom,
                        Direction dir) {
        native_addRect(mNativePath, left, top, right, bottom, dir.nativeInt);
    }
    public void addOval(RectF oval, Direction dir) {
        if (oval == null) {
            throw new NullPointerException("need oval parameter");
        }
        native_addOval(mNativePath, oval, dir.nativeInt);
    }
    public void addCircle(float x, float y, float radius, Direction dir) {
        native_addCircle(mNativePath, x, y, radius, dir.nativeInt);
    }
    public void addArc(RectF oval, float startAngle, float sweepAngle) {
        if (oval == null) {
            throw new NullPointerException("need oval parameter");
        }
        native_addArc(mNativePath, oval, startAngle, sweepAngle);
    }
    public void addRoundRect(RectF rect, float rx, float ry, Direction dir) {
        if (rect == null) {
            throw new NullPointerException("need rect parameter");
        }
        native_addRoundRect(mNativePath, rect, rx, ry, dir.nativeInt);
    }
    public void addRoundRect(RectF rect, float[] radii, Direction dir) {
        if (rect == null) {
            throw new NullPointerException("need rect parameter");
        }
        if (radii.length < 8) {
            throw new ArrayIndexOutOfBoundsException("radii[] needs 8 values");
        }
        native_addRoundRect(mNativePath, rect, radii, dir.nativeInt);
    }
    public void addPath(Path src, float dx, float dy) {
        native_addPath(mNativePath, src.mNativePath, dx, dy);
    }
    public void addPath(Path src) {
        native_addPath(mNativePath, src.mNativePath);
    }
    public void addPath(Path src, Matrix matrix) {
        native_addPath(mNativePath, src.mNativePath, matrix.native_instance);
    }
    public void offset(float dx, float dy, Path dst) {
        int dstNative = 0;
        if (dst != null) {
            dstNative = dst.mNativePath;
        }
        native_offset(mNativePath, dx, dy, dstNative);
    }
    public void offset(float dx, float dy) {
        native_offset(mNativePath, dx, dy);
    }
    public void setLastPoint(float dx, float dy) {
        native_setLastPoint(mNativePath, dx, dy);
    }
    public void transform(Matrix matrix, Path dst) {
        int dstNative = 0;
        if (dst != null) {
            dstNative = dst.mNativePath;
        }
        native_transform(mNativePath, matrix.native_instance, dstNative);
    }
    public void transform(Matrix matrix) {
        native_transform(mNativePath, matrix.native_instance);
    }
    protected void finalize() throws Throwable {
        try {
            finalizer(mNativePath);
        } finally {
            super.finalize();
        }
    }
     final int ni() {
        return mNativePath;
    }
    private static native int init1();
    private static native int init2(int nPath);
    private static native void native_reset(int nPath);
    private static native void native_rewind(int nPath);
    private static native void native_set(int native_dst, int native_src);
    private static native int native_getFillType(int nPath);
    private static native void native_setFillType(int nPath, int ft);
    private static native boolean native_isEmpty(int nPath);
    private static native boolean native_isRect(int nPath, RectF rect);
    private static native void native_computeBounds(int nPath, RectF bounds);
    private static native void native_incReserve(int nPath, int extraPtCount);
    private static native void native_moveTo(int nPath, float x, float y);
    private static native void native_rMoveTo(int nPath, float dx, float dy);
    private static native void native_lineTo(int nPath, float x, float y);
    private static native void native_rLineTo(int nPath, float dx, float dy);
    private static native void native_quadTo(int nPath, float x1, float y1,
                                             float x2, float y2);
    private static native void native_rQuadTo(int nPath, float dx1, float dy1,
                                              float dx2, float dy2);
    private static native void native_cubicTo(int nPath, float x1, float y1,
                                        float x2, float y2, float x3, float y3);
    private static native void native_rCubicTo(int nPath, float x1, float y1,
                                        float x2, float y2, float x3, float y3);
    private static native void native_arcTo(int nPath, RectF oval,
                    float startAngle, float sweepAngle, boolean forceMoveTo);
    private static native void native_close(int nPath);
    private static native void native_addRect(int nPath, RectF rect, int dir);
    private static native void native_addRect(int nPath, float left, float top,
                                            float right, float bottom, int dir);
    private static native void native_addOval(int nPath, RectF oval, int dir);
    private static native void native_addCircle(int nPath, float x, float y,
                                                float radius, int dir);
    private static native void native_addArc(int nPath, RectF oval,
                                            float startAngle, float sweepAngle);
    private static native void native_addRoundRect(int nPath, RectF rect,
                                                   float rx, float ry, int dir);
    private static native void native_addRoundRect(int nPath, RectF r,
                                                   float[] radii, int dir);
    private static native void native_addPath(int nPath, int src, float dx,
                                              float dy);
    private static native void native_addPath(int nPath, int src);
    private static native void native_addPath(int nPath, int src, int matrix);
    private static native void native_offset(int nPath, float dx, float dy,
                                             int dst_path);
    private static native void native_offset(int nPath, float dx, float dy);
    private static native void native_setLastPoint(int nPath, float dx, float dy);
    private static native void native_transform(int nPath, int matrix,
                                                int dst_path);
    private static native void native_transform(int nPath, int matrix);
    private static native void finalizer(int nPath);
    private final int mNativePath;
}
