final class NullsLastOrdering<T> extends Ordering<T> implements Serializable {
  final Ordering<? super T> ordering;
  NullsLastOrdering(Ordering<? super T> ordering) {
    this.ordering = ordering;
  }
  public int compare(T left, T right) {
    if (left == right) {
      return 0;
    }
    if (left == null) {
      return LEFT_IS_GREATER;
    }
    if (right == null) {
      return RIGHT_IS_GREATER;
    }
    return ordering.compare(left, right);
  }
  @Override public <S extends T> Ordering<S> reverse() {
    return ordering.reverse().nullsFirst();
  }
  @Override public <S extends T> Ordering<S> nullsFirst() {
    return ordering.nullsFirst();
  }
  @SuppressWarnings("unchecked") 
  @Override public <S extends T> Ordering<S> nullsLast() {
    return (Ordering) this;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof NullsLastOrdering) {
      NullsLastOrdering<?> that = (NullsLastOrdering<?>) object;
      return this.ordering.equals(that.ordering);
    }
    return false;
  }
  @Override public int hashCode() {
    return ordering.hashCode() ^ -921210296; 
  }
  @Override public String toString() {
    return ordering + ".nullsLast()";
  }
  private static final long serialVersionUID = 0;
}
