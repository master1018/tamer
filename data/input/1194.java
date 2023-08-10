public abstract class BaseUtilConcurrentExecutorManager extends BaseExecutorManager {
    private ExecutorService executor;
    public BaseUtilConcurrentExecutorManager(String identifier, ExecutorService executor) {
        super(identifier);
        setExecutor(executor);
    }
    private void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
    public void execute(Runnable command) {
        executor.execute(command);
    }
    public boolean isTerminated() {
        return executor.isTerminated();
    }
    public boolean orderProperShutdown() {
        executor.shutdown();
        return executor.isShutdown();
    }
    public boolean awaitTermination(long terminationTimeout) {
        if (!executor.isTerminated()) {
            try {
                executor.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LogController.getInternalLogger().error("Interrupted while wainting for executor \"{0}\" shutdown", e, getIdentifier());
            }
        }
        return executor.isTerminated();
    }
    public List<Runnable> terminateAndRetrieveTasks() {
        return executor.shutdownNow();
    }
}
