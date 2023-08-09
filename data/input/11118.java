public class SynchronizerDeadlock {
    private Lock a = new ReentrantLock();
    private Lock b = new ReentrantLock();
    private Lock c = new ReentrantLock();
    private final int EXPECTED_THREADS = 3;
    private Thread[] dThreads = new Thread[EXPECTED_THREADS];
    private Barrier go = new Barrier(1);
    private Barrier barr = new Barrier(EXPECTED_THREADS);
    public SynchronizerDeadlock() {
        dThreads[0] = new DeadlockingThread("Deadlock-Thread-1", a, b);
        dThreads[1] = new DeadlockingThread("Deadlock-Thread-2", b, c);
        dThreads[2] = new DeadlockingThread("Deadlock-Thread-3", c, a);
        for (int i = 0; i < EXPECTED_THREADS; i++) {
            dThreads[i].setDaemon(true);
            dThreads[i].start();
        }
    }
    void goDeadlock() {
        barr.await();
        barr.set(EXPECTED_THREADS);
        while (go.getWaiterCount() != EXPECTED_THREADS) {
            synchronized(this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        go.signal();
    }
    void waitUntilDeadlock() {
        barr.await();
        for (int i=0; i < 100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            boolean retry = false;
            for (Thread t: dThreads) {
                if (t.getState() == Thread.State.RUNNABLE) {
                    retry = true;
                    break;
                }
            }
            if (!retry) {
                break;
            }
        }
    }
    private class DeadlockingThread extends Thread {
        private final Lock lock1;
        private final Lock lock2;
        DeadlockingThread(String name, Lock lock1, Lock lock2) {
            super(name);
            this.lock1 = lock1;
            this.lock2 = lock2;
        }
        public void run() {
            f();
        }
        private void f() {
            lock1.lock();
            try {
                barr.signal();
                go.await();
                g();
            } finally {
                lock1.unlock();
            }
        }
        private void g() {
            barr.signal();
            lock2.lock();
            throw new RuntimeException("should not reach here.");
        }
    }
    void checkResult(long[] threads) {
        if (threads.length != EXPECTED_THREADS) {
            ThreadDump.threadDump();
            throw new RuntimeException("Expected to have " +
                EXPECTED_THREADS + " to be in the deadlock list");
        }
        boolean[] found = new boolean[EXPECTED_THREADS];
        for (int i = 0; i < threads.length; i++) {
            for (int j = 0; j < dThreads.length; j++) {
                if (dThreads[j].getId() == threads[i]) {
                    found[j] = true;
                }
            }
        }
        boolean ok = true;
        for (int j = 0; j < found.length; j++) {
            ok = ok && found[j];
        }
        if (!ok) {
            System.out.print("Returned result is [");
            for (int j = 0; j < threads.length; j++) {
                System.out.print(threads[j] + " ");
            }
            System.out.println("]");
            System.out.print("Expected result is [");
            for (int j = 0; j < threads.length; j++) {
                System.out.print(dThreads[j] + " ");
            }
            System.out.println("]");
            throw new RuntimeException("Unexpected result returned " +
                " by findMonitorDeadlockedThreads method.");
        }
    }
}
