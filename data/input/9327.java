public class ThreadStackTrace {
    private static ThreadMXBean mbean
        = ManagementFactory.getThreadMXBean();
    private static boolean notified = false;
    private static Object lockA = new Object();
    private static Object lockB = new Object();
    private static volatile boolean testFailed = false;
    private static String[] blockedStack = {"run", "test", "A", "B", "C", "D"};
    private static int bsDepth = 6;
    private static int methodB = 4;
    private static String[] examinerStack = {"run", "examine1", "examine2"};
    private static int esDepth = 3;
    private static int methodExamine1= 2;
    private static void checkNullThreadInfo(Thread t) throws Exception {
        ThreadInfo ti = mbean.getThreadInfo(t.getId());
        if (ti != null) {
            ThreadInfo info =
                mbean.getThreadInfo(t.getId(), Integer.MAX_VALUE);
            System.out.println(INDENT + "TEST FAILED:");
            if (info != null) {
                printStack(t, info.getStackTrace());
                System.out.println(INDENT + "Thread state: " + info.getThreadState());
            }
            throw new RuntimeException("TEST FAILED: " +
                "getThreadInfo() is expected to return null for " + t);
        }
    }
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        Examiner examiner = new Examiner("Examiner");
        BlockedThread blocked = new BlockedThread("BlockedThread");
        examiner.setThread(blocked);
        checkNullThreadInfo(examiner);
        checkNullThreadInfo(blocked);
        examiner.start();
        examiner.waitForStarted();
        System.out.println("Checking stack trace for the examiner thread " +
                           "is waiting to begin.");
        Utils.checkThreadState(examiner, Thread.State.WAITING);
        checkStack(examiner, examinerStack, esDepth);
        System.out.println("Now starting the blocked thread");
        blocked.start();
        try {
            examiner.join();
            blocked.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Unexpected exception.");
            testFailed = true;
        }
        checkNullThreadInfo(examiner);
        checkNullThreadInfo(blocked);
        if (testFailed)
            throw new RuntimeException("TEST FAILED.");
        System.out.println("Test passed.");
    }
    private static String INDENT = "    ";
    private static void printStack(Thread t, StackTraceElement[] stack) {
        System.out.println(INDENT +  t +
                           " stack: (length = " + stack.length + ")");
        if (t != null) {
            for (int j = 0; j < stack.length; j++) {
                System.out.println(INDENT + stack[j]);
            }
            System.out.println();
        }
    }
    private static void checkStack(Thread t, String[] expectedStack,
                                   int depth) throws Exception {
        ThreadInfo ti = mbean.getThreadInfo(t.getId(), Integer.MAX_VALUE);
        StackTraceElement[] stack = ti.getStackTrace();
        if (trace) {
            printStack(t, stack);
        }
        int frame = stack.length - 1;
        for (int i = 0; i < depth; i++) {
            if (! stack[frame].getMethodName().equals(expectedStack[i])) {
                throw new RuntimeException("TEST FAILED: " +
                    "Expected " + expectedStack[i] + " in frame " + frame +
                    " but got " + stack[frame].getMethodName());
            }
            frame--;
        }
    }
    static class BlockedThread extends Thread {
        private Semaphore handshake = new Semaphore();
        BlockedThread(String name) {
            super(name);
        }
        boolean hasWaitersForBlocked() {
            return (handshake.getWaiterCount() > 0);
        }
        void waitUntilBlocked() {
            handshake.semaP();
            Utils.goSleep(20);
        }
        void waitUntilLockAReleased() {
            handshake.semaP();
            Utils.goSleep(50);
        }
        private void notifyWaiter() {
            while (handshake.getWaiterCount() == 0) {
                Utils.goSleep(20);
            }
            handshake.semaV();
        }
        private void test() {
            A();
        }
        private void A() {
            B();
        }
        private void B() {
            C();
            notifyWaiter();
            synchronized (lockB) {
            };
        }
        private void C() {
            D();
        }
        private void D() {
            notifyWaiter();
            synchronized (lockA) {
                notified = false;
                while (!notified) {
                    try {
                        notifyWaiter();
                        lockA.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Unexpected exception.");
                        testFailed = true;
                    }
                }
                System.out.println("BlockedThread notified");
            }
        }
        public void run() {
            test();
        } 
    } 
    static class Examiner extends Thread {
        private static BlockedThread blockedThread;
        private Semaphore handshake = new Semaphore();
        Examiner(String name) {
            super(name);
        }
        public void setThread(BlockedThread thread) {
            blockedThread = thread;
        }
        public synchronized void waitForStarted() {
            handshake.semaP();
            while (!blockedThread.hasWaitersForBlocked()) {
                Utils.goSleep(50);
            }
            Utils.goSleep(20);
        }
        private Thread itself;
        private void examine1() {
            synchronized (lockB) {
                examine2();
                try {
                    System.out.println("Checking examiner's its own stack trace");
                    Utils.checkThreadState(itself, Thread.State.RUNNABLE);
                    checkStack(itself, examinerStack, methodExamine1);
                    blockedThread.waitUntilBlocked();
                    System.out.println("Checking stack trace for " +
                        "BlockedThread - should be blocked on lockB.");
                    Utils.checkThreadState(blockedThread, Thread.State.BLOCKED);
                    checkStack(blockedThread, blockedStack, methodB);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Unexpected exception.");
                    testFailed = true;
                }
            }
        }
        private void examine2() {
            synchronized (lockA) {
                while (handshake.getWaiterCount() == 0) {
                    Utils.goSleep(20);
                }
                handshake.semaV();  
                try {
                    blockedThread.waitUntilBlocked();
                    System.out.println("Checking examiner's its own stack trace");
                    Utils.checkThreadState(itself, Thread.State.RUNNABLE);
                    checkStack(itself, examinerStack, esDepth);
                    System.out.println("Checking stack trace for " +
                        "BlockedThread - should be blocked on lockA.");
                    Utils.checkThreadState(blockedThread, Thread.State.BLOCKED);
                    checkStack(blockedThread, blockedStack, bsDepth);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Unexpected exception.");
                    testFailed = true;
                }
            }
            blockedThread.waitUntilLockAReleased();
            synchronized (lockA) {
                try {
                    System.out.println("Checking stack trace for " +
                        "BlockedThread - should be waiting on lockA.");
                    Utils.checkThreadState(blockedThread, Thread.State.WAITING);
                    checkStack(blockedThread, blockedStack, bsDepth);
                    notified = true;
                    lockA.notify();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Unexpected exception.");
                    testFailed = true;
                }
            }
            Utils.goSleep(50);
        } 
        public void run() {
            itself = Thread.currentThread();
            examine1();
        } 
    } 
}
