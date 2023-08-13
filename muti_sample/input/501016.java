public final class TreeMultiset<E> extends AbstractMapBasedMultiset<E> {
  @SuppressWarnings("unchecked") 
  public static <E extends Comparable> TreeMultiset<E> create() {
    return new TreeMultiset<E>();
  }
  public static <E> TreeMultiset<E> create(Comparator<? super E> comparator) {
    return new TreeMultiset<E>(comparator);
  }
  @SuppressWarnings("unchecked") 
  public static <E extends Comparable> TreeMultiset<E> create(
      Iterable<? extends E> elements) {
    TreeMultiset<E> multiset = create();
    Iterables.addAll(multiset, elements);
    return multiset;
  }
  private TreeMultiset() {
    super(new TreeMap<E, AtomicInteger>());
  }
  private TreeMultiset(Comparator<? super E> comparator) {
    super(new TreeMap<E, AtomicInteger>(comparator));
  }
  @Override public SortedSet<E> elementSet() {
    return (SortedSet<E>) super.elementSet();
  }
  @Override public int count(@Nullable Object element) {
    try {
      return super.count(element);
    } catch (NullPointerException e) {
      return 0;
    } catch (ClassCastException e) {
      return 0;
    }
  }
  @Override Set<E> createElementSet() {
    return new SortedMapBasedElementSet(
        (SortedMap<E, AtomicInteger>) backingMap());
  }
  private class SortedMapBasedElementSet extends MapBasedElementSet
      implements SortedSet<E> {
    SortedMapBasedElementSet(SortedMap<E, AtomicInteger> map) {
      super(map);
    }
    SortedMap<E, AtomicInteger> sortedMap() {
      return (SortedMap<E, AtomicInteger>) getMap();
    }
    public Comparator<? super E> comparator() {
      return sortedMap().comparator();
    }
    public E first() {
      return sortedMap().firstKey();
    }
    public E last() {
      return sortedMap().lastKey();
    }
    public SortedSet<E> headSet(E toElement) {
      return new SortedMapBasedElementSet(sortedMap().headMap(toElement));
    }
    public SortedSet<E> subSet(E fromElement, E toElement) {
      return new SortedMapBasedElementSet(
          sortedMap().subMap(fromElement, toElement));
    }
    public SortedSet<E> tailSet(E fromElement) {
      return new SortedMapBasedElementSet(sortedMap().tailMap(fromElement));
    }
    @Override public boolean remove(Object element) {
      try {
        return super.remove(element);
      } catch (NullPointerException e) {
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(elementSet().comparator());
    Serialization.writeMultiset(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    @SuppressWarnings("unchecked") 
    Comparator<? super E> comparator
        = (Comparator<? super E>) stream.readObject();
    setBackingMap(new TreeMap<E, AtomicInteger>(comparator));
    Serialization.populateMultiset(this, stream);
  }
  private static final long serialVersionUID = 0;
}
