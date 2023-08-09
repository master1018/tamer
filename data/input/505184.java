public class WaitingThread {
    private final Condition cond;
    private final RouteSpecificPool pool;
    private Thread waiter;
    private boolean aborted;
    public WaitingThread(Condition cond, RouteSpecificPool pool) {
        if (cond == null) {
            throw new IllegalArgumentException("Condition must not be null.");
        }
        this.cond = cond;
        this.pool = pool;
    }
    public final Condition getCondition() {
        return this.cond;
    }
    public final RouteSpecificPool getPool() {
        return this.pool;
    }
    public final Thread getThread() {
        return this.waiter;
    }
    public boolean await(Date deadline)
        throws InterruptedException {
        if (this.waiter != null) {
            throw new IllegalStateException
                ("A thread is already waiting on this object." +
                 "\ncaller: " + Thread.currentThread() +
                 "\nwaiter: " + this.waiter);
        }
        if (aborted)
            throw new InterruptedException("Operation interrupted");
        this.waiter = Thread.currentThread();
        boolean success = false;
        try {
            if (deadline != null) {
                success = this.cond.awaitUntil(deadline);
            } else {
                this.cond.await();
                success = true;
            }
            if (aborted)
                throw new InterruptedException("Operation interrupted");
        } finally {
            this.waiter = null;
        }
        return success;
    } 
    public void wakeup() {
        if (this.waiter == null) {
            throw new IllegalStateException
                ("Nobody waiting on this object.");
        }
        this.cond.signalAll();
    }
    public void interrupt() {
        aborted = true;
        this.cond.signalAll();
    }
} 
