public class MainThreadTest {
    public static void main(String args[]) {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        int n = tg.activeCount();
        Thread[] ts = new Thread[n];
        int m = tg.enumerate(ts);
        for (int i = 0; i < ts.length; i++) {
            if (Thread.currentThread() == ts[i]) {
                return;
            }
        }
        throw new RuntimeException(
            "Current thread is not in its own thread group!");
    }
}
