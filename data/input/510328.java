public final class Flushables {
  private static final Logger logger
      = Logger.getLogger(Flushables.class.getName());
  private Flushables() {}
  public static void flush(Flushable flushable, boolean swallowIOException)
      throws IOException {
    try {
      flushable.flush();
    } catch (IOException e) {
      logger.log(Level.WARNING,
          "IOException thrown while flushing Flushable.", e);
      if (!swallowIOException) {
        throw e;
      }
    }
  }
  public static void flushQuietly(Flushable flushable) {
    try {
      flush(flushable, true);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "IOException should not have been thrown.", e);
    }
  }
}
