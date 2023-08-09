public class UserDataNotification {
  private final short operation;
  private final String key;
  private final Object data;
  private final Node src;
  private final Node dst;
  public UserDataNotification(short operation,
                              String key,
                              Object data,
                              Node src,
                              Node dst) {
    this.operation = operation;
    this.key = key;
    this.data = data;
    this.src = src;
    this.dst = dst;
  }
  public final short getOperation() {
    return operation;
  }
  public final String getKey() {
    return key;
  }
  public final Object getData() {
    return data;
  }
  public final Node getSrc() {
    return src;
  }
  public final Node getDst() {
    return dst;
  }
}
