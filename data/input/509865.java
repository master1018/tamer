public final class Defaults {
  private Defaults() {}
  private static final Map<Class<?>, Object> DEFAULTS =
      new HashMap<Class<?>, Object>(16);
  private static <T> void put(Class<T> type, T value) {
    DEFAULTS.put(type, value);
  }
  static {
    put(boolean.class, false);
    put(char.class, '\0');
    put(byte.class, (byte) 0);
    put(short.class, (short) 0);
    put(int.class, 0);
    put(long.class, 0L);
    put(float.class, 0f);
    put(double.class, 0d);
  }
  @SuppressWarnings("unchecked")
  public static <T> T defaultValue(Class<T> type) {
    return (T) DEFAULTS.get(type);
  }
}
