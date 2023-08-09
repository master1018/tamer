public abstract class AbstractFuture<V> implements Future<V> {
  private final Sync<V> sync = new Sync<V>();
  public V get(long timeout, TimeUnit unit) throws InterruptedException,
      TimeoutException, ExecutionException {
    return sync.get(unit.toNanos(timeout));
  }
  public V get() throws InterruptedException, ExecutionException {
    return sync.get();
  }
  public boolean isDone() {
    return sync.isDone();
  }
  public boolean isCancelled() {
    return sync.isCancelled();
  }
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }
  protected boolean set(V value) {
    boolean result = sync.set(value);
    if (result) {
      done();
    }
    return result;
  }
  protected boolean setException(Throwable throwable) {
    boolean result = sync.setException(throwable);
    if (result) {
      done();
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
    return result;
  }
  protected final boolean cancel() {
    boolean result = sync.cancel();
    if (result) {
      done();
    }
    return result;
  }
  protected void done() {
  }
  static final class Sync<V> extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 0L;
    static final int RUNNING = 0;
    static final int COMPLETING = 1;
    static final int COMPLETED = 2;
    static final int CANCELLED = 4;
    private V value;
    private ExecutionException exception;
    @Override
    protected int tryAcquireShared(int ignored) {
      if (isDone()) {
        return 1;
      }
      return -1;
    }
    @Override
    protected boolean tryReleaseShared(int finalState) {
      setState(finalState);
      return true;
    }
    V get(long nanos) throws TimeoutException, CancellationException,
        ExecutionException, InterruptedException {
      if (!tryAcquireSharedNanos(-1, nanos)) {
        throw new TimeoutException("Timeout waiting for task.");
      }
      return getValue();
    }
    V get() throws CancellationException, ExecutionException,
        InterruptedException {
      acquireSharedInterruptibly(-1);
      return getValue();
    }
    private V getValue() throws CancellationException, ExecutionException {
      int state = getState();
      switch (state) {
        case COMPLETED:
          if (exception != null) {
            throw exception;
          } else {
            return value;
          }
        case CANCELLED:
          throw new CancellationException("Task was cancelled.");
        default:
          throw new IllegalStateException(
              "Error, synchronizer in invalid state: " + state);
      }
    }
    boolean isDone() {
      return (getState() & (COMPLETED | CANCELLED)) != 0;
    }
    boolean isCancelled() {
      return getState() == CANCELLED;
    }
    boolean set(V v) {
      return complete(v, null, COMPLETED);
    }
    boolean setException(Throwable t) {
      return complete(null, t, COMPLETED);
    }
    boolean cancel() {
      return complete(null, null, CANCELLED);
    }
    private boolean complete(V v, Throwable t, int finalState) {
      if (compareAndSetState(RUNNING, COMPLETING)) {
        this.value = v;
        this.exception = t == null ? null : new ExecutionException(t);
        releaseShared(finalState);
        return true;
      }
      return false;
    }
  }
}
