final class EmptyImmutableList extends ImmutableList<Object> {
  static final EmptyImmutableList INSTANCE = new EmptyImmutableList();
  private EmptyImmutableList() {}
  public int size() {
    return 0;
  }
  @Override public boolean isEmpty() {
    return true;
  }
  @Override public boolean contains(Object target) {
    return false;
  }
  @Override public UnmodifiableIterator<Object> iterator() {
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
  public Object get(int index) {
    checkElementIndex(index, 0);
    throw new AssertionError("unreachable");
  }
  @Override public int indexOf(Object target) {
    return -1;
  }
  @Override public int lastIndexOf(Object target) {
    return -1;
  }
  @Override public ImmutableList<Object> subList(int fromIndex, int toIndex) {
    checkPositionIndexes(fromIndex, toIndex, 0);
    return this;
  }
  public ListIterator<Object> listIterator() {
    return Collections.emptyList().listIterator();
  }
  public ListIterator<Object> listIterator(int start) {
    checkPositionIndex(start, 0);
    return Collections.emptyList().listIterator();
  }
  @Override public boolean containsAll(Collection<?> targets) {
    return targets.isEmpty();
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof List) {
      List<?> that = (List<?>) object;
      return that.isEmpty();
    }
    return false;
  }
  @Override public int hashCode() {
    return 1;
  }
  @Override public String toString() {
    return "[]";
  }
  Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
