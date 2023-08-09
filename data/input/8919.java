public class ChangingNotifsTest {
    public static interface EmptyMBean {}
    public static interface EmptyMXBean {}
    public static class Base extends NotificationBroadcasterSupport {
        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            MBeanNotificationInfo inf =
                new MBeanNotificationInfo(new String[0],
                                          Integer.toString(++called),
                                          "description");
            return new MBeanNotificationInfo[] {inf};
        }
        private static int called;
    }
    public static class Empty extends Base implements EmptyMBean {}
    public static class EmptyMX extends Base implements EmptyMXBean {}
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        for (boolean mx : new boolean[] {false, true})
            test(mbs, name, mx);
        if (failure != null)
            throw new Exception(failure);
        System.out.println("TEST PASSED");
    }
    private static void test(MBeanServer mbs, ObjectName name, boolean mx)
            throws Exception {
        Object mbean = mx ? new EmptyMX() : new Empty();
        String what = mx ? "MXBean" : "Standard MBean";
        mbs.registerMBean(mbean, name);
        try {
            MBeanInfo mbi = mbs.getMBeanInfo(name);
            Descriptor d = mbi.getDescriptor();
            String immut = (String) d.getFieldValue("immutableInfo");
            boolean immutable = (immut != null && immut.equals("true"));
            if (immutable != mx) {
                fail(what + " has immutableInfo=" + immut + ", should be " +
                     mx);
                return;
            }
            MBeanNotificationInfo[] n1 = mbi.getNotifications().clone();
            mbi = mbs.getMBeanInfo(name);
            boolean unchanged = Arrays.deepEquals(mbi.getNotifications(), n1);
            if (unchanged != mx) {
                fail(what + " notif info " +
                     (unchanged ? "did not change" : "changed"));
                return;
            }
            System.out.println("OK: " + what);
        } finally {
            mbs.unregisterMBean(name);
        }
    }
    private static void fail(String why) {
        failure = "FAILED: " + why;
        System.out.println(failure);
    }
    private static String failure;
}
