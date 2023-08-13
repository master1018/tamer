class EmptyImmutableSortedSet<E> extends ImmutableSortedSet<E> {
  EmptyImmutableSortedSet(Comparator<? super E> comparator) {
    super(comparator);
  }
  public int size() {
    return 0;
  }
  @Override public boolean isEmpty() {
    return true;
  }
  @Override public boolean contains(Object target) {
    return false;
  }
  @Override public UnmodifiableIterator<E> iterator() {
    return Iterators.emptyIterator();
  }
  private static final Object[] EMPTY_ARRAY = new Object[0];
  @Override public Object[] toArray() {
    return EMPTY_ARRAY;
  }
  @Override public <T> T[] toArray(T[] a) {
    if (a.length > 0) {
      a[0] = null;
    }
    return a;
  }
  @Override public boolean containsAll(Collection<?> targets) {
    return targets.isEmpty();
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof Set) {
      Set<?> that = (Set<?>) object;
      return that.isEmpty();
    }
    return false;
  }
  @Override public int hashCode() {
    return 0;
  }
  @Override public String toString() {
    return "[]";
  }
  public E first() {
    throw new NoSuchElementException();
  }
  public E last() {
    throw new NoSuchElementException();
  }
  @Override ImmutableSortedSet<E> headSetImpl(E toElement) {
    return this;
  }
  @Override ImmutableSortedSet<E> subSetImpl(E fromElement, E toElement) {
    return this;
  }
  @Override ImmutableSortedSet<E> tailSetImpl(E fromElement) {
    return this;
  }
  @Override boolean hasPartialArray() {
    return false;
  }
  @Override int indexOf(Object target) {
    return -1;
  }
}
