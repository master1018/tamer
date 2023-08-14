public class SingleThreadNamedTaskExecutor implements NamedTaskExecutor {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SingleThreadNamedTaskExecutor";
    private final LinkedBlockingQueue<NamedTask> mQueue;
    private final Thread mWorker;
    private volatile boolean mClosed = false;
    public SingleThreadNamedTaskExecutor(ThreadFactory threadFactory) {
        mQueue = new LinkedBlockingQueue<NamedTask>();
        mWorker = threadFactory.newThread(new Worker());
        mWorker.start();
    }
    public void cancelPendingTasks() {
        if (DBG) Log.d(TAG, "Cancelling " + mQueue.size() + " tasks: " + mWorker.getName());
        if (mClosed) {
            throw new IllegalStateException("cancelPendingTasks() after close()");
        }
        mQueue.clear();
    }
    public void close() {
        mClosed = true;
        mWorker.interrupt();
        mQueue.clear();
    }
    public void execute(NamedTask task) {
        if (mClosed) {
            throw new IllegalStateException("execute() after close()");
        }
        mQueue.add(task);
    }
    private class Worker implements Runnable {
        public void run() {
            try {
                loop();
            } finally {
                if (!mClosed) Log.w(TAG, "Worker exited before close");
            }
        }
        private void loop() {
            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();
            while (!mClosed) {
                NamedTask task;
                try {
                    task = mQueue.take();
                } catch (InterruptedException ex) {
                    continue;
                }
                currentThread.setName(threadName + " " + task.getName());
                try {
                    task.run();
                } catch (RuntimeException ex) {
                    Log.e(TAG, "Task " + task.getName() + " failed", ex);
                }
            }
        }
    }
    public static Factory<NamedTaskExecutor> factory(final ThreadFactory threadFactory) {
        return new Factory<NamedTaskExecutor>() {
            public NamedTaskExecutor create() {
                return new SingleThreadNamedTaskExecutor(threadFactory);
            }
        };
    }
}
