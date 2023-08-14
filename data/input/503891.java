public class UncheckedTimeoutException extends RuntimeException {
  public UncheckedTimeoutException() {}
  public UncheckedTimeoutException(String message) {
    super(message);
  }
  public UncheckedTimeoutException(Throwable cause) {
    super(cause);
  }
  public UncheckedTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
  private static final long serialVersionUID = 0;
}
