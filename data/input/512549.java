public class DrawFilter {
     int mNativeInt;    
    protected void finalize() throws Throwable {
        nativeDestructor(mNativeInt);
    }
    private static native void nativeDestructor(int nativeDrawFilter);
}
