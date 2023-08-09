public class JniLibTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            System.loadLibrary("jni_lib_test");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("JniLibTest", "WARNING: Could not load jni_lib_test natives");
        }
    }
    private static native int nativeStaticThing(float f);
    private native void nativeThing(int val);
    public void testNativeCall() {
        Log.i("JniLibTest", "JNI search path is "
                + System.getProperty("java.library.path"));
        Log.i("JniLibTest", "'jni_lib_test' becomes '"
                + System.mapLibraryName("jni_lib_test") + "'");
        int result = nativeStaticThing(1234.5f);
        nativeThing(result);
    }
}
