public class TimeOutShrink {
    static void checkPoolSizes(ThreadPoolExecutor pool,
                               int size, int core, int max) {
        equal(pool.getPoolSize(), size);
        equal(pool.getCorePoolSize(), core);
        equal(pool.getMaximumPoolSize(), max);
    }
    private static void realMain(String[] args) throws Throwable {
        final int n = 4;
        final CyclicBarrier barrier = new CyclicBarrier(2*n+1);
        final ThreadPoolExecutor pool
            = new ThreadPoolExecutor(n, 2*n, 1L, TimeUnit.SECONDS,
                                     new SynchronousQueue<Runnable>());
        final Runnable r = new Runnable() { public void run() {
            try {
                barrier.await();
                barrier.await();
            } catch (Throwable t) { unexpected(t); }}};
        for (int i = 0; i < 2*n; i++)
            pool.execute(r);
        barrier.await();
        checkPoolSizes(pool, 2*n, n, 2*n);
        barrier.await();
        while (pool.getPoolSize() > n)
            Thread.sleep(100);
        Thread.sleep(100);
        checkPoolSizes(pool, n, n, 2*n);
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }
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
