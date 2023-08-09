public abstract class ImmutableMap<K, V> implements Map<K, V>, Serializable {
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableMap<K, V> of() {
    return (ImmutableMap) EmptyImmutableMap.INSTANCE;
  }
  public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
    return new SingletonImmutableMap<K, V>(
        checkNotNull(k1), checkNotNull(v1));
  }
  public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
    return new RegularImmutableMap<K, V>(entryOf(k1, v1), entryOf(k2, v2));
  }
  public static <K, V> ImmutableMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    return new RegularImmutableMap<K, V>(
        entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
  }
  public static <K, V> ImmutableMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return new RegularImmutableMap<K, V>(
        entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
  }
  public static <K, V> ImmutableMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return new RegularImmutableMap<K, V>(entryOf(k1, v1),
        entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
  }
  public static <K, V> Builder<K, V> builder() {
    return new Builder<K, V>();
  }
  static <K, V> Entry<K, V> entryOf(K key, V value) {
    return Maps.immutableEntry(checkNotNull(key), checkNotNull(value));
  }
  public static class Builder<K, V> {
    final List<Entry<K, V>> entries = Lists.newArrayList();
    public Builder() {}
    public Builder<K, V> put(K key, V value) {
      entries.add(entryOf(key, value));
      return this;
    }
    public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
      for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
      return this;
    }
    public ImmutableMap<K, V> build() {
      return fromEntryList(entries);
    }
    private static <K, V> ImmutableMap<K, V> fromEntryList(
        List<Entry<K, V>> entries) {
      int size = entries.size();
      switch (size) {
        case 0:
          return of();
        case 1:
          return new SingletonImmutableMap<K, V>(getOnlyElement(entries));
        default:
          Entry<?, ?>[] entryArray
              = entries.toArray(new Entry<?, ?>[entries.size()]);
          return new RegularImmutableMap<K, V>(entryArray);
      }
    }
  }
  public static <K, V> ImmutableMap<K, V> copyOf(
      Map<? extends K, ? extends V> map) {
    if ((map instanceof ImmutableMap) && !(map instanceof ImmutableSortedMap)) {
      @SuppressWarnings("unchecked") 
      ImmutableMap<K, V> kvMap = (ImmutableMap<K, V>) map;
      return kvMap;
    }
    @SuppressWarnings("unchecked") 
    Entry<K, V>[] entries = map.entrySet().toArray(new Entry[0]);
    switch (entries.length) {
      case 0:
        return of();
      case 1:
        return new SingletonImmutableMap<K, V>(entryOf(
            entries[0].getKey(), entries[0].getValue()));
      default:
        for (int i = 0; i < entries.length; i++) {
          K k = entries[i].getKey();
          V v = entries[i].getValue();
          entries[i] = entryOf(k, v);
        }
        return new RegularImmutableMap<K, V>(entries);
    }
  }
  ImmutableMap() {}
  public final V put(K k, V v) {
    throw new UnsupportedOperationException();
  }
  public final V remove(Object o) {
    throw new UnsupportedOperationException();
  }
  public final void putAll(Map<? extends K, ? extends V> map) {
    throw new UnsupportedOperationException();
  }
  public final void clear() {
    throw new UnsupportedOperationException();
  }
  public boolean isEmpty() {
    return size() == 0;
  }
  public boolean containsKey(@Nullable Object key) {
    return get(key) != null;
  }
  public abstract boolean containsValue(@Nullable Object value);
  public abstract V get(@Nullable Object key);
  public abstract ImmutableSet<Entry<K, V>> entrySet();
  public abstract ImmutableSet<K> keySet();
  public abstract ImmutableCollection<V> values();
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Map) {
      Map<?, ?> that = (Map<?, ?>) object;
      return this.entrySet().equals(that.entrySet());
    }
    return false;
  }
  @Override public int hashCode() {
    return entrySet().hashCode();
  }
  @Override public String toString() {
    StringBuilder result = new StringBuilder(size() * 16).append('{');
    Maps.standardJoiner.appendTo(result, this);
    return result.append('}').toString();
  }
  static class SerializedForm implements Serializable {
    private final Object[] keys;
    private final Object[] values;
    SerializedForm(ImmutableMap<?, ?> map) {
      keys = new Object[map.size()];
      values = new Object[map.size()];
      int i = 0;
      for (Entry<?, ?> entry : map.entrySet()) {
        keys[i] = entry.getKey();
        values[i] = entry.getValue();
        i++;
      }
    }
    Object readResolve() {
      Builder<Object, Object> builder = new Builder<Object, Object>();
      return createMap(builder);
    }
    Object createMap(Builder<Object, Object> builder) {
      for (int i = 0; i < keys.length; i++) {
        builder.put(keys[i], values[i]);
      }
      return builder.build();
    }
    private static final long serialVersionUID = 0;
  }
  Object writeReplace() {
    return new SerializedForm(this);
  }
}
