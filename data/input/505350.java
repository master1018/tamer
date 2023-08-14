public class DOMTestLoadException
    extends Exception {
  private final Throwable innerException;
  public DOMTestLoadException(Throwable innerException) {
    this.innerException = innerException;
  }
  public String toString() {
    if (innerException != null) {
      return innerException.toString();
    }
    return super.toString();
  }
}
