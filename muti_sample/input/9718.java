public class GetMBeanInfo {
    private final static int EXPECTED_NOTIF_TYPES = 2;
    private static int count = 0;
    public static void main(String argv[]) throws Exception {
        final ObjectName objName;
        objName = new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        MBeanInfo minfo = mbs.getMBeanInfo(objName);
        MBeanNotificationInfo[] notifs = minfo.getNotifications();
        for (int i = 0; i < notifs.length; i++) {
            printNotifType(notifs[i]);
        }
        if (count != EXPECTED_NOTIF_TYPES) {
            throw new RuntimeException("Unexpected number of notification types"
                                       + " count = " + count +
                                       " expected = " + EXPECTED_NOTIF_TYPES);
        }
    }
    private static void printNotifType(MBeanNotificationInfo notif) {
        String[] types = notif.getNotifTypes();
        for (int i = 0; i < types.length; i++) {
            System.out.println(types[i]);
            count++;
        }
    }
}
