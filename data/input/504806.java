public final class Functions {
  private Functions() {}
  public static Function<Object, String> toStringFunction() {
    return ToStringFunction.INSTANCE;
  }
  private enum ToStringFunction implements Function<Object, String> {
    INSTANCE;
    public String apply(Object o) {
      return o.toString();
    }
    @Override public String toString() {
      return "toString";
    }
  }
  @SuppressWarnings("unchecked")
  public static <E> Function<E, E> identity() {
    return (Function<E, E>) IdentityFunction.INSTANCE;
  }
  private enum IdentityFunction implements Function<Object, Object> {
    INSTANCE;
    public Object apply(Object o) {
      return o;
    }
    @Override public String toString() {
      return "identity";
    }
  }
  public static <K, V> Function<K, V> forMap(Map<K, V> map) {
    return new FunctionForMapNoDefault<K, V>(map);
  }
  private static class FunctionForMapNoDefault<K, V>
      implements Function<K, V>, Serializable {
    final Map<K, V> map;
    FunctionForMapNoDefault(Map<K, V> map) {
      this.map = checkNotNull(map);
    }
    public V apply(K key) {
      V result = map.get(key);
      checkArgument(result != null || map.containsKey(key),
          "Key '%s' not present in map", key);
      return result;
    }
    @Override public boolean equals(Object o) {
      if (o instanceof FunctionForMapNoDefault) {
        FunctionForMapNoDefault<?, ?> that = (FunctionForMapNoDefault<?, ?>) o;
        return map.equals(that.map);
      }
      return false;
    }
    @Override public int hashCode() {
      return map.hashCode();
    }
    @Override public String toString() {
      return "forMap(" + map + ")";
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> Function<K, V> forMap(
      Map<K, ? extends V> map, @Nullable V defaultValue) {
    return new ForMapWithDefault<K, V>(map, defaultValue);
  }
  private static class ForMapWithDefault<K, V>
      implements Function<K, V>, Serializable {
    final Map<K, ? extends V> map;
    final V defaultValue;
    ForMapWithDefault(Map<K, ? extends V> map, V defaultValue) {
      this.map = checkNotNull(map);
      this.defaultValue = defaultValue;
    }
    public V apply(K key) {
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    @Override public boolean equals(Object o) {
      if (o instanceof ForMapWithDefault) {
        ForMapWithDefault<?, ?> that = (ForMapWithDefault<?, ?>) o;
        return map.equals(that.map)
            && Objects.equal(defaultValue, that.defaultValue);
      }
      return false;
    }
    @Override public int hashCode() {
      return Objects.hashCode(map, defaultValue);
    }
    @Override public String toString() {
      return "forMap(" + map + ", defaultValue=" + defaultValue + ")";
    }
    private static final long serialVersionUID = 0;
  }
  public static <A, B, C> Function<A, C> compose(
      Function<B, C> g, Function<A, ? extends B> f) {
    return new FunctionComposition<A, B, C>(g, f);
  }
  private static class FunctionComposition<A, B, C>
      implements Function<A, C>, Serializable {
    private final Function<B, C> g;
    private final Function<A, ? extends B> f;
    public FunctionComposition(Function<B, C> g,
        Function<A, ? extends B> f) {
      this.g = checkNotNull(g);
      this.f = checkNotNull(f);
    }
    public C apply(A a) {
      return g.apply(f.apply(a));
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof FunctionComposition) {
        FunctionComposition<?, ?, ?> that = (FunctionComposition<?, ?, ?>) obj;
        return f.equals(that.f) && g.equals(that.g);
      }
      return false;
    }
    @Override public int hashCode() {
      return f.hashCode() ^ g.hashCode();
    }
    @Override public String toString() {
      return g.toString() + "(" + f.toString() + ")";
    }
    private static final long serialVersionUID = 0;
  }
  public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate) {
    return new PredicateFunction<T>(predicate);
  }
  private static class PredicateFunction<T>
      implements Function<T, Boolean>, Serializable {
    private final Predicate<T> predicate;
    private PredicateFunction(Predicate<T> predicate) {
      this.predicate = checkNotNull(predicate);
    }
    public Boolean apply(T t) {
      return predicate.apply(t);
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof PredicateFunction) {
        PredicateFunction<?> that = (PredicateFunction<?>) obj;
        return predicate.equals(that.predicate);
      }
      return false;
    }
    @Override public int hashCode() {
      return predicate.hashCode();
    }
    @Override public String toString() {
      return "forPredicate(" + predicate + ")";
    }
    private static final long serialVersionUID = 0;
  }
  public static <E> Function<Object, E> constant(@Nullable E value) {
    return new ConstantFunction<E>(value);
  }
  private static class ConstantFunction<E>
      implements Function<Object, E>, Serializable {
    private final E value;
    public ConstantFunction(@Nullable E value) {
      this.value = value;
    }
    public E apply(Object from) {
      return value;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof ConstantFunction) {
        ConstantFunction<?> that = (ConstantFunction<?>) obj;
        return Objects.equal(value, that.value);
      }
      return false;
    }
    @Override public int hashCode() {
      return (value == null) ? 0 : value.hashCode();
    }
    @Override public String toString() {
      return "constant(" + value + ")";
    }
    private static final long serialVersionUID = 0;
  }
}
