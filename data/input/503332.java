public abstract class AbstractExecutionThreadService implements Service {
  private final Service delegate = new AbstractService() {
    @Override protected final void doStart() {
      executor().execute(new Runnable() {
        public void run() {
          try {
            startUp();
            notifyStarted();
            if (isRunning()) {
              try {
                AbstractExecutionThreadService.this.run();
              } catch (Throwable t) {
                try {
                  shutDown();
                } catch (Exception ignored) {}
                throw t;
              }
            }
            shutDown();
            notifyStopped();
          } catch (Throwable t) {
            notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
    @Override protected void doStop() {
      triggerShutdown();
    }
  };
  protected void startUp() throws Exception {}
  protected abstract void run() throws Exception;
  protected void shutDown() throws Exception {}
  protected void triggerShutdown() {}
  protected Executor executor() {
    return new Executor() {
      public void execute(Runnable command) {
        new Thread(command, AbstractExecutionThreadService.this.toString())
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
