public abstract class ForwardingFuture<V> extends ForwardingObject
    implements Future<V> {
  @Override protected abstract Future<V> delegate();
  public boolean cancel(boolean mayInterruptIfRunning) {
    return delegate().cancel(mayInterruptIfRunning);
  }
  public boolean isCancelled() {
    return delegate().isCancelled();
  }
  public boolean isDone() {
    return delegate().isDone();
  }
  public V get() throws InterruptedException, ExecutionException {
    return delegate().get();
  }
  public V get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return delegate().get(timeout, unit);
  }
}
