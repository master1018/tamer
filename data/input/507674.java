final class EmptyImmutableMap extends ImmutableMap<Object, Object> {
  static final EmptyImmutableMap INSTANCE = new EmptyImmutableMap();
  private EmptyImmutableMap() {}
  @Override public Object get(Object key) {
    return null;
  }
  public int size() {
    return 0;
  }
  @Override public boolean isEmpty() {
    return true;
  }
  @Override public boolean containsKey(Object key) {
    return false;
  }
  @Override public boolean containsValue(Object value) {
    return false;
  }
  @Override public ImmutableSet<Entry<Object, Object>> entrySet() {
    return ImmutableSet.of();
  }
  @Override public ImmutableSet<Object> keySet() {
    return ImmutableSet.of();
  }
  @Override public ImmutableCollection<Object> values() {
    return ImmutableCollection.EMPTY_IMMUTABLE_COLLECTION;
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof Map) {
      Map<?, ?> that = (Map<?, ?>) object;
      return that.isEmpty();
    }
    return false;
  }
  @Override public int hashCode() {
    return 0;
  }
  @Override public String toString() {
    return "{}";
  }
  Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
