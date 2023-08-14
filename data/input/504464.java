public class UserDataMonitor
    implements UserDataHandler {
  private final List notifications = new ArrayList();
  public UserDataMonitor() {
  }
  public void handle(
      short operation,
      String key,
      Object data,
      Node src,
      Node dst) {
    notifications.add(
        new UserDataNotification(operation, key, data, src, dst));
  }
  public final List getAllNotifications() {
    return new ArrayList(notifications);
  }
}
