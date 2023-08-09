public class Shader {
     int native_instance;
    public enum TileMode {
        CLAMP   (0),
        REPEAT  (1),
        MIRROR  (2);
        TileMode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public boolean getLocalMatrix(Matrix localM) {
        return nativeGetLocalMatrix(native_instance, localM.native_instance);
    }
    public void setLocalMatrix(Matrix localM) {
        nativeSetLocalMatrix(native_instance,
                             localM != null ? localM.native_instance : 0);
    }
    protected void finalize() throws Throwable {
        nativeDestructor(native_instance);
    }
    private static native void nativeDestructor(int native_shader);
    private static native boolean nativeGetLocalMatrix(int native_shader,
                                                       int matrix_instance);
    private static native void nativeSetLocalMatrix(int native_shader,
                                                    int matrix_instance);
}
