public class DebuggerException extends RuntimeException {
  public DebuggerException() {
    super();
  }
  public DebuggerException(String message) {
    super(message);
  }
  public DebuggerException(String message, Throwable cause) {
    super(message, cause);
  }
  public DebuggerException(Throwable cause) {
    super(cause);
  }
}
