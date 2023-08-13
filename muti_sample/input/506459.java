public final class ShutdownWatchdog {
    private boolean nativeQueueEmpty = true;
    private boolean awtQueueEmpty = true;
    private boolean windowListEmpty = true;
    private boolean forcedShutdown = false;
    private ShutdownThread thread;
    public synchronized void setNativeQueueEmpty(boolean empty) {
        nativeQueueEmpty = empty;
        checkShutdown();
    }
    public synchronized void setAwtQueueEmpty(boolean empty) {
        awtQueueEmpty = empty;
        checkShutdown();
    }
    public synchronized void setWindowListEmpty(boolean empty) {
        windowListEmpty = empty;
        checkShutdown();
    }
    public synchronized void forceShutdown() {
        forcedShutdown = true;
        shutdown();
    }
    public synchronized void start() {
        keepAlive();
    }
    private void checkShutdown() {
        if (canShutdown()) {
            shutdown();
        } else {
            keepAlive();
        }
    }
    private boolean canShutdown() {
        return (nativeQueueEmpty && awtQueueEmpty && windowListEmpty) ||
                forcedShutdown;
    }
    private void keepAlive() {
        if (thread == null) {
            thread = new ShutdownThread();
            thread.start();
        }
    }
    private void shutdown() {
        if (thread != null) {
            thread.shutdown();
            thread = null;
        }
    }
}
