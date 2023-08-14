public class SimpleTimeLimiter implements TimeLimiter {
  private final ExecutorService executor;
  public SimpleTimeLimiter(ExecutorService executor) {
    checkNotNull(executor);
    this.executor = executor;
  }
  public SimpleTimeLimiter() {
    this(Executors.newCachedThreadPool());
  }
  public <T> T newProxy(final T target, Class<T> interfaceType,
      final long timeoutDuration, final TimeUnit timeoutUnit) {
    checkNotNull(target);
    checkNotNull(interfaceType);
    checkNotNull(timeoutUnit);
    checkArgument(timeoutDuration > 0, "bad timeout: " + timeoutDuration);
    checkArgument(interfaceType.isInterface(),
        "interfaceType must be an interface type");
    final Set<Method> interruptibleMethods
        = findInterruptibleMethods(interfaceType);
    InvocationHandler handler = new InvocationHandler() {
      public Object invoke(Object obj, final Method method, final Object[] args)
          throws Throwable {
        Callable<Object> callable = new Callable<Object>() {
          public Object call() throws Exception {
            try {
              return method.invoke(target, args);
            } catch (InvocationTargetException e) {
              Throwables.throwCause(e, false);
              throw new AssertionError("can't get here");
            }
          }
        };
        return callWithTimeout(callable, timeoutDuration, timeoutUnit,
            interruptibleMethods.contains(method));
      }
    };
    return newProxy(interfaceType, handler);
  }
  public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration,
      TimeUnit timeoutUnit, boolean amInterruptible) throws Exception {
    checkNotNull(callable);
    checkNotNull(timeoutUnit);
    checkArgument(timeoutDuration > 0, "bad timeout: " + timeoutDuration);
    Future<T> future = executor.submit(callable);
    try {
      if (amInterruptible) {
        try {
          return future.get(timeoutDuration, timeoutUnit);
        } catch (InterruptedException e) {
          future.cancel(true);
          throw e;
        }
      } else {
        Future<T> uninterruptible = Futures.makeUninterruptible(future);
        return uninterruptible.get(timeoutDuration, timeoutUnit);
      }
    } catch (ExecutionException e) {
      throw Throwables.throwCause(e, true);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new UncheckedTimeoutException(e);
    }
  }
  private static Set<Method> findInterruptibleMethods(Class<?> interfaceType) {
    Set<Method> set = Sets.newHashSet();
    for (Method m : interfaceType.getMethods()) {
      if (declaresInterruptedEx(m)) {
        set.add(m);
      }
    }
    return set;
  }
  private static boolean declaresInterruptedEx(Method method) {
    for (Class<?> exType : method.getExceptionTypes()) {
      if (exType == InterruptedException.class) {
        return true;
      }
    }
    return false;
  }
  private static <T> T newProxy(
      Class<T> interfaceType, InvocationHandler handler) {
    Object object = Proxy.newProxyInstance(
        interfaceType.getClassLoader(), new Class<?>[] { interfaceType }, handler);
    return interfaceType.cast(object);
  }
}
