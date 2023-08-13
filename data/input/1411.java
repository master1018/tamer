public class ThreadBlockedCount {
    final static long EXPECTED_BLOCKED_COUNT = 3;
    final static int  DEPTH = 10;
    private static ThreadMXBean mbean
        = ManagementFactory.getThreadMXBean();
    private static Object a = new Object();
    private static Object b = new Object();
    private static Object c = new Object();
    private static boolean aNotified = false;
    private static boolean bNotified = false;
    private static boolean cNotified = false;
    private static Object blockedObj1 = new Object();
    private static Object blockedObj2 = new Object();
    private static Object blockedObj3 = new Object();
    private static volatile boolean testFailed = false;
    private static BlockingThread blocking;
    private static BlockedThread blocked;
    private static ThreadExecutionSynchronizer thrsync;
    public static void main(String args[]) throws Exception {
        thrsync = new ThreadExecutionSynchronizer();
        blocking = new BlockingThread();
        blocking.start();
        blocked = new BlockedThread();
        blocked.start();
        try {
            blocking.join();
            blocked.join();
        } catch (InterruptedException e) {
            System.err.println("Unexpected exception.");
            e.printStackTrace(System.err);
            throw e;
        }
        if (testFailed) {
            throw new RuntimeException("TEST FAILED.");
        }
        System.out.println("Test passed.");
    }
    static class BlockedThread extends Thread {
        public void run() {
            thrsync.signal();
            synchronized (a) {
                while (!aNotified) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.err.println("Unexpected exception.");
                        e.printStackTrace(System.err);
                        testFailed = true;
                    }
                }
                thrsync.signal();
                synchronized (blockedObj1) {
                    System.out.println("BlockedThread entered lock blockedObj1.");
                }
            }
            thrsync.signal();
            synchronized (b) {
                while (!bNotified) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.err.println("Unexpected exception.");
                        e.printStackTrace(System.err);
                        testFailed = true;
                    }
                }
                thrsync.signal();
                synchronized (blockedObj2) {
                    System.out.println("BlockedThread entered lock blockedObj2.");
                }
            }
            thrsync.signal();
            synchronized (c) {
                while (!cNotified) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.err.println("Unexpected exception.");
                        e.printStackTrace(System.err);
                        testFailed = true;
                    }
                }
                thrsync.signal();
                synchronized (blockedObj3) {
                    System.out.println("BlockedThread entered lock blockedObj3.");
                }
            }
            ThreadInfo ti = mbean.getThreadInfo(Thread.currentThread().
                                                getId());
            long count = ti.getBlockedCount();
            if (count != EXPECTED_BLOCKED_COUNT) {
                System.err.println("TEST FAILED: Blocked thread has " + count +
                                   " blocked counts. Expected " +
                                   EXPECTED_BLOCKED_COUNT);
                testFailed = true;
            }
        } 
    } 
    static class BlockingThread extends Thread {
        private void waitForSignalToRelease() {
            thrsync.waitForSignal();
            boolean threadBlocked = false;
            while (!threadBlocked) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.err.println("Unexpected exception.");
                    e.printStackTrace(System.err);
                    testFailed = true;
                }
                ThreadInfo info = mbean.getThreadInfo(blocked.getId());
                threadBlocked = (info.getThreadState() == Thread.State.BLOCKED);
            }
        }
        public void run() {
            thrsync.waitForSignal();
            synchronized (blockedObj1) {
                System.out.println("BlockingThread attempts to notify a");
                aNotified = true;
                waitForSignalToRelease();
            }
            thrsync.waitForSignal();
            synchronized (blockedObj2) {
                System.out.println("BlockingThread attempts to notify b");
                bNotified = true;
                waitForSignalToRelease();
            }
            thrsync.waitForSignal();
            synchronized (blockedObj3) {
                System.out.println("BlockingThread attempts to notify c");
                cNotified = true;
                waitForSignalToRelease();
            }
        } 
    } 
}
