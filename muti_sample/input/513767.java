final class NullsFirstOrdering<T> extends Ordering<T> implements Serializable {
  final Ordering<? super T> ordering;
  NullsFirstOrdering(Ordering<? super T> ordering) {
    this.ordering = ordering;
  }
  public int compare(T left, T right) {
    if (left == right) {
      return 0;
    }
    if (left == null) {
      return RIGHT_IS_GREATER;
    }
    if (right == null) {
      return LEFT_IS_GREATER;
    }
    return ordering.compare(left, right);
  }
  @Override public <S extends T> Ordering<S> reverse() {
    return ordering.reverse().nullsLast();
  }
  @SuppressWarnings("unchecked") 
  @Override public <S extends T> Ordering<S> nullsFirst() {
    return (Ordering) this;
  }
  @Override public <S extends T> Ordering<S> nullsLast() {
    return ordering.nullsLast();
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof NullsFirstOrdering) {
      NullsFirstOrdering<?> that = (NullsFirstOrdering<?>) object;
      return this.ordering.equals(that.ordering);
    }
    return false;
  }
  @Override public int hashCode() {
    return ordering.hashCode() ^ 957692532; 
  }
  @Override public String toString() {
    return ordering + ".nullsFirst()";
  }
  private static final long serialVersionUID = 0;
}
