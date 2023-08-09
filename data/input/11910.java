class NativeThread {
    static native long current();
    static native void signal(long nt);
    static native void init();
    static {
        Util.load();
        init();
    }
}
