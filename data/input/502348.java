abstract class AbstractMultimap<K, V> implements Multimap<K, V>, Serializable {
  private transient Map<K, Collection<V>> map;
  private transient int totalSize;
  protected AbstractMultimap(Map<K, Collection<V>> map) {
    checkArgument(map.isEmpty());
    this.map = map;
  }
  final void setMap(Map<K, Collection<V>> map) {
    this.map = map;
    totalSize = 0;
    for (Collection<V> values : map.values()) {
      checkArgument(!values.isEmpty());
      totalSize += values.size();
    }
  }
  abstract Collection<V> createCollection();
  Collection<V> createCollection(@Nullable K key) {
    return createCollection();
  }
  Map<K, Collection<V>> backingMap() {
    return map;
  }
  public int size() {
    return totalSize;
  }
  public boolean isEmpty() {
    return totalSize == 0;
  }
  public boolean containsKey(@Nullable Object key) {
    return map.containsKey(key);
  }
  public boolean containsValue(@Nullable Object value) {
    for (Collection<V> collection : map.values()) {
      if (collection.contains(value)) {
        return true;
      }
    }
    return false;
  }
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    Collection<V> collection = map.get(key);
    return collection != null && collection.contains(value);
  }
  public boolean put(@Nullable K key, @Nullable V value) {
    Collection<V> collection = getOrCreateCollection(key);
    if (collection.add(value)) {
      totalSize++;
      return true;
    } else {
      return false;
    }
  }
  private Collection<V> getOrCreateCollection(@Nullable K key) {
    Collection<V> collection = map.get(key);
    if (collection == null) {
      collection = createCollection(key);
      map.put(key, collection);
    }
    return collection;
  }
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    Collection<V> collection = map.get(key);
    if (collection == null) {
      return false;
    }
    boolean changed = collection.remove(value);
    if (changed) {
      totalSize--;
      if (collection.isEmpty()) {
        map.remove(key);
      }
    }
    return changed;
  }
  public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
    if (!values.iterator().hasNext()) {
      return false;
    }
    Collection<V> collection = getOrCreateCollection(key);
    int oldSize = collection.size();
    boolean changed = false;
    if (values instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<? extends V> c = (Collection<? extends V>) values;
      changed = collection.addAll(c);
    } else {
      for (V value : values) {
        changed |= collection.add(value);
      }
    }
    totalSize += (collection.size() - oldSize);
    return changed;
  }
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    boolean changed = false;
    for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
      changed |= put(entry.getKey(), entry.getValue());
    }
    return changed;
  }
  public Collection<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    Iterator<? extends V> iterator = values.iterator();
    if (!iterator.hasNext()) {
      return removeAll(key);
    }
    Collection<V> collection = getOrCreateCollection(key);
    Collection<V> oldValues = createCollection();
    oldValues.addAll(collection);
    totalSize -= collection.size();
    collection.clear();
    while (iterator.hasNext()) {
      if (collection.add(iterator.next())) {
        totalSize++;
      }
    }
    return unmodifiableCollectionSubclass(oldValues);
  }
  public Collection<V> removeAll(@Nullable Object key) {
    Collection<V> collection = map.remove(key);
    Collection<V> output = createCollection();
    if (collection != null) {
      output.addAll(collection);
      totalSize -= collection.size();
      collection.clear();
    }
    return unmodifiableCollectionSubclass(output);
  }
  private Collection<V> unmodifiableCollectionSubclass(
      Collection<V> collection) {
    if (collection instanceof SortedSet) {
      return Collections.unmodifiableSortedSet((SortedSet<V>) collection);
    } else if (collection instanceof Set) {
      return Collections.unmodifiableSet((Set<V>) collection);
    } else if (collection instanceof List) {
      return Collections.unmodifiableList((List<V>) collection);
    } else {
      return Collections.unmodifiableCollection(collection);
    }
  }
  public void clear() {
    for (Collection<V> collection : map.values()) {
      collection.clear();
    }
    map.clear();
    totalSize = 0;
  }
  public Collection<V> get(@Nullable K key) {
    Collection<V> collection = map.get(key);
    if (collection == null) {
      collection = createCollection(key);
    }
    return wrapCollection(key, collection);
  }
  private Collection<V> wrapCollection(
      @Nullable K key, Collection<V> collection) {
    if (collection instanceof SortedSet) {
      return new WrappedSortedSet(key, (SortedSet<V>) collection, null);
    } else if (collection instanceof Set) {
      return new WrappedSet(key, (Set<V>) collection);
    } else if (collection instanceof List) {
      return wrapList(key, (List<V>) collection, null);
    } else {
      return new WrappedCollection(key, collection, null);
    }
  }
  private List<V> wrapList(
      K key, List<V> list, @Nullable WrappedCollection ancestor) {
    return (list instanceof RandomAccess)
        ? new RandomAccessWrappedList(key, list, ancestor)
        : new WrappedList(key, list, ancestor);
  }
  private class WrappedCollection extends AbstractCollection<V> {
    final K key;
    Collection<V> delegate;
    final WrappedCollection ancestor;
    final Collection<V> ancestorDelegate;
    WrappedCollection(@Nullable K key, Collection<V> delegate,
        @Nullable WrappedCollection ancestor) {
      this.key = key;
      this.delegate = delegate;
      this.ancestor = ancestor;
      this.ancestorDelegate
          = (ancestor == null) ? null : ancestor.getDelegate();
    }
    void refreshIfEmpty() {
      if (ancestor != null) {
        ancestor.refreshIfEmpty();
        if (ancestor.getDelegate() != ancestorDelegate) {
          throw new ConcurrentModificationException();
        }
      } else if (delegate.isEmpty()) {
        Collection<V> newDelegate = map.get(key);
        if (newDelegate != null) {
          delegate = newDelegate;
        }
      }
    }
    void removeIfEmpty() {
      if (ancestor != null) {
        ancestor.removeIfEmpty();
      } else if (delegate.isEmpty()) {
        map.remove(key);
      }
    }
    K getKey() {
      return key;
    }
    void addToMap() {
      if (ancestor != null) {
        ancestor.addToMap();
      } else {
        map.put(key, delegate);
      }
    }
    @Override public int size() {
      refreshIfEmpty();
      return delegate.size();
    }
    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      refreshIfEmpty();
      return delegate.equals(object);
    }
    @Override public int hashCode() {
      refreshIfEmpty();
      return delegate.hashCode();
    }
    @Override public String toString() {
      refreshIfEmpty();
      return delegate.toString();
    }
    Collection<V> getDelegate() {
      return delegate;
    }
    @Override public Iterator<V> iterator() {
      refreshIfEmpty();
      return new WrappedIterator();
    }
    class WrappedIterator implements Iterator<V> {
      final Iterator<V> delegateIterator;
      final Collection<V> originalDelegate = delegate;
      WrappedIterator() {
        delegateIterator = iteratorOrListIterator(delegate);
      }
      WrappedIterator(Iterator<V> delegateIterator) {
        this.delegateIterator = delegateIterator;
      }
      void validateIterator() {
        refreshIfEmpty();
        if (delegate != originalDelegate) {
          throw new ConcurrentModificationException();
        }
      }
      public boolean hasNext() {
        validateIterator();
        return delegateIterator.hasNext();
      }
      public V next() {
        validateIterator();
        return delegateIterator.next();
      }
      public void remove() {
        delegateIterator.remove();
        totalSize--;
        removeIfEmpty();
      }
      Iterator<V> getDelegateIterator() {
        validateIterator();
        return delegateIterator;
      }
    }
    @Override public boolean add(V value) {
      refreshIfEmpty();
      boolean wasEmpty = delegate.isEmpty();
      boolean changed = delegate.add(value);
      if (changed) {
        totalSize++;
        if (wasEmpty) {
          addToMap();
        }
      }
      return changed;
    }
    WrappedCollection getAncestor() {
      return ancestor;
    }
    @Override public boolean addAll(Collection<? extends V> collection) {
      if (collection.isEmpty()) {
        return false;
      }
      int oldSize = size();  
      boolean changed = delegate.addAll(collection);
      if (changed) {
        int newSize = delegate.size();
        totalSize += (newSize - oldSize);
        if (oldSize == 0) {
          addToMap();
        }
      }
      return changed;
    }
    @Override public boolean contains(Object o) {
      refreshIfEmpty();
      return delegate.contains(o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      refreshIfEmpty();
      return delegate.containsAll(c);
    }
    @Override public void clear() {
      int oldSize = size();  
      if (oldSize == 0) {
        return;
      }
      delegate.clear();
      totalSize -= oldSize;
      removeIfEmpty();       
    }
    @Override public boolean remove(Object o) {
      refreshIfEmpty();
      boolean changed = delegate.remove(o);
      if (changed) {
        totalSize--;
        removeIfEmpty();
      }
      return changed;
    }
    @Override public boolean removeAll(Collection<?> c) {
      if (c.isEmpty()) {
        return false;
      }
      int oldSize = size();  
      boolean changed = delegate.removeAll(c);
      if (changed) {
        int newSize = delegate.size();
        totalSize += (newSize - oldSize);
        removeIfEmpty();
      }
      return changed;
    }
    @Override public boolean retainAll(Collection<?> c) {
      checkNotNull(c);
      int oldSize = size();  
      boolean changed = delegate.retainAll(c);
      if (changed) {
        int newSize = delegate.size();
        totalSize += (newSize - oldSize);
        removeIfEmpty();
      }
      return changed;
    }
  }
  private Iterator<V> iteratorOrListIterator(Collection<V> collection) {
    return (collection instanceof List)
        ? ((List<V>) collection).listIterator()
        : collection.iterator();
  }
  private class WrappedSet extends WrappedCollection implements Set<V> {
    WrappedSet(K key, Set<V> delegate) {
      super(key, delegate, null);
    }
  }
  private class WrappedSortedSet extends WrappedCollection
      implements SortedSet<V> {
    WrappedSortedSet(@Nullable K key, SortedSet<V> delegate,
        @Nullable WrappedCollection ancestor) {
      super(key, delegate, ancestor);
    }
    SortedSet<V> getSortedSetDelegate() {
      return (SortedSet<V>) getDelegate();
    }
    public Comparator<? super V> comparator() {
      return getSortedSetDelegate().comparator();
    }
    public V first() {
      refreshIfEmpty();
      return getSortedSetDelegate().first();
    }
    public V last() {
      refreshIfEmpty();
      return getSortedSetDelegate().last();
    }
    public SortedSet<V> headSet(V toElement) {
      refreshIfEmpty();
      return new WrappedSortedSet(
          getKey(), getSortedSetDelegate().headSet(toElement),
          (getAncestor() == null) ? this : getAncestor());
    }
    public SortedSet<V> subSet(V fromElement, V toElement) {
      refreshIfEmpty();
      return new WrappedSortedSet(
          getKey(), getSortedSetDelegate().subSet(fromElement, toElement),
          (getAncestor() == null) ? this : getAncestor());
    }
    public SortedSet<V> tailSet(V fromElement) {
      refreshIfEmpty();
      return new WrappedSortedSet(
          getKey(), getSortedSetDelegate().tailSet(fromElement),
          (getAncestor() == null) ? this : getAncestor());
    }
  }
  private class WrappedList extends WrappedCollection implements List<V> {
    WrappedList(K key, List<V> delegate, @Nullable WrappedCollection ancestor) {
      super(key, delegate, ancestor);
    }
    List<V> getListDelegate() {
      return (List<V>) getDelegate();
    }
    public boolean addAll(int index, Collection<? extends V> c) {
      if (c.isEmpty()) {
        return false;
      }
      int oldSize = size();  
      boolean changed = getListDelegate().addAll(index, c);
      if (changed) {
        int newSize = getDelegate().size();
        totalSize += (newSize - oldSize);
        if (oldSize == 0) {
          addToMap();
        }
      }
      return changed;
    }
    public V get(int index) {
      refreshIfEmpty();
      return getListDelegate().get(index);
    }
    public V set(int index, V element) {
      refreshIfEmpty();
      return getListDelegate().set(index, element);
    }
    public void add(int index, V element) {
      refreshIfEmpty();
      boolean wasEmpty = getDelegate().isEmpty();
      getListDelegate().add(index, element);
      totalSize++;
      if (wasEmpty) {
        addToMap();
      }
    }
    public V remove(int index) {
      refreshIfEmpty();
      V value = getListDelegate().remove(index);
      totalSize--;
      removeIfEmpty();
      return value;
    }
    public int indexOf(Object o) {
      refreshIfEmpty();
      return getListDelegate().indexOf(o);
    }
    public int lastIndexOf(Object o) {
      refreshIfEmpty();
      return getListDelegate().lastIndexOf(o);
    }
    public ListIterator<V> listIterator() {
      refreshIfEmpty();
      return new WrappedListIterator();
    }
    public ListIterator<V> listIterator(int index) {
      refreshIfEmpty();
      return new WrappedListIterator(index);
    }
    @GwtIncompatible("List.subList")
    public List<V> subList(int fromIndex, int toIndex) {
      refreshIfEmpty();
      return wrapList(getKey(),
          Platform.subList(getListDelegate(), fromIndex, toIndex),
          (getAncestor() == null) ? this : getAncestor());
    }
    private class WrappedListIterator extends WrappedIterator
        implements ListIterator<V> {
      WrappedListIterator() {}
      public WrappedListIterator(int index) {
        super(getListDelegate().listIterator(index));
      }
      private ListIterator<V> getDelegateListIterator() {
        return (ListIterator<V>) getDelegateIterator();
      }
      public boolean hasPrevious() {
        return getDelegateListIterator().hasPrevious();
      }
      public V previous() {
        return getDelegateListIterator().previous();
      }
      public int nextIndex() {
        return getDelegateListIterator().nextIndex();
      }
      public int previousIndex() {
        return getDelegateListIterator().previousIndex();
      }
      public void set(V value) {
        getDelegateListIterator().set(value);
      }
      public void add(V value) {
        boolean wasEmpty = isEmpty();
        getDelegateListIterator().add(value);
        totalSize++;
        if (wasEmpty) {
          addToMap();
        }
      }
    }
  }
  private class RandomAccessWrappedList extends WrappedList
      implements RandomAccess {
    RandomAccessWrappedList(K key, List<V> delegate,
        @Nullable WrappedCollection ancestor) {
      super(key, delegate, ancestor);
    }
  }
  private transient Set<K> keySet;
  public Set<K> keySet() {
    Set<K> result = keySet;
    return (result == null) ? keySet = createKeySet() : result;
  }
  private Set<K> createKeySet() {
    return (map instanceof SortedMap)
        ? new SortedKeySet((SortedMap<K, Collection<V>>) map) : new KeySet(map);
  }
  private class KeySet extends AbstractSet<K> {
    final Map<K, Collection<V>> subMap;
    KeySet(final Map<K, Collection<V>> subMap) {
      this.subMap = subMap;
    }
    @Override public int size() {
      return subMap.size();
    }
    @Override public Iterator<K> iterator() {
      return new Iterator<K>() {
        final Iterator<Map.Entry<K, Collection<V>>> entryIterator
            = subMap.entrySet().iterator();
        Map.Entry<K, Collection<V>> entry;
        public boolean hasNext() {
          return entryIterator.hasNext();
        }
        public K next() {
          entry = entryIterator.next();
          return entry.getKey();
        }
        public void remove() {
          checkState(entry != null);
          Collection<V> collection = entry.getValue();
          entryIterator.remove();
          totalSize -= collection.size();
          collection.clear();
        }
      };
    }
    @Override public boolean contains(Object key) {
      return subMap.containsKey(key);
    }
    @Override public boolean remove(Object key) {
      int count = 0;
      Collection<V> collection = subMap.remove(key);
      if (collection != null) {
        count = collection.size();
        collection.clear();
        totalSize -= count;
      }
      return count > 0;
    }
    @Override public boolean containsAll(Collection<?> c) {
      return subMap.keySet().containsAll(c);
    }
    @Override public boolean equals(@Nullable Object object) {
      return this == object || this.subMap.keySet().equals(object);
    }
    @Override public int hashCode() {
      return subMap.keySet().hashCode();
    }
  }
  private class SortedKeySet extends KeySet implements SortedSet<K> {
    SortedKeySet(SortedMap<K, Collection<V>> subMap) {
      super(subMap);
    }
    SortedMap<K, Collection<V>> sortedMap() {
      return (SortedMap<K, Collection<V>>) subMap;
    }
    public Comparator<? super K> comparator() {
      return sortedMap().comparator();
    }
    public K first() {
      return sortedMap().firstKey();
    }
    public SortedSet<K> headSet(K toElement) {
      return new SortedKeySet(sortedMap().headMap(toElement));
    }
    public K last() {
      return sortedMap().lastKey();
    }
    public SortedSet<K> subSet(K fromElement, K toElement) {
      return new SortedKeySet(sortedMap().subMap(fromElement, toElement));
    }
    public SortedSet<K> tailSet(K fromElement) {
      return new SortedKeySet(sortedMap().tailMap(fromElement));
    }
  }
  private transient Multiset<K> multiset;
  public Multiset<K> keys() {
    Multiset<K> result = multiset;
    return (result == null) ? multiset = new MultisetView() : result;
  }
  private class MultisetView extends AbstractMultiset<K> {
    @Override public int remove(Object key, int occurrences) {
      if (occurrences == 0) {
        return count(key);
      }
      checkArgument(occurrences > 0);
      Collection<V> collection;
      try {
        collection = map.get(key);
      } catch (NullPointerException e) {
        return 0;
      } catch (ClassCastException e) {
        return 0;
      }
      if (collection == null) {
        return 0;
      }
      int count = collection.size();
      if (occurrences >= count) {
        return removeValuesForKey(key);
      }
      Iterator<V> iterator = collection.iterator();
      for (int i = 0; i < occurrences; i++) {
        iterator.next();
        iterator.remove();
      }
      totalSize -= occurrences;
      return count;
    }
    @Override public Set<K> elementSet() {
      return AbstractMultimap.this.keySet();
    }
    transient Set<Multiset.Entry<K>> entrySet;
    @Override public Set<Multiset.Entry<K>> entrySet() {
      Set<Multiset.Entry<K>> result = entrySet;
      return (result == null) ? entrySet = new EntrySet() : result;
    }
    private class EntrySet extends AbstractSet<Multiset.Entry<K>> {
      @Override public Iterator<Multiset.Entry<K>> iterator() {
        return new MultisetEntryIterator();
      }
      @Override public int size() {
        return map.size();
      }
      @Override public boolean contains(Object o) {
        if (!(o instanceof Multiset.Entry)) {
          return false;
        }
        Multiset.Entry<?> entry = (Multiset.Entry<?>) o;
        Collection<V> collection = map.get(entry.getElement());
        return (collection != null) &&
            (collection.size() == entry.getCount());
      }
      @Override public void clear() {
        AbstractMultimap.this.clear();
      }
      @Override public boolean remove(Object o) {
        return contains(o) &&
            (removeValuesForKey(((Multiset.Entry<?>) o).getElement()) > 0);
      }
    }
    @Override public Iterator<K> iterator() {
      return new MultisetKeyIterator();
    }
    @Override public int count(Object key) {
      try {
        Collection<V> collection = map.get(key);
        return (collection == null) ? 0 : collection.size();
      } catch (NullPointerException e) {
        return 0;
      } catch (ClassCastException e) {
        return 0;
      }
    }
    @Override public int size() {
      return totalSize;
    }
    @Override public void clear() {
      AbstractMultimap.this.clear();
    }
  }
  private int removeValuesForKey(Object key) {
    Collection<V> collection;
    try {
      collection = map.remove(key);
    } catch (NullPointerException e) {
      return 0;
    } catch (ClassCastException e) {
      return 0;
    }
    int count = 0;
    if (collection != null) {
      count = collection.size();
      collection.clear();
      totalSize -= count;
    }
    return count;
  }
  private class MultisetEntryIterator implements Iterator<Multiset.Entry<K>> {
    final Iterator<Map.Entry<K, Collection<V>>> asMapIterator
        = asMap().entrySet().iterator();
    public boolean hasNext() {
      return asMapIterator.hasNext();
    }
    public Multiset.Entry<K> next() {
      return new MultisetEntry(asMapIterator.next());
    }
    public void remove() {
      asMapIterator.remove();
    }
  }
  private class MultisetEntry extends Multisets.AbstractEntry<K> {
    final Map.Entry<K, Collection<V>> entry;
    public MultisetEntry(Map.Entry<K, Collection<V>> entry) {
      this.entry = entry;
    }
    public K getElement() {
      return entry.getKey();
    }
    public int getCount() {
      return entry.getValue().size();
    }
  }
  private class MultisetKeyIterator implements Iterator<K> {
    final Iterator<Map.Entry<K, V>> entryIterator = entries().iterator();
    public boolean hasNext() {
      return entryIterator.hasNext();
    }
    public K next() {
      return entryIterator.next().getKey();
    }
    public void remove() {
      entryIterator.remove();
    }
  }
  private transient Collection<V> valuesCollection;
  public Collection<V> values() {
    Collection<V> result = valuesCollection;
    return (result == null) ? valuesCollection = new Values() : result;
  }
  private class Values extends AbstractCollection<V> {
    @Override public Iterator<V> iterator() {
      return new ValueIterator();
    }
    @Override public int size() {
      return totalSize;
    }
    @Override public void clear() {
      AbstractMultimap.this.clear();
    }
    @Override public boolean contains(Object value) {
      return containsValue(value);
    }
  }
  private class ValueIterator implements Iterator<V> {
    final Iterator<Map.Entry<K, V>> entryIterator = createEntryIterator();
    public boolean hasNext() {
      return entryIterator.hasNext();
    }
    public V next() {
      return entryIterator.next().getValue();
    }
    public void remove() {
      entryIterator.remove();
    }
  }
  private transient Collection<Map.Entry<K, V>> entries;
  public Collection<Map.Entry<K, V>> entries() {
    Collection<Map.Entry<K, V>> result = entries;
    return (entries == null) ? entries = createEntries() : result;
  }
  private Collection<Map.Entry<K, V>> createEntries() {
    return (this instanceof SetMultimap) ? new EntrySet() : new Entries();
  }
  private class Entries extends AbstractCollection<Map.Entry<K, V>> {
    @Override public Iterator<Map.Entry<K, V>> iterator() {
      return createEntryIterator();
    }
    @Override public int size() {
      return totalSize;
    }
    @Override public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      return containsEntry(entry.getKey(), entry.getValue());
    }
    @Override public void clear() {
      AbstractMultimap.this.clear();
    }
    @Override public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      return AbstractMultimap.this.remove(entry.getKey(), entry.getValue());
    }
  }
  Iterator<Map.Entry<K, V>> createEntryIterator() {
    return new EntryIterator();
  }
  private class EntryIterator implements Iterator<Map.Entry<K, V>> {
    final Iterator<Map.Entry<K, Collection<V>>> keyIterator;
    K key;
    Collection<V> collection;
    Iterator<V> valueIterator;
    EntryIterator() {
      keyIterator = map.entrySet().iterator();
      if (keyIterator.hasNext()) {
        findValueIteratorAndKey();
      } else {
        valueIterator = Iterators.emptyModifiableIterator();
      }
    }
    void findValueIteratorAndKey() {
      Map.Entry<K, Collection<V>> entry = keyIterator.next();
      key = entry.getKey();
      collection = entry.getValue();
      valueIterator = collection.iterator();
    }
    public boolean hasNext() {
      return keyIterator.hasNext() || valueIterator.hasNext();
    }
    public Map.Entry<K, V> next() {
      if (!valueIterator.hasNext()) {
        findValueIteratorAndKey();
      }
      return Maps.immutableEntry(key, valueIterator.next());
    }
    public void remove() {
      valueIterator.remove();
      if (collection.isEmpty()) {
        keyIterator.remove();
      }
      totalSize--;
    }
  }
  private class EntrySet extends Entries implements Set<Map.Entry<K, V>> {
    @Override public boolean equals(@Nullable Object object) {
      return Collections2.setEquals(this, object);
    }
    @Override public int hashCode() {
      return Sets.hashCodeImpl(this);
    }
  }
  private transient Map<K, Collection<V>> asMap;
  public Map<K, Collection<V>> asMap() {
    Map<K, Collection<V>> result = asMap;
    return (result == null) ? asMap = createAsMap() : result;
  }
  private Map<K, Collection<V>> createAsMap() {
    return (map instanceof SortedMap)
        ? new SortedAsMap((SortedMap<K, Collection<V>>) map) : new AsMap(map);
  }
  private class AsMap extends AbstractMap<K, Collection<V>> {
    final transient Map<K, Collection<V>> submap;
    AsMap(Map<K, Collection<V>> submap) {
      this.submap = submap;
    }
    transient Set<Map.Entry<K, Collection<V>>> entrySet;
    @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
      Set<Map.Entry<K, Collection<V>>> result = entrySet;
      return (entrySet == null) ? entrySet = new AsMapEntries() : result;
    }
    @Override public boolean containsKey(Object key) {
      return Maps.safeContainsKey(submap, key);
    }
    @Override public Collection<V> get(Object key) {
      Collection<V> collection = Maps.safeGet(submap, key);
      if (collection == null) {
        return null;
      }
      @SuppressWarnings("unchecked")
      K k = (K) key;
      return wrapCollection(k, collection);
    }
    @Override public Set<K> keySet() {
      return AbstractMultimap.this.keySet();
    }
    @Override public Collection<V> remove(Object key) {
      Collection<V> collection = submap.remove(key);
      if (collection == null) {
        return null;
      }
      Collection<V> output = createCollection();
      output.addAll(collection);
      totalSize -= collection.size();
      collection.clear();
      return output;
    }
    @Override public boolean equals(@Nullable Object object) {
      return this == object || submap.equals(object);
    }
    @Override public int hashCode() {
      return submap.hashCode();
    }
    @Override public String toString() {
      return submap.toString();
    }
    class AsMapEntries extends AbstractSet<Map.Entry<K, Collection<V>>> {
      @Override public Iterator<Map.Entry<K, Collection<V>>> iterator() {
        return new AsMapIterator();
      }
      @Override public int size() {
        return submap.size();
      }
      @Override public boolean contains(Object o) {
        return Collections2.safeContains(submap.entrySet(), o);
      }
      @Override public boolean remove(Object o) {
        if (!contains(o)) {
          return false;
        }
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
        removeValuesForKey(entry.getKey());
        return true;
      }
    }
    class AsMapIterator implements Iterator<Map.Entry<K, Collection<V>>> {
      final Iterator<Map.Entry<K, Collection<V>>> delegateIterator
          = submap.entrySet().iterator();
      Collection<V> collection;
      public boolean hasNext() {
        return delegateIterator.hasNext();
      }
      public Map.Entry<K, Collection<V>> next() {
        Map.Entry<K, Collection<V>> entry = delegateIterator.next();
        K key = entry.getKey();
        collection = entry.getValue();
        return Maps.immutableEntry(key, wrapCollection(key, collection));
      }
      public void remove() {
        delegateIterator.remove();
        totalSize -= collection.size();
        collection.clear();
      }
    }
  }
  private class SortedAsMap extends AsMap
      implements SortedMap<K, Collection<V>> {
    SortedAsMap(SortedMap<K, Collection<V>> submap) {
      super(submap);
    }
    SortedMap<K, Collection<V>> sortedMap() {
      return (SortedMap<K, Collection<V>>) submap;
    }
    public Comparator<? super K> comparator() {
      return sortedMap().comparator();
    }
    public K firstKey() {
      return sortedMap().firstKey();
    }
    public K lastKey() {
      return sortedMap().lastKey();
    }
    public SortedMap<K, Collection<V>> headMap(K toKey) {
      return new SortedAsMap(sortedMap().headMap(toKey));
    }
    public SortedMap<K, Collection<V>> subMap(K fromKey, K toKey) {
      return new SortedAsMap(sortedMap().subMap(fromKey, toKey));
    }
    public SortedMap<K, Collection<V>> tailMap(K fromKey) {
      return new SortedAsMap(sortedMap().tailMap(fromKey));
    }
    SortedSet<K> sortedKeySet;
    @Override public SortedSet<K> keySet() {
      SortedSet<K> result = sortedKeySet;
      return (result == null)
          ? sortedKeySet = new SortedKeySet(sortedMap()) : result;
    }
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
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
  private static final long serialVersionUID = 2447537837011683357L;
}
