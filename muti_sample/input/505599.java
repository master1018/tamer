public abstract class ForwardingService extends ForwardingObject
    implements Service {
  @Override protected abstract Service delegate();
   public Future<State> start() {
    return delegate().start();
  }
   public State state() {
    return delegate().state();
  }
   public Future<State> stop() {
    return delegate().stop();
  }
   public State startAndWait() {
    return delegate().startAndWait();
  }
   public State stopAndWait() {
    return delegate().stopAndWait();
  }
   public boolean isRunning() {
    return delegate().isRunning();
  }
}
