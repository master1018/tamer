public class MockNamedTaskExecutor implements NamedTaskExecutor {
    private final MockExecutor mExecutor = new MockExecutor();
    public void execute(NamedTask task) {
        mExecutor.execute(task);
    }
    public void cancelPendingTasks() {
        mExecutor.cancelPendingTasks();
    }
    public void close() {
        mExecutor.close();
    }
    public boolean runNext() {
        return mExecutor.runNext();
    }
    public void assertPendingTaskCount(int expected) {
        Assert.assertEquals("Wrong number of pending tasks",
                expected, mExecutor.countPendingTasks());
    }
    public void assertDone() {
        assertPendingTaskCount(0);
    }
}
