@TestTargetClass(Unsafe.class)
public class ThreadsTest extends TestCase {
    private static Unsafe UNSAFE = null;
    private static RuntimeException INITIALIZEFAILED = null;
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("THE_ONE");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (NoSuchFieldException ex) {
            INITIALIZEFAILED = new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            INITIALIZEFAILED = new RuntimeException(ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkFor_1() {
        Parker parker = new Parker(false, 500);
        Thread parkerThread = new Thread(parker);
        Thread waiterThread =
            new Thread(new WaitAndUnpark(1000, parkerThread));
        parkerThread.start();
        waiterThread.start();
        parker.assertDurationIsInRange(500);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkFor_2() {
        Parker parker = new Parker(false, 1000);
        Thread parkerThread = new Thread(parker);
        Thread waiterThread =
            new Thread(new WaitAndUnpark(300, parkerThread));
        parkerThread.start();
        waiterThread.start();
        parker.assertDurationIsInRange(300);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkFor_3() {
        Parker parker = new Parker(false, 1000);
        Thread parkerThread = new Thread(parker);
        UNSAFE.unpark(parkerThread);
        parkerThread.start();
        parker.assertDurationIsInRange(0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkUntil_1() {
        Parker parker = new Parker(true, 500);
        Thread parkerThread = new Thread(parker);
        Thread waiterThread =
            new Thread(new WaitAndUnpark(1000, parkerThread));
        parkerThread.start();
        waiterThread.start();
        parker.assertDurationIsInRange(500);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkUntil_2() {
        Parker parker = new Parker(true, 1000);
        Thread parkerThread = new Thread(parker);
        Thread waiterThread =
            new Thread(new WaitAndUnpark(300, parkerThread));
        parkerThread.start();
        waiterThread.start();
        parker.assertDurationIsInRange(300);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "unpark",
        args = {Object.class}
    )    
    @AndroidOnly("Accesses Android-specific private field")
    public void test_parkUntil_3() {
        Parker parker = new Parker(true, 1000);
        Thread parkerThread = new Thread(parker);
        UNSAFE.unpark(parkerThread);
        parkerThread.start();
        parker.assertDurationIsInRange(0);
    }
    private static class Parker implements Runnable {
        private final boolean absolute;
        private final long amount;
        private boolean completed;
        private long startMillis;
        private long endMillis;
        public Parker(boolean absolute, long parkMillis) {
            this.absolute = absolute;
            this.amount = absolute ? parkMillis : parkMillis * 1000000;
        }
        public void run() {
            boolean absolute = this.absolute;
            long amount = this.amount;
            long start = System.currentTimeMillis();
            if (absolute) {
                UNSAFE.park(true, start + amount);
            } else {
                UNSAFE.park(false, amount);
            }
            long end = System.currentTimeMillis();
            synchronized (this) {
                startMillis = start;
                endMillis = end;
                completed = true;
                notifyAll();
            }
        }
        public long getDurationMillis(long maxWaitMillis) {
            synchronized (this) {
                if (! completed) {
                    try {
                        wait(maxWaitMillis);
                    } catch (InterruptedException ex) {
                    }
                    if (! completed) {
                        Assert.fail("parker hanging");
                    }
                }
                return endMillis - startMillis;
            }
        }
        public void assertDurationIsInRange(long expectedMillis) {
            long minimum = (long) ((double) expectedMillis * 0.95);
            long maximum =
                Math.max((long) ((double) expectedMillis * 1.05), 10);
            long waitMillis = Math.max(expectedMillis * 10, 10);
            long duration = getDurationMillis(waitMillis);
            if (duration < minimum) {
                Assert.fail("expected duration: " + expectedMillis + 
                        "; actual too short: " + duration);
            } else if (duration > maximum) {
                Assert.fail("expected duration: " + expectedMillis + 
                        "; actual too long: " + duration);
            }
        }
    }
    private static class WaitAndUnpark implements Runnable {
        private final long waitMillis;
        private final Thread thread;
        public WaitAndUnpark(long waitMillis, Thread thread) {
            this.waitMillis = waitMillis;
            this.thread = thread;
        }
        public void run() {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                throw new RuntimeException("shouldn't happen", ex);
            }
            UNSAFE.unpark(thread);
        }
    }
    @Override
    protected void setUp() throws Exception {
        if (INITIALIZEFAILED != null) {
            throw INITIALIZEFAILED;
        }
    }
}
