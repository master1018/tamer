public class ResetPeakThreadCount {
    private static final int DAEMON_THREADS_1 = 8;
    private static final int EXPECTED_PEAK_DELTA_1 = 8;
    private static final int TERMINATE_1 = 4;
    private static final int DAEMON_THREADS_2 = 2;
    private static final int EXPECTED_PEAK_DELTA_2 = 0;
    private static final int DAEMON_THREADS_3 = 4;
    private static final int EXPECTED_PEAK_DELTA_3 = 4;
    private static final int TERMINATE_2 = 8;
    private static final int ALL_THREADS = DAEMON_THREADS_1 +
        DAEMON_THREADS_2 + DAEMON_THREADS_3;
    private static Barrier barrier = new Barrier(DAEMON_THREADS_1);
    private static Thread allThreads[] = new Thread[ALL_THREADS];
    private static volatile boolean live[] = new boolean[ALL_THREADS];
    private static final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    public static void main(String[] argv) throws Exception {
        long previous = mbean.getThreadCount();
        long current;
        current = startThreads(0, DAEMON_THREADS_1, EXPECTED_PEAK_DELTA_1);
        checkThreadCount(previous, current, DAEMON_THREADS_1);
        previous = current;
        current = terminateThreads(0, TERMINATE_1);
        checkThreadCount(previous, current, TERMINATE_1 * -1);
        previous = current;
        current = startThreads(DAEMON_THREADS_1, DAEMON_THREADS_2,
                               EXPECTED_PEAK_DELTA_2);
        checkThreadCount(previous, current, DAEMON_THREADS_2);
        previous = current;
        resetPeak(current);
        current = startThreads(DAEMON_THREADS_1 + DAEMON_THREADS_2,
                               DAEMON_THREADS_3, EXPECTED_PEAK_DELTA_3);
        checkThreadCount(previous, current, DAEMON_THREADS_3);
        previous = current;
        current = terminateThreads(TERMINATE_1, TERMINATE_2);
        checkThreadCount(previous, current, TERMINATE_2 * -1);
        resetPeak(current);
        if (testFailed)
            throw new RuntimeException("TEST FAILED.");
        System.out.println("Test passed");
    }
    private static long startThreads(int from, int count, int delta) {
        long peak1 = mbean.getPeakThreadCount();
        long current = mbean.getThreadCount();
        System.out.println("Starting " + count + " threads....");
        barrier.set(count);
        for (int i = from; i < (from + count); i++) {
            live[i] = true;
            allThreads[i] = new MyThread(i);
            allThreads[i].setDaemon(true);
            allThreads[i].start();
        }
        barrier.await();
        long peak2 = mbean.getPeakThreadCount();
        System.out.println("   Current = " + mbean.getThreadCount() +
            " Peak before = " + peak1 + " after: " + peak2);
        if (peak2 != (peak1 + delta)) {
            throw new RuntimeException("Current Peak = " + peak2 +
                " Expected to be == previous peak = " + peak1 + " + " +
                delta);
        }
        while (mbean.getThreadCount() < (current + count)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Unexpected exception.");
                testFailed = true;
            }
        }
        current = mbean.getThreadCount();
        System.out.println("   Live thread count before returns " + current);
        return current;
    }
    private static long terminateThreads(int from, int count) {
        long peak1 = mbean.getPeakThreadCount();
        long current = mbean.getThreadCount();
        System.out.println("Terminating " + count + " threads....");
        barrier.set(count);
        for (int i = from; i < (from+count); i++) {
            live[i] = false;
        }
        barrier.await();
        long peak2 = mbean.getPeakThreadCount();
        if (peak2 != peak1) {
            throw new RuntimeException("Current Peak = " + peak2 +
                " Expected to be = previous peak = " + peak1);
        }
        while (mbean.getThreadCount() > (current - count)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Unexpected exception.");
                testFailed = true;
            }
        }
        current = mbean.getThreadCount();
        System.out.println("   Live thread count before returns " + current);
        return current;
    }
    private static void resetPeak(long expectedCount) {
        long peak3 = mbean.getPeakThreadCount();
        long current = mbean.getThreadCount();
        checkThreadCount(expectedCount, current, 0);
        mbean.resetPeakThreadCount();
        long afterResetPeak = mbean.getPeakThreadCount();
        long afterResetCurrent = mbean.getThreadCount();
        System.out.println("Reset peak before = " + peak3 +
            " current = " + current +
            " after reset peak = " + afterResetPeak +
            " current = " + afterResetCurrent);
        if (afterResetPeak != current) {
            throw new RuntimeException("Current Peak after reset = " +
                afterResetPeak +
                " Expected to be = current count = " + current);
        }
    }
    private static void checkThreadCount(long previous, long current, int expectedDelta) {
        if (current != previous + expectedDelta) {
            System.out.println("***** Unexpected thread count:" +
                               " previous = " + previous +
                               " current = " + current +
                               " delta = " + expectedDelta + "*****");
            ThreadDump.threadDump();
        }
    }
    private static class MyThread extends Thread {
        int id;
        MyThread(int id) {
            this.id = id;
        }
        public void run() {
            barrier.signal();
            while (live[id]) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Unexpected exception is thrown.");
                    e.printStackTrace(System.out);
                    testFailed = true;
                }
            }
            barrier.signal();
        }
    }
}
