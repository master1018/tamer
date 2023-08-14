public final class MutableClassToInstanceMap<B>
    extends ConstrainedMap<Class<? extends B>, B>
    implements ClassToInstanceMap<B> {
  public static <B> MutableClassToInstanceMap<B> create() {
    return new MutableClassToInstanceMap<B>(
        new HashMap<Class<? extends B>, B>());
  }
  public static <B> MutableClassToInstanceMap<B> create(
      Map<Class<? extends B>, B> backingMap) {
    return new MutableClassToInstanceMap<B>(backingMap);
  }
  private MutableClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
    super(delegate, VALUE_CAN_BE_CAST_TO_KEY);
  }
  private static final MapConstraint<Class<?>, Object> VALUE_CAN_BE_CAST_TO_KEY
      = new MapConstraint<Class<?>, Object>() {
    public void checkKeyValue(Class<?> key, Object value) {
      cast(key, value);
    }
  };
  public <T extends B> T putInstance(Class<T> type, T value) {
    return cast(type, put(type, value));
  }
  public <T extends B> T getInstance(Class<T> type) {
    return cast(type, get(type));
  }
  static <B, T extends B> T cast(Class<T> type, B value) {
    return wrap(type).cast(value);
  }
  @SuppressWarnings("unchecked")
  private static <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }
  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
      = new ImmutableMap.Builder<Class<?>, Class<?>>()
          .put(boolean.class, Boolean.class)
          .put(byte.class, Byte.class)
          .put(char.class, Character.class)
          .put(double.class, Double.class)
          .put(float.class, Float.class)
          .put(int.class, Integer.class)
          .put(long.class, Long.class)
          .put(short.class, Short.class)
          .put(void.class, Void.class)
          .build();
  private static final long serialVersionUID = 0;
}
