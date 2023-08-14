final class ImmutableSortedAsList<E> extends RegularImmutableList<E> {
  private final transient ImmutableSortedSet<E> set;
  ImmutableSortedAsList(Object[] array, int offset, int size,
      ImmutableSortedSet<E> set) {
    super(array, offset, size);
    this.set = set;
  }
  @Override public boolean contains(Object target) {
    return set.indexOf(target) >= 0;
  }
  @Override public int indexOf(Object target) {
    return set.indexOf(target);
  }
  @Override public int lastIndexOf(Object target) {
    return set.indexOf(target);
  }
  @Override public ImmutableList<E> subList(int fromIndex, int toIndex) {
    Preconditions.checkPositionIndexes(fromIndex, toIndex, size());
    return (fromIndex == toIndex)
        ? ImmutableList.<E>of()
        : new RegularImmutableSortedSet<E>(
            array(), set.comparator(),
            offset() + fromIndex, offset() + toIndex).asList();
  }
  @Override Object writeReplace() {
    return new ImmutableAsList.SerializedForm(set);
  }
}
