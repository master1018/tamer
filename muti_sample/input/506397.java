public class ListenableFutureTask<V> extends FutureTask<V>
    implements ListenableFuture<V> {
  private final ExecutionList executionList = new ExecutionList();
  public ListenableFutureTask(Callable<V> callable) {
    super(callable);
  }
  public ListenableFutureTask(Runnable runnable, V result) {
    super(runnable, result);
  }
  public void addListener(Runnable listener, Executor exec) {
    executionList.add(listener, exec);
  }
  @Override
  protected void done() {
    executionList.run();
  }
}
