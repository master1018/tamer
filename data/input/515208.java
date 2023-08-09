final class SingletonImmutableMap<K, V> extends ImmutableMap<K, V> {
  final transient K singleKey;
  final transient V singleValue;
  private transient Entry<K, V> entry;
  SingletonImmutableMap(K singleKey, V singleValue) {
    this.singleKey = singleKey;
    this.singleValue = singleValue;
  }
  SingletonImmutableMap(Entry<K, V> entry) {
    this.entry = checkNotNull(entry);  
    this.singleKey = checkNotNull(entry.getKey());  
    this.singleValue = checkNotNull(entry.getValue()); 
  }
  private Entry<K, V> entry() {
    Entry<K, V> e = entry;
    return (e == null)
        ? (entry = Maps.immutableEntry(singleKey, singleValue)) : e;
  }
  @Override public V get(Object key) {
    return singleKey.equals(key) ? singleValue : null;
  }
  public int size() {
    return 1;
  }
  @Override public boolean isEmpty() {
    return false;
  }
  @Override public boolean containsKey(Object key) {
    return singleKey.equals(key);
  }
  @Override public boolean containsValue(Object value) {
    return singleValue.equals(value);
  }
  private transient ImmutableSet<Entry<K, V>> entrySet;
  @Override public ImmutableSet<Entry<K, V>> entrySet() {
    ImmutableSet<Entry<K, V>> es = entrySet;
    return (es == null) ? (entrySet = ImmutableSet.of(entry())) : es;
  }
  private transient ImmutableSet<K> keySet;
  @Override public ImmutableSet<K> keySet() {
    ImmutableSet<K> ks = keySet;
    return (ks == null) ? (keySet = ImmutableSet.of(singleKey)) : ks;
  }
  private transient ImmutableCollection<V> values;
  @Override public ImmutableCollection<V> values() {
    ImmutableCollection<V> v = values;
    return (v == null) ? (values = new Values<V>(singleValue)) : v;
  }
  @SuppressWarnings("serial") 
  private static class Values<V> extends ImmutableCollection<V> {
    final V singleValue;
    Values(V singleValue) {
      this.singleValue = singleValue;
    }
    @Override public boolean contains(Object object) {
      return singleValue.equals(object);
    }
    @Override public boolean isEmpty() {
      return false;
    }
    public int size() {
      return 1;
    }
    @Override public UnmodifiableIterator<V> iterator() {
      return Iterators.singletonIterator(singleValue);
    }
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Map) {
      Map<?, ?> that = (Map<?, ?>) object;
      if (that.size() != 1) {
        return false;
      }
      Entry<?, ?> entry = that.entrySet().iterator().next();
      return singleKey.equals(entry.getKey())
          && singleValue.equals(entry.getValue());
    }
    return false;
  }
  @Override public int hashCode() {
    return singleKey.hashCode() ^ singleValue.hashCode();
  }
  @Override public String toString() {
    return new StringBuilder()
        .append('{')
        .append(singleKey.toString())
        .append('=')
        .append(singleValue.toString())
        .append('}')
        .toString();
  }
}
