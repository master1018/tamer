public final class Iterables {
  private Iterables() {}
  public static <T> Iterable<T> unmodifiableIterable(final Iterable<T> iterable)
  {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(iterable.iterator());
      }
      @Override public String toString() {
        return iterable.toString();
      }
    };
  }
  public static int size(Iterable<?> iterable) {
    return (iterable instanceof Collection)
        ? ((Collection<?>) iterable).size()
        : Iterators.size(iterable.iterator());
  }
  public static boolean contains(Iterable<?> iterable, @Nullable Object element)
  {
    if (iterable instanceof Collection) {
      Collection<?> collection = (Collection<?>) iterable;
      try {
        return collection.contains(element);
      } catch (NullPointerException e) {
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }
    return Iterators.contains(iterable.iterator(), element);
  }
  public static boolean removeAll(
      Iterable<?> removeFrom, Collection<?> elementsToRemove) {
    return (removeFrom instanceof Collection)
        ? ((Collection<?>) removeFrom).removeAll(checkNotNull(elementsToRemove))
        : Iterators.removeAll(removeFrom.iterator(), elementsToRemove);
  }
  public static boolean retainAll(
      Iterable<?> removeFrom, Collection<?> elementsToRetain) {
    return (removeFrom instanceof Collection)
        ? ((Collection<?>) removeFrom).retainAll(checkNotNull(elementsToRetain))
        : Iterators.retainAll(removeFrom.iterator(), elementsToRetain);
  }
  public static <T> boolean removeIf(
      Iterable<T> removeFrom, Predicate<? super T> predicate) {
    if (removeFrom instanceof RandomAccess && removeFrom instanceof List) {
      return removeIfFromRandomAccessList(
          (List<T>) removeFrom, checkNotNull(predicate));
    }
    return Iterators.removeIf(removeFrom.iterator(), predicate);
  }
  private static <T> boolean removeIfFromRandomAccessList(
      List<T> list, Predicate<? super T> predicate) {
    int from = 0;
    int to = 0;
    for (; from < list.size(); from++) {
      T element = list.get(from);
      if (!predicate.apply(element)) {
        if (from > to) {
          list.set(to, element);
        }
        to++;
      }
    }
    ListIterator<T> iter = list.listIterator(list.size());
    for (int idx = from - to; idx > 0; idx--) {
      iter.previous();
      iter.remove();
    }
    return from != to;
  }
  public static boolean elementsEqual(
      Iterable<?> iterable1, Iterable<?> iterable2) {
    return Iterators.elementsEqual(iterable1.iterator(), iterable2.iterator());
  }
  public static String toString(Iterable<?> iterable) {
    return Iterators.toString(iterable.iterator());
  }
  public static <T> T getOnlyElement(Iterable<T> iterable) {
    return Iterators.getOnlyElement(iterable.iterator());
  }
  public static <T> T getOnlyElement(
      Iterable<T> iterable, @Nullable T defaultValue) {
    return Iterators.getOnlyElement(iterable.iterator(), defaultValue);
  }
  @GwtIncompatible("Array.newInstance(Class, int)")
  public static <T> T[] toArray(Iterable<? extends T> iterable, Class<T> type) {
    @SuppressWarnings("unchecked") 
    Collection<? extends T> collection = (iterable instanceof Collection)
        ? (Collection<? extends T>) iterable
        : Lists.newArrayList(iterable);
    T[] array = ObjectArrays.newArray(type, collection.size());
    return collection.toArray(array);
  }
  public static <T> boolean addAll(
      Collection<T> addTo, Iterable<? extends T> elementsToAdd) {
    if (elementsToAdd instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<? extends T> c = (Collection<? extends T>) elementsToAdd;
      return addTo.addAll(c);
    }
    return Iterators.addAll(addTo, elementsToAdd.iterator());
  }
  public static int frequency(Iterable<?> iterable, @Nullable Object element) {
    if ((iterable instanceof Multiset)) {
      return ((Multiset<?>) iterable).count(element);
    }
    if ((iterable instanceof Set)) {
      return ((Set<?>) iterable).contains(element) ? 1 : 0;
    }
    return Iterators.frequency(iterable.iterator(), element);
  }
  public static <T> Iterable<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.cycle(iterable);
      }
      @Override public String toString() {
        return iterable.toString() + " (cycled)";
      }
    };
  }
  public static <T> Iterable<T> cycle(T... elements) {
    return cycle(Lists.newArrayList(elements));
  }
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> concat(
      Iterable<? extends T> a, Iterable<? extends T> b) {
    checkNotNull(a);
    checkNotNull(b);
    return concat(Arrays.asList(a, b));
  }
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> concat(Iterable<? extends T> a,
      Iterable<? extends T> b, Iterable<? extends T> c) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    return concat(Arrays.asList(a, b, c));
  }
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> concat(Iterable<? extends T> a,
      Iterable<? extends T> b, Iterable<? extends T> c,
      Iterable<? extends T> d) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    checkNotNull(d);
    return concat(Arrays.asList(a, b, c, d));
  }
  public static <T> Iterable<T> concat(Iterable<? extends T>... inputs) {
    return concat(ImmutableList.of(inputs));
  }
  public static <T> Iterable<T> concat(
      Iterable<? extends Iterable<? extends T>> inputs) {
    Function<Iterable<? extends T>, Iterator<? extends T>> function
        = new Function<Iterable<? extends T>, Iterator<? extends T>>() {
      public Iterator<? extends T> apply(Iterable<? extends T> from) {
        return from.iterator();
      }
    };
    final Iterable<Iterator<? extends T>> iterators
        = transform(inputs, function);
    return new IterableWithToString<T>() {
      public Iterator<T> iterator() {
        return Iterators.concat(iterators.iterator());
      }
    };
  }
  public static <T> Iterable<List<T>> partition(
      final Iterable<T> iterable, final int size) {
    checkNotNull(iterable);
    checkArgument(size > 0);
    return new IterableWithToString<List<T>>() {
      public Iterator<List<T>> iterator() {
        return Iterators.partition(iterable.iterator(), size);
      }
    };
  }
  public static <T> Iterable<List<T>> paddedPartition(
      final Iterable<T> iterable, final int size) {
    checkNotNull(iterable);
    checkArgument(size > 0);
    return new IterableWithToString<List<T>>() {
      public Iterator<List<T>> iterator() {
        return Iterators.paddedPartition(iterable.iterator(), size);
      }
    };
  }
  public static <T> Iterable<T> filter(
      final Iterable<T> unfiltered, final Predicate<? super T> predicate) {
    checkNotNull(unfiltered);
    checkNotNull(predicate);
    return new IterableWithToString<T>() {
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), predicate);
      }
    };
  }
  @GwtIncompatible("Class.isInstance")
  public static <T> Iterable<T> filter(
      final Iterable<?> unfiltered, final Class<T> type) {
    checkNotNull(unfiltered);
    checkNotNull(type);
    return new IterableWithToString<T>() {
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), type);
      }
    };
  }
  public static <T> boolean any(
      Iterable<T> iterable, Predicate<? super T> predicate) {
    return Iterators.any(iterable.iterator(), predicate);
  }
  public static <T> boolean all(
      Iterable<T> iterable, Predicate<? super T> predicate) {
    return Iterators.all(iterable.iterator(), predicate);
  }
  public static <T> T find(Iterable<T> iterable,
      Predicate<? super T> predicate) {
    return Iterators.find(iterable.iterator(), predicate);
  }
  public static <T> int indexOf(
      Iterable<T> iterable, Predicate<? super T> predicate) {
    return Iterators.indexOf(iterable.iterator(), predicate);
  }
  public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,
      final Function<? super F, ? extends T> function) {
    checkNotNull(fromIterable);
    checkNotNull(function);
    return new IterableWithToString<T>() {
      public Iterator<T> iterator() {
        return Iterators.transform(fromIterable.iterator(), function);
      }
    };
  }
  public static <T> T get(Iterable<T> iterable, int position) {
    checkNotNull(iterable);
    if (iterable instanceof List) {
      return ((List<T>) iterable).get(position);
    }
    if (iterable instanceof Collection) {
      Collection<T> collection = (Collection<T>) iterable;
      Preconditions.checkElementIndex(position, collection.size());
    } else {
      if (position < 0) {
        throw new IndexOutOfBoundsException(
            "position cannot be negative: " + position);
      }
    }
    return Iterators.get(iterable.iterator(), position);
  }
  public static <T> T getLast(Iterable<T> iterable) {
    if (iterable instanceof List) {
      List<T> list = (List<T>) iterable;
      if (list.isEmpty()) {
        throw new NoSuchElementException();
      }
      return list.get(list.size() - 1);
    }
    if (iterable instanceof SortedSet) {
      SortedSet<T> sortedSet = (SortedSet<T>) iterable;
      return sortedSet.last();
    }
    return Iterators.getLast(iterable.iterator());
  }
  public static <T> Iterable<T> consumingIterable(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.consumingIterator(iterable.iterator());
      }
    };
  }
  public static <T> Iterable<T> reverse(final List<T> list) {
    checkNotNull(list);
    return new IterableWithToString<T>() {
      public Iterator<T> iterator() {
        final ListIterator<T> listIter = list.listIterator(list.size());
        return new Iterator<T>() {
          public boolean hasNext() {
            return listIter.hasPrevious();
          }
          public T next() {
            return listIter.previous();
          }
          public void remove() {
            listIter.remove();
          }
        };
      }
    };
  }
  public static <T> boolean isEmpty(Iterable<T> iterable) {
    return !iterable.iterator().hasNext();
  }
  static boolean remove(Iterable<?> iterable, @Nullable Object o) {
    Iterator<?> i = iterable.iterator();
    while (i.hasNext()) {
      if (Objects.equal(i.next(), o)) {
        i.remove();
        return true;
      }
    }
    return false;
  }
  abstract static class IterableWithToString<E> implements Iterable<E> {
    @Override public String toString() {
      return Iterables.toString(this);
    }
  }
}
