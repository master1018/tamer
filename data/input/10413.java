public class LayoutQueue {
    private static final Object DEFAULT_QUEUE = new Object();
    private Vector<Runnable> tasks;
    private Thread worker;
    public LayoutQueue() {
        tasks = new Vector<Runnable>();
    }
    public static LayoutQueue getDefaultQueue() {
        AppContext ac = AppContext.getAppContext();
        synchronized (DEFAULT_QUEUE) {
            LayoutQueue defaultQueue = (LayoutQueue) ac.get(DEFAULT_QUEUE);
            if (defaultQueue == null) {
                defaultQueue = new LayoutQueue();
                ac.put(DEFAULT_QUEUE, defaultQueue);
            }
            return defaultQueue;
        }
    }
    public static void setDefaultQueue(LayoutQueue q) {
        synchronized (DEFAULT_QUEUE) {
            AppContext.getAppContext().put(DEFAULT_QUEUE, q);
        }
    }
    public synchronized void addTask(Runnable task) {
        if (worker == null) {
            worker = new LayoutThread();
            worker.start();
        }
        tasks.addElement(task);
        notifyAll();
    }
    protected synchronized Runnable waitForWork() {
        while (tasks.size() == 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                return null;
            }
        }
        Runnable work = tasks.firstElement();
        tasks.removeElementAt(0);
        return work;
    }
    class LayoutThread extends Thread {
        LayoutThread() {
            super("text-layout");
            setPriority(Thread.MIN_PRIORITY);
        }
        public void run() {
            Runnable work;
            do {
                work = waitForWork();
                if (work != null) {
                    work.run();
                }
            } while (work != null);
        }
    }
}
