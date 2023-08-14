final class ReverseNaturalOrdering
    extends Ordering<Comparable> implements Serializable {
  static final ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();
  public int compare(Comparable left, Comparable right) {
    checkNotNull(left); 
    if (left == right) {
      return 0;
    }
    @SuppressWarnings("unchecked") 
    int result = right.compareTo(left);
    return result;
  }
  @Override public <S extends Comparable> Ordering<S> reverse() {
    return Ordering.natural();
  }
  @Override public <E extends Comparable> E min(E a, E b) {
    return NaturalOrdering.INSTANCE.max(a, b);
  }
  @Override public <E extends Comparable> E min(E a, E b, E c, E... rest) {
    return NaturalOrdering.INSTANCE.max(a, b, c, rest);
  }
  @Override public <E extends Comparable> E min(Iterable<E> iterable) {
    return NaturalOrdering.INSTANCE.max(iterable);
  }
  @Override public <E extends Comparable> E max(E a, E b) {
    return NaturalOrdering.INSTANCE.min(a, b);
  }
  @Override public <E extends Comparable> E max(E a, E b, E c, E... rest) {
    return NaturalOrdering.INSTANCE.min(a, b, c, rest);
  }
  @Override public <E extends Comparable> E max(Iterable<E> iterable) {
    return NaturalOrdering.INSTANCE.min(iterable);
  }
  private Object readResolve() {
    return INSTANCE;
  }
  @Override public String toString() {
    return "Ordering.natural().reverse()";
  }
  private ReverseNaturalOrdering() {}
  private static final long serialVersionUID = 0;
}
