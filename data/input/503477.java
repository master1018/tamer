public abstract class AbstractCheckedFuture<V, E extends Exception>
    implements CheckedFuture<V, E> {
  protected final ListenableFuture<V> delegate;
  protected AbstractCheckedFuture(ListenableFuture<V> delegate) {
    this.delegate = delegate;
  }
  protected abstract E mapException(Exception e);
  public V checkedGet() throws E {
    try {
      return get();
    } catch (InterruptedException e) {
      cancel(true);
      throw mapException(e);
    } catch (CancellationException e) {
      throw mapException(e);
    } catch (ExecutionException e) {
      throw mapException(e);
    }
  }
  public V checkedGet(long timeout, TimeUnit unit) throws TimeoutException, E {
    try {
      return get(timeout, unit);
    } catch (InterruptedException e) {
      cancel(true);
      throw mapException(e);
    } catch (CancellationException e) {
      throw mapException(e);
    } catch (ExecutionException e) {
      throw mapException(e);
    }
  }
  public boolean cancel(boolean mayInterruptIfRunning) {
    return delegate.cancel(mayInterruptIfRunning);
  }
  public boolean isCancelled() {
    return delegate.isCancelled();
  }
  public boolean isDone() {
    return delegate.isDone();
  }
  public V get() throws InterruptedException, ExecutionException {
    return delegate.get();
  }
  public V get(long timeout, TimeUnit unit) throws InterruptedException,
      ExecutionException, TimeoutException {
    return delegate.get(timeout, unit);
  }
  public void addListener(Runnable listener, Executor exec) {
    delegate.addListener(listener, exec);
  }
}
