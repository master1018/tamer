final class Platform {
  private Platform() {}
  static boolean isInstance(Class<?> clazz, Object obj) {
    return clazz.isInstance(obj);
  }
  static char[] charBufferFromThreadLocal() {
    return DEST_TL.get();
  }
  private static final ThreadLocal<char[]> DEST_TL = new ThreadLocal<char[]>() {
    @Override
    protected char[] initialValue() {
      return new char[1024];
    }
  };
  static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
    return matcher.precomputedInternal();
  }
}
