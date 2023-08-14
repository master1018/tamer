public class LoggingDeadlock4 {
    private static CountDownLatch barrier      = new CountDownLatch(1);
    private static CountDownLatch lmIsRunning  = new CountDownLatch(1);
    private static CountDownLatch logIsRunning = new CountDownLatch(1);
    public static void main(String[] args) {
        System.out.println("main: LoggingDeadlock4 is starting.");
        try {
            Class.forName("java.awt.Container");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Test failed: could not load"
                + " java.awt.Container." + cnfe);
        }
        Thread lmThread = new Thread("LogManagerThread") {
            public void run() {
                lmIsRunning.countDown();
                System.out.println(Thread.currentThread().getName()
                    + ": is running.");
                try {
                    barrier.await();  
                } catch (InterruptedException e) {
                }
                LogManager manager = LogManager.getLogManager();
            }
        };
        lmThread.start();
        Thread logThread = new Thread("LoggerThread") {
            public void run() {
                logIsRunning.countDown();
                System.out.println(Thread.currentThread().getName()
                    + ": is running.");
                try {
                    barrier.await();  
                } catch (InterruptedException e) {
                }
                Logger foo = Logger.getLogger("foo logger");
            }
        };
        logThread.start();
        try {
            lmIsRunning.await();
            logIsRunning.await();
        } catch (InterruptedException e) {
        }
        barrier.countDown();  
        try {
            lmThread.join();
            logThread.join();
        } catch (InterruptedException ie) {
        }
        System.out.println("main: LoggingDeadlock4 is done.");
    }
}
