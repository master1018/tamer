public class ExecutionList implements Runnable {
  private static final Logger LOG =
      Logger.getLogger(ExecutionList.class.getName());
  private final List<RunnableExecutorPair> runnables = Lists.newArrayList();
  private boolean executed = false;
  public void add(Runnable runnable, Executor executor) {
    Preconditions.checkNotNull(runnable, "Runnable was null.");
    Preconditions.checkNotNull(executor, "Executor was null.");
    boolean executeImmediate = false;
    synchronized (runnables) {
      if (!executed) {
        runnables.add(new RunnableExecutorPair(runnable, executor));
      } else {
        executeImmediate = true;
      }
    }
    if (executeImmediate) {
      executor.execute(runnable);
    }
  }
  public void run() {
    synchronized (runnables) {
      executed = true;
    }
    for (RunnableExecutorPair runnableAndExecutor : runnables) {
      runnableAndExecutor.execute();
    }
  }
  private static class RunnableExecutorPair {
    final Runnable runnable;
    final Executor executor;
    RunnableExecutorPair(Runnable runnable, Executor executor) {
      this.runnable = runnable;
      this.executor = executor;
    }
    void execute() {
      try {
        executor.execute(runnable);
      } catch (RuntimeException e) {
        LOG.log(Level.SEVERE, "RuntimeException while executing runnable "
            + runnable + " with executor " + executor, e);
      }
    }
  }
}
