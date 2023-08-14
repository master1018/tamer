public class SurfaceSession {
    public SurfaceSession() {
        init();
    }
    public native void kill();
    @Override
    protected void finalize() throws Throwable {
        destroy();
    }
    private native void init();
    private native void destroy();
    private int mClient;
}
