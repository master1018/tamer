public class MaskFilter {
    protected void finalize() throws Throwable {
        nativeDestructor(native_instance);
    }
    private static native void nativeDestructor(int native_filter);
    int native_instance;
}
