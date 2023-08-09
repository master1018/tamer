public class ValueFuture<V> extends AbstractListenableFuture<V> {
  public static <T> ValueFuture<T> create() {
    return new ValueFuture<T>();
  }
  private ValueFuture() {}
  @Override
  public boolean set(V newValue) {
    return super.set(newValue);
  }
  @Override
  public boolean setException(Throwable t) {
    return super.setException(t);
  }
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return super.cancel();
  }
}
