final class MToolkitThreadBlockedHandler implements
                                 ToolkitThreadBlockedHandler {
    private static ToolkitThreadBlockedHandler priveleged_lock = null;
    static {
        priveleged_lock = new MToolkitThreadBlockedHandler();
    }
    private MToolkitThreadBlockedHandler() {}
    static ToolkitThreadBlockedHandler getToolkitThreadBlockedHandler() {
        return priveleged_lock;
    }
    public void lock() {
        SunToolkit.awtLock();
    }
    public void unlock() {
        SunToolkit.awtUnlock();
    }
    public native void enter();
    public native void exit();
}
