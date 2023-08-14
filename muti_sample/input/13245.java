public class InputCode {
  private final String name;
  private final int code;
  private final String logLevel;
  private final String message;
  public InputCode(final String name, final int code,
                   final String logLevel, final String message) {
    this.name = name;
    this.code = code;
    this.logLevel = logLevel;
    this.message = message;
  }
  public String getName() {
    return name;
  }
  public int getCode() {
    return code;
  }
  public String getLogLevel() {
    return logLevel;
  }
  public String getMessage() {
    return message;
  }
  public String toString() {
    return getClass().getName() +
      "[name=" + name +
      ",code=" + code +
      ",logLevel=" + logLevel +
      ",message=" + message +
      "]";
  }
}
