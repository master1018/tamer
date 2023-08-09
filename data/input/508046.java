public final class Suppliers {
  private Suppliers() {}
  public static <F, T> Supplier<T> compose(
      Function<? super F, T> function, Supplier<F> first) {
    Preconditions.checkNotNull(function);
    Preconditions.checkNotNull(first);
    return new SupplierComposition<F, T>(function, first);
  }
  private static class SupplierComposition<F, T>
      implements Supplier<T>, Serializable {
    final Function<? super F, ? extends T> function;
    final Supplier<? extends F> first;
    SupplierComposition(Function<? super F, ? extends T> function,
        Supplier<? extends F> first) {
      this.function = function;
      this.first = first;
    }
    public T get() {
      return function.apply(first.get());
    }
    private static final long serialVersionUID = 0;
  }
  public static <T> Supplier<T> memoize(Supplier<T> delegate) {
    return new MemoizingSupplier<T>(Preconditions.checkNotNull(delegate));
  }
  @VisibleForTesting static class MemoizingSupplier<T>
      implements Supplier<T>, Serializable {
    final Supplier<T> delegate;
    transient boolean initialized;
    transient T value;
    MemoizingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }
    public synchronized T get() {
      if (!initialized) {
        value = delegate.get();
        initialized = true;
      }
      return value;
    }
    private static final long serialVersionUID = 0;
  }
  public static <T> Supplier<T> memoizeWithExpiration(
      Supplier<T> delegate, long duration, TimeUnit unit) {
    return new ExpiringMemoizingSupplier<T>(delegate, duration, unit);
  }
  @VisibleForTesting static class ExpiringMemoizingSupplier<T>
      implements Supplier<T>, Serializable {
    final Supplier<T> delegate;
    final long durationNanos;
    transient boolean initialized;
    transient T value;
    transient long expirationNanos;
    ExpiringMemoizingSupplier(
        Supplier<T> delegate, long duration, TimeUnit unit) {
      this.delegate = Preconditions.checkNotNull(delegate);
      this.durationNanos = unit.toNanos(duration);
      Preconditions.checkArgument(duration > 0);
    }
    public synchronized T get() {
      if (!initialized || System.nanoTime() - expirationNanos >= 0) {
        value = delegate.get();
        initialized = true;
        expirationNanos = System.nanoTime() + durationNanos;
      }
      return value;
    }
    private static final long serialVersionUID = 0;
  }
  public static <T> Supplier<T> ofInstance(@Nullable T instance) {
    return new SupplierOfInstance<T>(instance);
  }
  private static class SupplierOfInstance<T>
      implements Supplier<T>, Serializable {
    final T instance;
    SupplierOfInstance(T instance) {
      this.instance = instance;
    }
    public T get() {
      return instance;
    }
    private static final long serialVersionUID = 0;
  }
  public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
    return new ThreadSafeSupplier<T>(Preconditions.checkNotNull(delegate));
  }
  private static class ThreadSafeSupplier<T>
      implements Supplier<T>, Serializable {
    final Supplier<T> delegate;
    ThreadSafeSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }
    public T get() {
      synchronized (delegate) {
        return delegate.get();
      }
    }
    private static final long serialVersionUID = 0;
  }
}
