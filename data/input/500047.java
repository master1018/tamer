public class MockNamedTaskExecutorFactory implements Factory<NamedTaskExecutor> {
    private final LinkedList<MockNamedTaskExecutor> mExecutors
            = new LinkedList<MockNamedTaskExecutor>();
    public void runNext() {
        for (MockNamedTaskExecutor executor : mExecutors) {
            executor.runNext();
        }
    }
    public NamedTaskExecutor create() {
        MockNamedTaskExecutor executor = new MockNamedTaskExecutor();
        mExecutors.add(executor);
        return executor;
    }
}
