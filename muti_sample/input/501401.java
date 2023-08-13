public class JSR166TestCase extends TestCase {
    public static void main (String[] args) {
        int iters = 1;
        if (args.length > 0)
            iters = Integer.parseInt(args[0]);
        Test s = suite();
        for (int i = 0; i < iters; ++i) {
            junit.textui.TestRunner.run (s);
            System.gc();
            System.runFinalization();
        }
        System.exit(0);
    }
    public static Test suite ( ) {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("JSR166 Unit Tests");
        suite.addTest(AbstractExecutorServiceTest.suite());
        suite.addTest(AbstractQueueTest.suite());
        suite.addTest(AbstractQueuedSynchronizerTest.suite());
        suite.addTest(ArrayBlockingQueueTest.suite());
        suite.addTest(AtomicBooleanTest.suite());
        suite.addTest(AtomicIntegerArrayTest.suite());
        suite.addTest(AtomicIntegerFieldUpdaterTest.suite());
        suite.addTest(AtomicIntegerTest.suite());
        suite.addTest(AtomicLongArrayTest.suite());
        suite.addTest(AtomicLongFieldUpdaterTest.suite());
        suite.addTest(AtomicLongTest.suite());
        suite.addTest(AtomicMarkableReferenceTest.suite());
        suite.addTest(AtomicReferenceArrayTest.suite());
        suite.addTest(AtomicReferenceFieldUpdaterTest.suite());
        suite.addTest(AtomicReferenceTest.suite());
        suite.addTest(AtomicStampedReferenceTest.suite());
        suite.addTest(ConcurrentHashMapTest.suite());
        suite.addTest(ConcurrentLinkedQueueTest.suite());
        suite.addTest(CopyOnWriteArrayListTest.suite());
        suite.addTest(CopyOnWriteArraySetTest.suite());
        suite.addTest(CountDownLatchTest.suite());
        suite.addTest(CyclicBarrierTest.suite());
        suite.addTest(DelayQueueTest.suite());
        suite.addTest(ExchangerTest.suite());
        suite.addTest(ExecutorsTest.suite());
        suite.addTest(ExecutorCompletionServiceTest.suite());
        suite.addTest(FutureTaskTest.suite());
        suite.addTest(LinkedBlockingQueueTest.suite());
        suite.addTest(LinkedListTest.suite());
        suite.addTest(LockSupportTest.suite());
        suite.addTest(PriorityBlockingQueueTest.suite());
        suite.addTest(PriorityQueueTest.suite());
        suite.addTest(ReentrantLockTest.suite());
        suite.addTest(ReentrantReadWriteLockTest.suite());
        suite.addTest(ScheduledExecutorTest.suite());
        suite.addTest(SemaphoreTest.suite());
        suite.addTest(SynchronousQueueTest.suite());
        suite.addTest(SystemTest.suite());
        suite.addTest(ThreadLocalTest.suite());
        suite.addTest(ThreadPoolExecutorTest.suite());
        suite.addTest(ThreadTest.suite());
        suite.addTest(TimeUnitTest.suite());
        return suite;
    }
    public static long SHORT_DELAY_MS;
    public static long SMALL_DELAY_MS;
    public static long MEDIUM_DELAY_MS;
    public static long LONG_DELAY_MS;
    protected long getShortDelay() {
        return 250;
    }
    protected  void setDelays() {
        SHORT_DELAY_MS = getShortDelay();
        SMALL_DELAY_MS = SHORT_DELAY_MS * 5;
        MEDIUM_DELAY_MS = SHORT_DELAY_MS * 10;
        LONG_DELAY_MS = SHORT_DELAY_MS * 50;
    }
    volatile boolean threadFailed;
    public void setUp() {
        setDelays();
        threadFailed = false;
    }
    public void tearDown() {
        assertFalse(threadFailed);
    }
    public void threadFail(String reason) {
        threadFailed = true;
        fail(reason);
    }
    public void threadAssertTrue(boolean b) {
        if (!b) {
            threadFailed = true;
            assertTrue(b);
        }
    }
    public void threadAssertFalse(boolean b) {
        if (b) {
            threadFailed = true;
            assertFalse(b);
        }
    }
    public void threadAssertNull(Object x) {
        if (x != null) {
            threadFailed = true;
            assertNull(x);
        }
    }
    public void threadAssertEquals(long x, long y) {
        if (x != y) {
            threadFailed = true;
            assertEquals(x, y);
        }
    }
    public void threadAssertEquals(Object x, Object y) {
        if (x != y && (x == null || !x.equals(y))) {
            threadFailed = true;
            assertEquals(x, y);
        }
    }
    public void threadShouldThrow() {
       try {
           threadFailed = true;
           fail("should throw exception");
       } catch (AssertionFailedError e) {
           e.printStackTrace();
           throw e;
       }
    }
    public void threadUnexpectedException() {
        threadFailed = true;
        fail("Unexpected exception");
    }
    public void threadUnexpectedException(Throwable ex) {
        threadFailed = true;
        ex.printStackTrace();
        fail("Unexpected exception: " + ex);
    }
    public void joinPool(ExecutorService exec) {
        try {
            exec.shutdown();
            assertTrue(exec.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch(SecurityException ok) {
        } catch(InterruptedException ie) {
            fail("Unexpected exception");
        }
    }
    public void shouldThrow() {
        fail("Should throw exception");
    }
    public void unexpectedException() {
        fail("Unexpected exception");
    }
    static final int SIZE = 20;
    static final Integer zero = new Integer(0);
    static final Integer one = new Integer(1);
    static final Integer two = new Integer(2);
    static final Integer three  = new Integer(3);
    static final Integer four  = new Integer(4);
    static final Integer five  = new Integer(5);
    static final Integer six = new Integer(6);
    static final Integer seven = new Integer(7);
    static final Integer eight = new Integer(8);
    static final Integer nine = new Integer(9);
    static final Integer m1  = new Integer(-1);
    static final Integer m2  = new Integer(-2);
    static final Integer m3  = new Integer(-3);
    static final Integer m4 = new Integer(-4);
    static final Integer m5 = new Integer(-5);
    static final Integer m6 = new Integer(-6);
    static final Integer m10 = new Integer(-10);
    static class AdjustablePolicy extends java.security.Policy {
        Permissions perms = new Permissions();
        AdjustablePolicy() { }
        void addPermission(Permission perm) { perms.add(perm); }
        void clearPermissions() { perms = new Permissions(); }
        public PermissionCollection getPermissions(CodeSource cs) {
            return perms;
        }
        public PermissionCollection getPermissions(ProtectionDomain pd) {
            return perms;
        }
        public boolean implies(ProtectionDomain pd, Permission p) {
            return perms.implies(p);
        }
        public void refresh() {}
    }
    static class NoOpRunnable implements Runnable {
        public void run() {}
    }
    static class NoOpCallable implements Callable {
        public Object call() { return Boolean.TRUE; }
    }
    static final String TEST_STRING = "a test string";
    static class StringTask implements Callable<String> {
        public String call() { return TEST_STRING; }
    }
    static class NPETask implements Callable<String> {
        public String call() { throw new NullPointerException(); }
    }
    static class CallableOne implements Callable<Integer> {
        public Integer call() { return one; }
    }
    class ShortRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(SHORT_DELAY_MS);
            }
            catch(Exception e) {
                threadUnexpectedException(e);
            }
        }
    }
    class ShortInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(SHORT_DELAY_MS);
                threadShouldThrow();
            }
            catch(InterruptedException success) {
            }
        }
    }
    class SmallRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
            }
            catch(Exception e) {
                threadUnexpectedException(e);
            }
        }
    }
    class SmallPossiblyInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
            }
            catch(Exception e) {
            }
        }
    }
    class SmallCallable implements Callable {
        public Object call() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
            }
            catch(Exception e) {
                threadUnexpectedException(e);
            }
            return Boolean.TRUE;
        }
    }
    class SmallInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
                threadShouldThrow();
            }
            catch(InterruptedException success) {
            }
        }
    }
    class MediumRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
            }
            catch(Exception e) {
                threadUnexpectedException(e);
            }
        }
    }
    class MediumInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
                threadShouldThrow();
            }
            catch(InterruptedException success) {
            }
        }
    }
    class MediumPossiblyInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
            }
            catch(InterruptedException success) {
            }
        }
    }
    class LongPossiblyInterruptedRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(LONG_DELAY_MS);
            }
            catch(InterruptedException success) {
            }
        }
    }
    static class SimpleThreadFactory implements ThreadFactory{
        public Thread newThread(Runnable r){
            return new Thread(r);
        }
    }
    static class TrackedShortRunnable implements Runnable {
        volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
                done = true;
            } catch(Exception e){
            }
        }
    }
    static class TrackedMediumRunnable implements Runnable {
        volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
                done = true;
            } catch(Exception e){
            }
        }
    }
    static class TrackedLongRunnable implements Runnable {
        volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(LONG_DELAY_MS);
                done = true;
            } catch(Exception e){
            }
        }
    }
    static class TrackedNoOpRunnable implements Runnable {
        volatile boolean done = false;
        public void run() {
            done = true;
        }
    }
    static class TrackedCallable implements Callable {
        volatile boolean done = false;
        public Object call() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
                done = true;
            } catch(Exception e){
            }
            return Boolean.TRUE;
        }
    }
    static class NoOpREHandler implements RejectedExecutionHandler{
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor){}
    }
}
