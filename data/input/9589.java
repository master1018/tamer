public class Assert {
  public static boolean ASSERTS_ENABLED = true;
  public static void that(boolean test, String message) {
    if (ASSERTS_ENABLED && !test) {
      throw new AssertionFailure(message);
    }
  }
}
