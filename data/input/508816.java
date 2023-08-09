public final class Throwables {
  private Throwables() {}
  public static <X extends Throwable> void propagateIfInstanceOf(
      Throwable throwable, Class<X> declaredType) throws X {
    if (declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
  }
  public static void propagateIfPossible(Throwable throwable) {
    propagateIfInstanceOf(throwable, Error.class);
    propagateIfInstanceOf(throwable, RuntimeException.class);
  }
  public static <X extends Throwable> void propagateIfPossible(
      Throwable throwable, Class<X> declaredType) throws X {
    propagateIfInstanceOf(throwable, declaredType);
    propagateIfPossible(throwable);
  }
  public static <X1 extends Throwable, X2 extends Throwable> void
      propagateIfPossible(Throwable throwable, Class<X1> aDeclaredType,
          Class<X2> anotherDeclaredType) throws X1, X2 {
    propagateIfInstanceOf(throwable, aDeclaredType);
    propagateIfPossible(throwable, anotherDeclaredType);
  }
  public static RuntimeException propagate(Throwable throwable) {
    propagateIfPossible(throwable);
    throw new RuntimeException(throwable);
  }
  public static Throwable getRootCause(Throwable throwable) {
    Throwable cause;
    while ((cause = throwable.getCause()) != null) {
      throwable = cause;
    }
    return throwable;
  }
  public static List<Throwable> getCausalChain(Throwable throwable) {
    Preconditions.checkNotNull(throwable);
    List<Throwable> causes = new ArrayList<Throwable>(4);
    while (throwable != null) {
      causes.add(throwable);
      throwable = throwable.getCause();
    }
    return Collections.unmodifiableList(causes);
  }
  public static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
  public static Exception throwCause(Exception exception, boolean combineStackTraces)
      throws Exception {
    Throwable cause = exception.getCause();
    if (cause == null) {
      throw exception;
    }
    if (combineStackTraces) {
      StackTraceElement[] causeTrace = cause.getStackTrace();
      StackTraceElement[] outerTrace = exception.getStackTrace();
      StackTraceElement[] combined = new StackTraceElement[causeTrace.length + outerTrace.length];
      System.arraycopy(causeTrace, 0, combined, 0, causeTrace.length);
      System.arraycopy(outerTrace, 0, combined, causeTrace.length, outerTrace.length);
      cause.setStackTrace(combined);
    }
    if (cause instanceof Exception) {
      throw (Exception) cause;
    }
    if (cause instanceof Error) {
      throw (Error) cause;
    }
    throw exception;
  }
}
