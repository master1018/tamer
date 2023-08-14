public class Stop implements Runnable {
    private static Thread first=null;
    private static Thread second=null;
    private static ThreadGroup group = new ThreadGroup("");
    Stop() {
        Thread thread = new Thread(group, this);
        if (first == null)
            first = thread;
        else
            second = thread;
        thread.start();
    }
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); 
                if (Thread.currentThread() == first)
                    group.stop();
            } catch(InterruptedException e){
            }
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i=0; i<2; i++)
            new Stop();
        Thread.sleep(3000);
        boolean failed = second.isAlive();
        first.stop(); second.stop();
        if (failed)
            throw new RuntimeException("Failure.");
    }
}
