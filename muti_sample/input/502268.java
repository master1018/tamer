public class NativeEventThread extends Thread {
    public interface Init {
        WTK init();
    }
    NativeEventQueue nativeQueue;
    Init init;
    private WTK wtk;
    public NativeEventThread() {
        super("AWT-NativeEventThread"); 
        setDaemon(true);
    }
    @Override
    public void run() {
        synchronized (this) {
            try {
                wtk = init.init();
                nativeQueue = wtk.getNativeEventQueue();
            } finally {
                notifyAll();
            }
        }
        runModalLoop();
    }
    void runModalLoop() {
        while (nativeQueue.waitEvent()) {
            nativeQueue.dispatchEvent();
        }
    }
    public void start(Init init) {
        synchronized (this) {
            this.init = init;
            super.start();
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public WTK getWTK() {
        return wtk;
    }
}
