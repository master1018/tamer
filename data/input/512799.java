final class ExplicitOrdering<T> extends Ordering<T> implements Serializable {
  final ImmutableMap<T, Integer> rankMap;
  ExplicitOrdering(List<T> valuesInOrder) {
    this(buildRankMap(valuesInOrder));
  }
  ExplicitOrdering(ImmutableMap<T, Integer> rankMap) {
    this.rankMap = rankMap;
  }
  public int compare(T left, T right) {
    return rank(left) - rank(right); 
  }
  private int rank(T value) {
    Integer rank = rankMap.get(value);
    if (rank == null) {
      throw new IncomparableValueException(value);
    }
    return rank;
  }
  private static <T> ImmutableMap<T, Integer> buildRankMap(
      List<T> valuesInOrder) {
    ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
    int rank = 0;
    for (T value : valuesInOrder) {
      builder.put(value, rank++);
    }
    return builder.build();
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof ExplicitOrdering) {
      ExplicitOrdering<?> that = (ExplicitOrdering<?>) object;
      return this.rankMap.equals(that.rankMap);
    }
    return false;
  }
  @Override public int hashCode() {
    return rankMap.hashCode();
  }
  @Override public String toString() {
    return "Ordering.explicit(" + rankMap.keySet() + ")";
  }
  private static final long serialVersionUID = 0;
}
