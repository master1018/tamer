abstract class WObjectPeer {
    static {
        initIDs();
    }
    long pData;
    boolean destroyed = false;
    Object target;
    private volatile boolean disposed;
    protected Error createError = null;
    private final Object stateLock = new Object();
    public static WObjectPeer getPeerForTarget(Object t) {
        WObjectPeer peer = (WObjectPeer) WToolkit.targetToPeer(t);
        return peer;
    }
    public long getData() {
        return pData;
    }
    public Object getTarget() {
        return target;
    }
    public final Object getStateLock() {
        return stateLock;
    }
    abstract protected void disposeImpl();
    public final void dispose() {
        boolean call_disposeImpl = false;
        synchronized (this) {
            if (!disposed) {
                disposed = call_disposeImpl = true;
            }
        }
        if (call_disposeImpl) {
            disposeImpl();
        }
    }
    protected final boolean isDisposed() {
        return disposed;
    }
    private static native void initIDs();
}
