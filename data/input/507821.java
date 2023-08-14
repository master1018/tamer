class ImmutableEntry<K, V> extends AbstractMapEntry<K, V>
    implements Serializable {
  private final K key;
  private final V value;
  ImmutableEntry(@Nullable K key, @Nullable V value) {
    this.key = key;
    this.value = value;
  }
  @Override public K getKey() {
    return key;
  }
  @Override public V getValue() {
    return value;
  }
  private static final long serialVersionUID = 0;
}
