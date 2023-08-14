public class JniCppTest extends TestCase {
    static {
        System.loadLibrary("jnitest");
    }
    public void testEverything() {
        String msg = runAllTests();
        if (msg != null) {
            fail(msg);
        }
    }
    private static native String runAllTests();
}
