public class ShutdownNowExecuteRace {
    static volatile boolean quit = false;
    static volatile ThreadPoolExecutor pool = null;
    static final Runnable sleeper = new Runnable() { public void run() {
        final long ONE_HOUR = 1000L * 60L * 60L;
        try { Thread.sleep(ONE_HOUR); }
        catch (InterruptedException ie) {}
        catch (Throwable t) { unexpected(t); }}};
    static void realMain(String[] args) throws Throwable {
        final int iterations = 1 << 8;
        Thread thread = new Thread() { public void run() {
            while (! quit) {
                ThreadPoolExecutor pool = ShutdownNowExecuteRace.pool;
                if (pool != null)
                    try { pool.execute(sleeper); }
                    catch (RejectedExecutionException e) {}
                    catch (Throwable t) { unexpected(t); }}}};
        thread.start();
        for (int i = 0; i < iterations; i++) {
            pool = new ThreadPoolExecutor(
                10, 10, 3L, TimeUnit.DAYS,
                new ArrayBlockingQueue<Runnable>(10));
            pool.shutdownNow();
            check(pool.awaitTermination(3L, TimeUnit.MINUTES));
        }
        quit = true;
        thread.join();
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
    private abstract static class Fun {abstract void f() throws Throwable;}
    static void THROWS(Class<? extends Throwable> k, Fun... fs) {
        for (Fun f : fs)
            try { f.f(); fail("Expected " + k.getName() + " not thrown"); }
            catch (Throwable t) {
                if (k.isAssignableFrom(t.getClass())) pass();
                else unexpected(t);}}
    private abstract static class CheckedThread extends Thread {
        abstract void realRun() throws Throwable;
        public void run() {
            try {realRun();} catch (Throwable t) {unexpected(t);}}}
}
