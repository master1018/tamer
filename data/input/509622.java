public final class Collections2 {
  private Collections2() {}
  static boolean containsAll(Collection<?> self, Collection<?> c) {
    checkNotNull(self);
    for (Object o : c) {
      if (!self.contains(o)) {
        return false;
      }
    }
    return true;
  }
  static <E> Collection<E> toCollection(Iterable<E> iterable) {
    return (iterable instanceof Collection)
        ? (Collection<E>) iterable : Lists.newArrayList(iterable);
  }
  public static <E> Collection<E> filter(
      Collection<E> unfiltered, Predicate<? super E> predicate) {
    if (unfiltered instanceof FilteredCollection) {
      return ((FilteredCollection<E>) unfiltered).createCombined(predicate);
    }
    return new FilteredCollection<E>(
        checkNotNull(unfiltered), checkNotNull(predicate));
  }
  static boolean safeContains(Collection<?> collection, Object object) {
    try {
      return collection.contains(object);
    } catch (ClassCastException e) {
      return false;
    }
  }
  static class FilteredCollection<E> implements Collection<E> {
    final Collection<E> unfiltered;
    final Predicate<? super E> predicate;
    FilteredCollection(Collection<E> unfiltered,
        Predicate<? super E> predicate) {
      this.unfiltered = unfiltered;
      this.predicate = predicate;
    }
    FilteredCollection<E> createCombined(Predicate<? super E> newPredicate) {
      return new FilteredCollection<E>(unfiltered,
          Predicates.<E>and(predicate, newPredicate));
    }
    public boolean add(E element) {
      checkArgument(predicate.apply(element));
      return unfiltered.add(element);
    }
    public boolean addAll(Collection<? extends E> collection) {
      for (E element : collection) {
        checkArgument(predicate.apply(element));
      }
      return unfiltered.addAll(collection);
    }
    public void clear() {
      Iterables.removeIf(unfiltered, predicate);
    }
    public boolean contains(Object element) {
      try {
        @SuppressWarnings("unchecked")
        E e = (E) element;
        return predicate.apply(e) && unfiltered.contains(element);
      } catch (NullPointerException e) {
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }
    public boolean containsAll(Collection<?> collection) {
      for (Object element : collection) {
        if (!contains(element)) {
          return false;
        }
      }
      return true;
    }
    public boolean isEmpty() {
      return !Iterators.any(unfiltered.iterator(), predicate);
    }
    public Iterator<E> iterator() {
      return Iterators.filter(unfiltered.iterator(), predicate);
    }
    public boolean remove(Object element) {
      try {
        @SuppressWarnings("unchecked")
        E e = (E) element;
        return predicate.apply(e) && unfiltered.remove(element);
      } catch (NullPointerException e) {
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }
    public boolean removeAll(final Collection<?> collection) {
      checkNotNull(collection);
      Predicate<E> combinedPredicate = new Predicate<E>() {
        public boolean apply(E input) {
          return predicate.apply(input) && collection.contains(input);
        }
      };
      return Iterables.removeIf(unfiltered, combinedPredicate);
    }
    public boolean retainAll(final Collection<?> collection) {
      checkNotNull(collection);
      Predicate<E> combinedPredicate = new Predicate<E>() {
        public boolean apply(E input) {
          return predicate.apply(input) && !collection.contains(input);
        }
      };
      return Iterables.removeIf(unfiltered, combinedPredicate);
    }
    public int size() {
      return Iterators.size(iterator());
    }
    public Object[] toArray() {
      return Lists.newArrayList(iterator()).toArray();
    }
    public <T> T[] toArray(T[] array) {
      return Lists.newArrayList(iterator()).toArray(array);
    }
    @Override public String toString() {
      return Iterators.toString(iterator());
    }
  }
  public static <F, T> Collection<T> transform(Collection<F> fromCollection,
      Function<? super F, T> function) {
    return new TransformedCollection<F, T>(fromCollection, function);
  }
  static class TransformedCollection<F, T> extends AbstractCollection<T> {
    final Collection<F> fromCollection;
    final Function<? super F, ? extends T> function;
    TransformedCollection(Collection<F> fromCollection,
        Function<? super F, ? extends T> function) {
      this.fromCollection = checkNotNull(fromCollection);
      this.function = checkNotNull(function);
    }
    @Override public void clear() {
      fromCollection.clear();
    }
    @Override public boolean isEmpty() {
      return fromCollection.isEmpty();
    }
    @Override public Iterator<T> iterator() {
      return Iterators.transform(fromCollection.iterator(), function);
    }
    @Override public int size() {
      return fromCollection.size();
    }
  }
  static boolean setEquals(Set<?> thisSet, @Nullable Object object) {
    if (object == thisSet) {
      return true;
    }
    if (object instanceof Set) {
      Set<?> thatSet = (Set<?>) object;
      return thisSet.size() == thatSet.size()
          && thisSet.containsAll(thatSet);
    }
    return false;
  }
  static final Joiner standardJoiner = Joiner.on(", ");
}
