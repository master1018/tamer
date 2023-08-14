public class Locks {
    private static Object objA = new Object();
    private static Object objB = new Object();
    private static Object objC = new Object();
    private static ThreadMXBean tm = ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static String getLockName(Object lock) {
        if (lock == null) return null;
        return lock.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(lock));
    }
    private static void checkBlockedObject(Thread t, Object lock, Thread owner,
                                           Thread.State expectedState) {
        ThreadInfo info = tm.getThreadInfo(t.getId());
        String result = info.getLockName();
        String expectedLock = (lock != null ? getLockName(lock) : null);
        String expectedOwner = (owner != null ? owner.getName() : null);
        if (lock != null) {
            if (expectedState ==Thread.State.BLOCKED) {
                int retryCount=0;
                while(info.getThreadState() != Thread.State.BLOCKED) {
                    if (retryCount++ > 500) {
                        throw new RuntimeException("Thread " + t.getName() +
                                  " is expected to block on " + expectedLock +
                                  " but got " + result +
                                  " Thread.State = " + info.getThreadState());
                    }
                    goSleep(100);
                }
            }
            if (expectedState == Thread.State.WAITING &&
                info.getThreadState() != Thread.State.WAITING) {
                throw new RuntimeException("Thread " + t.getName() +
                    " is expected to wait on " + expectedLock +
                    " but got " + result +
                    " Thread.State = " + info.getThreadState());
            }
        }
        if ((result != null && !result.equals(expectedLock)) ||
            (result == null && expectedLock != null)) {
            throw new RuntimeException("Thread " + t.getName() + " is blocked on " +
                expectedLock + " but got " + result);
        }
        result = info.getLockOwnerName();
        if ((result != null && !result.equals(expectedOwner)) ||
            (result == null && expectedOwner != null)) {
            throw new RuntimeException("Owner of " + lock + " should be " +
                expectedOwner + " but got " + result);
        }
    }
    private static void goSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
            testFailed = true;
        }
    }
    static ThreadExecutionSynchronizer thrsync = new ThreadExecutionSynchronizer();
    static ThreadExecutionSynchronizer thrsync1 = new ThreadExecutionSynchronizer();
    static class LockAThread extends Thread {
        public LockAThread() {
            super("LockAThread");
        }
        public void run() {
            synchronized(objA) {
               thrsync.waitForSignal();
               System.out.println("LockAThread about to block on objB");
               synchronized(objB) {};
            }
            System.out.println("LockAThread about to exit");
            checkBlockedObject(this, null, null, Thread.State.TERMINATED);
        }
    }
    static class LockBThread extends Thread {
        public LockBThread() {
            super("LockBThread");
        }
        public void run() {
            synchronized(objB) {
               thrsync.signal();
               System.out.println("LockBThread about to block on objC");
               thrsync1.signal();
               synchronized(objC) {};
            }
            System.out.println("LockBThread about to exit");
            checkBlockedObject(this, null, null, Thread.State.TERMINATED);
        }
        public void aboutToLockC() {
            thrsync1.waitForSignal();
            goSleep(500);
        }
    }
    private static WaitingThread waiter;
    private static Object ready = new Object();
    private static CheckerThread checker;
    static class WaitingThread extends Thread {
        public WaitingThread() {
            super("WaitingThread");
        }
        public void run() {
            synchronized(objC) {
               System.out.println("WaitingThread about to wait on objC");
               try {
                   thrsync.signal();
                   objC.wait();
               } catch (InterruptedException e) {
                   e.printStackTrace();
                   testFailed = true;
               }
               System.out.println("WaitingThread about to block on ready");
               thrsync.signal();
               synchronized(ready) {};
            }
            synchronized(objC) {
                try {
                    thrsync.signal();
                    objC.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    testFailed = true;
                }
            }
            System.out.println("WaitingThread about to exit waiting on objC 2");
        }
    }
    static class CheckerThread extends Thread {
        public CheckerThread() {
            super("CheckerThread");
        }
        public void run() {
            synchronized (ready) {
                thrsync.waitForSignal();
                int retryCount = 0;
                while (waiter.getState() != Thread.State.WAITING
                       && retryCount++ < 500) {
                   goSleep(100);
                }
                checkBlockedObject(waiter, objC, null, Thread.State.WAITING);
                synchronized (objC) {
                    objC.notify();
                }
                thrsync.waitForSignal();
                goSleep(50);
                checkBlockedObject(waiter, ready, this, Thread.State.BLOCKED);
            }
            thrsync.waitForSignal();
            synchronized(objC) {
                checkBlockedObject(waiter, objC, Thread.currentThread(), Thread.State.WAITING);
                objC.notify();
            }
        }
    }
    public static void main(String args[]) throws Exception {
        Thread mainThread = Thread.currentThread();
        LockAThread t1;
        LockBThread t2;
        synchronized(objC) {
            checkBlockedObject(mainThread, null, null, Thread.State.RUNNABLE);
            t1 = new LockAThread();
            t1.start();
            t2 = new LockBThread();
            t2.start();
            t2.aboutToLockC();
            checkBlockedObject(t1, objB, t2, Thread.State.BLOCKED);
            checkBlockedObject(t2, objC, mainThread, Thread.State.BLOCKED);
            long[] expectedThreads = new long[3];
            expectedThreads[0] = t1.getId(); 
            expectedThreads[1] = t2.getId(); 
            expectedThreads[2] = mainThread.getId(); 
            findThreadsBlockedOn(objB, expectedThreads);
        }
        goSleep(100);
        waiter = new WaitingThread();
        waiter.start();
        checker = new CheckerThread();
        checker.start();
        try {
            waiter.join();
            checker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            testFailed = true;
        }
        if (testFailed) {
            throw new RuntimeException("TEST FAILED.");
        }
        System.out.println("Test passed.");
    }
    private static ThreadInfo findOwnerInfo(ThreadInfo[] infos, String lock)
        throws Exception {
        ThreadInfo ownerInfo = null;
        for (int i = 0; i < infos.length; i++) {
            String blockedLock = infos[i].getLockName();
            if (lock.equals(blockedLock)) {
                long threadId = infos[i].getLockOwnerId();
                if (threadId == -1) {
                    throw new RuntimeException("TEST FAILED: " +
                        lock + " expected to have owner");
                }
                for (int j = 0; j < infos.length; j++) {
                    if (infos[j].getThreadId() == threadId) {
                        ownerInfo = infos[j];
                        break;
                    }
                }
            }
        }
        return ownerInfo;
    }
    private static void findThreadsBlockedOn(Object o, long[] expectedThreads)
        throws Exception {
        String lock = getLockName(o);
        ThreadInfo[] infos = tm.getThreadInfo(tm.getAllThreadIds());
        doCheck(infos, lock, expectedThreads);
        infos = tm.getThreadInfo(tm.getAllThreadIds(), 1);
        doCheck(infos, lock, expectedThreads);
    }
    private static void doCheck(ThreadInfo[] infos, String lock, long[] expectedThreads)
        throws Exception {
        ThreadInfo ownerInfo = null;
        for (int i = 0; i < infos.length;  i++) {
            String blockedLock = infos[i].getLockName();
            if (lock.equals(blockedLock)) {
                System.out.print(infos[i].getThreadName() +
                    " blocked on " + blockedLock);
                ownerInfo = infos[i];
            }
        }
        long[] threads = new long[10];
        int count = 0;
        threads[count++] = ownerInfo.getThreadId();
        while (ownerInfo != null && ownerInfo.getThreadState() == Thread.State.BLOCKED) {
            ownerInfo = findOwnerInfo(infos, lock);
            threads[count++] = ownerInfo.getThreadId();
            System.out.println(" Owner = " + ownerInfo.getThreadName() +
                " id = " + ownerInfo.getThreadId());
            lock = ownerInfo.getLockName();
            System.out.print(ownerInfo.getThreadName() + " Id = " +
                    ownerInfo.getThreadId() +
                    " blocked on " + lock);
        }
        System.out.println();
        if (count != expectedThreads.length) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected chain of threads not matched; current count =" + count);
        }
        for (int i = 0; i < count; i++) {
            if (threads[i] != expectedThreads[i]) {
                System.out.println("TEST FAILED: " +
                    "Unexpected thread in the chain " + threads[i] +
                    " expected to be " + expectedThreads[i]);
            }
        }
    }
}
