final class Synchronized {
  private Synchronized() {}
  static class SynchronizedObject implements Serializable {
    private final Object delegate;
    protected final Object mutex;
    public SynchronizedObject(Object delegate, @Nullable Object mutex) {
      this.delegate = checkNotNull(delegate);
      this.mutex = (mutex == null) ? this : mutex;
    }
    protected Object delegate() {
      return delegate;
    }
    @Override public String toString() {
      synchronized (mutex) {
        return delegate.toString();
      }
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
      synchronized (mutex) {
        stream.defaultWriteObject();
      }
    }
    private static final long serialVersionUID = 0;
  }
  static <E> Collection<E> collection(
      Collection<E> collection, @Nullable Object mutex) {
    return new SynchronizedCollection<E>(collection, mutex);
  }
  static class SynchronizedCollection<E> extends SynchronizedObject
      implements Collection<E> {
    public SynchronizedCollection(
        Collection<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @SuppressWarnings("unchecked")
    @Override protected Collection<E> delegate() {
      return (Collection<E>) super.delegate();
    }
    public boolean add(E e) {
      synchronized (mutex) {
        return delegate().add(e);
      }
    }
    public boolean addAll(Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(c);
      }
    }
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }
    public boolean contains(Object o) {
      synchronized (mutex) {
        return delegate().contains(o);
      }
    }
    public boolean containsAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().containsAll(c);
      }
    }
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }
    public Iterator<E> iterator() {
      return delegate().iterator(); 
    }
    public boolean remove(Object o) {
      synchronized (mutex) {
        return delegate().remove(o);
      }
    }
    public boolean removeAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().removeAll(c);
      }
    }
    public boolean retainAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().retainAll(c);
      }
    }
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }
    public Object[] toArray() {
      synchronized (mutex) {
        return delegate().toArray();
      }
    }
    public <T> T[] toArray(T[] a) {
      synchronized (mutex) {
        return delegate().toArray(a);
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <E> Set<E> set(Set<E> set, @Nullable Object mutex) {
    return new SynchronizedSet<E>(set, mutex);
  }
  static class SynchronizedSet<E> extends SynchronizedCollection<E>
      implements Set<E> {
    public SynchronizedSet(Set<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected Set<E> delegate() {
      return (Set<E>) super.delegate();
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }
    @Override public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }
    private static final long serialVersionUID = 0;
  }
  static <E> SortedSet<E> sortedSet(SortedSet<E> set, @Nullable Object mutex) {
    return new SynchronizedSortedSet<E>(set, mutex);
  }
  static class SynchronizedSortedSet<E> extends SynchronizedSet<E>
      implements SortedSet<E> {
    public SynchronizedSortedSet(
        SortedSet<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected SortedSet<E> delegate() {
      return (SortedSet<E>) super.delegate();
    }
    public Comparator<? super E> comparator() {
      synchronized (mutex) {
        return delegate().comparator();
      }
    }
    public SortedSet<E> subSet(E fromElement, E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().subSet(fromElement, toElement), mutex);
      }
    }
    public SortedSet<E> headSet(E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().headSet(toElement), mutex);
      }
    }
    public SortedSet<E> tailSet(E fromElement) {
      synchronized (mutex) {
        return sortedSet(delegate().tailSet(fromElement), mutex);
      }
    }
    public E first() {
      synchronized (mutex) {
        return delegate().first();
      }
    }
    public E last() {
      synchronized (mutex) {
        return delegate().last();
      }
    }
    private static final long serialVersionUID = 0;
  }
  static <E> List<E> list(List<E> list, @Nullable Object mutex) {
    return (list instanceof RandomAccess)
        ? new SynchronizedRandomAccessList<E>(list, mutex)
        : new SynchronizedList<E>(list, mutex);
  }
  static class SynchronizedList<E> extends SynchronizedCollection<E>
      implements List<E> {
    public SynchronizedList(List<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected List<E> delegate() {
      return (List<E>) super.delegate();
    }
    public void add(int index, E element) {
      synchronized (mutex) {
        delegate().add(index, element);
      }
    }
    public boolean addAll(int index, Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(index, c);
      }
    }
    public E get(int index) {
      synchronized (mutex) {
        return delegate().get(index);
      }
    }
    public int indexOf(Object o) {
      synchronized (mutex) {
        return delegate().indexOf(o);
      }
    }
    public int lastIndexOf(Object o) {
      synchronized (mutex) {
        return delegate().lastIndexOf(o);
      }
    }
    public ListIterator<E> listIterator() {
      return delegate().listIterator(); 
    }
    public ListIterator<E> listIterator(int index) {
      return delegate().listIterator(index); 
    }
    public E remove(int index) {
      synchronized (mutex) {
        return delegate().remove(index);
      }
    }
    public E set(int index, E element) {
      synchronized (mutex) {
        return delegate().set(index, element);
      }
    }
    @GwtIncompatible("List.subList")
    public List<E> subList(int fromIndex, int toIndex) {
      synchronized (mutex) {
        return list(Platform.subList(delegate(), fromIndex, toIndex), mutex);
      }
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }
    @Override public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }
    private static final long serialVersionUID = 0;
  }
  static class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
      implements RandomAccess {
    public SynchronizedRandomAccessList(List<E> list, @Nullable Object mutex) {
      super(list, mutex);
    }
    private static final long serialVersionUID = 0;
  }
  private static <E> Multiset<E> multiset(
      Multiset<E> multiset, @Nullable Object mutex) {
    return new SynchronizedMultiset<E>(multiset, mutex);
  }
  static class SynchronizedMultiset<E> extends SynchronizedCollection<E>
      implements Multiset<E> {
    private transient Set<E> elementSet;
    private transient Set<Entry<E>> entrySet;
    public SynchronizedMultiset(Multiset<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected Multiset<E> delegate() {
      return (Multiset<E>) super.delegate();
    }
    public int count(Object o) {
      synchronized (mutex) {
        return delegate().count(o);
      }
    }
    public int add(E e, int n) {
      synchronized (mutex) {
        return delegate().add(e, n);
      }
    }
    public int remove(Object o, int n) {
      synchronized (mutex) {
        return delegate().remove(o, n);
      }
    }
    public int setCount(E element, int count) {
      synchronized (mutex) {
        return delegate().setCount(element, count);
      }
    }
    public boolean setCount(E element, int oldCount, int newCount) {
      synchronized (mutex) {
        return delegate().setCount(element, oldCount, newCount);
      }
    }
    public Set<E> elementSet() {
      synchronized (mutex) {
        if (elementSet == null) {
          elementSet = typePreservingSet(delegate().elementSet(), mutex);
        }
        return elementSet;
      }
    }
    public Set<Entry<E>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = typePreservingSet(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }
    @Override public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> Multimap<K, V> multimap(
      Multimap<K, V> multimap, @Nullable Object mutex) {
    return new SynchronizedMultimap<K, V>(multimap, mutex);
  }
  private static class SynchronizedMultimap<K, V> extends SynchronizedObject
      implements Multimap<K, V> {
    transient Set<K> keySet;
    transient Collection<V> valuesCollection;
    transient Collection<Map.Entry<K, V>> entries;
    transient Map<K, Collection<V>> asMap;
    transient Multiset<K> keys;
    @SuppressWarnings("unchecked")
    @Override protected Multimap<K, V> delegate() {
      return (Multimap<K, V>) super.delegate();
    }
    SynchronizedMultimap(Multimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }
    public boolean containsKey(Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }
    public boolean containsValue(Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }
    public boolean containsEntry(Object key, Object value) {
      synchronized (mutex) {
        return delegate().containsEntry(key, value);
      }
    }
    public Collection<V> get(K key) {
      synchronized (mutex) {
        return typePreservingCollection(delegate().get(key), mutex);
      }
    }
    public boolean put(K key, V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }
    public boolean putAll(K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().putAll(key, values);
      }
    }
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      synchronized (mutex) {
        return delegate().putAll(multimap);
      }
    }
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); 
      }
    }
    public boolean remove(Object key, Object value) {
      synchronized (mutex) {
        return delegate().remove(key, value);
      }
    }
    public Collection<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); 
      }
    }
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = typePreservingSet(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }
    public Collection<V> values() {
      synchronized (mutex) {
        if (valuesCollection == null) {
          valuesCollection = collection(delegate().values(), mutex);
        }
        return valuesCollection;
      }
    }
    public Collection<Map.Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entries == null) {
          entries = typePreservingCollection(delegate().entries(), mutex);
        }
        return entries;
      }
    }
    public Map<K, Collection<V>> asMap() {
      synchronized (mutex) {
        if (asMap == null) {
          asMap = new SynchronizedAsMap<K, V>(delegate().asMap(), mutex);
        }
        return asMap;
      }
    }
    public Multiset<K> keys() {
      synchronized (mutex) {
        if (keys == null) {
          keys = multiset(delegate().keys(), mutex);
        }
        return keys;
      }
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }
    @Override public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> ListMultimap<K, V> listMultimap(
      ListMultimap<K, V> multimap, @Nullable Object mutex) {
    return new SynchronizedListMultimap<K, V>(multimap, mutex);
  }
  private static class SynchronizedListMultimap<K, V>
      extends SynchronizedMultimap<K, V> implements ListMultimap<K, V> {
    SynchronizedListMultimap(
        ListMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected ListMultimap<K, V> delegate() {
      return (ListMultimap<K, V>) super.delegate();
    }
    @Override public List<V> get(K key) {
      synchronized (mutex) {
        return list(delegate().get(key), mutex);
      }
    }
    @Override public List<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); 
      }
    }
    @Override public List<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); 
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> SetMultimap<K, V> setMultimap(
      SetMultimap<K, V> multimap, @Nullable Object mutex) {
    return new SynchronizedSetMultimap<K, V>(multimap, mutex);
  }
  private static class SynchronizedSetMultimap<K, V>
      extends SynchronizedMultimap<K, V> implements SetMultimap<K, V> {
    transient Set<Map.Entry<K, V>> entrySet;
    SynchronizedSetMultimap(
        SetMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected SetMultimap<K, V> delegate() {
      return (SetMultimap<K, V>) super.delegate();
    }
    @Override public Set<V> get(K key) {
      synchronized (mutex) {
        return set(delegate().get(key), mutex);
      }
    }
    @Override public Set<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); 
      }
    }
    @Override public Set<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); 
      }
    }
    @Override public Set<Map.Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entries(), mutex);
        }
        return entrySet;
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> SortedSetMultimap<K, V> sortedSetMultimap(
      SortedSetMultimap<K, V> multimap, @Nullable Object mutex) {
    return new SynchronizedSortedSetMultimap<K, V>(multimap, mutex);
  }
  private static class SynchronizedSortedSetMultimap<K, V>
      extends SynchronizedSetMultimap<K, V> implements SortedSetMultimap<K, V> {
    SynchronizedSortedSetMultimap(
        SortedSetMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override protected SortedSetMultimap<K, V> delegate() {
      return (SortedSetMultimap<K, V>) super.delegate();
    }
    @Override public SortedSet<V> get(K key) {
      synchronized (mutex) {
        return sortedSet(delegate().get(key), mutex);
      }
    }
    @Override public SortedSet<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); 
      }
    }
    @Override public SortedSet<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); 
      }
    }
    public Comparator<? super V> valueComparator() {
      synchronized (mutex) {
        return delegate().valueComparator();
      }
    }
    private static final long serialVersionUID = 0;
  }
  private static <E> Collection<E> typePreservingCollection(
      Collection<E> collection, @Nullable Object mutex) {
    if (collection instanceof SortedSet) {
      return sortedSet((SortedSet<E>) collection, mutex);
    } else if (collection instanceof Set) {
      return set((Set<E>) collection, mutex);
    } else if (collection instanceof List) {
      return list((List<E>) collection, mutex);
    } else {
      return collection(collection, mutex);
    }
  }
  public static <E> Set<E> typePreservingSet(
      Set<E> set, @Nullable Object mutex) {
    if (set instanceof SortedSet) {
      return sortedSet((SortedSet<E>) set, mutex);
    } else {
      return set(set, mutex);
    }
  }
  static class SynchronizedAsMapEntries<K, V>
      extends SynchronizedSet<Map.Entry<K, Collection<V>>> {
    public SynchronizedAsMapEntries(
        Set<Map.Entry<K, Collection<V>>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override public Iterator<Map.Entry<K, Collection<V>>> iterator() {
      final Iterator<Map.Entry<K, Collection<V>>> iterator = super.iterator();
      return new ForwardingIterator<Map.Entry<K, Collection<V>>>() {
        @Override protected Iterator<Map.Entry<K, Collection<V>>> delegate() {
          return iterator;
        }
        @Override public Map.Entry<K, Collection<V>> next() {
          final Map.Entry<K, Collection<V>> entry = iterator.next();
          return new ForwardingMapEntry<K, Collection<V>>() {
            @Override protected Map.Entry<K, Collection<V>> delegate() {
              return entry;
            }
            @Override public Collection<V> getValue() {
              return typePreservingCollection(entry.getValue(), mutex);
            }
          };
        }
      };
    }
    @Override public Object[] toArray() {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate());
      }
    }
    @Override public <T> T[] toArray(T[] array) {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate(), array);
      }
    }
    @Override public boolean contains(Object o) {
      synchronized (mutex) {
        return Maps.containsEntryImpl(delegate(), o);
      }
    }
    @Override public boolean containsAll(Collection<?> c) {
      synchronized (mutex) {
        return Collections2.containsAll(delegate(), c);
      }
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return Collections2.setEquals(delegate(), o);
      }
    }
    @Override public boolean remove(Object o) {
      synchronized (mutex) {
        return Maps.removeEntryImpl(delegate(), o);
      }
    }
    @Override public boolean removeAll(Collection<?> c) {
      synchronized (mutex) {
        return Iterators.removeAll(delegate().iterator(), c);
      }
    }
    @Override public boolean retainAll(Collection<?> c) {
      synchronized (mutex) {
        return Iterators.retainAll(delegate().iterator(), c);
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> Map<K, V> map(Map<K, V> map, @Nullable Object mutex) {
    return new SynchronizedMap<K, V>(map, mutex);
  }
  static class SynchronizedMap<K, V> extends SynchronizedObject
      implements Map<K, V> {
    private transient Set<K> keySet;
    private transient Collection<V> values;
    private transient Set<Map.Entry<K, V>> entrySet;
    public SynchronizedMap(Map<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @SuppressWarnings("unchecked")
    @Override protected Map<K, V> delegate() {
      return (Map<K, V>) super.delegate();
    }
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }
    public boolean containsKey(Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }
    public boolean containsValue(Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }
    public Set<Map.Entry<K, V>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }
    public V get(Object key) {
      synchronized (mutex) {
        return delegate().get(key);
      }
    }
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = set(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }
    public V put(K key, V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }
    public void putAll(Map<? extends K, ? extends V> map) {
      synchronized (mutex) {
        delegate().putAll(map);
      }
    }
    public V remove(Object key) {
      synchronized (mutex) {
        return delegate().remove(key);
      }
    }
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }
    public Collection<V> values() {
      synchronized (mutex) {
        if (values == null) {
          values = collection(delegate().values(), mutex);
        }
        return values;
      }
    }
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }
    @Override public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }
    private static final long serialVersionUID = 0;
  }
  public static <K, V> BiMap<K, V> biMap(
      BiMap<K, V> bimap, @Nullable Object mutex) {
    return new SynchronizedBiMap<K, V>(bimap, mutex, null);
  }
  static class SynchronizedBiMap<K, V> extends SynchronizedMap<K, V>
      implements BiMap<K, V>, Serializable {
    private transient Set<V> valueSet;
    private transient BiMap<V, K> inverse;
    public SynchronizedBiMap(
        BiMap<K, V> delegate, @Nullable Object mutex,
        @Nullable BiMap<V, K> inverse) {
      super(delegate, mutex);
      this.inverse = inverse;
    }
    @Override protected BiMap<K, V> delegate() {
      return (BiMap<K, V>) super.delegate();
    }
    @Override public Set<V> values() {
      synchronized (mutex) {
        if (valueSet == null) {
          valueSet = set(delegate().values(), mutex);
        }
        return valueSet;
      }
    }
    public V forcePut(K key, V value) {
      synchronized (mutex) {
        return delegate().forcePut(key, value);
      }
    }
    public BiMap<V, K> inverse() {
      synchronized (mutex) {
        if (inverse == null) {
          inverse
              = new SynchronizedBiMap<V, K>(delegate().inverse(), mutex, this);
        }
        return inverse;
      }
    }
    private static final long serialVersionUID = 0;
  }
  static class SynchronizedAsMap<K, V>
      extends SynchronizedMap<K, Collection<V>> {
    private transient Set<Map.Entry<K, Collection<V>>> asMapEntrySet;
    private transient Collection<Collection<V>> asMapValues;
    public SynchronizedAsMap(
        Map<K, Collection<V>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override public Collection<V> get(Object key) {
      synchronized (mutex) {
        Collection<V> collection = super.get(key);
        return (collection == null) ? null
            : typePreservingCollection(collection, mutex);
      }
    }
    @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
      synchronized (mutex) {
        if (asMapEntrySet == null) {
          asMapEntrySet = new SynchronizedAsMapEntries<K, V>(
              delegate().entrySet(), mutex);
        }
        return asMapEntrySet;
      }
    }
    @Override public Collection<Collection<V>> values() {
      synchronized (mutex) {
        if (asMapValues == null) {
          asMapValues
              = new SynchronizedAsMapValues<V>(delegate().values(), mutex);
        }
        return asMapValues;
      }
    }
    @Override public boolean containsValue(Object o) {
      return values().contains(o);
    }
    private static final long serialVersionUID = 0;
  }
  static class SynchronizedAsMapValues<V>
      extends SynchronizedCollection<Collection<V>> {
    SynchronizedAsMapValues(
        Collection<Collection<V>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
    @Override public Iterator<Collection<V>> iterator() {
      final Iterator<Collection<V>> iterator = super.iterator();
      return new ForwardingIterator<Collection<V>>() {
        @Override protected Iterator<Collection<V>> delegate() {
          return iterator;
        }
        @Override public Collection<V> next() {
          return typePreservingCollection(iterator.next(), mutex);
        }
      };
    }
    private static final long serialVersionUID = 0;
  }
}
