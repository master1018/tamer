public class Test6588003 implements Runnable {
    private static final LayoutQueue DEFAULT = new LayoutQueue();
    public static void main(String[] args) throws InterruptedException {
        LayoutQueue.setDefaultQueue(DEFAULT);
        ThreadGroup group = new ThreadGroup("Test6588003");
        Thread thread = new Thread(group, new Test6588003());
        thread.start();
        thread.join();
        if (LayoutQueue.getDefaultQueue() != DEFAULT) {
            throw new RuntimeException("Sharing detected");
        }
    }
    public void run() {
        SunToolkit.createNewAppContext();
        if (LayoutQueue.getDefaultQueue() == DEFAULT) {
            throw new RuntimeException("Sharing detected");
        }
        LayoutQueue.setDefaultQueue(new LayoutQueue());
    }
}
