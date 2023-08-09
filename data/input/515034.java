public final class HashMultiset<E> extends AbstractMapBasedMultiset<E> {
  public static <E> HashMultiset<E> create() {
    return new HashMultiset<E>();
  }
  public static <E> HashMultiset<E> create(int distinctElements) {
    return new HashMultiset<E>(distinctElements);
  }
  public static <E> HashMultiset<E> create(Iterable<? extends E> elements) {
    HashMultiset<E> multiset =
        create(Multisets.inferDistinctElements(elements));
    Iterables.addAll(multiset, elements);
    return multiset;
  }
  private HashMultiset() {
    super(new HashMap<E, AtomicInteger>());
  }
  private HashMultiset(int distinctElements) {
    super(new HashMap<E, AtomicInteger>(Maps.capacity(distinctElements)));
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultiset(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int distinctElements = Serialization.readCount(stream);
    setBackingMap(
        Maps.<E, AtomicInteger>newHashMapWithExpectedSize(distinctElements));
    Serialization.populateMultiset(this, stream, distinctElements);
  }
  private static final long serialVersionUID = 0;
}
