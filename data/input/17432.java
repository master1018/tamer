public class ELFException extends RuntimeException {
  public ELFException() {
    super();
  }
  public ELFException(String message) {
    super(message);
  }
  public ELFException(Throwable cause) {
    super(cause);
  }
  public ELFException(String message, Throwable cause) {
    super(message, cause);
  }
}
