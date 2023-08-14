public abstract class AbstractIdleService implements Service {
  private final Service delegate = new AbstractService() {
    @Override protected final void doStart() {
      executor(State.STARTING).execute(new Runnable() {
         public void run() {
          try {
            startUp();
            notifyStarted();
          } catch (Throwable t) {
            notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
    @Override protected final void doStop() {
      executor(State.STOPPING).execute(new Runnable() {
         public void run() {
          try {
            shutDown();
            notifyStopped();
          } catch (Throwable t) {
            notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
  };
  protected abstract void startUp() throws Exception;
  protected abstract void shutDown() throws Exception;
  protected Executor executor(final State state) {
    return new Executor() {
      public void execute(Runnable command) {
        new Thread(command, AbstractIdleService.this.toString() + " " + state)
            .start();
      }
    };
  }
  @Override public String toString() {
    return getClass().getSimpleName();
  }
   public final Future<State> start() {
    return delegate.start();
  }
   public final State startAndWait() {
    return delegate.startAndWait();
  }
   public final boolean isRunning() {
    return delegate.isRunning();
  }
   public final State state() {
    return delegate.state();
  }
   public final Future<State> stop() {
    return delegate.stop();
  }
   public final State stopAndWait() {
    return delegate.stopAndWait();
  }
}
