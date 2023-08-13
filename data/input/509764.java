public class ImmutableSetMultimap<K, V>
    extends ImmutableMultimap<K, V>
    implements SetMultimap<K, V> {
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableSetMultimap<K, V> of() {
    return (ImmutableSetMultimap) EmptyImmutableSetMultimap.INSTANCE;
  }
  public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    return builder.build();
  }
  public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    return builder.build();
  }
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    return builder.build();
  }
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    return builder.build();
  }
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    builder.put(k5, v5);
    return builder.build();
  }
  public static <K, V> Builder<K, V> builder() {
    return new Builder<K, V>();
  }
  private static class BuilderMultimap<K, V> extends AbstractMultimap<K, V> {
    BuilderMultimap() {
      super(new LinkedHashMap<K, Collection<V>>());
    }
    @Override Collection<V> createCollection() {
      return Sets.newLinkedHashSet();
    }
    private static final long serialVersionUID = 0;
  }
  public static final class Builder<K, V>
      extends ImmutableMultimap.Builder<K, V> {
    private final Multimap<K, V> builderMultimap = new BuilderMultimap<K, V>();
    public Builder() {}
    @Override public Builder<K, V> put(K key, V value) {
      builderMultimap.put(checkNotNull(key), checkNotNull(value));
      return this;
    }
    @Override public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
      Collection<V> collection = builderMultimap.get(checkNotNull(key));
      for (V value : values) {
        collection.add(checkNotNull(value));
      }
      return this;
    }
    @Override public Builder<K, V> putAll(K key, V... values) {
      return putAll(key, Arrays.asList(values));
    }
    @Override public Builder<K, V> putAll(
        Multimap<? extends K, ? extends V> multimap) {
      for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
          : multimap.asMap().entrySet()) {
        putAll(entry.getKey(), entry.getValue());
      }
      return this;
    }
    @Override public ImmutableSetMultimap<K, V> build() {
      return copyOf(builderMultimap);
    }
  }
  public static <K, V> ImmutableSetMultimap<K, V> copyOf(
      Multimap<? extends K, ? extends V> multimap) {
    if (multimap.isEmpty()) {
      return of();
    }
    if (multimap instanceof ImmutableSetMultimap) {
      @SuppressWarnings("unchecked") 
      ImmutableSetMultimap<K, V> kvMultimap
          = (ImmutableSetMultimap<K, V>) multimap;
      return kvMultimap;
    }
    ImmutableMap.Builder<K, ImmutableSet<V>> builder = ImmutableMap.builder();
    int size = 0;
    for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
        : multimap.asMap().entrySet()) {
      K key = entry.getKey();
      Collection<? extends V> values = entry.getValue();
      ImmutableSet<V> set = ImmutableSet.copyOf(values);
      if (!set.isEmpty()) {
        builder.put(key, set);
        size += set.size();
      }
    }
    return new ImmutableSetMultimap<K, V>(builder.build(), size);
  }
  ImmutableSetMultimap(ImmutableMap<K, ImmutableSet<V>> map, int size) {
    super(map, size);
  }
  @Override public ImmutableSet<V> get(@Nullable K key) {
    ImmutableSet<V> set = (ImmutableSet<V>) map.get(key);
    return (set == null) ? ImmutableSet.<V>of() : set;
  }
  @Override public ImmutableSet<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }
  @Override public ImmutableSet<V> replaceValues(
      K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }
  private transient ImmutableSet<Map.Entry<K, V>> entries;
  @Override public ImmutableSet<Map.Entry<K, V>> entries() {
    ImmutableSet<Map.Entry<K, V>> result = entries;
    return (result == null)
        ? (entries = ImmutableSet.copyOf(super.entries()))
        : result;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultimap(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int keyCount = stream.readInt();
    if (keyCount < 0) {
      throw new InvalidObjectException("Invalid key count " + keyCount);
    }
    ImmutableMap.Builder<Object, ImmutableSet<Object>> builder
        = ImmutableMap.builder();
    int tmpSize = 0;
    for (int i = 0; i < keyCount; i++) {
      Object key = stream.readObject();
      int valueCount = stream.readInt();
      if (valueCount <= 0) {
        throw new InvalidObjectException("Invalid value count " + valueCount);
      }
      Object[] array = new Object[valueCount];
      for (int j = 0; j < valueCount; j++) {
        array[j] = stream.readObject();
      }
      ImmutableSet<Object> valueSet = ImmutableSet.of(array);
      if (valueSet.size() != array.length) {
        throw new InvalidObjectException(
            "Duplicate key-value pairs exist for key " + key);
      }
      builder.put(key, valueSet);
      tmpSize += valueCount;
    }
    ImmutableMap<Object, ImmutableSet<Object>> tmpMap;
    try {
      tmpMap = builder.build();
    } catch (IllegalArgumentException e) {
      throw (InvalidObjectException)
          new InvalidObjectException(e.getMessage()).initCause(e);
    }
    FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
    FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
  }
  private static final long serialVersionUID = 0;
}
