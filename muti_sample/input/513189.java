public class NamingThreadFactory implements ThreadFactory {
  private final ThreadFactory backingFactory;
  private final String format;
  private final AtomicInteger count = new AtomicInteger(0);
  public static final ThreadFactory DEFAULT_FACTORY
      = Executors.defaultThreadFactory();
  public NamingThreadFactory(String format) {
    this(format, DEFAULT_FACTORY);
  }
  public NamingThreadFactory(String format, ThreadFactory backingFactory) {
    this.format = format;
    this.backingFactory = backingFactory;
    makeName(0); 
  }
  public Thread newThread(Runnable r) {
    Thread t = backingFactory.newThread(r);
    t.setName(makeName(count.getAndIncrement()));
    return t;
  }
  private String makeName(int ordinal) {
    return String.format(format, ordinal);
  }
}
