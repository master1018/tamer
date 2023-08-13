public class InstanceOfExpTest {
    public static class Simple implements SimpleMBean {}
    public static interface SimpleMBean {}
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Test the method javax.management.Query.isInstanceOf");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final String className = "javax.management.NotificationBroadcaster";
        final ObjectName name1 = new ObjectName("test:simple=1");
        mbs.createMBean(Simple.class.getName(), name1);
        final ObjectName name2 = new ObjectName("test:timer=1");
        mbs.createMBean("javax.management.timer.Timer", name2);
        QueryExp exp = Query.isInstanceOf(Query.value(className));
        Set<ObjectName> list = mbs.queryNames(new ObjectName("*:*"), exp);
        if (list.contains(name1) || !list.contains(name2)) {
            throw new RuntimeException("InstanceOfExp does not work.");
        }
        for (ObjectName on : list) {
            if (!mbs.isInstanceOf(on, className)) {
                throw new RuntimeException("InstanceOfQueryExp does not work.");
            }
        }
        Set<ObjectName> all = mbs.queryNames(null, null);
        for (ObjectName n : all) {
            if (mbs.isInstanceOf(n, className) != list.contains(n))
                throw new RuntimeException("InstanceOfExp does not work.");
        }
        try {
            QueryExp exp1 = Query.isInstanceOf(null);
            throw new RuntimeException("Not got an exception with a null class name.");
        } catch (IllegalArgumentException iae) {
        }
    }
}
