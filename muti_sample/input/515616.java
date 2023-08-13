public class ImmutableSortedMap<K, V>
    extends ImmutableSortedMapFauxverideShim<K, V> implements SortedMap<K, V> {
  @SuppressWarnings("unchecked")
  private static final Comparator NATURAL_ORDER = Ordering.natural();
  private static final Entry<?, ?>[] EMPTY_ARRAY = new Entry<?, ?>[0];
  @SuppressWarnings("unchecked")
  private static final ImmutableMap<Object, Object> NATURAL_EMPTY_MAP
      = new ImmutableSortedMap<Object, Object>(EMPTY_ARRAY, NATURAL_ORDER);
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableSortedMap<K, V> of() {
    return (ImmutableSortedMap) NATURAL_EMPTY_MAP;
  }
  private static <K, V> ImmutableSortedMap<K, V> emptyMap(
      Comparator<? super K> comparator) {
    if (NATURAL_ORDER.equals(comparator)) {
      return ImmutableSortedMap.of();
    } else {
      return new ImmutableSortedMap<K, V>(EMPTY_ARRAY, comparator);
    }
  }
  public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V>
      of(K k1, V v1) {
    Entry<?, ?>[] entries = { entryOf(k1, v1) };
    return new ImmutableSortedMap<K, V>(entries, Ordering.natural());
  }
  public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V>
      of(K k1, V v1, K k2, V v2) {
    return new Builder<K, V>(Ordering.natural())
        .put(k1, v1).put(k2, v2).build();
  }
  public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V>
      of(K k1, V v1, K k2, V v2, K k3, V v3) {
    return new Builder<K, V>(Ordering.natural())
        .put(k1, v1).put(k2, v2).put(k3, v3).build();
  }
  public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V>
      of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return new Builder<K, V>(Ordering.natural())
        .put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).build();
  }
  public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V>
      of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return new Builder<K, V>(Ordering.natural())
        .put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).build();
  }
  public static <K, V> ImmutableSortedMap<K, V> copyOf(
      Map<? extends K, ? extends V> map) {
    @SuppressWarnings("unchecked")
    Ordering<K> naturalOrder = (Ordering) Ordering.<Comparable>natural();
    return copyOfInternal(map, naturalOrder);
  }
  public static <K, V> ImmutableSortedMap<K, V> copyOf(
      Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
    return copyOfInternal(map, checkNotNull(comparator));
  }
  public static <K, V> ImmutableSortedMap<K, V> copyOfSorted(
      SortedMap<K, ? extends V> map) {
    @SuppressWarnings("unchecked")
    Comparator<? super K> comparator =
        (map.comparator() == null) ? NATURAL_ORDER : map.comparator();
    return copyOfInternal(map, comparator);
  }
  private static <K, V> ImmutableSortedMap<K, V> copyOfInternal(
      Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
    boolean sameComparator = false;
    if (map instanceof SortedMap) {
      SortedMap<?, ?> sortedMap = (SortedMap<?, ?>) map;
      Comparator<?> comparator2 = sortedMap.comparator();
      sameComparator = (comparator2 == null)
          ? comparator == NATURAL_ORDER
          : comparator.equals(comparator2);
    }
    if (sameComparator && (map instanceof ImmutableSortedMap)) {
      @SuppressWarnings("unchecked")
      ImmutableSortedMap<K, V> kvMap = (ImmutableSortedMap<K, V>) map;
      return kvMap;
    }
    List<Entry<?, ?>> list = Lists.newArrayListWithCapacity(map.size());
    for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
      list.add(entryOf(entry.getKey(), entry.getValue()));
    }
    Entry<?, ?>[] entryArray = list.toArray(new Entry<?, ?>[list.size()]);
    if (!sameComparator) {
      sortEntries(entryArray, comparator);
      validateEntries(entryArray, comparator);
    }
    return new ImmutableSortedMap<K, V>(entryArray, comparator);
  }
  private static void sortEntries(Entry<?, ?>[] entryArray,
      final Comparator<?> comparator) {
    Comparator<Entry<?, ?>> entryComparator = new Comparator<Entry<?, ?>>() {
      public int compare(Entry<?, ?> entry1, Entry<?, ?> entry2) {
        return ImmutableSortedSet.unsafeCompare(
            comparator, entry1.getKey(), entry2.getKey());
      }
    };
    Arrays.sort(entryArray, entryComparator);
  }
  private static void validateEntries(Entry<?, ?>[] entryArray,
      Comparator<?> comparator) {
    for (int i = 1; i < entryArray.length; i++) {
      if (ImmutableSortedSet.unsafeCompare(comparator,
          entryArray[i - 1].getKey(), entryArray[i].getKey()) == 0) {
        throw new IllegalArgumentException(
            "Duplicate keys in mappings "
                + entryArray[i - 1] + " and " + entryArray[i]);
      }
    }
  }
  public static <K extends Comparable<K>, V> Builder<K, V> naturalOrder() {
    return new Builder<K, V>(Ordering.natural());
  }
  public static <K, V> Builder<K, V> orderedBy(Comparator<K> comparator) {
    return new Builder<K, V>(comparator);
  }
  public static <K extends Comparable<K>, V> Builder<K, V> reverseOrder() {
    return new Builder<K, V>(Ordering.natural().reverse());
  }
  public static final class Builder<K, V> extends ImmutableMap.Builder<K, V> {
    private final Comparator<? super K> comparator;
    public Builder(Comparator<? super K> comparator) {
      this.comparator = checkNotNull(comparator);
    }
    @Override public Builder<K, V> put(K key, V value) {
      entries.add(entryOf(key, value));
      return this;
    }
    @Override public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
      for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
      return this;
    }
    @Override public ImmutableSortedMap<K, V> build() {
      Entry<?, ?>[] entryArray
          = entries.toArray(new Entry<?, ?>[entries.size()]);
      sortEntries(entryArray, comparator);
      validateEntries(entryArray, comparator);
      return new ImmutableSortedMap<K, V>(entryArray, comparator);
    }
  }
  private final transient Entry<K, V>[] entries;
  private final transient Comparator<? super K> comparator;
  private final transient int fromIndex;
  private final transient int toIndex;
  private ImmutableSortedMap(Entry<?, ?>[] entries,
      Comparator<? super K> comparator, int fromIndex, int toIndex) {
    @SuppressWarnings("unchecked")
    Entry<K, V>[] tmp = (Entry<K, V>[]) entries;
    this.entries = tmp;
    this.comparator = comparator;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }
  ImmutableSortedMap(Entry<?, ?>[] entries,
      Comparator<? super K> comparator) {
    this(entries, comparator, 0, entries.length);
  }
  public int size() {
    return toIndex - fromIndex;
  }
  @Override public V get(@Nullable Object key) {
    if (key == null) {
      return null;
    }
    int i;
    try {
      i = binarySearch(key);
    } catch (ClassCastException e) {
      return null;
    }
    return (i >= 0) ? entries[i].getValue() : null;
  }
  private int binarySearch(Object key) {
    int lower = fromIndex;
    int upper = toIndex - 1;
    while (lower <= upper) {
      int middle = lower + (upper - lower) / 2;
      int c = ImmutableSortedSet.unsafeCompare(
          comparator, key, entries[middle].getKey());
      if (c < 0) {
        upper = middle - 1;
      } else if (c > 0) {
        lower = middle + 1;
      } else {
        return middle;
      }
    }
    return -lower - 1;
  }
  @Override public boolean containsValue(@Nullable Object value) {
    if (value == null) {
      return false;
    }
    for (int i = fromIndex; i < toIndex; i++) {
      if (entries[i].getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
  private transient ImmutableSet<Entry<K, V>> entrySet;
  @Override public ImmutableSet<Entry<K, V>> entrySet() {
    ImmutableSet<Entry<K, V>> es = entrySet;
    return (es == null) ? (entrySet = createEntrySet()) : es;
  }
  private ImmutableSet<Entry<K, V>> createEntrySet() {
    return isEmpty() ? ImmutableSet.<Entry<K, V>>of()
        : new EntrySet<K, V>(this);
  }
  @SuppressWarnings("serial") 
  private static class EntrySet<K, V> extends ImmutableSet<Entry<K, V>> {
    final transient ImmutableSortedMap<K, V> map;
    EntrySet(ImmutableSortedMap<K, V> map) {
      this.map = map;
    }
    public int size() {
      return map.size();
    }
    @Override public UnmodifiableIterator<Entry<K, V>> iterator() {
      return Iterators.forArray(map.entries, map.fromIndex, size());
    }
    @Override public boolean contains(Object target) {
      if (target instanceof Entry) {
        Entry<?, ?> entry = (Entry<?, ?>) target;
        V mappedValue = map.get(entry.getKey());
        return mappedValue != null && mappedValue.equals(entry.getValue());
      }
      return false;
    }
    @Override Object writeReplace() {
      return new EntrySetSerializedForm<K, V>(map);
    }
  }
  private static class EntrySetSerializedForm<K, V> implements Serializable {
    final ImmutableSortedMap<K, V> map;
    EntrySetSerializedForm(ImmutableSortedMap<K, V> map) {
      this.map = map;
    }
    Object readResolve() {
      return map.entrySet();
    }
    private static final long serialVersionUID = 0;
  }
  private transient ImmutableSortedSet<K> keySet;
  @Override public ImmutableSortedSet<K> keySet() {
    ImmutableSortedSet<K> ks = keySet;
    return (ks == null) ? (keySet = createKeySet()) : ks;
  }
  private ImmutableSortedSet<K> createKeySet() {
    if (isEmpty()) {
      return ImmutableSortedSet.emptySet(comparator);
    }
    Object[] array = new Object[size()];
    for (int i = fromIndex; i < toIndex; i++) {
      array[i - fromIndex] = entries[i].getKey();
    }
    return new RegularImmutableSortedSet<K>(array, comparator);
  }
  private transient ImmutableCollection<V> values;
  @Override public ImmutableCollection<V> values() {
    ImmutableCollection<V> v = values;
    return (v == null) ? (values = new Values<V>(this)) : v;
  }
  @SuppressWarnings("serial") 
  private static class Values<V> extends ImmutableCollection<V> {
    private final ImmutableSortedMap<?, V> map;
    Values(ImmutableSortedMap<?, V> map) {
      this.map = map;
    }
    public int size() {
      return map.size();
    }
    @Override public UnmodifiableIterator<V> iterator() {
      return new AbstractIterator<V>() {
        int index = map.fromIndex;
        @Override protected V computeNext() {
          return (index < map.toIndex)
              ? map.entries[index++].getValue()
              : endOfData();
        }
      };
    }
    @Override public boolean contains(Object target) {
      return map.containsValue(target);
    }
    @Override Object writeReplace() {
      return new ValuesSerializedForm<V>(map);
    }
  }
  private static class ValuesSerializedForm<V> implements Serializable {
    final ImmutableSortedMap<?, V> map;
    ValuesSerializedForm(ImmutableSortedMap<?, V> map) {
      this.map = map;
    }
    Object readResolve() {
      return map.values();
    }
    private static final long serialVersionUID = 0;
  }
  public Comparator<? super K> comparator() {
    return comparator;
  }
  public K firstKey() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return entries[fromIndex].getKey();
  }
  public K lastKey() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return entries[toIndex - 1].getKey();
  }
  public ImmutableSortedMap<K, V> headMap(K toKey) {
    int newToIndex = findSubmapIndex(checkNotNull(toKey));
    return createSubmap(fromIndex, newToIndex);
  }
  public ImmutableSortedMap<K, V> subMap(K fromKey, K toKey) {
    checkNotNull(fromKey);
    checkNotNull(toKey);
    checkArgument(comparator.compare(fromKey, toKey) <= 0);
    int newFromIndex = findSubmapIndex(fromKey);
    int newToIndex = findSubmapIndex(toKey);
    return createSubmap(newFromIndex, newToIndex);
  }
  public ImmutableSortedMap<K, V> tailMap(K fromKey) {
    int newFromIndex = findSubmapIndex(checkNotNull(fromKey));
    return createSubmap(newFromIndex, toIndex);
  }
  private int findSubmapIndex(K key) {
    int index = binarySearch(key);
    return (index >= 0) ? index : (-index - 1);
  }
  private ImmutableSortedMap<K, V> createSubmap(
      int newFromIndex, int newToIndex) {
    if (newFromIndex < newToIndex) {
      return new ImmutableSortedMap<K, V>(entries, comparator,
          newFromIndex, newToIndex);
    } else {
      return emptyMap(comparator);
    }
  }
  private static class SerializedForm extends ImmutableMap.SerializedForm {
    private final Comparator<Object> comparator;
    @SuppressWarnings("unchecked")
    SerializedForm(ImmutableSortedMap<?, ?> sortedMap) {
      super(sortedMap);
      comparator = (Comparator<Object>) sortedMap.comparator();
    }
    @Override Object readResolve() {
      Builder<Object, Object> builder = new Builder<Object, Object>(comparator);
      return createMap(builder);
    }
    private static final long serialVersionUID = 0;
  }
  @Override Object writeReplace() {
    return new SerializedForm(this);
  }
  private static final long serialVersionUID = 0;
}
