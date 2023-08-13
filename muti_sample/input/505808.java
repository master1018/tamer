abstract class AbstractSetMultimap<K, V>
    extends AbstractMultimap<K, V> implements SetMultimap<K, V> {
  protected AbstractSetMultimap(Map<K, Collection<V>> map) {
    super(map);
  }
  @Override abstract Set<V> createCollection();
  @Override public Set<V> get(@Nullable K key) {
    return (Set<V>) super.get(key);
  }
  @Override public Set<Map.Entry<K, V>> entries() {
    return (Set<Map.Entry<K, V>>) super.entries();
  }
  @Override public Set<V> removeAll(@Nullable Object key) {
    return (Set<V>) super.removeAll(key);
  }
  @Override public Set<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return (Set<V>) super.replaceValues(key, values);
  }
  @Override public boolean put(K key, V value) {
    return super.put(key, value);
  }
  @Override public boolean equals(@Nullable Object object) {
    return super.equals(object);
  }
  private static final long serialVersionUID = 7431625294878419160L;  
}
