public final class Multimaps {
  private Multimaps() {}
  public static <K, V> Multimap<K, V> newMultimap(Map<K, Collection<V>> map,
      final Supplier<? extends Collection<V>> factory) {
    return new CustomMultimap<K, V>(map, factory);
  }
  private static class CustomMultimap<K, V> extends AbstractMultimap<K, V> {
    transient Supplier<? extends Collection<V>> factory;
    CustomMultimap(Map<K, Collection<V>> map,
        Supplier<? extends Collection<V>> factory) {
      super(map);
      this.factory = checkNotNull(factory);
    }
    @Override protected Collection<V> createCollection() {
      return factory.get();
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(factory);
      stream.writeObject(backingMap());
    }
    @SuppressWarnings("unchecked") 
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      factory = (Supplier<? extends Collection<V>>) stream.readObject();
      Map<K, Collection<V>> map = (Map<K, Collection<V>>) stream.readObject();
      setMap(map);
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> ListMultimap<K, V> newListMultimap(
      Map<K, Collection<V>> map, final Supplier<? extends List<V>> factory) {
    return new CustomListMultimap<K, V>(map, factory);
  }
  private static class CustomListMultimap<K, V>
      extends AbstractListMultimap<K, V> {
    transient Supplier<? extends List<V>> factory;
    CustomListMultimap(Map<K, Collection<V>> map,
        Supplier<? extends List<V>> factory) {
      super(map);
      this.factory = checkNotNull(factory);
    }
    @Override protected List<V> createCollection() {
      return factory.get();
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(factory);
      stream.writeObject(backingMap());
    }
    @SuppressWarnings("unchecked") 
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      factory = (Supplier<? extends List<V>>) stream.readObject();
      Map<K, Collection<V>> map = (Map<K, Collection<V>>) stream.readObject();
      setMap(map);
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> SetMultimap<K, V> newSetMultimap(
      Map<K, Collection<V>> map, final Supplier<? extends Set<V>> factory) {
    return new CustomSetMultimap<K, V>(map, factory);
  }
  private static class CustomSetMultimap<K, V>
      extends AbstractSetMultimap<K, V> {
    transient Supplier<? extends Set<V>> factory;
    CustomSetMultimap(Map<K, Collection<V>> map,
        Supplier<? extends Set<V>> factory) {
      super(map);
      this.factory = checkNotNull(factory);
    }
    @Override protected Set<V> createCollection() {
      return factory.get();
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(factory);
      stream.writeObject(backingMap());
    }
    @SuppressWarnings("unchecked") 
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      factory = (Supplier<? extends Set<V>>) stream.readObject();
      Map<K, Collection<V>> map = (Map<K, Collection<V>>) stream.readObject();
      setMap(map);
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> SortedSetMultimap<K, V> newSortedSetMultimap(
      Map<K, Collection<V>> map,
      final Supplier<? extends SortedSet<V>> factory) {
    return new CustomSortedSetMultimap<K, V>(map, factory);
  }
  private static class CustomSortedSetMultimap<K, V>
      extends AbstractSortedSetMultimap<K, V> {
    transient Supplier<? extends SortedSet<V>> factory;
    transient Comparator<? super V> valueComparator;
    CustomSortedSetMultimap(Map<K, Collection<V>> map,
        Supplier<? extends SortedSet<V>> factory) {
      super(map);
      this.factory = checkNotNull(factory);
      valueComparator = factory.get().comparator();
    }
    @Override protected SortedSet<V> createCollection() {
      return factory.get();
    }
     public Comparator<? super V> valueComparator() {
      return valueComparator;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(factory);
      stream.writeObject(backingMap());
    }
    @SuppressWarnings("unchecked") 
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      factory = (Supplier<? extends SortedSet<V>>) stream.readObject();
      valueComparator = factory.get().comparator();
      Map<K, Collection<V>> map = (Map<K, Collection<V>>) stream.readObject();
      setMap(map);
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V, M extends Multimap<K, V>> M invertFrom(
      Multimap<? extends V, ? extends K> source, M dest) {
    for (Map.Entry<? extends V, ? extends K> entry : source.entries()) {
      dest.put(entry.getValue(), entry.getKey());
    }
    return dest;
  }
  public static <K, V> Multimap<K, V> synchronizedMultimap(
      Multimap<K, V> multimap) {
    return Synchronized.multimap(multimap, null);
  }
  public static <K, V> Multimap<K, V> unmodifiableMultimap(
      Multimap<K, V> delegate) {
    return new UnmodifiableMultimap<K, V>(delegate);
  }
  private static class UnmodifiableMultimap<K, V>
      extends ForwardingMultimap<K, V> implements Serializable {
    final Multimap<K, V> delegate;
    transient Collection<Entry<K, V>> entries;
    transient Multiset<K> keys;
    transient Set<K> keySet;
    transient Collection<V> values;
    transient Map<K, Collection<V>> map;
    UnmodifiableMultimap(final Multimap<K, V> delegate) {
      this.delegate = delegate;
    }
    @Override protected Multimap<K, V> delegate() {
      return delegate;
    }
    @Override public void clear() {
      throw new UnsupportedOperationException();
    }
    @Override public Map<K, Collection<V>> asMap() {
      Map<K, Collection<V>> result = map;
      if (result == null) {
        final Map<K, Collection<V>> unmodifiableMap
            = Collections.unmodifiableMap(delegate.asMap());
        map = result = new ForwardingMap<K, Collection<V>>() {
          @Override protected Map<K, Collection<V>> delegate() {
            return unmodifiableMap;
          }
          Set<Entry<K, Collection<V>>> entrySet;
          @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
            Set<Entry<K, Collection<V>>> result = entrySet;
            return (result == null)
                ? entrySet
                    = unmodifiableAsMapEntries(unmodifiableMap.entrySet())
                : result;
          }
          @Override public Collection<V> get(Object key) {
            Collection<V> collection = unmodifiableMap.get(key);
            return (collection == null)
                ? null : unmodifiableValueCollection(collection);
          }
          Collection<Collection<V>> asMapValues;
          @Override public Collection<Collection<V>> values() {
            Collection<Collection<V>> result = asMapValues;
            return (result == null)
                ? asMapValues
                    = new UnmodifiableAsMapValues<V>(unmodifiableMap.values())
                : result;
          }
          @Override public boolean containsValue(Object o) {
            return values().contains(o);
          }
        };
      }
      return result;
    }
    @Override public Collection<Entry<K, V>> entries() {
      Collection<Entry<K, V>> result = entries;
      if (result == null) {
        entries = result = unmodifiableEntries(delegate.entries());
      }
      return result;
    }
    @Override public Collection<V> get(K key) {
      return unmodifiableValueCollection(delegate.get(key));
    }
    @Override public Multiset<K> keys() {
      Multiset<K> result = keys;
      if (result == null) {
        keys = result = Multisets.unmodifiableMultiset(delegate.keys());
      }
      return result;
    }
    @Override public Set<K> keySet() {
      Set<K> result = keySet;
      if (result == null) {
        keySet = result = Collections.unmodifiableSet(delegate.keySet());
      }
      return result;
    }
    @Override public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean putAll(K key,
        @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    @Override
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }
    @Override public Collection<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    @Override public Collection<V> replaceValues(K key,
        @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    @Override public Collection<V> values() {
      Collection<V> result = values;
      if (result == null) {
        values = result = Collections.unmodifiableCollection(delegate.values());
      }
      return result;
    }
    private static final long serialVersionUID = 0;
  }
  private static class UnmodifiableAsMapValues<V>
      extends ForwardingCollection<Collection<V>> {
    final Collection<Collection<V>> delegate;
    UnmodifiableAsMapValues(Collection<Collection<V>> delegate) {
      this.delegate = Collections.unmodifiableCollection(delegate);
    }
    @Override protected Collection<Collection<V>> delegate() {
      return delegate;
    }
    @Override public Iterator<Collection<V>> iterator() {
      final Iterator<Collection<V>> iterator = delegate.iterator();
      return new Iterator<Collection<V>>() {
        public boolean hasNext() {
          return iterator.hasNext();
        }
        public Collection<V> next() {
          return unmodifiableValueCollection(iterator.next());
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    @Override public Object[] toArray() {
      return ObjectArrays.toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return ObjectArrays.toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return Iterators.contains(iterator(), o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return Collections2.containsAll(this, c);
    }
  }
  private static class UnmodifiableListMultimap<K, V>
      extends UnmodifiableMultimap<K, V> implements ListMultimap<K, V> {
    UnmodifiableListMultimap(ListMultimap<K, V> delegate) {
      super(delegate);
    }
    @Override public ListMultimap<K, V> delegate() {
      return (ListMultimap<K, V>) super.delegate();
    }
    @Override public List<V> get(K key) {
      return Collections.unmodifiableList(delegate().get(key));
    }
    @Override public List<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    @Override public List<V> replaceValues(
        K key, @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    private static final long serialVersionUID = 0;
  }
  private static class UnmodifiableSetMultimap<K, V>
      extends UnmodifiableMultimap<K, V> implements SetMultimap<K, V> {
    UnmodifiableSetMultimap(SetMultimap<K, V> delegate) {
      super(delegate);
    }
    @Override public SetMultimap<K, V> delegate() {
      return (SetMultimap<K, V>) super.delegate();
    }
    @Override public Set<V> get(K key) {
      return Collections.unmodifiableSet(delegate().get(key));
    }
    @Override public Set<Map.Entry<K, V>> entries() {
      return Maps.unmodifiableEntrySet(delegate().entries());
    }
    @Override public Set<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    @Override public Set<V> replaceValues(
        K key, @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    private static final long serialVersionUID = 0;
  }
  private static class UnmodifiableSortedSetMultimap<K, V>
      extends UnmodifiableSetMultimap<K, V> implements SortedSetMultimap<K, V> {
    UnmodifiableSortedSetMultimap(SortedSetMultimap<K, V> delegate) {
      super(delegate);
    }
    @Override public SortedSetMultimap<K, V> delegate() {
      return (SortedSetMultimap<K, V>) super.delegate();
    }
    @Override public SortedSet<V> get(K key) {
      return Collections.unmodifiableSortedSet(delegate().get(key));
    }
    @Override public SortedSet<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    @Override public SortedSet<V> replaceValues(
        K key, @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public Comparator<? super V> valueComparator() {
      return delegate().valueComparator();
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> SetMultimap<K, V> synchronizedSetMultimap(
      SetMultimap<K, V> multimap) {
    return Synchronized.setMultimap(multimap, null);
  }
  public static <K, V> SetMultimap<K, V> unmodifiableSetMultimap(
      SetMultimap<K, V> delegate) {
    return new UnmodifiableSetMultimap<K, V>(delegate);
  }
  public static <K, V> SortedSetMultimap<K, V>
      synchronizedSortedSetMultimap(SortedSetMultimap<K, V> multimap) {
    return Synchronized.sortedSetMultimap(multimap, null);
  }
  public static <K, V> SortedSetMultimap<K, V> unmodifiableSortedSetMultimap(
      SortedSetMultimap<K, V> delegate) {
    return new UnmodifiableSortedSetMultimap<K, V>(delegate);
  }
  public static <K, V> ListMultimap<K, V> synchronizedListMultimap(
      ListMultimap<K, V> multimap) {
    return Synchronized.listMultimap(multimap, null);
  }
  public static <K, V> ListMultimap<K, V> unmodifiableListMultimap(
      ListMultimap<K, V> delegate) {
    return new UnmodifiableListMultimap<K, V>(delegate);
  }
  private static <V> Collection<V> unmodifiableValueCollection(
      Collection<V> collection) {
    if (collection instanceof SortedSet) {
      return Collections.unmodifiableSortedSet((SortedSet<V>) collection);
    } else if (collection instanceof Set) {
      return Collections.unmodifiableSet((Set<V>) collection);
    } else if (collection instanceof List) {
      return Collections.unmodifiableList((List<V>) collection);
    }
    return Collections.unmodifiableCollection(collection);
  }
  private static <K, V> Map.Entry<K, Collection<V>> unmodifiableAsMapEntry(
      final Map.Entry<K, Collection<V>> entry) {
    checkNotNull(entry);
    return new AbstractMapEntry<K, Collection<V>>() {
      @Override public K getKey() {
        return entry.getKey();
      }
      @Override public Collection<V> getValue() {
        return unmodifiableValueCollection(entry.getValue());
      }
    };
  }
  private static <K, V> Collection<Entry<K, V>> unmodifiableEntries(
      Collection<Entry<K, V>> entries) {
    if (entries instanceof Set) {
      return Maps.unmodifiableEntrySet((Set<Entry<K, V>>) entries);
    }
    return new Maps.UnmodifiableEntries<K, V>(
        Collections.unmodifiableCollection(entries));
  }
  private static <K, V> Set<Entry<K, Collection<V>>> unmodifiableAsMapEntries(
      Set<Entry<K, Collection<V>>> asMapEntries) {
    return new UnmodifiableAsMapEntries<K, V>(
        Collections.unmodifiableSet(asMapEntries));
  }
  static class UnmodifiableAsMapEntries<K, V>
      extends ForwardingSet<Entry<K, Collection<V>>> {
    private final Set<Entry<K, Collection<V>>> delegate;
    UnmodifiableAsMapEntries(Set<Entry<K, Collection<V>>> delegate) {
      this.delegate = delegate;
    }
    @Override protected Set<Entry<K, Collection<V>>> delegate() {
      return delegate;
    }
    @Override public Iterator<Entry<K, Collection<V>>> iterator() {
      final Iterator<Entry<K, Collection<V>>> iterator = delegate.iterator();
      return new ForwardingIterator<Entry<K, Collection<V>>>() {
        @Override protected Iterator<Entry<K, Collection<V>>> delegate() {
          return iterator;
        }
        @Override public Entry<K, Collection<V>> next() {
          return unmodifiableAsMapEntry(iterator.next());
        }
      };
    }
    @Override public Object[] toArray() {
      return ObjectArrays.toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return ObjectArrays.toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return Maps.containsEntryImpl(delegate(), o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return Collections2.containsAll(this, c);
    }
    @Override public boolean equals(@Nullable Object object) {
      return Collections2.setEquals(this, object);
    }
  }
  public static <K, V> SetMultimap<K, V> forMap(Map<K, V> map) {
    return new MapMultimap<K, V>(map);
  }
  private static class MapMultimap<K, V>
      implements SetMultimap<K, V>, Serializable {
    final Map<K, V> map;
    transient Map<K, Collection<V>> asMap;
    MapMultimap(Map<K, V> map) {
      this.map = checkNotNull(map);
    }
    public int size() {
      return map.size();
    }
    public boolean isEmpty() {
      return map.isEmpty();
    }
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }
    public boolean containsEntry(Object key, Object value) {
      return map.entrySet().contains(Maps.immutableEntry(key, value));
    }
    public Set<V> get(final K key) {
      return new AbstractSet<V>() {
        @Override public Iterator<V> iterator() {
          return new Iterator<V>() {
            int i;
            public boolean hasNext() {
              return (i == 0) && map.containsKey(key);
            }
            public V next() {
              if (!hasNext()) {
                throw new NoSuchElementException();
              }
              i++;
              return map.get(key);
            }
            public void remove() {
              checkState(i == 1);
              i = -1;
              map.remove(key);
            }
          };
        }
        @Override public int size() {
          return map.containsKey(key) ? 1 : 0;
        }
      };
    }
    public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public boolean putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      throw new UnsupportedOperationException();
    }
    public Set<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public boolean remove(Object key, Object value) {
      return map.entrySet().remove(Maps.immutableEntry(key, value));
    }
    public Set<V> removeAll(Object key) {
      Set<V> values = new HashSet<V>(2);
      if (!map.containsKey(key)) {
        return values;
      }
      values.add(map.remove(key));
      return values;
    }
    public void clear() {
      map.clear();
    }
    public Set<K> keySet() {
      return map.keySet();
    }
    public Multiset<K> keys() {
      return Multisets.forSet(map.keySet());
    }
    public Collection<V> values() {
      return map.values();
    }
    public Set<Entry<K, V>> entries() {
      return map.entrySet();
    }
    public Map<K, Collection<V>> asMap() {
      Map<K, Collection<V>> result = asMap;
      if (result == null) {
        asMap = result = new AsMap();
      }
      return result;
    }
    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof Multimap) {
        Multimap<?, ?> that = (Multimap<?, ?>) object;
        return this.size() == that.size() && asMap().equals(that.asMap());
      }
      return false;
    }
    @Override public int hashCode() {
      return map.hashCode();
    }
    private static final MapJoiner joiner
        = Joiner.on("], ").withKeyValueSeparator("=[").useForNull("null");
    @Override public String toString() {
      if (map.isEmpty()) {
        return "{}";
      }
      StringBuilder builder = new StringBuilder(map.size() * 16).append('{');
      joiner.appendTo(builder, map);
      return builder.append("]}").toString();
    }
    class AsMapEntries extends AbstractSet<Entry<K, Collection<V>>> {
      @Override public int size() {
        return map.size();
      }
      @Override public Iterator<Entry<K, Collection<V>>> iterator() {
        return new Iterator<Entry<K, Collection<V>>>() {
          final Iterator<K> keys = map.keySet().iterator();
          public boolean hasNext() {
            return keys.hasNext();
          }
          public Entry<K, Collection<V>> next() {
            final K key = keys.next();
            return new AbstractMapEntry<K, Collection<V>>() {
              @Override public K getKey() {
                return key;
              }
              @Override public Collection<V> getValue() {
                return get(key);
              }
            };
          }
          public void remove() {
            keys.remove();
          }
        };
      }
      @Override public boolean contains(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> entry = (Entry<?, ?>) o;
        if (!(entry.getValue() instanceof Set)) {
          return false;
        }
        Set<?> set = (Set<?>) entry.getValue();
        return (set.size() == 1)
            && containsEntry(entry.getKey(), set.iterator().next());
      }
      @Override public boolean remove(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> entry = (Entry<?, ?>) o;
        if (!(entry.getValue() instanceof Set)) {
          return false;
        }
        Set<?> set = (Set<?>) entry.getValue();
        return (set.size() == 1)
            && map.entrySet().remove(
                Maps.immutableEntry(entry.getKey(), set.iterator().next()));
      }
    }
    class AsMap extends Maps.ImprovedAbstractMap<K, Collection<V>> {
      @Override protected Set<Entry<K, Collection<V>>> createEntrySet() {
        return new AsMapEntries();
      }
      @Override public boolean containsKey(Object key) {
        return map.containsKey(key);
      }
      @SuppressWarnings("unchecked")
      @Override public Collection<V> get(Object key) {
        Collection<V> collection = MapMultimap.this.get((K) key);
        return collection.isEmpty() ? null : collection;
      }
      @Override public Collection<V> remove(Object key) {
        Collection<V> collection = removeAll(key);
        return collection.isEmpty() ? null : collection;
      }
    }
    private static final long serialVersionUID = 7845222491160860175L;
  }
  public static <K, V> ImmutableListMultimap<K, V> index(
      Iterable<V> values, Function<? super V, K> keyFunction) {
    checkNotNull(keyFunction);
    ImmutableListMultimap.Builder<K, V> builder
        = ImmutableListMultimap.builder();
    for (V value : values) {
      Preconditions.checkNotNull(value, values);
      builder.put(keyFunction.apply(value), value);
    }
    return builder.build();
  }
}
