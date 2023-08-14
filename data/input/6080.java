public class NullQueue {
    private static Reference r = null;
    private static Thread findThread(String name) {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        int nt = tg.activeCount();
        Thread[] ts = new Thread[nt];
        tg.enumerate(ts);
        Thread refHandler = null;
        for (int i = 0; i < ts.length; i++) {
            if (ts[i].getName().equals(name)) return ts[i];
        }
        return null;
    }
    private static void fork(Runnable proc) throws InterruptedException {
        Thread t = new Thread(proc);
        t.start();
        t.join();
    }
    public static void main(String[] args) throws Exception {
        Thread refHandler = findThread("Reference Handler");
        if (refHandler == null)
            throw new Exception("Couldn't find Reference-handler thread");
        if (!refHandler.isAlive())
            throw new Exception("Reference-handler thread is not alive");
        fork(new Runnable() {
            public void run() {
                r = new WeakReference(new Object(), null);
            }});
        for (int i = 0;; i++) {
            Thread.sleep(10);
            System.gc();
            if (r.get() == null) break;
            if (i >= 10)
                throw new Exception("Couldn't cause weak ref to be cleared");
        }
        if (!refHandler.isAlive())
            throw new Exception("Reference-handler thread died");
    }
}
