public final class HashBiMap<K, V> extends AbstractBiMap<K, V> {
  public static <K, V> HashBiMap<K, V> create() {
    return new HashBiMap<K, V>();
  }
  public static <K, V> HashBiMap<K, V> create(int expectedSize) {
    return new HashBiMap<K, V>(expectedSize);
  }
  public static <K, V> HashBiMap<K, V> create(
      Map<? extends K, ? extends V> map) {
    HashBiMap<K, V> bimap = create(map.size());
    bimap.putAll(map);
    return bimap;
  }
  private HashBiMap() {
    super(new HashMap<K, V>(), new HashMap<V, K>());
  }
  private HashBiMap(int expectedSize) {
    super(new HashMap<K, V>(Maps.capacity(expectedSize)),
        new HashMap<V, K>(Maps.capacity(expectedSize)));
  }
  @Override public V put(@Nullable K key, @Nullable V value) {
    return super.put(key, value);
  }
  @Override public V forcePut(@Nullable K key, @Nullable V value) {
    return super.forcePut(key, value);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMap(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int size = Serialization.readCount(stream);
    setDelegates(Maps.<K, V>newHashMapWithExpectedSize(size),
        Maps.<V, K>newHashMapWithExpectedSize(size));
    Serialization.populateMap(this, stream, size);
  }
  private static final long serialVersionUID = 0;
}
