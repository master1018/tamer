public class ModifyCorePoolSize {
    static void awaitPoolSize(ThreadPoolExecutor pool, int n) {
        while (pool.getPoolSize() != n) Thread.yield();
        pass();
    }
    static void setCorePoolSize(ThreadPoolExecutor pool, int n) {
        pool.setCorePoolSize(n);
        equal(pool.getCorePoolSize(), n);
        awaitPoolSize(pool, n);
    }
    static void realMain(String[] args) throws Throwable {
        final int size = 10;
        final ScheduledThreadPoolExecutor pool
            = new ScheduledThreadPoolExecutor(size);
        final Runnable nop = new Runnable() { public void run() {}};
        for (int i = 0; i < size; i++)
            pool.scheduleAtFixedRate(nop, 100L * (i + 1),
                                     1000L, TimeUnit.MILLISECONDS);
        awaitPoolSize(pool, size);
        setCorePoolSize(pool, size - 3);
        setCorePoolSize(pool, size + 3);
        pool.shutdownNow();
        check(pool.awaitTermination(1L, TimeUnit.DAYS));
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
