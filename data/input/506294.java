public final class ArrayListMultimap<K, V> extends AbstractListMultimap<K, V> {
  private static final int DEFAULT_VALUES_PER_KEY = 10;
  @VisibleForTesting transient int expectedValuesPerKey;
  public static <K, V> ArrayListMultimap<K, V> create() {
    return new ArrayListMultimap<K, V>();
  }
  public static <K, V> ArrayListMultimap<K, V> create(
      int expectedKeys, int expectedValuesPerKey) {
    return new ArrayListMultimap<K, V>(expectedKeys, expectedValuesPerKey);
  }
  public static <K, V> ArrayListMultimap<K, V> create(
      Multimap<? extends K, ? extends V> multimap) {
    return new ArrayListMultimap<K, V>(multimap);
  }
  private ArrayListMultimap() {
    super(new HashMap<K, Collection<V>>());
    expectedValuesPerKey = DEFAULT_VALUES_PER_KEY;
  }
  private ArrayListMultimap(int expectedKeys, int expectedValuesPerKey) {
    super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(expectedKeys));
    checkArgument(expectedValuesPerKey >= 0);
    this.expectedValuesPerKey = expectedValuesPerKey;
  }
  private ArrayListMultimap(Multimap<? extends K, ? extends V> multimap) {
    this(multimap.keySet().size(),
        (multimap instanceof ArrayListMultimap) ?
            ((ArrayListMultimap<?, ?>) multimap).expectedValuesPerKey :
            DEFAULT_VALUES_PER_KEY);
    putAll(multimap);
  }
  @Override List<V> createCollection() {
    return new ArrayList<V>(expectedValuesPerKey);
  }
  public void trimToSize() {
    for (Collection<V> collection : backingMap().values()) {
      ArrayList<V> arrayList = (ArrayList<V>) collection;
      arrayList.trimToSize();
    }
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
