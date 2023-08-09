abstract class ImmutableSortedSetFauxverideShim<E> extends ImmutableSet<E> {
  @Deprecated public static <E> ImmutableSortedSet.Builder<E> builder() {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(E element) {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(E e1, E e2) {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(E e1, E e2, E e3) {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4) {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4, E e5) {
    throw new UnsupportedOperationException();
  }
  @Deprecated public static <E> ImmutableSortedSet<E> of(E... elements) {
    throw new UnsupportedOperationException();
  }
}
