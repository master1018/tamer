public abstract class ForwardingMapEntry<K, V>
    extends ForwardingObject implements Map.Entry<K, V> {
  @Override protected abstract Map.Entry<K, V> delegate();
  public K getKey() {
    return delegate().getKey();
  }
  public V getValue() {
    return delegate().getValue();
  }
  public V setValue(V value) {
    return delegate().setValue(value);
  }
  @Override public boolean equals(@Nullable Object object) {
    return delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
