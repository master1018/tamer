public class RefQueueWorker implements Runnable {
    private final Log log = LogFactory.getLog(getClass());
    protected final ReferenceQueue<?> refQueue;
    protected final RefQueueHandler refHandler;
    protected volatile Thread workerThread;
    public RefQueueWorker(ReferenceQueue<?> queue, RefQueueHandler handler) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue must not be null.");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null.");
        }
        refQueue   = queue;
        refHandler = handler;
    }
    public void run() {
        if (this.workerThread == null) {
            this.workerThread = Thread.currentThread();
        }
        while (this.workerThread == Thread.currentThread()) {
            try {
                Reference<?> ref = refQueue.remove();
                refHandler.handleReference(ref);
            } catch (InterruptedException e) {
                if (log.isDebugEnabled()) {
                    log.debug(this.toString() + " interrupted", e);
                }
            }
        }
    }
    public void shutdown() {
        Thread wt = this.workerThread;
        if (wt != null) {
            this.workerThread = null; 
            wt.interrupt();
        }
    }
    @Override
    public String toString() {
        return "RefQueueWorker::" + this.workerThread;
    }
} 
