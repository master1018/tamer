public class ThreadStateTest {
    private static boolean testFailed = false;
    static class Lock {
        private String name;
        Lock(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
    private static Lock globalLock = new Lock("my lock");
    public static void main(String[] argv) {
        Thread.currentThread().getState();
        MyThread myThread = new MyThread("MyThread");
        checkThreadState(myThread, Thread.State.NEW);
        myThread.start();
        myThread.waitUntilStarted();
        checkThreadState(myThread, Thread.State.RUNNABLE);
        synchronized (globalLock) {
            myThread.goBlocked();
            checkThreadState(myThread, Thread.State.BLOCKED);
        }
        myThread.goWaiting();
        checkThreadState(myThread, Thread.State.WAITING);
        myThread.goTimedWaiting();
        checkThreadState(myThread, Thread.State.TIMED_WAITING);
        myThread.goSleeping();
        checkThreadState(myThread, Thread.State.TIMED_WAITING);
        myThread.terminate();
        checkThreadState(myThread, Thread.State.TERMINATED);
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Unexpected exception.");
            testFailed = true;
        }
        if (testFailed)
            throw new RuntimeException("TEST FAILED.");
        System.out.println("Test passed.");
    }
    private static void checkThreadState(Thread t, Thread.State expected) {
        Thread.State state = t.getState();
        System.out.println("Checking thread state " + state);
        if (state == null) {
            throw new RuntimeException(t.getName() + " expected to have " +
                expected + " but got null.");
        }
        if (state != expected) {
            throw new RuntimeException(t.getName() + " expected to have " +
                expected + " but got " + state);
        }
    }
    private static String getLockName(Object lock) {
        if (lock == null) return null;
        return lock.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(lock));
    }
    private static void goSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Unexpected exception.");
            testFailed = true;
        }
    }
    static class MyThread extends Thread {
        private ThreadExecutionSynchronizer thrsync = new ThreadExecutionSynchronizer();
        MyThread(String name) {
            super(name);
        }
        private final int RUNNABLE = 0;
        private final int BLOCKED = 1;
        private final int WAITING = 2;
        private final int TIMED_WAITING = 3;
        private final int PARKED = 4;
        private final int TIMED_PARKED = 5;
        private final int SLEEPING = 6;
        private final int TERMINATE = 7;
        private int state = RUNNABLE;
        private boolean done = false;
        public void run() {
            thrsync.signal();
            while (!done) {
                switch (state) {
                    case RUNNABLE: {
                        double sum = 0;
                        for (int i = 0; i < 1000; i++) {
                           double r = Math.random();
                           double x = Math.pow(3, r);
                           sum += x - r;
                        }
                        break;
                    }
                    case BLOCKED: {
                        thrsync.signal();
                        System.out.println("  myThread is going to block.");
                        synchronized (globalLock) {
                            state = RUNNABLE;
                        }
                        break;
                    }
                    case WAITING: {
                        synchronized (globalLock) {
                            thrsync.signal();
                            System.out.println("  myThread is going to wait.");
                            try {
                                globalLock.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case TIMED_WAITING: {
                        synchronized (globalLock) {
                            thrsync.signal();
                            System.out.println("  myThread is going to timed wait.");
                            try {
                                globalLock.wait(10000);
                            } catch (InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case PARKED: {
                        thrsync.signal();
                        System.out.println("  myThread is going to park.");
                        LockSupport.park();
                        goSleep(10);
                        break;
                    }
                    case TIMED_PARKED: {
                        thrsync.signal();
                        System.out.println("  myThread is going to timed park.");
                        long deadline = System.currentTimeMillis() + 10000*1000;
                        LockSupport.parkUntil(deadline);
                        goSleep(10);
                        break;
                    }
                    case SLEEPING: {
                        thrsync.signal();
                        System.out.println("  myThread is going to sleep.");
                        try {
                            Thread.sleep(1000000);
                        } catch (InterruptedException e) {
                            interrupted();
                        }
                        break;
                    }
                    case TERMINATE: {
                        done = true;
                        thrsync.signal();
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        public void waitUntilStarted() {
            thrsync.waitForSignal();
            goSleep(10);
        }
        public void goBlocked() {
            System.out.println("Waiting myThread to go blocked.");
            setState(BLOCKED);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void goWaiting() {
            System.out.println("Waiting myThread to go waiting.");
            setState(WAITING);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void goTimedWaiting() {
            System.out.println("Waiting myThread to go timed waiting.");
            setState(TIMED_WAITING);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void goParked() {
            System.out.println("Waiting myThread to go parked.");
            setState(PARKED);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void goTimedParked() {
            System.out.println("Waiting myThread to go timed parked.");
            setState(TIMED_PARKED);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void goSleeping() {
            System.out.println("Waiting myThread to go sleeping.");
            setState(SLEEPING);
            thrsync.waitForSignal();
            goSleep(20);
        }
        public void terminate() {
            System.out.println("Waiting myThread to terminate.");
            setState(TERMINATE);
            thrsync.waitForSignal();
            goSleep(20);
        }
        private void setState(int newState) {
            switch (state) {
                case BLOCKED:
                    while (state == BLOCKED) {
                        goSleep(20);
                    }
                    state = newState;
                    break;
                case WAITING:
                case TIMED_WAITING:
                    state = newState;
                    synchronized (globalLock) {
                        globalLock.notify();
                    }
                    break;
                case PARKED:
                case TIMED_PARKED:
                    state = newState;
                    LockSupport.unpark(this);
                    break;
                case SLEEPING:
                    state = newState;
                    this.interrupt();
                    break;
                default:
                    state = newState;
                    break;
            }
        }
    }
    static class ThreadExecutionSynchronizer {
        private boolean  waiting;
        private Semaphore semaphore;
        public ThreadExecutionSynchronizer() {
            semaphore = new Semaphore(1);
        waiting = false;
        }
        void stopOrGo() {
        semaphore.acquireUninterruptibly(); 
        if (!waiting) {
            waiting = true;
            while(!semaphore.hasQueuedThreads()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException xx) {}
            }
            semaphore.release();
        } else {
            waiting = false;
            semaphore.release();
        }
        }
        void waitForSignal() {
        stopOrGo();
        }
        void signal() {
        stopOrGo();
        }
    }
}
