public final class Multisets {
  private Multisets() {}
  public static <E> Multiset<E> unmodifiableMultiset(
      Multiset<? extends E> multiset) {
    return new UnmodifiableMultiset<E>(multiset);
  }
  private static class UnmodifiableMultiset<E>
      extends ForwardingMultiset<E> implements Serializable {
    final Multiset<? extends E> delegate;
    UnmodifiableMultiset(Multiset<? extends E> delegate) {
      this.delegate = delegate;
    }
    @SuppressWarnings("unchecked")
    @Override protected Multiset<E> delegate() {
      return (Multiset) delegate;
    }
    transient Set<E> elementSet;
    @Override public Set<E> elementSet() {
      Set<E> es = elementSet;
      return (es == null)
          ? elementSet = Collections.<E>unmodifiableSet(delegate.elementSet())
          : es;
    }
    transient Set<Multiset.Entry<E>> entrySet;
    @SuppressWarnings("unchecked")
    @Override public Set<Multiset.Entry<E>> entrySet() {
      Set<Multiset.Entry<E>> es = entrySet;
      return (es == null)
          ? entrySet = (Set) Collections.unmodifiableSet(delegate.entrySet())
          : es;
    }
    @SuppressWarnings("unchecked")
    @Override public Iterator<E> iterator() {
      return (Iterator) Iterators.unmodifiableIterator(delegate.iterator());
    }
    @Override public boolean add(E element) {
      throw new UnsupportedOperationException();
    }
    @Override public int add(E element, int occurences) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean addAll(Collection<? extends E> elementsToAdd) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean remove(Object element) {
      throw new UnsupportedOperationException();
    }
    @Override public int remove(Object element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean removeAll(Collection<?> elementsToRemove) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean retainAll(Collection<?> elementsToRetain) {
      throw new UnsupportedOperationException();
    }
    @Override public void clear() {
      throw new UnsupportedOperationException();
    }
    @Override public int setCount(E element, int count) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean setCount(E element, int oldCount, int newCount) {
      throw new UnsupportedOperationException();
    }
    private static final long serialVersionUID = 0;
  }
  public static <E> Multiset.Entry<E> immutableEntry(
      @Nullable final E e, final int n) {
    checkArgument(n >= 0);
    return new AbstractEntry<E>() {
      public E getElement() {
        return e;
      }
      public int getCount() {
        return n;
      }
    };
  }
  static <E> Multiset<E> forSet(Set<E> set) {
    return new SetMultiset<E>(set);
  }
  private static class SetMultiset<E> extends ForwardingCollection<E>
      implements Multiset<E>, Serializable {
    final Set<E> delegate;
    SetMultiset(Set<E> set) {
      delegate = checkNotNull(set);
    }
    @Override protected Set<E> delegate() {
      return delegate;
    }
    public int count(Object element) {
      return delegate.contains(element) ? 1 : 0;
    }
    public int add(E element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int remove(Object element, int occurrences) {
      if (occurrences == 0) {
        return count(element);
      }
      checkArgument(occurrences > 0);
      return delegate.remove(element) ? 1 : 0;
    }
    transient Set<E> elementSet;
    public Set<E> elementSet() {
      Set<E> es = elementSet;
      return (es == null) ? elementSet = new ElementSet() : es;
    }
    transient Set<Entry<E>> entrySet;
    public Set<Entry<E>> entrySet() {
      Set<Entry<E>> es = entrySet;
      return (es == null) ? entrySet = new EntrySet() : es;
    }
    @Override public boolean add(E o) {
      throw new UnsupportedOperationException();
    }
    @Override public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }
    public int setCount(E element, int count) {
      checkNonnegative(count, "count");
      if (count == count(element)) {
        return count;
      } else if (count == 0) {
        remove(element);
        return 1;
      } else {
        throw new UnsupportedOperationException();
      }
    }
    public boolean setCount(E element, int oldCount, int newCount) {
      return setCountImpl(this, element, oldCount, newCount);
    }
    @Override public boolean equals(@Nullable Object object) {
      if (object instanceof Multiset) {
        Multiset<?> that = (Multiset<?>) object;
        return this.size() == that.size() && delegate.equals(that.elementSet());
      }
      return false;
    }
    @Override public int hashCode() {
      int sum = 0;
      for (E e : this) {
        sum += ((e == null) ? 0 : e.hashCode()) ^ 1;
      }
      return sum;
    }
    class ElementSet extends ForwardingSet<E> {
      @Override protected Set<E> delegate() {
        return delegate;
      }
      @Override public boolean add(E o) {
        throw new UnsupportedOperationException();
      }
      @Override public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
      }
    }
    class EntrySet extends AbstractSet<Entry<E>> {
      @Override public int size() {
        return delegate.size();
      }
      @Override public Iterator<Entry<E>> iterator() {
        return new Iterator<Entry<E>>() {
          final Iterator<E> elements = delegate.iterator();
          public boolean hasNext() {
            return elements.hasNext();
          }
          public Entry<E> next() {
            return immutableEntry(elements.next(), 1);
          }
          public void remove() {
            elements.remove();
          }
        };
      }
    }
    private static final long serialVersionUID = 0;
  }
  static int inferDistinctElements(Iterable<?> elements) {
    if (elements instanceof Multiset) {
      return ((Multiset<?>) elements).elementSet().size();
    }
    return 11; 
  }
  public static <E> Multiset<E> intersection(
      final Multiset<E> multiset1, final Multiset<?> multiset2) {
    checkNotNull(multiset1);
    checkNotNull(multiset2);
    return new AbstractMultiset<E>() {
      @Override public int count(Object element) {
        int count1 = multiset1.count(element);
        return (count1 == 0) ? 0 : Math.min(count1, multiset2.count(element));
      }
      @Override Set<E> createElementSet() {
        return Sets.intersection(
            multiset1.elementSet(), multiset2.elementSet());
      }
      @Override public Set<Entry<E>> entrySet() {
        return entrySet;
      }
      final Set<Entry<E>> entrySet = new AbstractSet<Entry<E>>() {
        @Override public Iterator<Entry<E>> iterator() {
          final Iterator<Entry<E>> iterator1 = multiset1.entrySet().iterator();
          return new AbstractIterator<Entry<E>>() {
            @Override protected Entry<E> computeNext() {
              while (iterator1.hasNext()) {
                Entry<E> entry1 = iterator1.next();
                E element = entry1.getElement();
                int count
                    = Math.min(entry1.getCount(), multiset2.count(element));
                if (count > 0) {
                  return Multisets.immutableEntry(element, count);
                }
              }
              return endOfData();
            }
          };
        }
        @Override public int size() {
          return elementSet().size();
        }
        @Override public boolean contains(Object o) {
          if (o instanceof Entry) {
            Entry<?> entry = (Entry<?>) o;
            int entryCount = entry.getCount();
            return (entryCount > 0)
                && (count(entry.getElement()) == entryCount);
          }
          return false;
        }
        @Override public boolean isEmpty() {
          return elementSet().isEmpty();
        }
      };
    };
  }
  abstract static class AbstractEntry<E> implements Multiset.Entry<E> {
    @Override public boolean equals(@Nullable Object object) {
      if (object instanceof Multiset.Entry) {
        Multiset.Entry<?> that = (Multiset.Entry<?>) object;
        return this.getCount() == that.getCount()
            && Objects.equal(this.getElement(), that.getElement());
      }
      return false;
    }
    @Override public int hashCode() {
      E e = getElement();
      return ((e == null) ? 0 : e.hashCode()) ^ getCount();
    }
    @Override public String toString() {
      String text = String.valueOf(getElement());
      int n = getCount();
      return (n == 1) ? text : (text + " x " + n);
    }
  }
  static <E> int setCountImpl(Multiset<E> self, E element, int count) {
    checkNonnegative(count, "count");
    int oldCount = self.count(element);
    int delta = count - oldCount;
    if (delta > 0) {
      self.add(element, delta);
    } else if (delta < 0) {
      self.remove(element, -delta);
    }
    return oldCount;
  }
  static <E> boolean setCountImpl(
      Multiset<E> self, E element, int oldCount, int newCount) {
    checkNonnegative(oldCount, "oldCount");
    checkNonnegative(newCount, "newCount");
    if (self.count(element) == oldCount) {
      self.setCount(element, newCount);
      return true;
    } else {
      return false;
    }
  }
  static void checkNonnegative(int count, String name) {
    checkArgument(count >= 0, "%s cannot be negative: %s", name, count);
  }
}
