public class StartOOMTest
{
    public static void main(String[] args) throws Throwable {
        Runnable r = new SleepRunnable();
        ThreadGroup tg = new ThreadGroup("buggy");
        List<Thread> threads = new ArrayList<Thread>();
        Thread failedThread;
        int i = 0;
        for (i = 0; ; i++) {
            Thread t = new Thread(tg, r);
            try {
                t.start();
                threads.add(t);
            } catch (Throwable x) {
                failedThread = t;
                System.out.println(x);
                System.out.println(i);
                break;
            }
        }
        int j = 0;
        for (Thread t : threads)
            t.interrupt();
        while (tg.activeCount() > i/2)
            Thread.yield();
        failedThread.start();
        failedThread.interrupt();
        for (Thread t : threads)
            t.join();
        failedThread.join();
        try {
            Thread.sleep(1000);
        } catch (Throwable ignore) {
        }
        int activeCount = tg.activeCount();
        System.out.println("activeCount = " + activeCount);
        if (activeCount > 0) {
            throw new RuntimeException("Failed: there  should be no active Threads in the group");
        }
    }
    static class SleepRunnable implements Runnable
    {
        public void run() {
            try {
                Thread.sleep(60*1000);
            } catch (Throwable t) {
            }
        }
    }
}
