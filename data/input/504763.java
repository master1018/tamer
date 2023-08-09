final class LexicographicalOrdering<T>
    extends Ordering<Iterable<T>> implements Serializable {
  final Ordering<? super T> elementOrder;
  LexicographicalOrdering(Ordering<? super T> elementOrder) {
    this.elementOrder = elementOrder;
  }
  public int compare(Iterable<T> leftIterable, Iterable<T> rightIterable) {
    Iterator<T> left = leftIterable.iterator();
    Iterator<T> right = rightIterable.iterator();
    while (left.hasNext()) {
      if (!right.hasNext()) {
        return LEFT_IS_GREATER; 
      }
      int result = elementOrder.compare(left.next(), right.next());
      if (result != 0) {
        return result;
      }
    }
    if (right.hasNext()) {
      return RIGHT_IS_GREATER; 
    }
    return 0;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof LexicographicalOrdering) {
      LexicographicalOrdering<?> that = (LexicographicalOrdering<?>) object;
      return this.elementOrder.equals(that.elementOrder);
    }
    return false;
  }
  @Override public int hashCode() {
    return elementOrder.hashCode() ^ 2075626741; 
  }
  @Override public String toString() {
    return elementOrder + ".lexicographical()";
  }
  private static final long serialVersionUID = 0;
}
