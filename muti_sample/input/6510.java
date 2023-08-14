public class BlockingTaskExecutor {
    static void realMain(String[] args) throws Throwable {
        for (int i = 1; i <= 100; i++) {
            System.out.print(".");
            test();
        }
    }
    static void test() throws Throwable {
        final ExecutorService executor = Executors.newCachedThreadPool();
        final NotificationReceiver notifiee1 = new NotificationReceiver();
        final NotificationReceiver notifiee2 = new NotificationReceiver();
        final Collection<Callable<Object>> tasks =
            new ArrayList<Callable<Object>>();
        tasks.add(new BlockingTask(notifiee1));
        tasks.add(new BlockingTask(notifiee2));
        tasks.add(new NonBlockingTask());
        Thread thread = new Thread() { public void run() {
            try { executor.invokeAll(tasks); }
            catch (RejectedExecutionException t) {}
            catch (Throwable t) { unexpected(t); }}};
        thread.start();
        notifiee1.waitForNotification();
        notifiee2.waitForNotification();
        executor.shutdownNow();
        if (! executor.awaitTermination(5, TimeUnit.SECONDS))
            throw new Error("Executor stuck");
        thread.join(1000);
        if (thread.isAlive()) {
            thread.interrupt();
            thread.join(1000);
            throw new Error("invokeAll stuck");
        }
    }
    static class NotificationReceiver {
        boolean notified = false;
        public synchronized void sendNotification() {
            notified = true;
            notifyAll();
        }
        public synchronized void waitForNotification()
            throws InterruptedException {
            while (! notified)
                wait();
        }
    }
    static class BlockingTask implements Callable<Object> {
        private final NotificationReceiver notifiee;
        BlockingTask(NotificationReceiver notifiee) {
            this.notifiee = notifiee;
        }
        public Object call() throws InterruptedException {
            notifiee.sendNotification();
            while (true) {
                synchronized (this) {
                    wait();
                }
            }
        }
    }
    static class NonBlockingTask implements Callable<Object> {
        public Object call() {
            return "NonBlockingTaskResult";
        }
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
