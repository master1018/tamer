public class CoreThreadTimeOut {
    static class IdentifiableThreadFactory implements ThreadFactory {
        static ThreadFactory defaultThreadFactory
            = Executors.defaultThreadFactory();
        public Thread newThread(Runnable r) {
            Thread t = defaultThreadFactory.newThread(r);
            t.setName("CoreThreadTimeOut-" + t.getName());
            return t;
        }
    }
    int countExecutorThreads() {
        Thread[] threads = new Thread[Thread.activeCount()+100];
        Thread.enumerate(threads);
        int count = 0;
        for (Thread t : threads)
            if (t != null &&
                t.getName().matches
                ("CoreThreadTimeOut-pool-[0-9]+-thread-[0-9]+"))
                count++;
        return count;
    }
    long millisElapsedSince(long t0) {
        return (System.nanoTime() - t0) / (1000L * 1000L);
    }
    void test(String[] args) throws Throwable {
        final int threadCount = 10;
        final int timeoutMillis = 30;
        BlockingQueue<Runnable> q
            = new ArrayBlockingQueue<Runnable>(2*threadCount);
        ThreadPoolExecutor tpe
            = new ThreadPoolExecutor(threadCount, threadCount,
                                     timeoutMillis, TimeUnit.MILLISECONDS,
                                     q, new IdentifiableThreadFactory());
        equal(tpe.getCorePoolSize(), threadCount);
        check(! tpe.allowsCoreThreadTimeOut());
        tpe.allowCoreThreadTimeOut(true);
        check(tpe.allowsCoreThreadTimeOut());
        equal(countExecutorThreads(), 0);
        long t0 = System.nanoTime();
        for (int i = 0; i < threadCount; i++)
            tpe.submit(new Runnable() { public void run() {}});
        int count = countExecutorThreads();
        if (millisElapsedSince(t0) < timeoutMillis)
            equal(count, threadCount);
        while (countExecutorThreads() > 0 &&
               millisElapsedSince(t0) < 10 * 1000);
        equal(countExecutorThreads(), 0);
        tpe.shutdown();
        check(tpe.allowsCoreThreadTimeOut());
        check(tpe.awaitTermination(10, TimeUnit.SECONDS));
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Exception("Some tests failed");
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
        new CoreThreadTimeOut().instanceMain(args);}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
