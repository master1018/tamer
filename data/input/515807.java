public class TreeMultimap<K, V> extends AbstractSortedSetMultimap<K, V> {
  private transient Comparator<? super K> keyComparator;
  private transient Comparator<? super V> valueComparator;
  @SuppressWarnings("unchecked") 
  public static <K extends Comparable, V extends Comparable>
      TreeMultimap<K, V> create() {
    return new TreeMultimap<K, V>(Ordering.natural(), Ordering.natural());
  }
  public static <K, V> TreeMultimap<K, V> create(
      Comparator<? super K> keyComparator,
      Comparator<? super V> valueComparator) {
    return new TreeMultimap<K, V>(checkNotNull(keyComparator),
        checkNotNull(valueComparator));
  }
  @SuppressWarnings("unchecked") 
  public static <K extends Comparable, V extends Comparable>
      TreeMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
    return new TreeMultimap<K, V>(Ordering.natural(), Ordering.natural(),
        multimap);
  }
  TreeMultimap() {
    this(null, null);
  }
  TreeMultimap(@Nullable Comparator<? super K> keyComparator,
      @Nullable Comparator<? super V> valueComparator) {
    super((keyComparator == null)
        ? new TreeMap<K, Collection<V>>()
        : new TreeMap<K, Collection<V>>(keyComparator));
    this.keyComparator = keyComparator;
    this.valueComparator = valueComparator;
  }
  private TreeMultimap(Comparator<? super K> keyComparator,
      Comparator<? super V> valueComparator,
      Multimap<? extends K, ? extends V> multimap) {
    this(keyComparator, valueComparator);
    putAll(multimap);
  }
  @Override SortedSet<V> createCollection() {
    return (valueComparator == null)
        ? new TreeSet<V>() : new TreeSet<V>(valueComparator);
  }
  public Comparator<? super K> keyComparator() {
    return keyComparator;
  }
  public Comparator<? super V> valueComparator() {
    return valueComparator;
  }
  @Override public SortedSet<K> keySet() {
    return (SortedSet<K>) super.keySet();
  }
  @Override public SortedMap<K, Collection<V>> asMap() {
    return (SortedMap<K, Collection<V>>) super.asMap();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(keyComparator());
    stream.writeObject(valueComparator());
    Serialization.writeMultimap(this, stream);
  }
  @SuppressWarnings("unchecked") 
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyComparator = (Comparator<? super K>) stream.readObject();
    valueComparator = (Comparator<? super V>) stream.readObject();
    setMap(new TreeMap<K, Collection<V>>(keyComparator));
    Serialization.populateMultimap(this, stream);
  }
  private static final long serialVersionUID = 0;
}
