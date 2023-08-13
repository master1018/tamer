public class ThreadExecutionSynchronizer {
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
        goSleep(50);
    }
    void signal() {
        stopOrGo();
        goSleep(50);
    }
    private static void goSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Unexpected exception.");
        }
    }
}
