public abstract class AbstractService implements Service {
  private final ReentrantLock lock = new ReentrantLock();
  private final Transition startup = new Transition();
  private final Transition shutdown = new Transition();
  private State state = State.NEW;
  private boolean shutdownWhenStartupFinishes = false;
  protected abstract void doStart();
  protected abstract void doStop();
  public final Future<State> start() {
    lock.lock();
    try {
      if (state == State.NEW) {
        state = State.STARTING;
        doStart();
      }
    } catch (Throwable startupFailure) {
      notifyFailed(startupFailure);
    } finally {
      lock.unlock();
    }
    return startup;
  }
  public final Future<State> stop() {
    lock.lock();
    try {
      if (state == State.NEW) {
        state = State.TERMINATED;
        startup.transitionSucceeded(State.TERMINATED);
        shutdown.transitionSucceeded(State.TERMINATED);
      } else if (state == State.STARTING) {
        shutdownWhenStartupFinishes = true;
        startup.transitionSucceeded(State.STOPPING);
      } else if (state == State.RUNNING) {
        state = State.STOPPING;
        doStop();
      }
    } catch (Throwable shutdownFailure) {
      notifyFailed(shutdownFailure);
    } finally {
      lock.unlock();
    }
    return shutdown;
  }
  public State startAndWait() {
    try {
      return start().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw Throwables.propagate(e.getCause());
    }
  }
  public State stopAndWait() {
    try {
      return stop().get();
    } catch (ExecutionException e) {
      throw Throwables.propagate(e.getCause());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
  protected final void notifyStarted() {
    lock.lock();
    try {
      if (state != State.STARTING) {
        IllegalStateException failure = new IllegalStateException(
            "Cannot notifyStarted() when the service is " + state);
        notifyFailed(failure);
        throw failure;
      }
      state = State.RUNNING;
      if (shutdownWhenStartupFinishes) {
        stop();
      } else {
        startup.transitionSucceeded(State.RUNNING);
      }
    } finally {
      lock.unlock();
    }
  }
  protected final void notifyStopped() {
    lock.lock();
    try {
      if (state != State.STOPPING && state != State.RUNNING) {
        IllegalStateException failure = new IllegalStateException(
            "Cannot notifyStopped() when the service is " + state);
        notifyFailed(failure);
        throw failure;
      }
      state = State.TERMINATED;
      shutdown.transitionSucceeded(State.TERMINATED);
    } finally {
      lock.unlock();
    }
  }
  protected final void notifyFailed(Throwable cause) {
    checkNotNull(cause);
    lock.lock();
    try {
      if (state == State.STARTING) {
        startup.transitionFailed(cause);
        shutdown.transitionFailed(new Exception(
            "Service failed to start.", cause));
      } else if (state == State.STOPPING) {
        shutdown.transitionFailed(cause);
      }
      state = State.FAILED;
    } finally {
      lock.unlock();
    }
  }
  public final boolean isRunning() {
    return state() == State.RUNNING;
  }
  public final State state() {
    lock.lock();
    try {
      if (shutdownWhenStartupFinishes && state == State.STARTING) {
        return State.STOPPING;
      } else {
        return state;
      }
    } finally {
      lock.unlock();
    }
  }
  private static class Transition implements Future<State> {
    private final CountDownLatch done = new CountDownLatch(1);
    private State result;
    private Throwable failureCause;
    void transitionSucceeded(State result) {
      checkState(this.result == null);
      this.result = result;
      done.countDown();
    }
    void transitionFailed(Throwable cause) {
      checkState(result == null);
      this.result = State.FAILED;
      this.failureCause = cause;
      done.countDown();
    }
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }
    public boolean isCancelled() {
      return false;
    }
    public boolean isDone() {
      return done.getCount() == 0;
    }
    public State get() throws InterruptedException, ExecutionException {
      done.await();
      return getImmediately();
    }
    public State get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      if (done.await(timeout, unit)) {
        return getImmediately();
      }
      throw new TimeoutException();
    }
    private State getImmediately() throws ExecutionException {
      if (result == State.FAILED) {
        throw new ExecutionException(failureCause);
      } else {
        return result;
      }
    }
  }
}
