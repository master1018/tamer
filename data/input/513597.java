public class MockExecutor implements Executor {
    private final LinkedList<Runnable> mQueue = new LinkedList<Runnable>();
    private boolean mClosed = false;
    public void execute(Runnable task) {
        if (mClosed) throw new IllegalStateException("closed");
        mQueue.addLast(task);
    }
    public void cancelPendingTasks() {
        mQueue.clear();
    }
    public void close() {
        cancelPendingTasks();
        mClosed = true;
    }
    public int countPendingTasks() {
        return mQueue.size();
    }
    public boolean runNext() {
        if (mQueue.isEmpty()) {
            return false;
        }
        Runnable command = mQueue.removeFirst();
        command.run();
        return true;
    }
}
