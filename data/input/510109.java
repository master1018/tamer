public class PriorityThreadFactory implements ThreadFactory {
    private final int mPriority;
    public PriorityThreadFactory(int priority) {
        mPriority = priority;
    }
    public Thread newThread(Runnable r) {
        return new Thread(r) {
            @Override
            public void run() {
                Process.setThreadPriority(mPriority);
                super.run();
            }
        };
    }
}
