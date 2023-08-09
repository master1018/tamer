public class Bug6571733 {
    void test(String[] args) throws Throwable {
        test(true);
        test(false);
    }
    void test(boolean fairness) throws Throwable {
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(fairness);
        lock.readLock().lock();
        Thread thread = new Thread() { public void run() {
            try {
                check(! lock.writeLock().tryLock(0, TimeUnit.DAYS));
                lock.readLock().lock();
                lock.readLock().unlock();
            } catch (Throwable t) { unexpected(t); }}};
        thread.start();
        thread.join();
    }
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    public static void main(String[] args) throws Throwable {
        new Bug6571733().instanceMain(args);}
    void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
