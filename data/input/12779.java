public class AllThreadIds {
    final static int DAEMON_THREADS = 20;
    final static int USER_THREADS = 5;
    final static int ALL_THREADS = DAEMON_THREADS + USER_THREADS;
    private static volatile boolean live[] = new boolean[ALL_THREADS];
    private static Thread allThreads[] = new Thread[ALL_THREADS];
    private static ThreadMXBean mbean
        = ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static boolean trace = false;
    private static long prevTotalThreadCount = 0;
    private static int prevLiveThreadCount = 0;
    private static int prevPeakThreadCount = 0;
    private static long curTotalThreadCount = 0;
    private static int curLiveThreadCount = 0;
    private static int curPeakThreadCount = 0;
    private static Barrier barrier = new Barrier(ALL_THREADS);
    private static void printThreadList() {
        if (!trace) return;
        long[] list = mbean.getAllThreadIds();
        for (int i = 1; i <= list.length; i++) {
            System.out.println(i + ": Thread id = " + list[i-1]);
        }
        for (int i = 0; i < ALL_THREADS; i++) {
            Thread t = allThreads[i];
            System.out.println(t.getName() + " Id = " + t.getId() +
                " die = " + live[i] +
                " alive = " + t.isAlive());
        }
    }
    private static void fail(String msg) {
        trace = true;
        printThreadList();
        throw new RuntimeException(msg);
    }
    private static void checkThreadCount(int numNewThreads,
                                         int numTerminatedThreads)
        throws Exception {
        prevTotalThreadCount = curTotalThreadCount;
        prevLiveThreadCount = curLiveThreadCount;
        prevPeakThreadCount = curPeakThreadCount;
        curTotalThreadCount = mbean.getTotalStartedThreadCount();
        curLiveThreadCount = mbean.getThreadCount();
        curPeakThreadCount = mbean.getPeakThreadCount();
        if ((curLiveThreadCount - prevLiveThreadCount) !=
            (numNewThreads - numTerminatedThreads)) {
            fail("Unexpected number of live threads: " +
                " Prev live = " + prevLiveThreadCount +
                " Current live = " + curLiveThreadCount +
                " Threads added = " + numNewThreads +
                " Threads terminated = " + numTerminatedThreads);
        }
        if (curPeakThreadCount - prevPeakThreadCount != numNewThreads) {
            fail("Unexpected number of peak threads: " +
                " Prev peak = " + prevPeakThreadCount +
                " Current peak = " + curPeakThreadCount +
                " Threads added = " + numNewThreads);
        }
        if (curTotalThreadCount - prevTotalThreadCount != numNewThreads) {
            fail("Unexpected number of total threads: " +
                " Prev Total = " + prevTotalThreadCount +
                " Current Total = " + curTotalThreadCount +
                " Threads added = " + numNewThreads);
        }
        long[] list = mbean.getAllThreadIds();
        if (list.length != curLiveThreadCount) {
            fail("Array length returned by " +
                "getAllThreadIds() = " + list.length +
                " not matched count = " + curLiveThreadCount);
        }
    }
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        curTotalThreadCount = mbean.getTotalStartedThreadCount();
        curLiveThreadCount = mbean.getThreadCount();
        curPeakThreadCount = mbean.getPeakThreadCount();
        checkThreadCount(0, 0);
        barrier.set(ALL_THREADS);
        for (int i = 0; i < ALL_THREADS; i++) {
            live[i] = true;
            allThreads[i] = new MyThread(i);
            allThreads[i].setDaemon( (i < DAEMON_THREADS) ? true : false);
            allThreads[i].start();
        }
        barrier.await();
        checkThreadCount(ALL_THREADS, 0);
        printThreadList();
        long[] list = mbean.getAllThreadIds();
        for (int i = 0; i < ALL_THREADS; i++) {
            long expectedId = allThreads[i].getId();
            boolean found = false;
            if (trace) {
                System.out.print("Looking for thread with id " + expectedId);
            }
            for (int j = 0; j < list.length; j++) {
                if (expectedId == list[j]) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                testFailed = true;
            }
            if (trace) {
                if (!found) {
                    System.out.print(". TEST FAILED.");
                }
                System.out.println();
            }
        }
        if (trace) {
            System.out.println();
        }
        barrier.set(DAEMON_THREADS);
        for (int i = 0; i < DAEMON_THREADS; i++) {
            live[i] = false;
        }
        barrier.await();
        pause();
        checkThreadCount(0, DAEMON_THREADS);
        list = mbean.getAllThreadIds();
        for (int i = 0; i < ALL_THREADS; i++) {
            long expectedId = allThreads[i].getId();
            boolean found = false;
            boolean live = (i >= DAEMON_THREADS);
            if (trace) {
                System.out.print("Looking for thread with id " + expectedId +
                    (live ? " expected alive." : " expected terminated."));
            }
            for (int j = 0; j < list.length; j++) {
                if (expectedId == list[j]) {
                    found = true;
                    break;
                }
            }
            if (live != found) {
                testFailed = true;
            }
            if (trace) {
                if (live != found) {
                    System.out.println(" TEST FAILED.");
                } else {
                    System.out.println();
                }
            }
        }
        barrier.set(ALL_THREADS - DAEMON_THREADS);
        for (int i = DAEMON_THREADS; i < ALL_THREADS; i++) {
            live[i] = false;
        }
        barrier.await();
        pause();
        checkThreadCount(0, ALL_THREADS - DAEMON_THREADS);
        if (testFailed)
            throw new RuntimeException("TEST FAILED.");
        System.out.println("Test passed.");
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
    private static Object pauseObj = new Object();
    private static void pause() {
        synchronized (pauseObj) {
            try {
                pauseObj.wait(50);
            } catch (Exception e) {
                System.err.println("Unexpected exception.");
                e.printStackTrace(System.err);
            }
        }
    }
}
