public abstract class ForwardingMultimap<K, V> extends ForwardingObject
    implements Multimap<K, V> {
  @Override protected abstract Multimap<K, V> delegate();
  public Map<K, Collection<V>> asMap() {
    return delegate().asMap();
  }
  public void clear() {
    delegate().clear();
  }
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    return delegate().containsEntry(key, value);
  }
  public boolean containsKey(@Nullable Object key) {
    return delegate().containsKey(key);
  }
  public boolean containsValue(@Nullable Object value) {
    return delegate().containsValue(value);
  }
  public Collection<Entry<K, V>> entries() {
    return delegate().entries();
  }
  public Collection<V> get(@Nullable K key) {
    return delegate().get(key);
  }
  public boolean isEmpty() {
    return delegate().isEmpty();
  }
  public Multiset<K> keys() {
    return delegate().keys();
  }
  public Set<K> keySet() {
    return delegate().keySet();
  }
  public boolean put(K key, V value) {
    return delegate().put(key, value);
  }
  public boolean putAll(K key, Iterable<? extends V> values) {
    return delegate().putAll(key, values);
  }
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    return delegate().putAll(multimap);
  }
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    return delegate().remove(key, value);
  }
  public Collection<V> removeAll(@Nullable Object key) {
    return delegate().removeAll(key);
  }
  public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
    return delegate().replaceValues(key, values);
  }
  public int size() {
    return delegate().size();
  }
  public Collection<V> values() {
    return delegate().values();
  }
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
