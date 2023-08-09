public class NotifInfoTest {
    public static void main(String[] args) throws Exception {
        final MBeanNotificationInfo info1 =
            new MBeanNotificationInfo(new String[] {"t11", "t12"}, "n1", null);
        final MBeanNotificationInfo info2 =
            new MBeanNotificationInfo(new String[] {"t21", "t22"}, "n2", null);
        final MBeanNotificationInfo[] mninfo1 =
            new MBeanNotificationInfo[] {info1, info2};
        final MBeanNotificationInfo[] mninfo2 = new MBeanNotificationInfo[] {info1, info2};
        final NotificationBroadcasterSupport support1 = new NotificationBroadcasterSupport(mninfo1);
        final NotificationBroadcasterSupport support2 = new NotificationBroadcasterSupport(null, mninfo1);
        if (!Arrays.deepEquals(mninfo2, support1.getNotificationInfo()) ||
            !Arrays.deepEquals(mninfo2, support2.getNotificationInfo())) {
            throw new RuntimeException("Not got expected MBeanNotificationInfo!");
        }
        MBeanNotificationInfo[] mninfo3 = support1.getNotificationInfo();
        mninfo3[0] = null;
        mninfo3[1] = null;
        if (!Arrays.deepEquals(mninfo2, support1.getNotificationInfo()))
            throw new RuntimeException("Caller changed info array!");
    }
}
