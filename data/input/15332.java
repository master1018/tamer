public class ThreadAllocatedMemory {
    private static com.sun.management.ThreadMXBean mbean =
        (com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static boolean done = false;
    private static boolean done1 = false;
    private static Object obj = new Object();
    private static final int NUM_THREADS = 10;
    private static Thread[] threads = new Thread[NUM_THREADS];
    private static long[] sizes = new long[NUM_THREADS];
    public static void main(String[] argv)
        throws Exception {
        if (!mbean.isThreadAllocatedMemorySupported()) {
            return;
        }
        if (mbean.isThreadAllocatedMemoryEnabled()) {
            mbean.setThreadAllocatedMemoryEnabled(false);
        }
        if (mbean.isThreadAllocatedMemoryEnabled()) {
            throw new RuntimeException(
                "ThreadAllocatedMemory is expected to be disabled");
        }
        Thread curThread = Thread.currentThread();
        long id = curThread.getId();
        long s = mbean.getThreadAllocatedBytes(id);
        if (s != -1) {
            throw new RuntimeException(
                "Invalid ThreadAllocatedBytes returned = " +
                s + " expected = -1");
        }
        if (!mbean.isThreadAllocatedMemoryEnabled()) {
            mbean.setThreadAllocatedMemoryEnabled(true);
        }
        if (!mbean.isThreadAllocatedMemoryEnabled()) {
            throw new RuntimeException(
                "ThreadAllocatedMemory is expected to be enabled");
        }
        long size = mbean.getThreadAllocatedBytes(id);
        if (size < 0) {
            throw new RuntimeException(
                "Invalid allocated bytes returned = " + size);
        }
        doit();
        long size1 = mbean.getThreadAllocatedBytes(id);
        if (size1 < size) {
            throw new RuntimeException("Allocated bytes " + size1 +
                " expected >= " + size);
        }
        System.out.println(curThread.getName() +
            " Current thread allocated bytes = " + size +
            " allocated bytes = " + size1);
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new MyThread("MyThread-" + i);
            threads[i].start();
        }
        waitUntilThreadBlocked();
        for (int i = 0; i < NUM_THREADS; i++) {
            sizes[i] = mbean.getThreadAllocatedBytes(threads[i].getId());
        }
        synchronized (obj) {
            done = true;
            obj.notifyAll();
        }
        goSleep(400);
        for (int i = 0; i < NUM_THREADS; i++) {
            long newSize = mbean.getThreadAllocatedBytes(threads[i].getId());
            if (sizes[i] > newSize) {
                throw new RuntimeException("TEST FAILED: " +
                    threads[i].getName() +
                    " previous allocated bytes = " + sizes[i] +
                    " > current allocated bytes = " + newSize);
            }
            System.out.println(threads[i].getName() +
                " Previous allocated bytes = " + sizes[i] +
                " Current allocated bytes = " + newSize);
        }
        synchronized (obj) {
            done1 = true;
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
    public static void doit() {
        String tmp = "";
        long hashCode = 0;
        for (int counter = 0; counter < 1000; counter++) {
            tmp += counter;
            hashCode = tmp.hashCode();
        }
        System.out.println(Thread.currentThread().getName() +
                           " hashcode: " + hashCode);
    }
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        public void run() {
            ThreadAllocatedMemory.doit();
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
            long size1 = mbean.getThreadAllocatedBytes(getId());
            ThreadAllocatedMemory.doit();
            long size2 = mbean.getThreadAllocatedBytes(getId());
            System.out.println(getName() + ": " +
                "ThreadAllocatedBytes  = " + size1 +
                " ThreadAllocatedBytes  = " + size2);
            if (size1 > size2) {
                throw new RuntimeException("TEST FAILED: " + getName() +
                    " ThreadAllocatedBytes = " + size1 +
                    " > ThreadAllocatedBytes = " + size2);
            }
            synchronized (obj) {
                while (!done1) {
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
        }
    }
}
