public class NotificationSender
        extends NotificationBroadcasterSupport
        implements NotificationSenderMBean {
    public void sendNotifs(String type, int count) {
        for (int i = 0; i < count; i++) {
            Notification n = new Notification(type, this, newSeqNo());
            sendNotification(n);
        }
    }
    public int getListenerCount() {
        return listenerCount;
    }
    public void addNotificationListener(NotificationListener l,
                                        NotificationFilter f,
                                        Object h) {
        super.addNotificationListener(l, f, h);
        listenerCount++;
    }
    public void removeNotificationListener(NotificationListener l)
            throws ListenerNotFoundException {
        super.removeNotificationListener(l);
        listenerCount--;
    }
    public void removeNotificationListener(NotificationListener l,
                                           NotificationFilter f,
                                           Object h)
            throws ListenerNotFoundException {
        super.removeNotificationListener(l, f, h);
        listenerCount--;
    }
    private static long newSeqNo() {
        return ++seqNo;
    }
    private static long seqNo = 0;
    private int listenerCount = 0;
}
