public abstract class ImmutableBiMap<K, V> extends ImmutableMap<K, V>
    implements BiMap<K, V> {
  private static final ImmutableBiMap<Object, Object> EMPTY_IMMUTABLE_BIMAP
      = new EmptyBiMap();
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableBiMap<K, V> of() {
    return (ImmutableBiMap<K, V>) EMPTY_IMMUTABLE_BIMAP;
  }
  public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
    return new RegularImmutableBiMap<K, V>(ImmutableMap.of(k1, v1));
  }
  public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2) {
    return new RegularImmutableBiMap<K, V>(ImmutableMap.of(k1, v1, k2, v2));
  }
  public static <K, V> ImmutableBiMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    return new RegularImmutableBiMap<K, V>(ImmutableMap.of(
        k1, v1, k2, v2, k3, v3));
  }
  public static <K, V> ImmutableBiMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return new RegularImmutableBiMap<K, V>(ImmutableMap.of(
        k1, v1, k2, v2, k3, v3, k4, v4));
  }
  public static <K, V> ImmutableBiMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return new RegularImmutableBiMap<K, V>(ImmutableMap.of(
        k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
  }
  public static <K, V> Builder<K, V> builder() {
    return new Builder<K, V>();
  }
  public static final class Builder<K, V> extends ImmutableMap.Builder<K, V> {
    public Builder() {}
    @Override public Builder<K, V> put(K key, V value) {
      super.put(key, value);
      return this;
    }
    @Override public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
      super.putAll(map);
      return this;
    }
    @Override public ImmutableBiMap<K, V> build() {
      ImmutableMap<K, V> map = super.build();
      if (map.isEmpty()) {
        return of();
      }
      return new RegularImmutableBiMap<K, V>(super.build());
    }
  }
  public static <K, V> ImmutableBiMap<K, V> copyOf(
      Map<? extends K, ? extends V> map) {
    if (map instanceof ImmutableBiMap) {
      @SuppressWarnings("unchecked") 
      ImmutableBiMap<K, V> bimap = (ImmutableBiMap<K, V>) map;
      return bimap;
    }
    if (map.isEmpty()) {
      return of();
    }
    ImmutableMap<K, V> immutableMap = ImmutableMap.copyOf(map);
    return new RegularImmutableBiMap<K, V>(immutableMap);
  }
  ImmutableBiMap() {}
  abstract ImmutableMap<K, V> delegate();
  public abstract ImmutableBiMap<V, K> inverse();
  @Override public boolean containsKey(@Nullable Object key) {
    return delegate().containsKey(key);
  }
  @Override public boolean containsValue(@Nullable Object value) {
    return inverse().containsKey(value);
  }
  @Override public ImmutableSet<Entry<K, V>> entrySet() {
    return delegate().entrySet();
  }
  @Override public V get(@Nullable Object key) {
    return delegate().get(key);
  }
  @Override public ImmutableSet<K> keySet() {
    return delegate().keySet();
  }
  @Override public ImmutableSet<V> values() {
    return inverse().keySet();
  }
  public V forcePut(K key, V value) {
    throw new UnsupportedOperationException();
  }
  @Override public boolean isEmpty() {
    return delegate().isEmpty();
  }
  public int size() {
    return delegate().size();
  }
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
  @Override public String toString() {
    return delegate().toString();
  }
  @SuppressWarnings("serial") 
  static class EmptyBiMap extends ImmutableBiMap<Object, Object> {
    @Override ImmutableMap<Object, Object> delegate() {
      return ImmutableMap.of();
    }
    @Override public ImmutableBiMap<Object, Object> inverse() {
      return this;
    }
    Object readResolve() {
      return EMPTY_IMMUTABLE_BIMAP; 
    }
  }
  private static class SerializedForm extends ImmutableMap.SerializedForm {
    SerializedForm(ImmutableBiMap<?, ?> bimap) {
      super(bimap);
    }
    @Override Object readResolve() {
      Builder<Object, Object> builder = new Builder<Object, Object>();
      return createMap(builder);
    }
    private static final long serialVersionUID = 0;
  }
  @Override Object writeReplace() {
    return new SerializedForm(this);
  }
}
