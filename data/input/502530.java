public final class LinkedHashMultiset<E> extends AbstractMapBasedMultiset<E> {
  public static <E> LinkedHashMultiset<E> create() {
    return new LinkedHashMultiset<E>();
  }
  public static <E> LinkedHashMultiset<E> create(int distinctElements) {
    return new LinkedHashMultiset<E>(distinctElements);
  }
  public static <E> LinkedHashMultiset<E> create(
      Iterable<? extends E> elements) {
    LinkedHashMultiset<E> multiset =
        create(Multisets.inferDistinctElements(elements));
    Iterables.addAll(multiset, elements);
    return multiset;
  }
  private LinkedHashMultiset() {
    super(new LinkedHashMap<E, AtomicInteger>());
  }
  private LinkedHashMultiset(int distinctElements) {
    super(new LinkedHashMap<E, AtomicInteger>(Maps.capacity(distinctElements)));
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultiset(this, stream);
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int distinctElements = Serialization.readCount(stream);
    setBackingMap(new LinkedHashMap<E, AtomicInteger>(
        Maps.capacity(distinctElements)));
    Serialization.populateMultiset(this, stream, distinctElements);
  }
  private static final long serialVersionUID = 0;
}
