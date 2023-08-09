public class Camera {
    public Camera() {
        nativeConstructor();
    }
    public native void save();
    public native void restore();
    public native void translate(float x, float y, float z);
    public native void rotateX(float deg);
    public native void rotateY(float deg);
    public native void rotateZ(float deg);
    public void getMatrix(Matrix matrix) {
        nativeGetMatrix(matrix.native_instance);
    }
    public void applyToCanvas(Canvas canvas) {
        nativeApplyToCanvas(canvas.mNativeCanvas);
    }
    public native float dotWithNormal(float dx, float dy, float dz);
    protected void finalize() throws Throwable {
        nativeDestructor();
    }
    private native void nativeConstructor();
    private native void nativeDestructor();
    private native void nativeGetMatrix(int native_matrix);
    private native void nativeApplyToCanvas(int native_canvas);
    int native_instance;
}
