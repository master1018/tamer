final class UsingToStringOrdering
    extends Ordering<Object> implements Serializable {
  static final UsingToStringOrdering INSTANCE = new UsingToStringOrdering();
  public int compare(Object left, Object right) {
    return left.toString().compareTo(right.toString());
  }
  private Object readResolve() {
    return INSTANCE;
  }
  @Override public String toString() {
    return "Ordering.usingToString()";
  }
  private UsingToStringOrdering() {}
  private static final long serialVersionUID = 0;
}
