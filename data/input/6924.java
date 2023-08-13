public class AutoShutdown {
    private static void waitForFinalizersToRun() {
        for (int i = 0; i < 2; i++)
            tryWaitForFinalizersToRun();
    }
    private static void tryWaitForFinalizersToRun() {
        System.gc();
        final CountDownLatch fin = new CountDownLatch(1);
        new Object() { protected void finalize() { fin.countDown(); }};
        System.gc();
        try { fin.await(); }
        catch (InterruptedException ie) { throw new Error(ie); }
    }
    private static void realMain(String[] args) throws Throwable {
        final Phaser phaser = new Phaser(3);
        Runnable trivialRunnable = new Runnable() {
            public void run() {
                phaser.arriveAndAwaitAdvance();
            }
        };
        int count0 = Thread.activeCount();
        Executor e1 = newSingleThreadExecutor();
        Executor e2 = newSingleThreadExecutor(defaultThreadFactory());
        e1.execute(trivialRunnable);
        e2.execute(trivialRunnable);
        phaser.arriveAndAwaitAdvance();
        equal(Thread.activeCount(), count0 + 2);
        e1 = e2 = null;
        for (int i = 0; i < 10 && Thread.activeCount() > count0; i++)
            tryWaitForFinalizersToRun();
        equal(Thread.activeCount(), count0);
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
