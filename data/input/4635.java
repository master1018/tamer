public class OGLRenderQueue extends RenderQueue {
    private static OGLRenderQueue theInstance;
    private final QueueFlusher flusher;
    private OGLRenderQueue() {
        flusher = new QueueFlusher();
    }
    public static synchronized OGLRenderQueue getInstance() {
        if (theInstance == null) {
            theInstance = new OGLRenderQueue();
        }
        return theInstance;
    }
    public static void sync() {
        if (theInstance != null) {
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
    public static void disposeGraphicsConfig(long pConfigInfo) {
        OGLRenderQueue rq = getInstance();
        rq.lock();
        try {
            OGLContext.setScratchSurface(pConfigInfo);
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(DISPOSE_CONFIG);
            buf.putLong(pConfigInfo);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    public static boolean isQueueFlusherThread() {
        return (Thread.currentThread() == getInstance().flusher);
    }
    public void flushNow() {
        try {
            flusher.flushNow();
        } catch (Exception e) {
            System.err.println("exception in flushNow:");
            e.printStackTrace();
        }
    }
    public void flushAndInvokeNow(Runnable r) {
        try {
            flusher.flushAndInvokeNow(r);
        } catch (Exception e) {
            System.err.println("exception in flushAndInvokeNow:");
            e.printStackTrace();
        }
    }
    private native void flushBuffer(long buf, int limit);
    private void flushBuffer() {
        int limit = buf.position();
        if (limit > 0) {
            flushBuffer(buf.getAddress(), limit);
        }
        buf.clear();
        refSet.clear();
    }
    private class QueueFlusher extends Thread {
        private boolean needsFlush;
        private Runnable task;
        private Error error;
        public QueueFlusher() {
            super("Java2D Queue Flusher");
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY);
            start();
        }
        public synchronized void flushNow() {
            needsFlush = true;
            notify();
            while (needsFlush) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (error != null) {
                throw error;
            }
        }
        public synchronized void flushAndInvokeNow(Runnable task) {
            this.task = task;
            flushNow();
        }
        public synchronized void run() {
            boolean timedOut = false;
            while (true) {
                while (!needsFlush) {
                    try {
                        timedOut = false;
                        wait(100);
                        if (!needsFlush && (timedOut = tryLock())) {
                            if (buf.position() > 0) {
                                needsFlush = true;
                            } else {
                                unlock();
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    error = null;
                    flushBuffer();
                    if (task != null) {
                        task.run();
                    }
                } catch (Error e) {
                    error = e;
                } catch (Exception x) {
                    System.err.println("exception in QueueFlusher:");
                    x.printStackTrace();
                } finally {
                    if (timedOut) {
                        unlock();
                    }
                    task = null;
                    needsFlush = false;
                    notify();
                }
            }
        }
    }
}
