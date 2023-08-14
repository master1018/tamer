public class D3DRenderQueue extends RenderQueue {
    private static D3DRenderQueue theInstance;
    private static Thread rqThread;
    private D3DRenderQueue() {
    }
    public static synchronized D3DRenderQueue getInstance() {
        if (theInstance == null) {
            theInstance = new D3DRenderQueue();
            theInstance.flushAndInvokeNow(new Runnable() {
                public void run() {
                    rqThread = Thread.currentThread();
                }
            });
        }
        return theInstance;
    }
    public static void sync() {
        if (theInstance != null) {
            D3DScreenUpdateManager mgr =
                (D3DScreenUpdateManager)ScreenUpdateManager.getInstance();
            mgr.runUpdateNow();
            theInstance.lock();
            try {
                theInstance.ensureCapacity(4);
                theInstance.getBuffer().putInt(SYNC);
                theInstance.flushNow();
            } finally {
                theInstance.unlock();
            }
        }
    }
    public static void restoreDevices() {
        D3DRenderQueue rq = getInstance();
        rq.lock();
        try {
            rq.ensureCapacity(4);
            rq.getBuffer().putInt(RESTORE_DEVICES);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    public static boolean isRenderQueueThread() {
        return (Thread.currentThread() == rqThread);
    }
    public static void disposeGraphicsConfig(long pConfigInfo) {
        D3DRenderQueue rq = getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(DISPOSE_CONFIG);
            buf.putLong(pConfigInfo);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    public void flushNow() {
        flushBuffer(null);
    }
    public void flushAndInvokeNow(Runnable r) {
        flushBuffer(r);
    }
    private native void flushBuffer(long buf, int limit, Runnable task);
    private void flushBuffer(Runnable task) {
        int limit = buf.position();
        if (limit > 0 || task != null) {
            flushBuffer(buf.getAddress(), limit, task);
        }
        buf.clear();
        refSet.clear();
    }
}
