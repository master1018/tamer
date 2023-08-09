public class Deadlock {
    public static void main(String[] argv) {
        new Deadlock();
        ThreadMonitor monitor = new ThreadMonitor();
        boolean found = false;
        while (!found) {
            found = monitor.findDeadlock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.exit(1);
            }
        }
        System.out.println("\nPress <Enter> to exit this Deadlock program.\n");
        waitForEnterPressed();
    }
    private CyclicBarrier barrier = new CyclicBarrier(6);
    public Deadlock() {
        DeadlockThread[] dThreads = new DeadlockThread[6];
        Monitor a = new Monitor("a");
        Monitor b = new Monitor("b");
        Monitor c = new Monitor("c");
        dThreads[0] = new DeadlockThread("MThread-1", a, b);
        dThreads[1] = new DeadlockThread("MThread-2", b, c);
        dThreads[2] = new DeadlockThread("MThread-3", c, a);
        Lock d = new ReentrantLock();
        Lock e = new ReentrantLock();
        Lock f = new ReentrantLock();
        dThreads[3] = new DeadlockThread("SThread-4", d, e);
        dThreads[4] = new DeadlockThread("SThread-5", e, f);
        dThreads[5] = new DeadlockThread("SThread-6", f, d);
        for (int i = 0; i < 6; i++) {
            dThreads[i].setDaemon(true);
            dThreads[i].start();
        }
    }
    class DeadlockThread extends Thread {
        private Lock lock1 = null;
        private Lock lock2 = null;
        private Monitor mon1 = null;
        private Monitor mon2 = null;
        private boolean useSync;
        DeadlockThread(String name, Lock lock1, Lock lock2) {
            super(name);
            this.lock1 = lock1;
            this.lock2 = lock2;
            this.useSync = true;
        }
        DeadlockThread(String name, Monitor mon1, Monitor mon2) {
            super(name);
            this.mon1 = mon1;
            this.mon2 = mon2;
            this.useSync = false;
        }
        @Override
        public void run() {
            if (useSync) {
                syncLock();
            } else {
                monitorLock();
            }
        }
        private void syncLock() {
            lock1.lock();
            try {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                goSyncDeadlock();
            } finally {
                lock1.unlock();
            }
        }
        private void goSyncDeadlock() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
                System.exit(1);
            }
            lock2.lock();
            throw new RuntimeException("should not reach here.");
        }
        private void monitorLock() {
            synchronized (mon1) {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                goMonitorDeadlock();
            }
        }
        private void goMonitorDeadlock() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
                System.exit(1);
            }
            synchronized (mon2) {
                throw new RuntimeException(getName() + " should not reach here.");
            }
        }
    }
    class Monitor {
        String name;
        Monitor(String name) {
            this.name = name;
        }
    }
    private static void waitForEnterPressed() {
        try {
            boolean done = false;
            while (!done) {
                char ch = (char) System.in.read();
                if (ch<0||ch=='\n') {
                    done = true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
