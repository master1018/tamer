public class PathEffect {
    protected void finalize() throws Throwable {
        nativeDestructor(native_instance);
    }
    private static native void nativeDestructor(int native_patheffect);
    int native_instance;
}
