public abstract class ImmutableCollection<E>
    implements Collection<E>, Serializable {
  static final ImmutableCollection<Object> EMPTY_IMMUTABLE_COLLECTION
      = new EmptyImmutableCollection();
  ImmutableCollection() {}
  public abstract UnmodifiableIterator<E> iterator();
  public Object[] toArray() {
    Object[] newArray = new Object[size()];
    return toArray(newArray);
  }
  public <T> T[] toArray(T[] other) {
    int size = size();
    if (other.length < size) {
      other = ObjectArrays.newArray(other, size);
    } else if (other.length > size) {
      other[size] = null;
    }
    Object[] otherAsObjectArray = other;
    int index = 0;
    for (E element : this) {
      otherAsObjectArray[index++] = element;
    }
    return other;
  }
  public boolean contains(@Nullable Object object) {
    if (object == null) {
      return false;
    }
    for (E element : this) {
      if (element.equals(object)) {
        return true;
      }
    }
    return false;
  }
  public boolean containsAll(Collection<?> targets) {
    for (Object target : targets) {
      if (!contains(target)) {
        return false;
      }
    }
    return true;
  }
  public boolean isEmpty() {
    return size() == 0;
  }
  @Override public String toString() {
    StringBuilder sb = new StringBuilder(size() * 16).append('[');
    Collections2.standardJoiner.appendTo(sb, this);
    return sb.append(']').toString();
  }
  public final boolean add(E e) {
    throw new UnsupportedOperationException();
  }
  public final boolean remove(Object object) {
    throw new UnsupportedOperationException();
  }
  public final boolean addAll(Collection<? extends E> newElements) {
    throw new UnsupportedOperationException();
  }
  public final boolean removeAll(Collection<?> oldElements) {
    throw new UnsupportedOperationException();
  }
  public final boolean retainAll(Collection<?> elementsToKeep) {
    throw new UnsupportedOperationException();
  }
  public final void clear() {
    throw new UnsupportedOperationException();
  }
  private transient ImmutableList<E> asList;
  public ImmutableList<E> asList() {
    ImmutableList<E> list = asList;
    return (list == null) ? (asList = createAsList()) : list;
  }
  ImmutableList<E> createAsList() {
    switch (size()) {
      case 0:
        return ImmutableList.of();
      case 1:
        return ImmutableList.of(iterator().next());
      default:
        return new ImmutableAsList<E>(toArray(), this);
    }
  }
  private static class EmptyImmutableCollection
      extends ImmutableCollection<Object> {
    public int size() {
      return 0;
    }
    @Override public boolean isEmpty() {
      return true;
    }
    @Override public boolean contains(@Nullable Object object) {
      return false;
    }
    @Override public UnmodifiableIterator<Object> iterator() {
      return Iterators.EMPTY_ITERATOR;
    }
    private static final Object[] EMPTY_ARRAY = new Object[0];
    @Override public Object[] toArray() {
      return EMPTY_ARRAY;
    }
    @Override public <T> T[] toArray(T[] array) {
      if (array.length > 0) {
        array[0] = null;
      }
      return array;
    }
  }
  private static class ArrayImmutableCollection<E>
      extends ImmutableCollection<E> {
    private final E[] elements;
    ArrayImmutableCollection(E[] elements) {
      this.elements = elements;
    }
    public int size() {
      return elements.length;
    }
    @Override public boolean isEmpty() {
      return false;
    }
    @Override public UnmodifiableIterator<E> iterator() {
      return Iterators.forArray(elements);
    }
  }
  private static class SerializedForm implements Serializable {
    final Object[] elements;
    SerializedForm(Object[] elements) {
      this.elements = elements;
    }
    Object readResolve() {
      return elements.length == 0
          ? EMPTY_IMMUTABLE_COLLECTION
          : new ArrayImmutableCollection<Object>(Platform.clone(elements));
    }
    private static final long serialVersionUID = 0;
  }
  Object writeReplace() {
    return new SerializedForm(toArray());
  }
  abstract static class Builder<E> {
    public abstract Builder<E> add(E element);
    public Builder<E> add(E... elements) {
      checkNotNull(elements); 
      for (E element : elements) {
        add(element);
      }
      return this;
    }
    public Builder<E> addAll(Iterable<? extends E> elements) {
      checkNotNull(elements); 
      for (E element : elements) {
        add(element);
      }
      return this;
    }
    public Builder<E> addAll(Iterator<? extends E> elements) {
      checkNotNull(elements); 
      while (elements.hasNext()) {
        add(elements.next());
      }
      return this;
    }
    public abstract ImmutableCollection<E> build();
  }
}
