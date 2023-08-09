public final class ImmutableClassToInstanceMap<B> extends
    ForwardingMap<Class<? extends B>, B> implements ClassToInstanceMap<B> {
  public static <B> Builder<B> builder() {
    return new Builder<B>();
  }
  public static final class Builder<B> {
    private final ImmutableMap.Builder<Class<? extends B>, B> mapBuilder
        = ImmutableMap.builder();
    public <T extends B> Builder<B> put(Class<T> type, T value) {
      mapBuilder.put(type, value);
      return this;
    }
    public <T extends B> Builder<B> putAll(
        Map<? extends Class<? extends T>, ? extends T> map) {
      for (Entry<? extends Class<? extends T>, ? extends T> entry
          : map.entrySet()) {
        Class<? extends T> type = entry.getKey();
        T value = entry.getValue();
        mapBuilder.put(type, cast(type, value));
      }
      return this;
    }
    public ImmutableClassToInstanceMap<B> build() {
      return new ImmutableClassToInstanceMap<B>(mapBuilder.build());
    }
  }
  @SuppressWarnings("unchecked") 
  public static <B, S extends B> ImmutableClassToInstanceMap<B> copyOf(
      Map<? extends Class<? extends S>, ? extends S> map) {
    if (map instanceof ImmutableClassToInstanceMap) {
      return (ImmutableClassToInstanceMap<B>) (Map) map;
    }
    return new Builder<B>().putAll(map).build();
  }
  private final ImmutableMap<Class<? extends B>, B> delegate;
  private ImmutableClassToInstanceMap(
      ImmutableMap<Class<? extends B>, B> delegate) {
    this.delegate = delegate;
  }
  @Override protected Map<Class<? extends B>, B> delegate() {
    return delegate;
  }
  @SuppressWarnings("unchecked") 
  public <T extends B> T getInstance(Class<T> type) {
    return (T) delegate.get(type);
  }
  public <T extends B> T putInstance(Class<T> type, T value) {
    throw new UnsupportedOperationException();
  }
}
