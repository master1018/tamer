public final class HashMultimap<K, V> extends AbstractSetMultimap<K, V> {
  private static final int DEFAULT_VALUES_PER_KEY = 8;
  @VisibleForTesting
  transient int expectedValuesPerKey = DEFAULT_VALUES_PER_KEY;
  public static <K, V> HashMultimap<K, V> create() {
    return new HashMultimap<K, V>();
  }
  public static <K, V> HashMultimap<K, V> create(
      int expectedKeys, int expectedValuesPerKey) {
    return new HashMultimap<K, V>(expectedKeys, expectedValuesPerKey);
  }
  public static <K, V> HashMultimap<K, V> create(
      Multimap<? extends K, ? extends V> multimap) {
    return new HashMultimap<K, V>(multimap);
  }
  private HashMultimap() {
    super(new HashMap<K, Collection<V>>());
  }
  private HashMultimap(int expectedKeys, int expectedValuesPerKey) {
    super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(expectedKeys));
    Preconditions.checkArgument(expectedValuesPerKey >= 0);
    this.expectedValuesPerKey = expectedValuesPerKey;
  }
  private HashMultimap(Multimap<? extends K, ? extends V> multimap) {
    super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(
        multimap.keySet().size()));
    putAll(multimap);
  }
  @Override Set<V> createCollection() {
    return Sets.<V>newHashSetWithExpectedSize(expectedValuesPerKey);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(expectedValuesPerKey);
    Serialization.writeMultimap(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    expectedValuesPerKey = stream.readInt();
    int distinctKeys = Serialization.readCount(stream);
    Map<K, Collection<V>> map = Maps.newHashMapWithExpectedSize(distinctKeys);
    setMap(map);
    Serialization.populateMultimap(this, stream, distinctKeys);
  }
  private static final long serialVersionUID = 0;
}
