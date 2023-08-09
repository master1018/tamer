public class FilterExceptionTest {
    public static Exception listenerException;
    public static void main(String[] args) throws Exception {
        System.out.println(
         ">>> FilterExceptionTest-main: test on an exception thrown by NotificationFilter.");
        FilterExceptionTest.listenerException = null;
        NotificationFilter filter = new NotificationFilter() {
                public boolean isNotificationEnabled(Notification notification) {
                    System.out.println(">>> FilterExceptionTest-filter: throws exception.");
                    throw new RuntimeException("For test");
                }
            };
        NotificationListener listener = new NotificationListener() {
                public void handleNotification(Notification n, Object hb) {
                    FilterExceptionTest.listenerException =
                        new Exception("The listener received unexpected notif.");
                }
            };
        NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();
        broadcaster.addNotificationListener(listener, filter, null);
        broadcaster.sendNotification(new Notification("", "", 1L));
        if (FilterExceptionTest.listenerException != null) {
            throw FilterExceptionTest.listenerException;
        }
        System.out.println(">>> FilterExceptionTest-main: Done.");
    }
}
