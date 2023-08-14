public class FlakyMutex implements Lock {
    static class MyError extends Error {}
    static class MyException extends Exception {}
    static class MyRuntimeException extends RuntimeException {}
    static final Random rnd = new Random();
    static void maybeThrow() {
        switch (rnd.nextInt(10)) {
        case 0: throw new MyError();
        case 1: throw new MyRuntimeException();
        case 2: Thread.currentThread().stop(new MyException()); break;
        default:  break;
        }
    }
    static void checkThrowable(Throwable t) {
        check((t instanceof MyError) ||
              (t instanceof MyException) ||
              (t instanceof MyRuntimeException));
    }
    static void realMain(String[] args) throws Throwable {
        final int nThreads = 3;
        final CyclicBarrier barrier = new CyclicBarrier(nThreads + 1);
        final FlakyMutex m = new FlakyMutex();
        final ExecutorService es = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            es.submit(new Runnable() { public void run() {
                try {
                    barrier.await();
                    for (int i = 0; i < 10000; i++) {
                        for (;;) {
                            try { m.lock(); break; }
                            catch (Throwable t) { checkThrowable(t); }
                        }
                        try { check(! m.tryLock()); }
                        catch (Throwable t) { checkThrowable(t); }
                        try { check(! m.tryLock(1, TimeUnit.MICROSECONDS)); }
                        catch (Throwable t) { checkThrowable(t); }
                        m.unlock();
                    }
                } catch (Throwable t) { unexpected(t); }}});}
        barrier.await();
        es.shutdown();
        check(es.awaitTermination(10, TimeUnit.SECONDS));
    }
    private static class FlakySync extends AbstractQueuedLongSynchronizer {
        private static final long serialVersionUID = -1L;
        public boolean isHeldExclusively() { return getState() == 1; }
        public boolean tryAcquire(long acquires) {
            if (hasQueuedPredecessors())
                check(getFirstQueuedThread() != Thread.currentThread());
            if (getFirstQueuedThread() == Thread.currentThread()) {
                check(hasQueuedThreads());
                check(!hasQueuedPredecessors());
            } else {
                do {} while (hasQueuedPredecessors() != hasQueuedThreads());
            }
            maybeThrow();
            return compareAndSetState(0, 1);
        }
        public boolean tryRelease(long releases) {
            setState(0);
            return true;
        }
        Condition newCondition() { return new ConditionObject(); }
    }
    private final FlakySync sync = new FlakySync();
    public void lock() { sync.acquire(1); }
    public boolean tryLock() { return sync.tryAcquire(1); }
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }
    public void unlock() { sync.release(1); }
    public Condition newCondition()   { return sync.newCondition(); }
    public boolean isLocked()         { return sync.isHeldExclusively(); }
    public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
