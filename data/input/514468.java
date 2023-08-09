public abstract class AbstractListenableFuture<V>
    extends AbstractFuture<V> implements ListenableFuture<V> {
  private final ExecutionList executionList = new ExecutionList();
  public void addListener(Runnable listener, Executor exec) {
    executionList.add(listener, exec);
  }
  @Override
  protected void done() {
    executionList.run();
  }
}
