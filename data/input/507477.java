public class ImmutableMultiset<E> extends ImmutableCollection<E>
    implements Multiset<E> {
  @SuppressWarnings("unchecked") 
  public static <E> ImmutableMultiset<E> of() {
    return (ImmutableMultiset) EmptyImmutableMultiset.INSTANCE;
  }
  public static <E> ImmutableMultiset<E> of(E... elements) {
    return copyOf(Arrays.asList(elements));
  }
  public static <E> ImmutableMultiset<E> copyOf(
      Iterable<? extends E> elements) {
    if (elements instanceof ImmutableMultiset) {
      @SuppressWarnings("unchecked") 
      ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
      return result;
    }
    @SuppressWarnings("unchecked") 
    Multiset<? extends E> multiset = (elements instanceof Multiset)
        ? (Multiset<? extends E>) elements
        : LinkedHashMultiset.create(elements);
    return copyOfInternal(multiset);
  }
  private static <E> ImmutableMultiset<E> copyOfInternal(
      Multiset<? extends E> multiset) {
    long size = 0;
    ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
    for (Entry<? extends E> entry : multiset.entrySet()) {
      int count = entry.getCount();
      if (count > 0) {
        builder.put(entry.getElement(), count);
        size += count;
      }
    }
    if (size == 0) {
      return of();
    }
    return new ImmutableMultiset<E>(
        builder.build(), (int) Math.min(size, Integer.MAX_VALUE));
  }
  public static <E> ImmutableMultiset<E> copyOf(
      Iterator<? extends E> elements) {
    Multiset<E> multiset = LinkedHashMultiset.create();
    Iterators.addAll(multiset, elements);
    return copyOfInternal(multiset);
  }
  private final transient ImmutableMap<E, Integer> map;
  private final transient int size;
  @SuppressWarnings("unchecked")
  private static class FieldSettersHolder {
    static final FieldSetter<ImmutableMultiset> MAP_FIELD_SETTER
        = Serialization.getFieldSetter(ImmutableMultiset.class, "map");
    static final FieldSetter<ImmutableMultiset> SIZE_FIELD_SETTER
        = Serialization.getFieldSetter(ImmutableMultiset.class, "size");
  }
  ImmutableMultiset(ImmutableMap<E, Integer> map, int size) {
    this.map = map;
    this.size = size;
  }
  public int count(@Nullable Object element) {
    Integer value = map.get(element);
    return (value == null) ? 0 : value;
  }
  @Override public UnmodifiableIterator<E> iterator() {
    final Iterator<Map.Entry<E, Integer>> mapIterator
        = map.entrySet().iterator();
    return new UnmodifiableIterator<E>() {
      int remaining;
      E element;
      public boolean hasNext() {
        return (remaining > 0) || mapIterator.hasNext();
      }
      public E next() {
        if (remaining <= 0) {
          Map.Entry<E, Integer> entry = mapIterator.next();
          element = entry.getKey();
          remaining = entry.getValue();
        }
        remaining--;
        return element;
      }
    };
  }
  public int size() {
    return size;
  }
  @Override public boolean contains(@Nullable Object element) {
    return map.containsKey(element);
  }
  public int add(E element, int occurrences) {
    throw new UnsupportedOperationException();
  }
  public int remove(Object element, int occurrences) {
    throw new UnsupportedOperationException();
  }
  public int setCount(E element, int count) {
    throw new UnsupportedOperationException();
  }
  public boolean setCount(E element, int oldCount, int newCount) {
    throw new UnsupportedOperationException();
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Multiset) {
      Multiset<?> that = (Multiset<?>) object;
      if (this.size() != that.size()) {
        return false;
      }
      for (Entry<?> entry : that.entrySet()) {
        if (count(entry.getElement()) != entry.getCount()) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  @Override public int hashCode() {
    return map.hashCode();
  }
  @Override public String toString() {
    return entrySet().toString();
  }
  public Set<E> elementSet() {
    return map.keySet();
  }
  private transient ImmutableSet<Entry<E>> entrySet;
  public Set<Entry<E>> entrySet() {
    ImmutableSet<Entry<E>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet<E>(this)) : es;
  }
  private static class EntrySet<E> extends ImmutableSet<Entry<E>> {
    final ImmutableMultiset<E> multiset;
    public EntrySet(ImmutableMultiset<E> multiset) {
      this.multiset = multiset;
    }
    @Override public UnmodifiableIterator<Entry<E>> iterator() {
      final Iterator<Map.Entry<E, Integer>> mapIterator
          = multiset.map.entrySet().iterator();
      return new UnmodifiableIterator<Entry<E>>() {
        public boolean hasNext() {
          return mapIterator.hasNext();
        }
        public Entry<E> next() {
          Map.Entry<E, Integer> mapEntry = mapIterator.next();
          return
              Multisets.immutableEntry(mapEntry.getKey(), mapEntry.getValue());
        }
      };
    }
    public int size() {
      return multiset.map.size();
    }
    @Override public boolean contains(Object o) {
      if (o instanceof Entry) {
        Entry<?> entry = (Entry<?>) o;
        if (entry.getCount() <= 0) {
          return false;
        }
        int count = multiset.count(entry.getElement());
        return count == entry.getCount();
      }
      return false;
    }
    @Override public int hashCode() {
      return multiset.map.hashCode();
    }
    @Override Object writeReplace() {
      return this;
    }
    private static final long serialVersionUID = 0;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultiset(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int entryCount = stream.readInt();
    ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
    long tmpSize = 0;
    for (int i = 0; i < entryCount; i++) {
      @SuppressWarnings("unchecked") 
      E element = (E) stream.readObject();
      int count = stream.readInt();
      if (count <= 0) {
        throw new InvalidObjectException("Invalid count " + count);
      }
      builder.put(element, count);
      tmpSize += count;
    }
    FieldSettersHolder.MAP_FIELD_SETTER.set(this, builder.build());
    FieldSettersHolder.SIZE_FIELD_SETTER.set(
        this, (int) Math.min(tmpSize, Integer.MAX_VALUE));
  }
  @Override Object writeReplace() {
    return this;
  }
  private static final long serialVersionUID = 0;
  public static <E> Builder<E> builder() {
    return new Builder<E>();
  }
  public static final class Builder<E> extends ImmutableCollection.Builder<E> {
    private final Multiset<E> contents = LinkedHashMultiset.create();
    public Builder() {}
    @Override public Builder<E> add(E element) {
      contents.add(checkNotNull(element));
      return this;
    }
    public Builder<E> addCopies(E element, int occurrences) {
      contents.add(checkNotNull(element), occurrences);
      return this;
    }
    public Builder<E> setCount(E element, int count) {
      contents.setCount(checkNotNull(element), count);
      return this;
    }
    @Override public Builder<E> add(E... elements) {
      super.add(elements);
      return this;
    }
    @Override public Builder<E> addAll(Iterable<? extends E> elements) {
      if (elements instanceof Multiset) {
        @SuppressWarnings("unchecked")
        Multiset<? extends E> multiset = (Multiset<? extends E>) elements;
        for (Entry<? extends E> entry : multiset.entrySet()) {
          addCopies(entry.getElement(), entry.getCount());
        }
      } else {
        super.addAll(elements);
      }
      return this;
    }
    @Override public Builder<E> addAll(Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }
    @Override public ImmutableMultiset<E> build() {
      return copyOf(contents);
    }
  }
}
