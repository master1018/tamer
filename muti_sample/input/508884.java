public class ImmutableListMultimap<K, V>
    extends ImmutableMultimap<K, V>
    implements ListMultimap<K, V> {
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableListMultimap<K, V> of() {
    return (ImmutableListMultimap) EmptyImmutableListMultimap.INSTANCE;
  }
  public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1) {
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    return builder.build();
  }
  public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2) {
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    return builder.build();
  }
  public static <K, V> ImmutableListMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    return builder.build();
  }
  public static <K, V> ImmutableListMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    return builder.build();
  }
  public static <K, V> ImmutableListMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
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
  public static final class Builder<K, V>
      extends ImmutableMultimap.Builder<K, V> {
    public Builder() {}
    @Override public Builder<K, V> put(K key, V value) {
      super.put(key, value);
      return this;
    }
    @Override public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
      super.putAll(key, values);
      return this;
    }
    @Override public Builder<K, V> putAll(K key, V... values) {
      super.putAll(key, values);
      return this;
    }
    @Override public Builder<K, V> putAll(
        Multimap<? extends K, ? extends V> multimap) {
      super.putAll(multimap);
      return this;
    }
    @Override public ImmutableListMultimap<K, V> build() {
      return (ImmutableListMultimap<K, V>) super.build();
    }
  }
  public static <K, V> ImmutableListMultimap<K, V> copyOf(
      Multimap<? extends K, ? extends V> multimap) {
    if (multimap.isEmpty()) {
      return of();
    }
    if (multimap instanceof ImmutableListMultimap) {
      @SuppressWarnings("unchecked") 
      ImmutableListMultimap<K, V> kvMultimap
          = (ImmutableListMultimap<K, V>) multimap;
      return kvMultimap;
    }
    ImmutableMap.Builder<K, ImmutableList<V>> builder = ImmutableMap.builder();
    int size = 0;
    for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
        : multimap.asMap().entrySet()) {
      ImmutableList<V> list = ImmutableList.copyOf(entry.getValue());
      if (!list.isEmpty()) {
        builder.put(entry.getKey(), list);
        size += list.size();
      }
    }
    return new ImmutableListMultimap<K, V>(builder.build(), size);
  }
  ImmutableListMultimap(ImmutableMap<K, ImmutableList<V>> map, int size) {
    super(map, size);
  }
  @Override public ImmutableList<V> get(@Nullable K key) {
    ImmutableList<V> list = (ImmutableList<V>) map.get(key);
    return (list == null) ? ImmutableList.<V>of() : list;
  }
  @Override public ImmutableList<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }
  @Override public ImmutableList<V> replaceValues(
      K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
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
    ImmutableMap.Builder<Object, ImmutableList<Object>> builder
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
      builder.put(key, ImmutableList.of(array));
      tmpSize += valueCount;
    }
    ImmutableMap<Object, ImmutableList<Object>> tmpMap;
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
