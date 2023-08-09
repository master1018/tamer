final class XToolkitThreadBlockedHandler implements
                                 ToolkitThreadBlockedHandler {
    private static final ToolkitThreadBlockedHandler priveleged_lock;
    static {
        priveleged_lock = new XToolkitThreadBlockedHandler();
    }
    private static final XToolkit tk = (XToolkit)java.awt.Toolkit.getDefaultToolkit();
    private XToolkitThreadBlockedHandler() {}
    static ToolkitThreadBlockedHandler getToolkitThreadBlockedHandler() {
        return priveleged_lock;
    }
    public void lock() {
        XToolkit.awtLock();
    }
    public void unlock() {
        XToolkit.awtUnlock();
    }
    public void enter() {
        tk.run(XToolkit.SECONDARY_LOOP);
    }
    public void exit() {
        XlibWrapper.ExitSecondaryLoop();
    }
}
