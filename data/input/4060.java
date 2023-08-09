public class ThreadCpuTime {
    private static ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static boolean done = false;
    private static Object obj = new Object();
    private static final int NUM_THREADS = 10;
    private static Thread[] threads = new Thread[NUM_THREADS];
    private static long[] times = new long[NUM_THREADS];
    private static final int DELTA = 100;
    public static void main(String[] argv)
        throws Exception {
        if (!mbean.isCurrentThreadCpuTimeSupported()) {
            return;
        }
        if (mbean.isThreadCpuTimeEnabled()) {
            mbean.setThreadCpuTimeEnabled(false);
        }
        Thread curThread = Thread.currentThread();
        long t = mbean.getCurrentThreadCpuTime();
        if (t != -1) {
            throw new RuntimeException("Invalid CurrenThreadCpuTime returned = " +
                t + " expected = -1");
        }
        if (mbean.isThreadCpuTimeSupported()) {
            long t1 = mbean.getThreadCpuTime(curThread.getId());
            if (t1 != -1) {
                throw new RuntimeException("Invalid ThreadCpuTime returned = " +
                    t1 + " expected = -1");
            }
        }
        if (!mbean.isThreadCpuTimeEnabled()) {
            mbean.setThreadCpuTimeEnabled(true);
        }
        if (!mbean.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("ThreadCpuTime is expected to be enabled");
        }
        long time = mbean.getCurrentThreadCpuTime();
        if (time < 0) {
            throw new RuntimeException("Invalid CPU time returned = " + time);
        }
        if (!mbean.isThreadCpuTimeSupported()) {
            return;
        }
        long time1 = mbean.getThreadCpuTime(curThread.getId());
        if (time1 < time) {
            throw new RuntimeException("CPU time " + time1 +
                " expected >= " + time);
        }
        System.out.println(curThread.getName() +
            " Current Thread Cpu Time = " + time +
            " CPU time = " + time1);
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new MyThread("MyThread-" + i);
            threads[i].start();
        }
        waitUntilThreadBlocked();
        for (int i = 0; i < NUM_THREADS; i++) {
            times[i] = mbean.getThreadCpuTime(threads[i].getId());
        }
        goSleep(200);
        for (int i = 0; i < NUM_THREADS; i++) {
            long newTime = mbean.getThreadCpuTime(threads[i].getId());
            if (times[i] > newTime) {
                throw new RuntimeException("TEST FAILED: " +
                    threads[i].getName() +
                    " previous CPU time = " + times[i] +
                    " > current CPU time = " + newTime);
            }
            if ((times[i] + DELTA) < newTime) {
                throw new RuntimeException("TEST FAILED: " +
                    threads[i].getName() +
                    " CPU time = " + newTime +
                    " previous CPU time " + times[i] +
                    " out of expected range");
            }
            System.out.println(threads[i].getName() +
                " Previous Cpu Time = " + times[i] +
                " Current CPU time = " + newTime);
        }
        synchronized (obj) {
            done = true;
            obj.notifyAll();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println("Unexpected exception is thrown.");
                e.printStackTrace(System.out);
                testFailed = true;
                break;
            }
        }
        if (testFailed) {
            throw new RuntimeException("TEST FAILED");
        }
        System.out.println("Test passed");
    }
    private static void goSleep(long ms) throws Exception {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("Unexpected exception is thrown.");
            throw e;
        }
    }
    private static void waitUntilThreadBlocked()
        throws Exception {
        int count = 0;
        while (count != NUM_THREADS) {
            goSleep(100);
            count = 0;
            for (int i = 0; i < NUM_THREADS; i++) {
                ThreadInfo info = mbean.getThreadInfo(threads[i].getId());
                if (info.getThreadState() == Thread.State.WAITING) {
                    count++;
                }
            }
        }
    }
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        public void run() {
            double sum = 0;
            for (int i = 0; i < 5000; i++) {
               double r = Math.random();
               double x = Math.pow(3, r);
               sum += x - r;
            }
            synchronized (obj) {
                while (!done) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Unexpected exception is thrown.");
                        e.printStackTrace(System.out);
                        testFailed = true;
                        break;
                    }
                }
            }
            sum = 0;
            for (int i = 0; i < 5000; i++) {
               double r = Math.random();
               double x = Math.pow(3, r);
               sum += x - r;
            }
            long utime1 = mbean.getCurrentThreadUserTime();
            long utime2 = mbean.getThreadUserTime(getId());
            long time1 = mbean.getCurrentThreadCpuTime();
            long time2 = mbean.getThreadCpuTime(getId());
            System.out.println(getName() + ": " +
                "CurrentThreadUserTime = " + utime1 +
                " ThreadUserTime = " + utime2);
            System.out.println(getName() + ": " +
                "CurrentThreadCpuTime  = " + time1 +
                " ThreadCpuTime  = " + time2);
            if (time1 > time2) {
                throw new RuntimeException("TEST FAILED: " + getName() +
                    " CurrentThreadCpuTime = " + time1 +
                    " > ThreadCpuTime = " + time2);
            }
        }
    }
}
