final class EmptyImmutableSet extends ImmutableSet<Object> {
  static final EmptyImmutableSet INSTANCE = new EmptyImmutableSet();
  private EmptyImmutableSet() {}
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
  @Override public final int hashCode() {
    return 0;
  }
  @Override boolean isHashCodeFast() {
    return true;
  }
  @Override public String toString() {
    return "[]";
  }
  Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
