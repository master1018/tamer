public abstract class ImmutableList<E> extends ImmutableCollection<E>
    implements List<E>, RandomAccess {
  @SuppressWarnings("unchecked")
  public static <E> ImmutableList<E> of() {
    return (ImmutableList) EmptyImmutableList.INSTANCE;
  }
  public static <E> ImmutableList<E> of(E element) {
    return new SingletonImmutableList<E>(element);
  }
  public static <E> ImmutableList<E> of(E e1, E e2) {
    return new RegularImmutableList<E>(copyIntoArray(e1, e2));
  }
  public static <E> ImmutableList<E> of(E e1, E e2, E e3) {
    return new RegularImmutableList<E>(copyIntoArray(e1, e2, e3));
  }
  public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4) {
    return new RegularImmutableList<E>(copyIntoArray(e1, e2, e3, e4));
  }
  public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5) {
    return new RegularImmutableList<E>(copyIntoArray(e1, e2, e3, e4, e5));
  }
  public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
    return new RegularImmutableList<E>(copyIntoArray(e1, e2, e3, e4, e5, e6));
  }
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
    return new RegularImmutableList<E>(
        copyIntoArray(e1, e2, e3, e4, e5, e6, e7));
  }
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
    return new RegularImmutableList<E>(
        copyIntoArray(e1, e2, e3, e4, e5, e6, e7, e8));
  }
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
    return new RegularImmutableList<E>(
        copyIntoArray(e1, e2, e3, e4, e5, e6, e7, e8, e9));
  }
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
    return new RegularImmutableList<E>(
        copyIntoArray(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
  }
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10, E e11) {
    return new RegularImmutableList<E>(
        copyIntoArray(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11));
  }
  public static <E> ImmutableList<E> of(E... elements) {
    checkNotNull(elements); 
    switch (elements.length) {
      case 0:
        return ImmutableList.of();
      case 1:
        return new SingletonImmutableList<E>(elements[0]);
      default:
        return new RegularImmutableList<E>(copyIntoArray(elements));
    }
  }
  @SuppressWarnings("unchecked") 
  public static <E> ImmutableList<E> copyOf(Iterable<? extends E> elements) {
    checkNotNull(elements);
    return (elements instanceof Collection)
      ? copyOf((Collection<? extends E>) elements)
      : copyOf(elements.iterator());
  }
  public static <E> ImmutableList<E> copyOf(Collection<? extends E> elements) {
    checkNotNull(elements);
    if (elements instanceof ImmutableList) {
      @SuppressWarnings("unchecked") 
      ImmutableList<E> list = (ImmutableList<E>) elements;
      return list;
    }
    return copyFromCollection(elements);
  }
  public static <E> ImmutableList<E> copyOf(Iterator<? extends E> elements) {
    return copyFromCollection(Lists.newArrayList(elements));
  }
  private static <E> ImmutableList<E> copyFromCollection(
      Collection<? extends E> collection) {
    Object[] elements = collection.toArray();
    switch (elements.length) {
      case 0:
        return of();
      case 1:
        @SuppressWarnings("unchecked") 
        ImmutableList<E> list = new SingletonImmutableList<E>((E) elements[0]);
        return list;
      default:
        return new RegularImmutableList<E>(copyIntoArray(elements));
    }
  }
  ImmutableList() {}
  @Override public abstract UnmodifiableIterator<E> iterator();
  public abstract int indexOf(@Nullable Object object);
  public abstract int lastIndexOf(@Nullable Object object);
  public abstract ImmutableList<E> subList(int fromIndex, int toIndex);
  public final boolean addAll(int index, Collection<? extends E> newElements) {
    throw new UnsupportedOperationException();
  }
  public final E set(int index, E element) {
    throw new UnsupportedOperationException();
  }
  public final void add(int index, E element) {
    throw new UnsupportedOperationException();
  }
  public final E remove(int index) {
    throw new UnsupportedOperationException();
  }
  private static Object[] copyIntoArray(Object... source) {
    Object[] array = new Object[source.length];
    int index = 0;
    for (Object element : source) {
      if (element == null) {
        throw new NullPointerException("at index " + index);
      }
      array[index++] = element;
    }
    return array;
  }
  @Override public ImmutableList<E> asList() {
    return this;
  }
  private static class SerializedForm implements Serializable {
    final Object[] elements;
    SerializedForm(Object[] elements) {
      this.elements = elements;
    }
    Object readResolve() {
      return of(elements);
    }
    private static final long serialVersionUID = 0;
  }
  private void readObject(ObjectInputStream stream)
      throws InvalidObjectException {
    throw new InvalidObjectException("Use SerializedForm");
  }
  @Override Object writeReplace() {
    return new SerializedForm(toArray());
  }
  public static <E> Builder<E> builder() {
    return new Builder<E>();
  }
  public static final class Builder<E> extends ImmutableCollection.Builder<E> {
    private final ArrayList<E> contents = Lists.newArrayList();
    public Builder() {}
    @Override public Builder<E> add(E element) {
      contents.add(checkNotNull(element));
      return this;
    }
    @Override public Builder<E> addAll(Iterable<? extends E> elements) {
      if (elements instanceof Collection) {
        Collection<?> collection = (Collection<?>) elements;
        contents.ensureCapacity(contents.size() + collection.size());
      }
      super.addAll(elements);
      return this;
    }
    @Override public Builder<E> add(E... elements) {
      checkNotNull(elements); 
      contents.ensureCapacity(contents.size() + elements.length);
      super.add(elements);
      return this;
    }
    @Override public Builder<E> addAll(Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }
    @Override public ImmutableList<E> build() {
      return copyOf(contents);
    }
  }
}
