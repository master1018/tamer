class SolarisNativeDispatcher extends UnixNativeDispatcher {
    private SolarisNativeDispatcher() { }
    static native int facl(int fd, int cmd, int nentries, long aclbufp)
        throws UnixException;
    private static native void init();
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                System.loadLibrary("nio");
                return null;
        }});
        init();
    }
}
