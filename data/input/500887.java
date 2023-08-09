public final class EnumMultiset<E extends Enum<E>>
    extends AbstractMapBasedMultiset<E> {
  public static <E extends Enum<E>> EnumMultiset<E> create(Class<E> type) {
    return new EnumMultiset<E>(type);
  }
  public static <E extends Enum<E>> EnumMultiset<E> create(
      Iterable<E> elements) {
    Iterator<E> iterator = elements.iterator();
    checkArgument(iterator.hasNext(),
        "EnumMultiset constructor passed empty Iterable");
    EnumMultiset<E> multiset
        = new EnumMultiset<E>(iterator.next().getDeclaringClass());
    Iterables.addAll(multiset, elements);
    return multiset;
  }
  private transient Class<E> type;
  private EnumMultiset(Class<E> type) {
    super(new EnumMap<E, AtomicInteger>(type));
    this.type = type;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(type);
    Serialization.writeMultiset(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    @SuppressWarnings("unchecked") 
    Class<E> localType = (Class<E>) stream.readObject();
    type = localType;
    setBackingMap(new EnumMap<E, AtomicInteger>(type));
    Serialization.populateMultiset(this, stream);
  }
  private static final long serialVersionUID = 0;
}
