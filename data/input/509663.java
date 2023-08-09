public class Vm {
    private long handle = 0;
    protected int error_code = 0;
    public native boolean step(Callback cb) throws SQLite.Exception;
    public native boolean compile() throws SQLite.Exception;
    public native void stop() throws SQLite.Exception;
    protected native void finalize();
    private static native void internal_init();
    static {
    internal_init();
    }
}
