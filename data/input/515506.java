public abstract class ImmutableSet<E> extends ImmutableCollection<E>
    implements Set<E> {
  @SuppressWarnings({"unchecked"})
  public static <E> ImmutableSet<E> of() {
    return (ImmutableSet) EmptyImmutableSet.INSTANCE;
  }
  public static <E> ImmutableSet<E> of(E element) {
    return new SingletonImmutableSet<E>(element);
  }
  @SuppressWarnings("unchecked")
  public static <E> ImmutableSet<E> of(E e1, E e2) {
    return create(e1, e2);
  }
  @SuppressWarnings("unchecked")
  public static <E> ImmutableSet<E> of(E e1, E e2, E e3) {
    return create(e1, e2, e3);
  }
  @SuppressWarnings("unchecked")
  public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4) {
    return create(e1, e2, e3, e4);
  }
  @SuppressWarnings("unchecked")
  public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5) {
    return create(e1, e2, e3, e4, e5);
  }
  public static <E> ImmutableSet<E> of(E... elements) {
    checkNotNull(elements); 
    switch (elements.length) {
      case 0:
        return of();
      case 1:
        return of(elements[0]);
      default:
        return create(elements);
    }
  }
  public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
    if (elements instanceof ImmutableSet
        && !(elements instanceof ImmutableSortedSet)) {
      @SuppressWarnings("unchecked") 
      ImmutableSet<E> set = (ImmutableSet<E>) elements;
      return set;
    }
    return copyOfInternal(Collections2.toCollection(elements));
  }
  public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
    Collection<E> list = Lists.newArrayList(elements);
    return copyOfInternal(list);
  }
  private static <E> ImmutableSet<E> copyOfInternal(
      Collection<? extends E> collection) {
    switch (collection.size()) {
      case 0:
        return of();
      case 1:
        return ImmutableSet.<E>of(collection.iterator().next());
      default:
        return create(collection, collection.size());
    }
  }
  ImmutableSet() {}
  boolean isHashCodeFast() {
    return false;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof ImmutableSet
        && isHashCodeFast()
        && ((ImmutableSet<?>) object).isHashCodeFast()
        && hashCode() != object.hashCode()) {
      return false;
    }
    return Collections2.setEquals(this, object);
  }
  @Override public int hashCode() {
    int hashCode = 0;
    for (Object o : this) {
      hashCode += o.hashCode();
    }
    return hashCode;
  }
  @Override public abstract UnmodifiableIterator<E> iterator();
  private static <E> ImmutableSet<E> create(E... elements) {
    return create(Arrays.asList(elements), elements.length);
  }
  private static <E> ImmutableSet<E> create(
      Iterable<? extends E> iterable, int count) {
    int tableSize = Hashing.chooseTableSize(count);
    Object[] table = new Object[tableSize];
    int mask = tableSize - 1;
    List<E> elements = new ArrayList<E>(count);
    int hashCode = 0;
    for (E element : iterable) {
      checkNotNull(element); 
      int hash = element.hashCode();
      for (int i = Hashing.smear(hash); true; i++) {
        int index = i & mask;
        Object value = table[index];
        if (value == null) {
          table[index] = element;
          elements.add(element);
          hashCode += hash;
          break;
        } else if (value.equals(element)) {
          break; 
        }
      }
    }
    if (elements.size() == 1) {
      return new SingletonImmutableSet<E>(elements.get(0), hashCode);
    } else if (tableSize > Hashing.chooseTableSize(elements.size())) {
      return create(elements, elements.size());
    } else {
      return new RegularImmutableSet<E>(
          elements.toArray(), hashCode, table, mask);
    }
  }
  abstract static class ArrayImmutableSet<E> extends ImmutableSet<E> {
    final transient Object[] elements;
    ArrayImmutableSet(Object[] elements) {
      this.elements = elements;
    }
    public int size() {
      return elements.length;
    }
    @Override public boolean isEmpty() {
      return false;
    }
    @SuppressWarnings("unchecked")
    @Override public UnmodifiableIterator<E> iterator() {
      return (UnmodifiableIterator<E>) Iterators.forArray(elements);
    }
    @Override public Object[] toArray() {
      Object[] array = new Object[size()];
      Platform.unsafeArrayCopy(elements, 0, array, 0, size());
      return array;
    }
    @Override public <T> T[] toArray(T[] array) {
      int size = size();
      if (array.length < size) {
        array = ObjectArrays.newArray(array, size);
      } else if (array.length > size) {
        array[size] = null;
      }
      Platform.unsafeArrayCopy(elements, 0, array, 0, size);
      return array;
    }
    @Override public boolean containsAll(Collection<?> targets) {
      if (targets == this) {
        return true;
      }
      if (!(targets instanceof ArrayImmutableSet)) {
        return super.containsAll(targets);
      }
      if (targets.size() > size()) {
        return false;
      }
      for (Object target : ((ArrayImmutableSet<?>) targets).elements) {
        if (!contains(target)) {
          return false;
        }
      }
      return true;
    }
    @Override ImmutableList<E> createAsList() {
      return new ImmutableAsList<E>(elements, this);
    }
  }
  abstract static class TransformedImmutableSet<D, E> extends ImmutableSet<E> {
    final D[] source;
    final int hashCode;
    TransformedImmutableSet(D[] source, int hashCode) {
      this.source = source;
      this.hashCode = hashCode;
    }
    abstract E transform(D element);
    public int size() {
      return source.length;
    }
    @Override public boolean isEmpty() {
      return false;
    }
    @Override public UnmodifiableIterator<E> iterator() {
      return new AbstractIterator<E>() {
        int index = 0;
        @Override protected E computeNext() {
          return index < source.length
              ? transform(source[index++])
              : endOfData();
        }
      };
    }
    @Override public Object[] toArray() {
      return toArray(new Object[size()]);
    }
    @Override public <T> T[] toArray(T[] array) {
      int size = size();
      if (array.length < size) {
        array = ObjectArrays.newArray(array, size);
      } else if (array.length > size) {
        array[size] = null;
      }
      Object[] objectArray = array;
      for (int i = 0; i < source.length; i++) {
        objectArray[i] = transform(source[i]);
      }
      return array;
    }
    @Override public final int hashCode() {
      return hashCode;
    }
    @Override boolean isHashCodeFast() {
      return true;
    }
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
  @Override Object writeReplace() {
    return new SerializedForm(toArray());
  }
  public static <E> Builder<E> builder() {
    return new Builder<E>();
  }
  public static class Builder<E> extends ImmutableCollection.Builder<E> {
    final ArrayList<E> contents = Lists.newArrayList();
    public Builder() {}
    @Override public Builder<E> add(E element) {
      contents.add(checkNotNull(element));
      return this;
    }
    @Override public Builder<E> add(E... elements) {
      checkNotNull(elements); 
      contents.ensureCapacity(contents.size() + elements.length);
      super.add(elements);
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
    @Override public Builder<E> addAll(Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }
    @Override public ImmutableSet<E> build() {
      return copyOf(contents);
    }
  }
}
