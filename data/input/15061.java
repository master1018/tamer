public class LoggingDeadlock {
    public static void randomDelay() {
        int runs = (int) Math.random() * 1000000;
        int c = 0;
        for (int i = 0; i < runs; ++i) {
            c = c + i;
        }
    }
    public static void main(String[] args) throws InterruptedException{
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                randomDelay();
                Logger.getAnonymousLogger();
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                randomDelay();
                LogManager.getLogManager();
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("\nTest passed");
    }
}
