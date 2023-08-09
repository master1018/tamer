final class SingletonImmutableSet<E> extends ImmutableSet<E> {
  final transient E element;
  private transient Integer cachedHashCode;
  SingletonImmutableSet(E element) {
    this.element = Preconditions.checkNotNull(element);
  }
  SingletonImmutableSet(E element, int hashCode) {
    this.element = element;
    cachedHashCode = hashCode;
  }
  public int size() {
    return 1;
  }
  @Override public boolean isEmpty() {
    return false;
  }
  @Override public boolean contains(Object target) {
    return element.equals(target);
  }
  @Override public UnmodifiableIterator<E> iterator() {
    return Iterators.singletonIterator(element);
  }
  @Override public Object[] toArray() {
    return new Object[] { element };
  }
  @SuppressWarnings({"unchecked"})
  @Override public <T> T[] toArray(T[] array) {
    if (array.length == 0) {
      array = ObjectArrays.newArray(array, 1);
    } else if (array.length > 1) {
      array[1] = null;
    }
    Object[] objectArray = array;
    objectArray[0] = element;
    return array;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Set) {
      Set<?> that = (Set<?>) object;
      return that.size() == 1 && element.equals(that.iterator().next());
    }
    return false;
  }
  @Override public final int hashCode() {
    Integer code = cachedHashCode;
    if (code == null) {
      return cachedHashCode = element.hashCode();
    }
    return code;
  }
  @Override boolean isHashCodeFast() {
    return false;
  }
  @Override public String toString() {
    String elementToString = element.toString();
    return new StringBuilder(elementToString.length() + 2)
        .append('[')
        .append(elementToString)
        .append(']')
        .toString();
  }
}
