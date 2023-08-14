public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V>
    implements SortedMap<K, V> {
  @Override protected abstract SortedMap<K, V> delegate();
  public Comparator<? super K> comparator() {
    return delegate().comparator();
  }
  public K firstKey() {
    return delegate().firstKey();
  }
  public SortedMap<K, V> headMap(K toKey) {
    return delegate().headMap(toKey);
  }
  public K lastKey() {
    return delegate().lastKey();
  }
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return delegate().subMap(fromKey, toKey);
  }
  public SortedMap<K, V> tailMap(K fromKey) {
    return delegate().tailMap(fromKey);
  }
}
