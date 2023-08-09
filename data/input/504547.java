public abstract class ForwardingMap<K, V> extends ForwardingObject
    implements Map<K, V> {
  @Override protected abstract Map<K, V> delegate();
  public int size() {
    return delegate().size();
  }
  public boolean isEmpty() {
    return delegate().isEmpty();
  }
  public V remove(Object object) {
    return delegate().remove(object);
  }
  public void clear() {
    delegate().clear();
  }
  public boolean containsKey(Object key) {
    return delegate().containsKey(key);
  }
  public boolean containsValue(Object value) {
    return delegate().containsValue(value);
  }
  public V get(Object key) {
    return delegate().get(key);
  }
  public V put(K key, V value) {
    return delegate().put(key, value);
  }
  public void putAll(Map<? extends K, ? extends V> map) {
    delegate().putAll(map);
  }
  public Set<K> keySet() {
    return delegate().keySet();
  }
  public Collection<V> values() {
    return delegate().values();
  }
  public Set<Entry<K, V>> entrySet() {
    return delegate().entrySet();
  }
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
