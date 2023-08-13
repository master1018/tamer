final class SingletonImmutableList<E> extends ImmutableList<E> {
  final transient E element;
  SingletonImmutableList(E element) {
    this.element = checkNotNull(element);
  }
  public E get(int index) {
    Preconditions.checkElementIndex(index, 1);
    return element;
  }
  @Override public int indexOf(@Nullable Object object) {
    return element.equals(object) ? 0 : -1;
  }
  @Override public UnmodifiableIterator<E> iterator() {
    return Iterators.singletonIterator(element);
  }
  @Override public int lastIndexOf(@Nullable Object object) {
    return element.equals(object) ? 0 : -1;
  }
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }
  public ListIterator<E> listIterator(final int start) {
    return Collections.singletonList(element).listIterator(start);
  }
  public int size() {
    return 1;
  }
  @Override public ImmutableList<E> subList(int fromIndex, int toIndex) {
    Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
    return (fromIndex == toIndex) ? ImmutableList.<E>of() : this;
  }
  @Override public boolean contains(@Nullable Object object) {
    return element.equals(object);
  }
  @Override public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof List) {
      List<?> that = (List<?>) object;
      return that.size() == 1 && element.equals(that.get(0));
    }
    return false;
  }
  @Override public int hashCode() {
    return 31 + element.hashCode();
  }
  @Override public boolean isEmpty() {
    return false;
  }
  @Override public Object[] toArray() {
    return new Object[] { element };
  }
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
}
