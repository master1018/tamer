abstract class AbstractSortedSetMultimap<K, V>
    extends AbstractSetMultimap<K, V> implements SortedSetMultimap<K, V> {
  protected AbstractSortedSetMultimap(Map<K, Collection<V>> map) {
    super(map);
  }
  @Override abstract SortedSet<V> createCollection();
  @Override public SortedSet<V> get(@Nullable K key) {
    return (SortedSet<V>) super.get(key);
  }
  @Override public SortedSet<V> removeAll(@Nullable Object key) {
    return (SortedSet<V>) super.removeAll(key);
  }
  @Override public SortedSet<V> replaceValues(
      K key, Iterable<? extends V> values) {
    return (SortedSet<V>) super.replaceValues(key, values);
  }
  @Override public Collection<V> values() {
    return super.values();
  }
  private static final long serialVersionUID = 430848587173315748L;  
}