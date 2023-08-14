public class Xfermode {
    protected void finalize() throws Throwable {
        finalizer(native_instance);
    }
    private static native void finalizer(int native_instance);
    int native_instance;
}
