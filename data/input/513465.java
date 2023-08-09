final class NaturalOrdering
    extends Ordering<Comparable> implements Serializable {
  static final NaturalOrdering INSTANCE = new NaturalOrdering();
  public int compare(Comparable left, Comparable right) {
    checkNotNull(right); 
    if (left == right) {
      return 0;
    }
    @SuppressWarnings("unchecked") 
    int result = left.compareTo(right);
    return result;
  }
  @SuppressWarnings("unchecked") 
  @Override public <S extends Comparable> Ordering<S> reverse() {
    return (Ordering) ReverseNaturalOrdering.INSTANCE;
  }
  @SuppressWarnings("unchecked") 
  @Override public int binarySearch(
      List<? extends Comparable> sortedList, Comparable key) {
    return Collections.binarySearch((List) sortedList, key);
  }
  @Override public <E extends Comparable> List<E> sortedCopy(
      Iterable<E> iterable) {
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list);
    return list;
  }
  private Object readResolve() {
    return INSTANCE;
  }
  @Override public String toString() {
    return "Ordering.natural()";
  }
  private NaturalOrdering() {}
  private static final long serialVersionUID = 0;
}
