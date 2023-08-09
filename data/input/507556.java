public class PerNameExecutor implements NamedTaskExecutor {
    private final Factory<NamedTaskExecutor> mExecutorFactory;
    private HashMap<String, NamedTaskExecutor> mExecutors;
    public PerNameExecutor(Factory<NamedTaskExecutor> executorFactory) {
        mExecutorFactory = executorFactory;
    }
    public synchronized void cancelPendingTasks() {
        for (NamedTaskExecutor executor : mExecutors.values()) {
            executor.cancelPendingTasks();
        }
    }
    public synchronized void close() {
        for (NamedTaskExecutor executor : mExecutors.values()) {
            executor.close();
        }
    }
    public synchronized void execute(NamedTask task) {
        if (mExecutors == null) {
            mExecutors = new HashMap<String, NamedTaskExecutor>();
        }
        String name = task.getName();
        NamedTaskExecutor executor = mExecutors.get(name);
        if (executor == null) {
            executor = mExecutorFactory.create();
            mExecutors.put(name, executor);
        }
        executor.execute(task);
    }
}
