public class ThreadAllocatedMemoryArray {
    private static com.sun.management.ThreadMXBean mbean =
        (com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static boolean done = false;
    private static boolean done1 = false;
    private static Object obj = new Object();
    private static final int NUM_THREADS = 10;
    private static Thread[] threads = new Thread[NUM_THREADS];
    public static void main(String[] argv)
        throws Exception {
        if (!mbean.isThreadAllocatedMemorySupported()) {
            return;
        }
        long[] ids = new long[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new MyThread("MyThread-" + i);
            threads[i].start();
            ids[i] = threads[i].getId();
        }
        waitUntilThreadBlocked();
        if (mbean.isThreadAllocatedMemoryEnabled()) {
            mbean.setThreadAllocatedMemoryEnabled(false);
        }
        if (mbean.isThreadAllocatedMemoryEnabled()) {
            throw new RuntimeException(
                "ThreadAllocatedMemory is expected to be disabled");
        }
        long sizes[] = mbean.getThreadAllocatedBytes(ids);
        if (sizes == null) {
          throw new RuntimeException("Null ThreadAllocatedBytes array returned");
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            long s = sizes[i];
            if (s != -1) {
                throw new RuntimeException(
                    "Invalid ThreadAllocatedBytes returned for thread " +
                    threads[i].getName() + " = " +  s + " expected = -1");
            }
        }
        if (!mbean.isThreadAllocatedMemoryEnabled()) {
            mbean.setThreadAllocatedMemoryEnabled(true);
        }
        if (!mbean.isThreadAllocatedMemoryEnabled()) {
            throw new RuntimeException(
                "ThreadAllocatedMemory is expected to be enabled");
        }
        sizes = mbean.getThreadAllocatedBytes(ids);
        for (int i = 0; i < NUM_THREADS; i++) {
            long s = sizes[i];
            if (s < 0) {
                throw new RuntimeException(
                    "Invalid allocated bytes returned for thread " +
                    threads[i].getName() + " = " + s);
            }
        }
        synchronized (obj) {
            done = true;
            obj.notifyAll();
        }
        goSleep(400);
        long[] sizes1 = mbean.getThreadAllocatedBytes(ids);
        for (int i = 0; i < NUM_THREADS; i++) {
            long newSize = sizes1[i];
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
        try {
            sizes = mbean.getThreadAllocatedBytes(null);
        } catch (NullPointerException e) {
            System.out.println(
                "Caught expected NullPointerException: " + e.getMessage());
        }
        try {
            ids[0] = 0;
            sizes = mbean.getThreadAllocatedBytes(ids);
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Caught expected IllegalArgumentException: " + e.getMessage());
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
            ThreadAllocatedMemoryArray.doit();
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
            ThreadAllocatedMemoryArray.doit();
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
