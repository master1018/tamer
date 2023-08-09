final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {
  private final transient Entry<K, V>[] entries; 
  private final transient Object[] table; 
  private final transient int mask;
  private final transient int keySetHashCode;
  RegularImmutableMap(Entry<?, ?>... immutableEntries) {
    @SuppressWarnings("unchecked")
    Entry<K, V>[] tmp = (Entry<K, V>[]) checkNotNull(immutableEntries);
    this.entries = tmp;
    int tableSize = Hashing.chooseTableSize(immutableEntries.length);
    table = new Object[tableSize * 2];
    mask = tableSize - 1;
    int keySetHashCodeMutable = 0;
    for (Entry<K, V> entry : this.entries) {
      checkNotNull(entry);  
      K key = checkNotNull(entry.getKey());  
      int keyHashCode = key.hashCode();
      for (int i = Hashing.smear(keyHashCode); true; i++) {
        int index = (i & mask) * 2;
        Object existing = table[index];
        if (existing == null) {
          V value = checkNotNull(entry.getValue());  
          table[index] = key;
          table[index + 1] = value;
          keySetHashCodeMutable += keyHashCode;
          break;
        } else if (existing.equals(key)) {
          throw new IllegalArgumentException("duplicate key: " + key);
        }
      }
    }
    keySetHashCode = keySetHashCodeMutable;
  }
  @Override public V get(Object key) {
    if (key == null) {
      return null;
    }
    for (int i = Hashing.smear(key.hashCode()); true; i++) {
      int index = (i & mask) * 2;
      Object candidate = table[index];
      if (candidate == null) {
        return null;
      }
      if (candidate.equals(key)) {
        @SuppressWarnings("unchecked")
        V value = (V) table[index + 1];
        return value;
      }
    }
  }
  public int size() {
    return entries.length;
  }
  @Override public boolean isEmpty() {
    return false;
  }
  @Override public boolean containsValue(Object value) {
    if (value == null) {
      return false;
    }
    for (Entry<K, V> entry : entries) {
      if (entry.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
  private transient ImmutableSet<Entry<K, V>> entrySet;
  @Override public ImmutableSet<Entry<K, V>> entrySet() {
    ImmutableSet<Entry<K, V>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet<K, V>(this)) : es;
  }
  @SuppressWarnings("serial") 
  private static class EntrySet<K, V> extends ArrayImmutableSet<Entry<K, V>> {
    final transient RegularImmutableMap<K, V> map;
    EntrySet(RegularImmutableMap<K, V> map) {
      super(map.entries);
      this.map = map;
    }
    @Override public boolean contains(Object target) {
      if (target instanceof Entry) {
        Entry<?, ?> entry = (Entry<?, ?>) target;
        V mappedValue = map.get(entry.getKey());
        return mappedValue != null && mappedValue.equals(entry.getValue());
      }
      return false;
    }
  }
  private transient ImmutableSet<K> keySet;
  @Override public ImmutableSet<K> keySet() {
    ImmutableSet<K> ks = keySet;
    return (ks == null) ? (keySet = new KeySet<K, V>(this)) : ks;
  }
  @SuppressWarnings("serial") 
  private static class KeySet<K, V>
      extends TransformedImmutableSet<Entry<K, V>, K> {
    final RegularImmutableMap<K, V> map;
    KeySet(RegularImmutableMap<K, V> map) {
      super(map.entries, map.keySetHashCode);
      this.map = map;
    }
    @Override K transform(Entry<K, V> element) {
      return element.getKey();
    }
    @Override public boolean contains(Object target) {
      return map.containsKey(target);
    }
  }
  private transient ImmutableCollection<V> values;
  @Override public ImmutableCollection<V> values() {
    ImmutableCollection<V> v = values;
    return (v == null) ? (values = new Values<V>(this)) : v;
  }
  @SuppressWarnings("serial") 
  private static class Values<V> extends ImmutableCollection<V> {
    final RegularImmutableMap<?, V> map;
    Values(RegularImmutableMap<?, V> map) {
      this.map = map;
    }
    public int size() {
      return map.entries.length;
    }
    @Override public UnmodifiableIterator<V> iterator() {
      return new AbstractIterator<V>() {
        int index = 0;
        @Override protected V computeNext() {
          return (index < map.entries.length)
              ? map.entries[index++].getValue()
              : endOfData();
        }
      };
    }
    @Override public boolean contains(Object target) {
      return map.containsValue(target);
    }
  }
  @Override public String toString() {
    StringBuilder result = new StringBuilder(size() * 16).append('{');
    Collections2.standardJoiner.appendTo(result, entries);
    return result.append('}').toString();
  }
  private static final long serialVersionUID = 0;
}
