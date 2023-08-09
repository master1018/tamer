public class SelfInterrupt {
    void test(String[] args) throws Throwable {
        final int n = 100;
        final ThreadPoolExecutor pool =
            new ThreadPoolExecutor(n, n, 1L, TimeUnit.NANOSECONDS,
                                   new SynchronousQueue<Runnable>());
        final CountDownLatch startingGate = new CountDownLatch(n);
        final CountDownLatch finishLine = new CountDownLatch(n);
        equal(pool.getCorePoolSize(), n);
        equal(pool.getPoolSize(), 0);
        for (int i = 0; i < n; i++)
            pool.execute(new Runnable() { public void run() {
                try {
                    startingGate.countDown();
                    startingGate.await();
                    equal(pool.getPoolSize(), n);
                    pool.setCorePoolSize(n);
                    pool.setCorePoolSize(1);
                    check(! Thread.interrupted());
                    equal(pool.getPoolSize(), n);
                    finishLine.countDown();
                    finishLine.await();
                    check(! Thread.interrupted());
                } catch (Throwable t) { unexpected(t); }}});
        finishLine.await();
        pool.shutdown();
        check(pool.awaitTermination(1000L, TimeUnit.SECONDS));
    }
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        new SelfInterrupt().instanceMain(args);}
    void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
