public class PathMeasure {
    public PathMeasure() {
        native_instance = native_create(0, false);
    }
    public PathMeasure(Path path, boolean forceClosed) {
        native_instance = native_create(path != null ? path.ni() : 0,
                                        forceClosed);
    }
    public void setPath(Path path, boolean forceClosed) {
        native_setPath(native_instance,
                       path != null ? path.ni() : 0,
                       forceClosed);
    }
    public float getLength() {
        return native_getLength(native_instance);
    }
    public boolean getPosTan(float distance, float pos[], float tan[]) {
        if (pos != null && pos.length < 2 ||
            tan != null && tan.length < 2) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return native_getPosTan(native_instance, distance, pos, tan);
    }
    public static final int POSITION_MATRIX_FLAG = 0x01;    
    public static final int TANGENT_MATRIX_FLAG  = 0x02;    
    public boolean getMatrix(float distance, Matrix matrix, int flags) {
        return native_getMatrix(native_instance, distance, matrix.native_instance, flags);
    }
    public boolean getSegment(float startD, float stopD, Path dst, boolean startWithMoveTo) {
        return native_getSegment(native_instance, startD, stopD, dst.ni(), startWithMoveTo);
    }
    public boolean isClosed() {
        return native_isClosed(native_instance);
    }
    public boolean nextContour() {
        return native_nextContour(native_instance);
    }
    protected void finalize() throws Throwable {
        native_destroy(native_instance);
    }
    private static native int native_create(int native_path, boolean forceClosed);
    private static native void native_setPath(int native_instance, int native_path, boolean forceClosed);
    private static native float native_getLength(int native_instance);
    private static native boolean native_getPosTan(int native_instance, float distance, float pos[], float tan[]);
    private static native boolean native_getMatrix(int native_instance, float distance, int native_matrix, int flags);
    private static native boolean native_getSegment(int native_instance, float startD, float stopD, int native_path, boolean startWithMoveTo);
    private static native boolean native_isClosed(int native_instance);
    private static native boolean native_nextContour(int native_instance);
    private static native void native_destroy(int native_instance);
    private final int native_instance;
}
