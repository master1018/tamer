public final class LinkedHashMultimap<K, V> extends AbstractSetMultimap<K, V> {
  private static final int DEFAULT_VALUES_PER_KEY = 8;
  @VisibleForTesting
  transient int expectedValuesPerKey = DEFAULT_VALUES_PER_KEY;
  transient Collection<Map.Entry<K, V>> linkedEntries;
  public static <K, V> LinkedHashMultimap<K, V> create() {
    return new LinkedHashMultimap<K, V>();
  }
  public static <K, V> LinkedHashMultimap<K, V> create(
      int expectedKeys, int expectedValuesPerKey) {
    return new LinkedHashMultimap<K, V>(expectedKeys, expectedValuesPerKey);
  }
  public static <K, V> LinkedHashMultimap<K, V> create(
      Multimap<? extends K, ? extends V> multimap) {
    return new LinkedHashMultimap<K, V>(multimap);
  }
  private LinkedHashMultimap() {
    super(new LinkedHashMap<K, Collection<V>>());
    linkedEntries = Sets.newLinkedHashSet();
  }
  private LinkedHashMultimap(int expectedKeys, int expectedValuesPerKey) {
    super(new LinkedHashMap<K, Collection<V>>(expectedKeys));
    Preconditions.checkArgument(expectedValuesPerKey >= 0);
    this.expectedValuesPerKey = expectedValuesPerKey;
    linkedEntries = new LinkedHashSet<Map.Entry<K, V>>(
        expectedKeys * expectedValuesPerKey);
  }
  private LinkedHashMultimap(Multimap<? extends K, ? extends V> multimap) {
    super(new LinkedHashMap<K, Collection<V>>(
        Maps.capacity(multimap.keySet().size())));
    linkedEntries
        = new LinkedHashSet<Map.Entry<K, V>>(Maps.capacity(multimap.size()));
    putAll(multimap);
  }
  @Override Set<V> createCollection() {
    return new LinkedHashSet<V>(Maps.capacity(expectedValuesPerKey));
  }
  @Override Collection<V> createCollection(@Nullable K key) {
    return new SetDecorator(key, createCollection());
  }
  private class SetDecorator extends ForwardingSet<V> {
    final Set<V> delegate;
    final K key;
    SetDecorator(@Nullable K key, Set<V> delegate) {
      this.delegate = delegate;
      this.key = key;
    }
    @Override protected Set<V> delegate() {
      return delegate;
    }
    <E> Map.Entry<K, E> createEntry(@Nullable E value) {
      return Maps.immutableEntry(key, value);
    }
    <E> Collection<Map.Entry<K, E>> createEntries(Collection<E> values) {
      Collection<Map.Entry<K, E>> entries
          = Lists.newArrayListWithExpectedSize(values.size());
      for (E value : values) {
        entries.add(createEntry(value));
      }
      return entries;
    }
    @Override public boolean add(@Nullable V value) {
      boolean changed = delegate.add(value);
      if (changed) {
        linkedEntries.add(createEntry(value));
      }
      return changed;
    }
    @Override public boolean addAll(Collection<? extends V> values) {
      boolean changed = delegate.addAll(values);
      if (changed) {
        linkedEntries.addAll(createEntries(delegate()));
      }
      return changed;
    }
    @Override public void clear() {
      linkedEntries.removeAll(createEntries(delegate()));
      delegate.clear();
    }
    @Override public Iterator<V> iterator() {
      final Iterator<V> delegateIterator = delegate.iterator();
      return new Iterator<V>() {
        V value;
        public boolean hasNext() {
          return delegateIterator.hasNext();
        }
        public V next() {
          value = delegateIterator.next();
          return value;
        }
        public void remove() {
          delegateIterator.remove();
          linkedEntries.remove(createEntry(value));
        }
      };
    }
    @Override public boolean remove(@Nullable Object value) {
      boolean changed = delegate.remove(value);
      if (changed) {
        linkedEntries.remove(createEntry(value));
      }
      return changed;
    }
    @Override public boolean removeAll(Collection<?> values) {
      boolean changed = delegate.removeAll(values);
      if (changed) {
        linkedEntries.removeAll(createEntries(values));
      }
      return changed;
    }
    @Override public boolean retainAll(Collection<?> values) {
      boolean changed = false;
      Iterator<V> iterator = delegate.iterator();
      while (iterator.hasNext()) {
        V value = iterator.next();
        if (!values.contains(value)) {
          iterator.remove();
          linkedEntries.remove(Maps.immutableEntry(key, value));
          changed = true;
        }
      }
      return changed;
    }
  }
  @Override Iterator<Map.Entry<K, V>> createEntryIterator() {
    final Iterator<Map.Entry<K, V>> delegateIterator = linkedEntries.iterator();
    return new Iterator<Map.Entry<K, V>>() {
      Map.Entry<K, V> entry;
      public boolean hasNext() {
        return delegateIterator.hasNext();
      }
      public Map.Entry<K, V> next() {
        entry = delegateIterator.next();
        return entry;
      }
      public void remove() {
        delegateIterator.remove();
        LinkedHashMultimap.this.remove(entry.getKey(), entry.getValue());
      }
    };
  }
  @Override public Set<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return super.replaceValues(key, values);
  }
  @Override public Set<Map.Entry<K, V>> entries() {
    return super.entries();
  }
  @Override public Collection<V> values() {
    return super.values();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(expectedValuesPerKey);
    Serialization.writeMultimap(this, stream);
    for (Map.Entry<K, V> entry : linkedEntries) {
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    }
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    expectedValuesPerKey = stream.readInt();
    int distinctKeys = Serialization.readCount(stream);
    setMap(new LinkedHashMap<K, Collection<V>>(Maps.capacity(distinctKeys)));
    linkedEntries = new LinkedHashSet<Map.Entry<K, V>>(
        distinctKeys * expectedValuesPerKey);
    Serialization.populateMultimap(this, stream, distinctKeys);
    linkedEntries.clear(); 
    for (int i = 0; i < size(); i++) {
      @SuppressWarnings("unchecked") 
      K key = (K) stream.readObject();
      @SuppressWarnings("unchecked") 
      V value = (V) stream.readObject();
      linkedEntries.add(Maps.immutableEntry(key, value));
    }
  }
  private static final long serialVersionUID = 0;
}
