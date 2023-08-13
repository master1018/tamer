public class FakeTimeLimiter implements TimeLimiter {
  public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration,
      TimeUnit timeoutUnit) {
    return target; 
  }
  public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration,
      TimeUnit timeoutUnit, boolean amInterruptible) throws Exception {
    return callable.call(); 
  }
}
