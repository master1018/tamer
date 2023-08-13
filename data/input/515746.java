public class FutureTask<V> implements Future<V>, Runnable {
    private final Sync sync;
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        sync = new Sync(callable);
    }
    public FutureTask(Runnable runnable, V result) {
        sync = new Sync(Executors.callable(runnable, result));
    }
    public boolean isCancelled() {
        return sync.innerIsCancelled();
    }
    public boolean isDone() {
        return sync.innerIsDone();
    }
    public boolean cancel(boolean mayInterruptIfRunning) {
        return sync.innerCancel(mayInterruptIfRunning);
    }
    public V get() throws InterruptedException, ExecutionException {
        return sync.innerGet();
    }
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return sync.innerGet(unit.toNanos(timeout));
    }
    protected void done() { }
    protected void set(V v) {
        sync.innerSet(v);
    }
    protected void setException(Throwable t) {
        sync.innerSetException(t);
    }
    public void run() {
        sync.innerRun();
    }
    protected boolean runAndReset() {
        return sync.innerRunAndReset();
    }
    private final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -7828117401763700385L;
        private static final int READY     = 0;
        private static final int RUNNING   = 1;
        private static final int RAN       = 2;
        private static final int CANCELLED = 4;
        private final Callable<V> callable;
        private V result;
        private Throwable exception;
        private volatile Thread runner;
        Sync(Callable<V> callable) {
            this.callable = callable;
        }
        private boolean ranOrCancelled(int state) {
            return (state & (RAN | CANCELLED)) != 0;
        }
        protected int tryAcquireShared(int ignore) {
            return innerIsDone() ? 1 : -1;
        }
        protected boolean tryReleaseShared(int ignore) {
            runner = null;
            return true;
        }
        boolean innerIsCancelled() {
            return getState() == CANCELLED;
        }
        boolean innerIsDone() {
            return ranOrCancelled(getState()) && runner == null;
        }
        V innerGet() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (getState() == CANCELLED)
                throw new CancellationException();
            if (exception != null)
                throw new ExecutionException(exception);
            return result;
        }
        V innerGet(long nanosTimeout) throws InterruptedException, ExecutionException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout))
                throw new TimeoutException();
            if (getState() == CANCELLED)
                throw new CancellationException();
            if (exception != null)
                throw new ExecutionException(exception);
            return result;
        }
        void innerSet(V v) {
            for (;;) {
                int s = getState();
                if (s == RAN)
                    return;
                if (s == CANCELLED) {
                    releaseShared(0);
                    return;
                }
                if (compareAndSetState(s, RAN)) {
                    result = v;
                    releaseShared(0);
                    done();
                    return;
                }
            }
        }
        void innerSetException(Throwable t) {
            for (;;) {
                int s = getState();
                if (s == RAN)
                    return;
                if (s == CANCELLED) {
                    releaseShared(0);
                    return;
                }
                if (compareAndSetState(s, RAN)) {
                    exception = t;
                    releaseShared(0);
                    done();
                    return;
                }
            }
        }
        boolean innerCancel(boolean mayInterruptIfRunning) {
            for (;;) {
                int s = getState();
                if (ranOrCancelled(s))
                    return false;
                if (compareAndSetState(s, CANCELLED))
                    break;
            }
            if (mayInterruptIfRunning) {
                Thread r = runner;
                if (r != null)
                    r.interrupt();
            }
            releaseShared(0);
            done();
            return true;
        }
        void innerRun() {
            if (!compareAndSetState(READY, RUNNING))
                return;
            runner = Thread.currentThread();
            if (getState() == RUNNING) { 
                V result;
                try {
                    result = callable.call();
                } catch (Throwable ex) {
                    setException(ex);
                    return;
                }
                set(result);
            } else {
                releaseShared(0); 
            }
        }
        boolean innerRunAndReset() {
            if (!compareAndSetState(READY, RUNNING))
                return false;
            try {
                runner = Thread.currentThread();
                if (getState() == RUNNING)
                    callable.call(); 
                runner = null;
                return compareAndSetState(RUNNING, READY);
            } catch (Throwable ex) {
                setException(ex);
                return false;
            }
        }
    }
}
