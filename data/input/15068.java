public class Invoke {
    static volatile int passed = 0, failed = 0;
    static void fail(String msg) {
        failed++;
        new AssertionError(msg).printStackTrace();
    }
    static void pass() {
        passed++;
    }
    static void unexpected(Throwable t) {
        failed++;
        t.printStackTrace();
    }
    static void check(boolean condition, String msg) {
        if (condition) pass(); else fail(msg);
    }
    static void check(boolean condition) {
        check(condition, "Assertion failure");
    }
    public static void main(String[] args) {
        try {
            final AtomicLong count = new AtomicLong(0);
            ExecutorService fixed = Executors.newFixedThreadPool(5);
            class Inc implements Callable<Long> {
                public Long call() throws Exception {
                    Thread.sleep(200); 
                    return count.incrementAndGet();
                }
            }
            List<Inc> tasks = Arrays.asList(new Inc(), new Inc(), new Inc());
            List<Future<Long>> futures = fixed.invokeAll(tasks);
            check(futures.size() == tasks.size());
            check(count.get() == tasks.size());
            long gauss = 0;
            for (Future<Long> future : futures) gauss += future.get();
            check(gauss == ((tasks.size()+1)*tasks.size())/2);
            ExecutorService single = Executors.newSingleThreadExecutor();
            long save = count.get();
            check(single.invokeAny(tasks) == save + 1);
            check(count.get() == save + 1);
            fixed.shutdown();
            single.shutdown();
        } catch (Throwable t) { unexpected(t); }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Error("Some tests failed");
    }
}
