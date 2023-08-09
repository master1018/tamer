public class Test6963811 implements Runnable {
    private static final Encoder ENCODER = new Encoder();
    private final long time;
    private final boolean sync;
    public Test6963811(long time, boolean sync) {
        this.time = time;
        this.sync = sync;
    }
    public void run() {
        try {
            Thread.sleep(this.time); 
            if (this.sync) {
                synchronized (Test6963811.class) {
                    ENCODER.getPersistenceDelegate(Super.class);
                }
            }
            else {
                ENCODER.getPersistenceDelegate(Sub.class);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        Thread[] threads = new Thread[2];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Test6963811(0L, i > 0));
            threads[i].start();
            Thread.sleep(500L); 
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
    public static class Super {
    }
    public static class Sub extends Super {
    }
    public static class SubPersistenceDelegate extends DefaultPersistenceDelegate {
        public SubPersistenceDelegate() {
            new Test6963811(1000L, true).run();
        }
    }
}
