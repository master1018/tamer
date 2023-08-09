class EventDispatchThread extends Thread  {
    private static final class MarkerEvent extends AWTEvent {
        MarkerEvent(Object source, int id) {
            super(source, id);
        }
    }
    final Dispatcher dispatcher;
    final Toolkit toolkit;
    private NativeEventQueue nativeQueue;
    protected volatile boolean shutdownPending = false;
    @Override
    public void run() {
        nativeQueue = toolkit.getNativeEventQueue();
        try {
            runModalLoop(null);
        } finally {
            toolkit.shutdownWatchdog.forceShutdown();
        }
    }
    void runModalLoop(ModalContext context) {
        long lastPaintTime = System.currentTimeMillis();
        while (!shutdownPending && (context == null || context.isModalLoopRunning())) {
            try {
            EventQueue eventQueue = toolkit.getSystemEventQueueImpl();
            NativeEvent ne = nativeQueue.getNextEvent();
            if (ne != null) {
                dispatcher.onEvent(ne);
                MarkerEvent marker = new MarkerEvent(this, 0);
                eventQueue.postEvent(marker);
                for (AWTEvent ae = eventQueue.getNextEventNoWait(); 
                        (ae != null) && (ae != marker); 
                        ae = eventQueue.getNextEventNoWait()) {
                    eventQueue.dispatchEvent(ae);
                }
            } else {
                toolkit.shutdownWatchdog.setNativeQueueEmpty(true);
                AWTEvent ae = eventQueue.getNextEventNoWait();
                if (ae != null) {
                    eventQueue.dispatchEvent(ae);
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastPaintTime > 10) {
                        toolkit.onQueueEmpty();
                        lastPaintTime = System.currentTimeMillis();
                    }
                } else {
                    toolkit.shutdownWatchdog.setAwtQueueEmpty(true);
                    toolkit.onQueueEmpty();
                    lastPaintTime = System.currentTimeMillis();
                    waitForAnyEvent();
                }
            }
            } catch (Throwable t) {
            }
        }
    }
    private void waitForAnyEvent() {
        EventQueue eventQueue = toolkit.getSystemEventQueueImpl();
        if (!eventQueue.isEmpty() || !nativeQueue.isEmpty()) {
            return;
        }
        Object eventMonitor = nativeQueue.getEventMonitor();
        synchronized(eventMonitor) {
            try {
                eventMonitor.wait();
            } catch (InterruptedException e) {}
        }
    }
    void shutdown() {
        shutdownPending = true;
    }
    EventDispatchThread(Toolkit toolkit, Dispatcher dispatcher ) {
        this.toolkit = toolkit;
        this.dispatcher = dispatcher;
        setName("AWT-EventDispatchThread"); 
        setDaemon(true);
    }
}
