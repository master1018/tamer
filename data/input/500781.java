public abstract class ImmutableSortedSet<E>
    extends ImmutableSortedSetFauxverideShim<E> implements SortedSet<E> {
  @SuppressWarnings("unchecked")
  private static final Comparator NATURAL_ORDER = Ordering.natural();
  @SuppressWarnings("unchecked")
  private static final ImmutableSortedSet<Object> NATURAL_EMPTY_SET =
      new EmptyImmutableSortedSet<Object>(NATURAL_ORDER);
  @SuppressWarnings("unchecked")
  private static <E> ImmutableSortedSet<E> emptySet() {
    return (ImmutableSortedSet<E>) NATURAL_EMPTY_SET;
  }
  static <E> ImmutableSortedSet<E> emptySet(
      Comparator<? super E> comparator) {
    if (NATURAL_ORDER.equals(comparator)) {
      return emptySet();
    } else {
      return new EmptyImmutableSortedSet<E>(comparator);
    }
  }
  public static <E> ImmutableSortedSet<E> of() {
    return emptySet();
  }
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E element) {
    Object[] array = { checkNotNull(element) };
    return new RegularImmutableSortedSet<E>(array, Ordering.natural());
  }
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E e1, E e2) {
    return ofInternal(Ordering.natural(), e1, e2);
  }
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E e1, E e2, E e3) {
    return ofInternal(Ordering.natural(), e1, e2, e3);
  }
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4) {
    return ofInternal(Ordering.natural(), e1, e2, e3, e4);
  }
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4, E e5) {
    return ofInternal(Ordering.natural(), e1, e2, e3, e4, e5);
  }
  public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(
      E... elements) {
    return ofInternal(Ordering.natural(), elements);
  }
  private static <E> ImmutableSortedSet<E> ofInternal(
      Comparator<? super E> comparator, E... elements) {
    checkNotNull(elements); 
    switch (elements.length) {
      case 0:
        return emptySet(comparator);
      default:
        Object[] array = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
          array[i] = checkNotNull(elements[i]);
        }
        sort(array, comparator);
        array = removeDupes(array, comparator);
        return new RegularImmutableSortedSet<E>(array, comparator);
    }
  }
  @SuppressWarnings("unchecked") 
  private static <E> void sort(
      Object[] array, Comparator<? super E> comparator) {
    Arrays.sort(array, (Comparator<Object>) comparator);
  }
  private static <E> Object[] removeDupes(Object[] array,
      Comparator<? super E> comparator) {
    int size = 1;
    for (int i = 1; i < array.length; i++) {
      Object element = array[i];
      if (unsafeCompare(comparator, array[size - 1], element) != 0) {
        array[size] = element;
        size++;
      }
    }
    if (size == array.length) {
      return array;
    } else {
      Object[] copy = new Object[size];
      Platform.unsafeArrayCopy(array, 0, copy, 0, size);
      return copy;
    }
  }
  public static <E> ImmutableSortedSet<E> copyOf(
      Iterable<? extends E> elements) {
    @SuppressWarnings("unchecked")
    Ordering<E> naturalOrder = (Ordering) Ordering.<Comparable>natural();
    return copyOfInternal(naturalOrder, elements, false);
  }
  public static <E> ImmutableSortedSet<E> copyOf(
      Iterator<? extends E> elements) {
    @SuppressWarnings("unchecked")
    Ordering<E> naturalOrder = (Ordering) Ordering.<Comparable>natural();
    return copyOfInternal(naturalOrder, elements);
  }
  public static <E> ImmutableSortedSet<E> copyOf(
      Comparator<? super E> comparator, Iterable<? extends E> elements) {
    checkNotNull(comparator);
    return copyOfInternal(comparator, elements, false);
  }
  public static <E> ImmutableSortedSet<E> copyOf(
      Comparator<? super E> comparator, Iterator<? extends E> elements) {
    checkNotNull(comparator);
    return copyOfInternal(comparator, elements);
  }
  @SuppressWarnings("unchecked")
  public static <E> ImmutableSortedSet<E> copyOfSorted(SortedSet<E> sortedSet) {
    Comparator<? super E> comparator = sortedSet.comparator();
    if (comparator == null) {
      comparator = NATURAL_ORDER;
    }
    return copyOfInternal(comparator, sortedSet, true);
  }
  private static <E> ImmutableSortedSet<E> copyOfInternal(
      Comparator<? super E> comparator, Iterable<? extends E> elements,
      boolean fromSortedSet) {
    boolean hasSameComparator
        = fromSortedSet || hasSameComparator(elements, comparator);
    if (hasSameComparator && (elements instanceof ImmutableSortedSet)) {
      @SuppressWarnings("unchecked")
      ImmutableSortedSet<E> result = (ImmutableSortedSet<E>) elements;
      if (!result.hasPartialArray()) {
        return result;
      }
    }
    Object[] array = newObjectArray(elements);
    if (array.length == 0) {
      return emptySet(comparator);
    }
    for (Object e : array) {
      checkNotNull(e);
    }
    if (!hasSameComparator) {
      sort(array, comparator);
      array = removeDupes(array, comparator);
    }
    return new RegularImmutableSortedSet<E>(array, comparator);
  }
  private static <T> Object[] newObjectArray(Iterable<T> iterable) {
    Collection<T> collection = (iterable instanceof Collection)
        ? (Collection<T>) iterable : Lists.newArrayList(iterable);
    Object[] array = new Object[collection.size()];
    return collection.toArray(array);
  }
  private static <E> ImmutableSortedSet<E> copyOfInternal(
      Comparator<? super E> comparator, Iterator<? extends E> elements) {
    if (!elements.hasNext()) {
      return emptySet(comparator);
    }
    List<E> list = Lists.newArrayList();
    while (elements.hasNext()) {
      list.add(checkNotNull(elements.next()));
    }
    Object[] array = list.toArray();
    sort(array, comparator);
    array = removeDupes(array, comparator);
    return new RegularImmutableSortedSet<E>(array, comparator);
  }
  static boolean hasSameComparator(
      Iterable<?> elements, Comparator<?> comparator) {
    if (elements instanceof SortedSet) {
      SortedSet<?> sortedSet = (SortedSet<?>) elements;
      Comparator<?> comparator2 = sortedSet.comparator();
      return (comparator2 == null)
          ? comparator == Ordering.natural()
          : comparator.equals(comparator2);
    }
    return false;
  }
  public static <E> Builder<E> orderedBy(Comparator<E> comparator) {
    return new Builder<E>(comparator);
  }
  public static <E extends Comparable<E>> Builder<E> reverseOrder() {
    return new Builder<E>(Ordering.natural().reverse());
  }
  public static <E extends Comparable<E>> Builder<E> naturalOrder() {
    return new Builder<E>(Ordering.natural());
  }
  public static final class Builder<E> extends ImmutableSet.Builder<E> {
    private final Comparator<? super E> comparator;
    public Builder(Comparator<? super E> comparator) {
      this.comparator = checkNotNull(comparator);
    }
    @Override public Builder<E> add(E element) {
      super.add(element);
      return this;
    }
    @Override public Builder<E> add(E... elements) {
      super.add(elements);
      return this;
    }
    @Override public Builder<E> addAll(Iterable<? extends E> elements) {
      super.addAll(elements);
      return this;
    }
    @Override public Builder<E> addAll(Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }
    @Override public ImmutableSortedSet<E> build() {
      return copyOfInternal(comparator, contents.iterator());
    }
  }
  int unsafeCompare(Object a, Object b) {
    return unsafeCompare(comparator, a, b);
  }
  static int unsafeCompare(
      Comparator<?> comparator, Object a, Object b) {
    @SuppressWarnings("unchecked")
    Comparator<Object> unsafeComparator = (Comparator<Object>) comparator;
    return unsafeComparator.compare(a, b);
  }
  final transient Comparator<? super E> comparator;
  ImmutableSortedSet(Comparator<? super E> comparator) {
    this.comparator = comparator;
  }
  public Comparator<? super E> comparator() {
    return comparator;
  }
  public ImmutableSortedSet<E> headSet(E toElement) {
    return headSetImpl(checkNotNull(toElement));
  }
  public ImmutableSortedSet<E> subSet(E fromElement, E toElement) {
    checkNotNull(fromElement);
    checkNotNull(toElement);
    checkArgument(comparator.compare(fromElement, toElement) <= 0);
    return subSetImpl(fromElement, toElement);
  }
  public ImmutableSortedSet<E> tailSet(E fromElement) {
    return tailSetImpl(checkNotNull(fromElement));
  }
  abstract ImmutableSortedSet<E> headSetImpl(E toElement);
  abstract ImmutableSortedSet<E> subSetImpl(E fromElement, E toElement);
  abstract ImmutableSortedSet<E> tailSetImpl(E fromElement);
  abstract boolean hasPartialArray();
  abstract int indexOf(Object target);
  private static class SerializedForm<E> implements Serializable {
    final Comparator<? super E> comparator;
    final Object[] elements;
    public SerializedForm(Comparator<? super E> comparator, Object[] elements) {
      this.comparator = comparator;
      this.elements = elements;
    }
    @SuppressWarnings("unchecked")
    Object readResolve() {
      return new Builder<E>(comparator).add((E[]) elements).build();
    }
    private static final long serialVersionUID = 0;
  }
  private void readObject(ObjectInputStream stream)
      throws InvalidObjectException {
    throw new InvalidObjectException("Use SerializedForm");
  }
  @Override Object writeReplace() {
    return new SerializedForm<E>(comparator, toArray());
  }
}
