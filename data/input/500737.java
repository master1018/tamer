abstract class AbstractMapBasedMultiset<E> extends AbstractMultiset<E>
    implements Serializable {
  private transient Map<E, AtomicInteger> backingMap;
  private transient long size;
  protected AbstractMapBasedMultiset(Map<E, AtomicInteger> backingMap) {
    this.backingMap = checkNotNull(backingMap);
    this.size = super.size();
  }
  Map<E, AtomicInteger> backingMap() {
    return backingMap;
  }
  void setBackingMap(Map<E, AtomicInteger> backingMap) {
    this.backingMap = backingMap;
  }
  private transient EntrySet entrySet;
  @Override public Set<Multiset.Entry<E>> entrySet() {
    EntrySet result = entrySet;
    if (result == null) {
      entrySet = result = new EntrySet();
    }
    return result;
  }
  private class EntrySet extends AbstractSet<Multiset.Entry<E>> {
    @Override public Iterator<Multiset.Entry<E>> iterator() {
      final Iterator<Map.Entry<E, AtomicInteger>> backingEntries
          = backingMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
        Map.Entry<E, AtomicInteger> toRemove;
        public boolean hasNext() {
          return backingEntries.hasNext();
        }
        public Multiset.Entry<E> next() {
          final Map.Entry<E, AtomicInteger> mapEntry = backingEntries.next();
          toRemove = mapEntry;
          return new Multisets.AbstractEntry<E>() {
            public E getElement() {
              return mapEntry.getKey();
            }
            public int getCount() {
              int count = mapEntry.getValue().get();
              if (count == 0) {
                AtomicInteger frequency = backingMap.get(getElement());
                if (frequency != null) {
                  count = frequency.get();
                }
              }
              return count;
            }
          };
        }
        public void remove() {
          checkState(toRemove != null,
              "no calls to next() since the last call to remove()");
          size -= toRemove.getValue().getAndSet(0);
          backingEntries.remove();
          toRemove = null;
        }
      };
    }
    @Override public int size() {
      return backingMap.size();
    }
    @Override public void clear() {
      for (AtomicInteger frequency : backingMap.values()) {
        frequency.set(0);
      }
      backingMap.clear();
      size = 0L;
    }
    @Override public boolean contains(Object o) {
      if (o instanceof Entry) {
        Entry<?> entry = (Entry<?>) o;
        int count = count(entry.getElement());
        return (count == entry.getCount()) && (count > 0);
      }
      return false;
    }
    @Override public boolean remove(Object o) {
      if (contains(o)) {
        Entry<?> entry = (Entry<?>) o;
        AtomicInteger frequency = backingMap.remove(entry.getElement());
        int numberRemoved = frequency.getAndSet(0);
        size -= numberRemoved;
        return true;
      }
      return false;
    }
  }
  @Override public int size() {
    return (int) Math.min(this.size, Integer.MAX_VALUE);
  }
  @Override public Iterator<E> iterator() {
    return new MapBasedMultisetIterator();
  }
  private class MapBasedMultisetIterator implements Iterator<E> {
    final Iterator<Map.Entry<E, AtomicInteger>> entryIterator;
    Map.Entry<E, AtomicInteger> currentEntry;
    int occurrencesLeft;
    boolean canRemove;
    MapBasedMultisetIterator() {
      this.entryIterator = backingMap.entrySet().iterator();
    }
    public boolean hasNext() {
      return occurrencesLeft > 0 || entryIterator.hasNext();
    }
    public E next() {
      if (occurrencesLeft == 0) {
        currentEntry = entryIterator.next();
        occurrencesLeft = currentEntry.getValue().get();
      }
      occurrencesLeft--;
      canRemove = true;
      return currentEntry.getKey();
    }
    public void remove() {
      checkState(canRemove,
          "no calls to next() since the last call to remove()");
      int frequency = currentEntry.getValue().get();
      if (frequency <= 0) {
        throw new ConcurrentModificationException();
      }
      if (currentEntry.getValue().addAndGet(-1) == 0) {
        entryIterator.remove();
      }
      size--;
      canRemove = false;
    }
  }
  @Override public int count(@Nullable Object element) {
    AtomicInteger frequency = backingMap.get(element);
    return (frequency == null) ? 0 : frequency.get();
  }
  @Override public int add(@Nullable E element, int occurrences) {
    if (occurrences == 0) {
      return count(element);
    }
    checkArgument(
        occurrences > 0, "occurrences cannot be negative: %s", occurrences);
    AtomicInteger frequency = backingMap.get(element);
    int oldCount;
    if (frequency == null) {
      oldCount = 0;
      backingMap.put(element, new AtomicInteger(occurrences));
    } else {
      oldCount = frequency.get();
      long newCount = (long) oldCount + (long) occurrences;
      checkArgument(newCount <= Integer.MAX_VALUE,
          "too many occurrences: %s", newCount);
      frequency.getAndAdd(occurrences);
    }
    size += occurrences;
    return oldCount;
  }
  @Override public int remove(@Nullable Object element, int occurrences) {
    if (occurrences == 0) {
      return count(element);
    }
    checkArgument(
        occurrences > 0, "occurrences cannot be negative: %s", occurrences);
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      return 0;
    }
    int oldCount = frequency.get();
    int numberRemoved;
    if (oldCount > occurrences) {
      numberRemoved = occurrences;
    } else {
      numberRemoved = oldCount;
      backingMap.remove(element);
    }
    frequency.addAndGet(-numberRemoved);
    size -= numberRemoved;
    return oldCount;
  }
  @Override public int setCount(E element, int count) {
    checkNonnegative(count, "count");
    AtomicInteger existingCounter;
    int oldCount;
    if (count == 0) {
      existingCounter = backingMap.remove(element);
      oldCount = getAndSet(existingCounter, count);
    } else {
      existingCounter = backingMap.get(element);
      oldCount = getAndSet(existingCounter, count);
      if (existingCounter == null) {
        backingMap.put(element, new AtomicInteger(count));
      }
    }
    size += (count - oldCount);
    return oldCount;
  }
  private static int getAndSet(AtomicInteger i, int count) {
    if (i == null) {
      return 0;
    }
    return i.getAndSet(count);
  }
  private int removeAllOccurrences(@Nullable Object element,
      Map<E, AtomicInteger> map) {
    AtomicInteger frequency = map.remove(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = frequency.getAndSet(0);
    size -= numberRemoved;
    return numberRemoved;
  }
  @Override Set<E> createElementSet() {
    return new MapBasedElementSet(backingMap);
  }
  class MapBasedElementSet extends ForwardingSet<E> {
    private final Map<E, AtomicInteger> map;
    private final Set<E> delegate;
    MapBasedElementSet(Map<E, AtomicInteger> map) {
      this.map = map;
      delegate = map.keySet();
    }
    @Override protected Set<E> delegate() {
      return delegate;
    }
    @Override public Iterator<E> iterator() {
      final Iterator<Map.Entry<E, AtomicInteger>> entries
          = map.entrySet().iterator();
      return new Iterator<E>() {
        Map.Entry<E, AtomicInteger> toRemove;
        public boolean hasNext() {
          return entries.hasNext();
        }
        public E next() {
          toRemove = entries.next();
          return toRemove.getKey();
        }
        public void remove() {
          checkState(toRemove != null,
              "no calls to next() since the last call to remove()");
          size -= toRemove.getValue().getAndSet(0);
          entries.remove();
          toRemove = null;
        }
      };
    }
    @Override public boolean remove(Object element) {
      return removeAllOccurrences(element, map) != 0;
    }
    @Override public boolean removeAll(Collection<?> elementsToRemove) {
      return Iterators.removeAll(iterator(), elementsToRemove);
    }
    @Override public boolean retainAll(Collection<?> elementsToRetain) {
      return Iterators.retainAll(iterator(), elementsToRetain);
    }
    @Override public void clear() {
      if (map == backingMap) {
        AbstractMapBasedMultiset.this.clear();
      } else {
        Iterator<E> i = iterator();
        while (i.hasNext()) {
          i.next();
          i.remove();
        }
      }
    }
    public Map<E, AtomicInteger> getMap() {
      return map;
    }
  }
  @SuppressWarnings("unused") 
  private void readObjectNoData() throws ObjectStreamException {
    throw new InvalidObjectException("Stream data required");
  }
  private static final long serialVersionUID = -2250766705698539974L;
}
