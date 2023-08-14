public class BatchingNamedTaskExecutor implements NamedTaskExecutor {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.BatchingNamedTaskExecutor";
    private final NamedTaskExecutor mExecutor;
    private final ArrayList<NamedTask> mQueuedTasks = new ArrayList<NamedTask>();
    public BatchingNamedTaskExecutor(NamedTaskExecutor executor) {
        mExecutor = executor;
    }
    public void execute(NamedTask task) {
        synchronized (mQueuedTasks) {
            if (DBG) Log.d(TAG, "Queuing " + task);
            mQueuedTasks.add(task);
        }
    }
    private void dispatch(NamedTask task) {
        if (DBG) Log.d(TAG, "Dispatching " + task);
        mExecutor.execute(task);
    }
    public void executeNextBatch(int batchSize) {
        NamedTask[] batch = new NamedTask[0];
        synchronized (mQueuedTasks) {
            int count = Math.min(mQueuedTasks.size(), batchSize);
            List<NamedTask> nextTasks = mQueuedTasks.subList(0, count);
            batch = nextTasks.toArray(batch);
            nextTasks.clear();
            if (DBG) Log.d(TAG, "Dispatching batch of " + count);
        }
        for (NamedTask task : batch) {
            dispatch(task);
        }
    }
    public void cancelPendingTasks() {
        synchronized (mQueuedTasks) {
            mQueuedTasks.clear();
        }
        mExecutor.cancelPendingTasks();
    }
    public void close() {
        cancelPendingTasks();
        mExecutor.close();
    }
}
