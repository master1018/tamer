public class SharedSynchronizer {
    public static void main(String[] args) throws Exception {
        MyThread t = new MyThread();
        t.setDaemon(true);
        t.start();
        ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
        if (!tmbean.isSynchronizerUsageSupported()) {
            System.out.println("Monitoring of synchronizer usage not supported")
;
            return;
        }
        long[] result = tmbean.findDeadlockedThreads();
        if (result != null) {
             throw new RuntimeException("TEST FAILED: result should be null");
        }
    }
    static class MyThread extends Thread {
        public void run() {
            FutureTask f = new FutureTask(
                new Callable() {
                    public Object call() {
                        throw new RuntimeException("should never reach here");
                    }
                }
            );
            try {
                f.get();
            } catch (Exception e) {
                RuntimeException re = new RuntimeException(e.getMessage());
                re.initCause(e);
                throw re;
            }
        }
    }
}
