final class ImmutableEnumSet<E > extends ImmutableSet<E> {
  private final transient Set<E> delegate;
  ImmutableEnumSet(Set<E> delegate) {
    this.delegate = delegate;
  }
  @Override public UnmodifiableIterator<E> iterator() {
    return Iterators.unmodifiableIterator(delegate.iterator());
  }
  public int size() {
    return delegate.size();
  }
  @Override public boolean contains(Object object) {
    return delegate.contains(object);
  }
  @Override public boolean containsAll(Collection<?> collection) {
    return delegate.containsAll(collection);
  }
  @Override public boolean isEmpty() {
    return delegate.isEmpty();
  }
  @Override public Object[] toArray() {
    return delegate.toArray();
  }
  @Override public <T> T[] toArray(T[] array) {
    return delegate.toArray(array);
  }
  @Override public boolean equals(Object object) {
    return object == this || delegate.equals(object);
  }
  private transient int hashCode;
  @Override public int hashCode() {
    int result = hashCode;
    return (result == 0) ? hashCode = delegate.hashCode() : result;
  }
  @Override public String toString() {
    return delegate.toString();
  }
  @SuppressWarnings("unchecked")
  @Override Object writeReplace() {
    return new EnumSerializedForm((EnumSet) delegate);
  }
  private static class EnumSerializedForm<E extends Enum<E>>
      implements Serializable {
    final EnumSet<E> delegate;
    EnumSerializedForm(EnumSet<E> delegate) {
      this.delegate = delegate;
    }
    Object readResolve() {
      return new ImmutableEnumSet<E>(delegate.clone());
    }
    private static final long serialVersionUID = 0;
  }
}
