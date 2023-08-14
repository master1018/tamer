final class ImmutableAsList<E> extends RegularImmutableList<E> {
  private final transient ImmutableCollection<E> collection;
  ImmutableAsList(Object[] array, ImmutableCollection<E> collection) {
    super(array, 0, array.length);
    this.collection = collection;
  }
  @Override public boolean contains(Object target) {
    return collection.contains(target);
  }
  static class SerializedForm implements Serializable {
    final ImmutableCollection<?> collection;
    SerializedForm(ImmutableCollection<?> collection) {
      this.collection = collection;
    }
    Object readResolve() {
      return collection.asList();
    }
    private static final long serialVersionUID = 0;
  }
  private void readObject(ObjectInputStream stream)
      throws InvalidObjectException {
    throw new InvalidObjectException("Use SerializedForm");
  }
  @Override Object writeReplace() {
    return new SerializedForm(collection);
  }
}
