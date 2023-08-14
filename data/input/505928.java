public class DaemonThreadFactory implements ThreadFactory {
  private final ThreadFactory factory;
  public DaemonThreadFactory(ThreadFactory factory) {
    Preconditions.checkNotNull(factory);
    this.factory = factory;
  }
  public Thread newThread(Runnable r) {
    Thread t = factory.newThread(r);
    t.setDaemon(true);
    return t;
  }
}
