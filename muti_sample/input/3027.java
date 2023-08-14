public class InterruptedTest {
    public static void main(String[] args) throws Exception {
        Thread workerThread = new Thread("worker") {
            public void run() {
                System.out.println("Worker thread: running...");
                while (!Thread.currentThread().isInterrupted()) {
                }
                System.out.println("Worker thread: bye");
            }
        };
        System.out.println("Main thread: starts a worker thread...");
        workerThread.start();
        System.out.println("Main thread: waits at most 5s for the worker thread to die...");
        workerThread.join(5000); 
        int ntries = 0;
        while (workerThread.isAlive() && ntries < 5) {
            System.out.println("Main thread: interrupts the worker thread...");
            workerThread.interrupt();
            if (workerThread.isInterrupted()) {
                System.out.println("Main thread: worker thread is interrupted");
            }
            ntries++;
            System.out.println("Main thread: waits for the worker thread to die...");
            workerThread.join(1000); 
        }
        if (ntries == 5) {
          System.out.println("Main thread: the worker thread dod not die");
          System.exit(97);
        }
        System.out.println("Main thread: bye");
    }
}
