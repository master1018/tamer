public final class Callables {
  private Callables() {}
  public static <T> Callable<T> returning(final @Nullable T value) {
    return new Callable<T>() {
       public T call() {
        return value;
      }
    };
  }
}
