public abstract class Ordering<T> implements Comparator<T> {
  @GwtCompatible(serializable = true)
  @SuppressWarnings("unchecked") 
  public static <C extends Comparable> Ordering<C> natural() {
    return (Ordering) NaturalOrdering.INSTANCE;
  }
  @GwtCompatible(serializable = true)
  public static <T> Ordering<T> from(Comparator<T> comparator) {
    return (comparator instanceof Ordering)
        ? (Ordering<T>) comparator
        : new ComparatorOrdering<T>(comparator);
  }
  @GwtCompatible(serializable = true)
  @Deprecated public static <T> Ordering<T> from(Ordering<T> ordering) {
    return checkNotNull(ordering);
  }
  @GwtCompatible(serializable = true)
  public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
    return new ExplicitOrdering<T>(valuesInOrder);
  }
  @GwtCompatible(serializable = true)
  public static <T> Ordering<T> explicit(
      T leastValue, T... remainingValuesInOrder) {
    return explicit(Lists.asList(leastValue, remainingValuesInOrder));
  }
  @VisibleForTesting
  static class IncomparableValueException extends ClassCastException {
    final Object value;
    IncomparableValueException(Object value) {
      super("Cannot compare value: " + value);
      this.value = value;
    }
    private static final long serialVersionUID = 0;
  }
  public static Ordering<Object> arbitrary() {
    return ArbitraryOrderingHolder.ARBITRARY_ORDERING;
  }
  private static class ArbitraryOrderingHolder {
    static final Ordering<Object> ARBITRARY_ORDERING = new ArbitraryOrdering();
  }
  @VisibleForTesting static class ArbitraryOrdering extends Ordering<Object> {
    private Map<Object, Integer> uids =
        Platform.tryWeakKeys(new MapMaker()).makeComputingMap(
            new Function<Object, Integer>() {
              final AtomicInteger counter = new AtomicInteger(0);
              public Integer apply(Object from) {
                return counter.getAndIncrement();
              }
            });
     public int compare(Object left, Object right) {
      if (left == right) {
        return 0;
      }
      int leftCode = identityHashCode(left);
      int rightCode = identityHashCode(right);
      if (leftCode != rightCode) {
        return leftCode < rightCode ? -1 : 1;
      }
      int result = uids.get(left).compareTo(uids.get(right));
      if (result == 0) {
        throw new AssertionError(); 
      }
      return result;
    }
    @Override public String toString() {
      return "Ordering.arbitrary()";
    }
    int identityHashCode(Object object) {
      return System.identityHashCode(object);
    }
  }
  @GwtCompatible(serializable = true)
  public static Ordering<Object> usingToString() {
    return UsingToStringOrdering.INSTANCE;
  }
  @GwtCompatible(serializable = true)
  public static <T> Ordering<T> compound(
      Iterable<? extends Comparator<? super T>> comparators) {
    return new CompoundOrdering<T>(comparators);
  }
  protected Ordering() {}
  @GwtCompatible(serializable = true)
  public <U extends T> Ordering<U> compound(
      Comparator<? super U> secondaryComparator) {
    return new CompoundOrdering<U>(this, checkNotNull(secondaryComparator));
  }
  @GwtCompatible(serializable = true)
  public <S extends T> Ordering<S> reverse() {
    return new ReverseOrdering<S>(this);
  }
  @GwtCompatible(serializable = true)
  public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
    return new ByFunctionOrdering<F, T>(function, this);
  }
  @GwtCompatible(serializable = true)
  public <S extends T> Ordering<Iterable<S>> lexicographical() {
    return new LexicographicalOrdering<S>(this);
  }
  @GwtCompatible(serializable = true)
  public <S extends T> Ordering<S> nullsFirst() {
    return new NullsFirstOrdering<S>(this);
  }
  @GwtCompatible(serializable = true)
  public <S extends T> Ordering<S> nullsLast() {
    return new NullsLastOrdering<S>(this);
  }
  public int binarySearch(List<? extends T> sortedList, T key) {
    return Collections.binarySearch(sortedList, key, this);
  }
  public <E extends T> List<E> sortedCopy(Iterable<E> iterable) {
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list, this);
    return list;
  }
  public boolean isOrdered(Iterable<? extends T> iterable) {
    Iterator<? extends T> it = iterable.iterator();
    if (it.hasNext()) {
      T prev = it.next();
      while (it.hasNext()) {
        T next = it.next();
        if (compare(prev, next) > 0) {
          return false;
        }
        prev = next;
      }
    }
    return true;
  }
  public boolean isStrictlyOrdered(Iterable<? extends T> iterable) {
    Iterator<? extends T> it = iterable.iterator();
    if (it.hasNext()) {
      T prev = it.next();
      while (it.hasNext()) {
        T next = it.next();
        if (compare(prev, next) >= 0) {
          return false;
        }
        prev = next;
      }
    }
    return true;
  }
  public <E extends T> E max(Iterable<E> iterable) {
    Iterator<E> iterator = iterable.iterator();
    E maxSoFar = iterator.next();
    while (iterator.hasNext()) {
      maxSoFar = max(maxSoFar, iterator.next());
    }
    return maxSoFar;
  }
  public <E extends T> E max(E a, E b, E c, E... rest) {
    E maxSoFar = max(max(a, b), c);
    for (E r : rest) {
      maxSoFar = max(maxSoFar, r);
    }
    return maxSoFar;
  }
  public <E extends T> E max(E a, E b) {
    return compare(a, b) >= 0 ? a : b;
  }
  public <E extends T> E min(Iterable<E> iterable) {
    Iterator<E> iterator = iterable.iterator();
    E minSoFar = iterator.next();
    while (iterator.hasNext()) {
      minSoFar = min(minSoFar, iterator.next());
    }
    return minSoFar;
  }
  public <E extends T> E min(E a, E b, E c, E... rest) {
    E minSoFar = min(min(a, b), c);
    for (E r : rest) {
      minSoFar = min(minSoFar, r);
    }
    return minSoFar;
  }
  public <E extends T> E min(E a, E b) {
    return compare(a, b) <= 0 ? a : b;
  }
  static final int LEFT_IS_GREATER = 1;
  static final int RIGHT_IS_GREATER = -1;
}
