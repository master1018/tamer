@TestTargetClass(NotificationManager.class)
public class NotificationManagerTest extends AndroidTestCase {
    private NotificationManager mNotificationManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mNotificationManager = (NotificationManager) mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mNotificationManager.cancelAll();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "notify",
            args = {int.class, android.app.Notification.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "cancel",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1716929", explanation = "NotificationManager#notify(int, Notification) "
            + "find a way to get ticker from status bar")
    public void testNotify() {
        final int id = 1;
        sendNotification(id, R.drawable.black);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "notify",
            args = {int.class, android.app.Notification.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "cancel",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1716929", explanation = "NotificationManager#cancel(int) find a way "
            + "to get ticker from status bar")
    public void testCancel() {
        final int id = 9;
        sendNotification(id, R.drawable.black);
        mNotificationManager.cancel(id);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "notify",
            args = {int.class, android.app.Notification.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "cancelAll",
            args = {}
        )
    })
    @ToBeFixed(bug = "1716929", explanation = "NotificationManager#cancelAll() find a way to "
            + "get ticker from status bar")
    public void testCancelAll() {
        sendNotification(1, R.drawable.black);
        sendNotification(2, R.drawable.blue);
        sendNotification(3, R.drawable.yellow);
        mNotificationManager.cancelAll();
    }
    private void sendNotification(final int id, final int icon) {
        final Notification notification = new Notification(
                icon, "No intent", System.currentTimeMillis());
        final Intent intent = new Intent(Intent.ACTION_MAIN, Threads.CONTENT_URI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        notification.setLatestEventInfo(mContext, "notify#" + id, "This is #" + id
                + "notification  ", pendingIntent);
        mNotificationManager.notify(id, notification);
    }
}
