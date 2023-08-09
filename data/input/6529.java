public class Suspend implements Runnable {
    private static Thread first=null;
    private static Thread second=null;
    private static ThreadGroup group = new ThreadGroup("");
    private static int count = 0;
    Suspend() {
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
                    group.suspend();
                else
                    count++;
            } catch(InterruptedException e){
            }
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i=0; i<2; i++)
            new Suspend();
        Thread.sleep(3000);
        boolean failed = (count > 1);
        first.stop(); second.stop();
        if (failed)
            throw new RuntimeException("Failure.");
    }
}
