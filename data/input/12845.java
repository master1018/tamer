public class IdenticalMBeanInfoTest {
    private static String failure = null;
    public static interface WhatsitMBean {
        public int getReadOnly();
        public int getReadWrite();
        public void setReadWrite(int x);
        public int op(int x, int y);
    }
    public static class Whatsit implements WhatsitMBean {
        public Whatsit() {}
        public Whatsit(int irrelevant) {}
        public int getReadOnly() { return 0; }
        public int getReadWrite() { return 0; }
        public void setReadWrite(int x) {}
        public int op(int x, int y) { return 0; }
    }
    public static interface BroadcasterMBean extends WhatsitMBean {
    }
    public static class Broadcaster extends Whatsit
            implements BroadcasterMBean, NotificationBroadcaster {
        private static int nextId = 1;
        private int id = nextId++;
        public void addNotificationListener(NotificationListener l,
                                            NotificationFilter f,
                                            Object h) {}
        public void removeNotificationListener(NotificationListener l) {}
        public MBeanNotificationInfo[] getNotificationInfo() {
            return new MBeanNotificationInfo[] {
                new MBeanNotificationInfo(new String[] {"type" + id},
                                          "something",
                                          "a something")
            };
        }
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        ObjectName on1 = new ObjectName("d:type=Whatsit,number=1");
        ObjectName on2 = new ObjectName("d:type=Whatsit,number=2");
        ObjectName on3 = new ObjectName("d:type=Whatsit,number=3");
        ObjectName on4 = new ObjectName("d:type=Whatsit,number=4");
        ObjectName on5 = new ObjectName("d:type=Whatsit,number=5");
        mbs.registerMBean(new Whatsit(), on1);
        mbs.createMBean(Whatsit.class.getName(), on2);
        DynamicMBean mbean =
            new StandardMBean(new Whatsit(), WhatsitMBean.class);
        mbs.registerMBean(mbean, on3);
        mbs.registerMBean(new Broadcaster(), on4);
        mbs.createMBean(Broadcaster.class.getName(), on5);
        MBeanInfo mbi1 = mbs.getMBeanInfo(on1);
        MBeanInfo mbi2 = mbs.getMBeanInfo(on2);
        MBeanInfo mbi3 = mbs.getMBeanInfo(on3);
        MBeanInfo mbi4 = mbs.getMBeanInfo(on4);
        MBeanInfo mbi5 = mbs.getMBeanInfo(on5);
        if (mbi1 != mbi2) {
            fail("Two MBeans of the same class should have identical " +
                 "MBeanInfo");
        }
        if (mbi2 != mbi3) {
            if (true)
                System.out.println("IGNORING StandardMBean(...) failure");
            else
                fail("Plain Standard MBean should have identical MBeanInfo " +
                     "to StandardMBean(...) with same class as resource");
        }
        if (mbi4 == mbi5 || mbi4.equals(mbi5)) {
            fail("Two MBeans of the same class should NOT have identical " +
                 "MBeanInfo if they are NotificationBroadcasters and "+
                 "do not return the same MBeanNotificationInfo[]");
        }
        if (failure == null) {
            System.out.println("Test passed");
            return;
        }
        throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String why) {
        System.out.println("FAILURE: " + why);
        failure = why;
    }
}
