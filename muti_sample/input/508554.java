public abstract class ImmutableMultimap<K, V>
    implements Multimap<K, V>, Serializable {
  public static <K, V> ImmutableMultimap<K, V> of() {
    return ImmutableListMultimap.of();
  }
  public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1) {
    return ImmutableListMultimap.of(k1, v1);
  }
  public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2) {
    return ImmutableListMultimap.of(k1, v1, k2, v2);
  }
  public static <K, V> ImmutableMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3);
  }
  public static <K, V> ImmutableMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
  }
  public static <K, V> ImmutableMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
  }
  public static <K, V> Builder<K, V> builder() {
    return new Builder<K, V>();
  }
  private static class BuilderMultimap<K, V> extends AbstractMultimap<K, V> {
    BuilderMultimap() {
      super(new LinkedHashMap<K, Collection<V>>());
    }
    @Override Collection<V> createCollection() {
      return Lists.newArrayList();
    }
    private static final long serialVersionUID = 0;
  }
  public static class Builder<K, V> {
    private final Multimap<K, V> builderMultimap = new BuilderMultimap<K, V>();
    public Builder() {}
    public Builder<K, V> put(K key, V value) {
      builderMultimap.put(checkNotNull(key), checkNotNull(value));
      return this;
    }
    public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
      Collection<V> valueList = builderMultimap.get(checkNotNull(key));
      for (V value : values) {
        valueList.add(checkNotNull(value));
      }
      return this;
    }
    public Builder<K, V> putAll(K key, V... values) {
      return putAll(key, Arrays.asList(values));
    }
    public Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
      for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
          : multimap.asMap().entrySet()) {
        putAll(entry.getKey(), entry.getValue());
      }
      return this;
    }
    public ImmutableMultimap<K, V> build() {
      return copyOf(builderMultimap);
    }
  }
  public static <K, V> ImmutableMultimap<K, V> copyOf(
      Multimap<? extends K, ? extends V> multimap) {
    if (multimap instanceof ImmutableMultimap) {
      @SuppressWarnings("unchecked") 
      ImmutableMultimap<K, V> kvMultimap
          = (ImmutableMultimap<K, V>) multimap;
      return kvMultimap;
    } else {
      return ImmutableListMultimap.copyOf(multimap);
    }
  }
  final transient ImmutableMap<K, ? extends ImmutableCollection<V>> map;
  final transient int size;
  static class FieldSettersHolder {
    @SuppressWarnings("unchecked")
    static final Serialization.FieldSetter<ImmutableMultimap>
        MAP_FIELD_SETTER = Serialization.getFieldSetter(
        ImmutableMultimap.class, "map");
    @SuppressWarnings("unchecked")
    static final Serialization.FieldSetter<ImmutableMultimap>
        SIZE_FIELD_SETTER = Serialization.getFieldSetter(
        ImmutableMultimap.class, "size");
  }
  ImmutableMultimap(ImmutableMap<K, ? extends ImmutableCollection<V>> map,
      int size) {
    this.map = map;
    this.size = size;
  }
  public ImmutableCollection<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }
  public ImmutableCollection<V> replaceValues(K key,
      Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }
  public void clear() {
    throw new UnsupportedOperationException();
  }
  public abstract ImmutableCollection<V> get(K key);
  public boolean put(K key, V value) {
    throw new UnsupportedOperationException();
  }
  public boolean putAll(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    throw new UnsupportedOperationException();
  }
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    Collection<V> values = map.get(key);
    return values != null && values.contains(value);
  }
  public boolean containsKey(@Nullable Object key) {
    return map.containsKey(key);
  }
  public boolean containsValue(@Nullable Object value) {
    for (Collection<V> valueCollection : map.values()) {
      if (valueCollection.contains(value)) {
        return true;
      }
    }
    return false;
  }
  public boolean isEmpty() {
    return size == 0;
  }
  public int size() {
    return size;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof Multimap) {
      Multimap<?, ?> that = (Multimap<?, ?>) object;
      return this.map.equals(that.asMap());
    }
    return false;
  }
  @Override public int hashCode() {
    return map.hashCode();
  }
  @Override public String toString() {
    return map.toString();
  }
  public ImmutableSet<K> keySet() {
    return map.keySet();
  }
  @SuppressWarnings("unchecked") 
  public ImmutableMap<K, Collection<V>> asMap() {
    return (ImmutableMap) map;
  }
  private transient ImmutableCollection<Map.Entry<K, V>> entries;
  public ImmutableCollection<Map.Entry<K, V>> entries() {
    ImmutableCollection<Map.Entry<K, V>> result = entries;
    return (result == null)
        ? (entries = new EntryCollection<K, V>(this)) : result;
  }
  private static class EntryCollection<K, V>
      extends ImmutableCollection<Map.Entry<K, V>> {
    final ImmutableMultimap<K, V> multimap;
    EntryCollection(ImmutableMultimap<K, V> multimap) {
      this.multimap = multimap;
    }
    @Override public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
      final Iterator<? extends Map.Entry<K, ? extends ImmutableCollection<V>>>
          mapIterator = this.multimap.map.entrySet().iterator();
      return new UnmodifiableIterator<Map.Entry<K, V>>() {
        K key;
        Iterator<V> valueIterator;
        public boolean hasNext() {
          return (key != null && valueIterator.hasNext())
              || mapIterator.hasNext();
        }
        public Map.Entry<K, V> next() {
          if (key == null || !valueIterator.hasNext()) {
            Map.Entry<K, ? extends ImmutableCollection<V>> entry
                = mapIterator.next();
            key = entry.getKey();
            valueIterator = entry.getValue().iterator();
          }
          return Maps.immutableEntry(key, valueIterator.next());
        }
      };
    }
    public int size() {
      return multimap.size();
    }
    @Override public boolean contains(Object object) {
      if (object instanceof Map.Entry) {
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
        return multimap.containsEntry(entry.getKey(), entry.getValue());
      }
      return false;
    }
    private static final long serialVersionUID = 0;
  }
  private transient ImmutableMultiset<K> keys;
  public ImmutableMultiset<K> keys() {
    ImmutableMultiset<K> result = keys;
    return (result == null) ? (keys = createKeys()) : result;
  }
  private ImmutableMultiset<K> createKeys() {
    ImmutableMultiset.Builder<K> builder = ImmutableMultiset.builder();
    for (Map.Entry<K, ? extends ImmutableCollection<V>> entry
        : map.entrySet()) {
      builder.addCopies(entry.getKey(), entry.getValue().size());
    }
    return builder.build();
  }
  private transient ImmutableCollection<V> values;
  public ImmutableCollection<V> values() {
    ImmutableCollection<V> result = values;
    return (result == null) ? (values = new Values<V>(this)) : result;
  }
  private static class Values<V> extends ImmutableCollection<V> {
    final Multimap<?, V> multimap;
    Values(Multimap<?, V> multimap) {
      this.multimap = multimap;
    }
    @Override public UnmodifiableIterator<V> iterator() {
      final Iterator<? extends Map.Entry<?, V>> entryIterator
          = multimap.entries().iterator();
      return new UnmodifiableIterator<V>() {
        public boolean hasNext() {
          return entryIterator.hasNext();
        }
        public V next() {
          return entryIterator.next().getValue();
        }
      };
    }
    public int size() {
      return multimap.size();
    }
    private static final long serialVersionUID = 0;
  }
  private static final long serialVersionUID = 0;
}
