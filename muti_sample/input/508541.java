public class Executors {
  public static ExecutorService getExitingExecutorService(
      ThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
    executor.setThreadFactory(daemonThreadFactory(executor.getThreadFactory()));
    ExecutorService service = java.util.concurrent.Executors
        .unconfigurableExecutorService(executor);
    addDelayedShutdownHook(service, terminationTimeout, timeUnit);
    return service;
  }
  public static ScheduledExecutorService getExitingScheduledExecutorService(
      ScheduledThreadPoolExecutor executor, long terminationTimeout,
      TimeUnit timeUnit) {
    executor.setThreadFactory(daemonThreadFactory(executor.getThreadFactory()));
    ScheduledExecutorService service = java.util.concurrent.Executors
        .unconfigurableScheduledExecutorService(executor);
    addDelayedShutdownHook(service, terminationTimeout, timeUnit);
    return service;
  }
  public static void addDelayedShutdownHook(
      final ExecutorService service, final long terminationTimeout,
      final TimeUnit timeUnit) {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          service.shutdown();
          service.awaitTermination(terminationTimeout, timeUnit);
        } catch (InterruptedException ignored) {
        }
      }
    }));
  }
  public static ExecutorService getExitingExecutorService(
      ThreadPoolExecutor executor) {
    return getExitingExecutorService(executor, 120, TimeUnit.SECONDS);
  }
  public static ScheduledExecutorService getExitingScheduledExecutorService(
      ScheduledThreadPoolExecutor executor) {
    return getExitingScheduledExecutorService(executor, 120, TimeUnit.SECONDS);
  }
  public static ThreadFactory daemonThreadFactory() {
    return daemonThreadFactory(
        java.util.concurrent.Executors.defaultThreadFactory());
  }
  public static ThreadFactory daemonThreadFactory(ThreadFactory factory) {
    return new DaemonThreadFactory(factory);
  }
  public static ExecutorService sameThreadExecutor() {
    return new SameThreadExecutorService();
  }
  private static class SameThreadExecutorService extends AbstractExecutorService {
    private final Lock lock = new ReentrantLock();
    private final Condition termination = lock.newCondition();
    private int runningTasks = 0;
    private boolean shutdown = false;
    public void execute(Runnable command) {
      startTask();
      try {
        command.run();
      } finally {
        endTask();
      }
    }
    public boolean isShutdown() {
      lock.lock();
      try {
        return shutdown;
      } finally {
        lock.unlock();
      }
    }
    public void shutdown() {
      lock.lock();
      try {
        shutdown = true;
      } finally {
        lock.unlock();
      }
    }
    public List<Runnable> shutdownNow() {
      shutdown();
      return Collections.emptyList();
    }
    public boolean isTerminated() {
      lock.lock();
      try {
        return shutdown && runningTasks == 0;
      } finally {
        lock.unlock();
      }
    }
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
      long nanos = unit.toNanos(timeout);
      lock.lock();
      try {
        for (;;) {
          if (isTerminated()) {
            return true;
          } else if (nanos <= 0) {
            return false;
          } else {
            nanos = termination.awaitNanos(nanos);
          }
        }
      } finally {
        lock.unlock();
      }
    }
    private void startTask() {
      lock.lock();
      try {
        if (isShutdown()) {
          throw new RejectedExecutionException("Executor already shutdown");
        }
        runningTasks++;
      } finally {
        lock.unlock();
      }
    }
    private void endTask() {
      lock.lock();
      try {
        runningTasks--;
        if (isTerminated()) {
          termination.signalAll();
        }
      } finally {
        lock.unlock();
      }
    }
  }
}
