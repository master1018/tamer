public final class ConcurrentHashMultiset<E> extends AbstractMultiset<E>
    implements Serializable {
  private final transient ConcurrentMap<E, Integer> countMap;
  private static class FieldSettersHolder {
    @SuppressWarnings("unchecked")
    static final FieldSetter<ConcurrentHashMultiset> COUNT_MAP_FIELD_SETTER
        = Serialization.getFieldSetter(
            ConcurrentHashMultiset.class, "countMap");
  }
  public static <E> ConcurrentHashMultiset<E> create() {
    return new ConcurrentHashMultiset<E>(new ConcurrentHashMap<E, Integer>());
  }
  public static <E> ConcurrentHashMultiset<E> create(
      Iterable<? extends E> elements) {
    ConcurrentHashMultiset<E> multiset = ConcurrentHashMultiset.create();
    Iterables.addAll(multiset, elements);
    return multiset;
  }
  @VisibleForTesting ConcurrentHashMultiset(
      ConcurrentMap<E, Integer> countMap) {
    checkArgument(countMap.isEmpty());
    this.countMap = countMap;
  }
  @Override public int count(@Nullable Object element) {
    try {
      return unbox(countMap.get(element));
    } catch (NullPointerException e) {
      return 0;
    } catch (ClassCastException e) {
      return 0;
    }
  }
  @Override public int size() {
    long sum = 0L;
    for (Integer value : countMap.values()) {
      sum += value;
    }
    return (int) Math.min(sum, Integer.MAX_VALUE);
  }
  @Override public Object[] toArray() {
    return snapshot().toArray();
  }
  @Override public <T> T[] toArray(T[] array) {
    return snapshot().toArray(array);
  }
  private List<E> snapshot() {
    List<E> list = Lists.newArrayListWithExpectedSize(size());
    for (Multiset.Entry<E> entry : entrySet()) {
      E element = entry.getElement();
      for (int i = entry.getCount(); i > 0; i--) {
        list.add(element);
      }
    }
    return list;
  }
  @Override public int add(E element, int occurrences) {
    if (occurrences == 0) {
      return count(element);
    }
    checkArgument(occurrences > 0, "Invalid occurrences: %s", occurrences);
    while (true) {
      int current = count(element);
      if (current == 0) {
        if (countMap.putIfAbsent(element, occurrences) == null) {
          return 0;
        }
      } else {
        checkArgument(occurrences <= Integer.MAX_VALUE - current,
            "Overflow adding %s occurrences to a count of %s",
            occurrences, current);
        int next = current + occurrences;
        if (countMap.replace(element, current, next)) {
          return current;
        }
      }
    }
  }
  @Override public int remove(@Nullable Object element, int occurrences) {
    if (occurrences == 0) {
      return count(element);
    }
    checkArgument(occurrences > 0, "Invalid occurrences: %s", occurrences);
    while (true) {
      int current = count(element);
      if (current == 0) {
        return 0;
      }
      if (occurrences >= current) {
        if (countMap.remove(element, current)) {
          return current;
        }
      } else {
        @SuppressWarnings("unchecked")
        E casted = (E) element;
        if (countMap.replace(casted, current, current - occurrences)) {
          return current;
        }
      }
    }
  }
  private int removeAllOccurrences(@Nullable Object element) {
    try {
      return unbox(countMap.remove(element));
    } catch (NullPointerException e) {
      return 0;
    } catch (ClassCastException e) {
      return 0;
    }
  }
  public boolean removeExactly(@Nullable Object element, int occurrences) {
    if (occurrences == 0) {
      return true;
    }
    checkArgument(occurrences > 0, "Invalid occurrences: %s", occurrences);
    while (true) {
      int current = count(element);
      if (occurrences > current) {
        return false;
      }
      if (occurrences == current) {
        if (countMap.remove(element, occurrences)) {
          return true;
        }
      } else {
        @SuppressWarnings("unchecked") 
        E casted = (E) element;
        if (countMap.replace(casted, current, current - occurrences)) {
          return true;
        }
      }
    }
  }
  @Override public int setCount(E element, int count) {
    checkNonnegative(count, "count");
    return (count == 0)
        ? removeAllOccurrences(element)
        : unbox(countMap.put(element, count));
  }
  @Override public boolean setCount(E element, int oldCount, int newCount) {
    checkNonnegative(oldCount, "oldCount");
    checkNonnegative(newCount, "newCount");
    if (newCount == 0) {
      if (oldCount == 0) {
        return !countMap.containsKey(element);
      } else {
        return countMap.remove(element, oldCount);
      }
    }
    if (oldCount == 0) {
      return countMap.putIfAbsent(element, newCount) == null;
    }
    return countMap.replace(element, oldCount, newCount);
  }
  @Override Set<E> createElementSet() {
    final Set<E> delegate = countMap.keySet();
    return new ForwardingSet<E>() {
      @Override protected Set<E> delegate() {
        return delegate;
      }
      @Override public boolean remove(Object object) {
        try {
          return delegate.remove(object);
        } catch (NullPointerException e) {
          return false;
        } catch (ClassCastException e) {
          return false;
        }
      }
    };
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
    @Override public int size() {
      return countMap.size();
    }
    @Override public boolean isEmpty() {
      return countMap.isEmpty();
    }
    @Override public boolean contains(Object object) {
      if (object instanceof Multiset.Entry) {
        Multiset.Entry<?> entry = (Multiset.Entry<?>) object;
        Object element = entry.getElement();
        int entryCount = entry.getCount();
        return entryCount > 0 && count(element) == entryCount;
      }
      return false;
    }
    @Override public Iterator<Multiset.Entry<E>> iterator() {
      final Iterator<Map.Entry<E, Integer>> backingIterator
          = countMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
        public boolean hasNext() {
          return backingIterator.hasNext();
        }
        public Multiset.Entry<E> next() {
          Map.Entry<E, Integer> backingEntry = backingIterator.next();
          return Multisets.immutableEntry(
              backingEntry.getKey(), backingEntry.getValue());
        }
        public void remove() {
          backingIterator.remove();
        }
      };
    }
    @Override public Object[] toArray() {
      return snapshot().toArray();
    }
    @Override public <T> T[] toArray(T[] array) {
      return snapshot().toArray(array);
    }
    private List<Multiset.Entry<E>> snapshot() {
      List<Multiset.Entry<E>> list = Lists.newArrayListWithExpectedSize(size());
      for (Multiset.Entry<E> entry : this) {
        list.add(entry);
      }
      return list;
    }
    @Override public boolean remove(Object object) {
      if (object instanceof Multiset.Entry) {
        Multiset.Entry<?> entry = (Multiset.Entry<?>) object;
        Object element = entry.getElement();
        int entryCount = entry.getCount();
        return countMap.remove(element, entryCount);
      }
      return false;
    }
    @Override public void clear() {
      countMap.clear();
    }
    @Override public int hashCode() {
      return countMap.hashCode();
    }
  }
  private static int unbox(Integer i) {
    return (i == null) ? 0 : i;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultiset(HashMultiset.create(this), stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    FieldSettersHolder.COUNT_MAP_FIELD_SETTER.set(
        this, new ConcurrentHashMap<Object, Object>());
    Serialization.populateMultiset(this, stream);
  }
  private static final long serialVersionUID = 0L;
}
