public class FunctionContext {
    private long handle = 0;
    public native void set_result(String r);
    public native void set_result(int r);
    public native void set_result(double r);
    public native void set_error(String r);
    public native void set_result(byte[] r);
    public native void set_result_zeroblob(int n);
    public native int count();
    private static native void internal_init();
    static {
    internal_init();
    }
}
