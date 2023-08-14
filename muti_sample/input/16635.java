public class ImmutableNotificationInfoTest {
    public interface UserBroadcasterMBean {}
    public interface NoOverrideNBSMBean {}
    public interface OverrideNBSMBean {}
    public static class UserBroadcaster
            implements UserBroadcasterMBean, NotificationBroadcaster {
        public void removeNotificationListener(NotificationListener listener) {
        }
        public void addNotificationListener(NotificationListener listener,
                NotificationFilter filter, Object handback) {
        }
        public MBeanNotificationInfo[] getNotificationInfo() {
            return new MBeanNotificationInfo[0];
        }
    }
    public static class NoOverrideNBS extends NotificationBroadcasterSupport
            implements NoOverrideNBSMBean {
    }
    public static class OverrideNBS extends NotificationBroadcasterSupport
            implements OverrideNBSMBean {
        public MBeanNotificationInfo[] getNotificationInfo() {
            return new MBeanNotificationInfo[0];
        }
    }
    public static void main(String[] args) throws Exception {
        boolean ok;
        ok = test(new UserBroadcaster(), false);
        ok &= test(new NoOverrideNBS(), true);
        ok &= test(new OverrideNBS(), false);
        if (!ok)
            throw new Exception("TEST FAILED: immutability incorrect");
    }
    private static boolean test(Object mbean, boolean expectImmutable)
            throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        mbs.registerMBean(mbean, on);
        MBeanInfo mbi = mbs.getMBeanInfo(on);
        Descriptor d = mbi.getDescriptor();
        String immutableValue = (String) d.getFieldValue("immutableInfo");
        boolean immutable = ("true".equals(immutableValue));
        if (immutable != expectImmutable) {
            System.out.println("FAILED: " + mbean.getClass().getName() +
                    " -> " + immutableValue);
            return false;
        } else {
            System.out.println("OK: " + mbean.getClass().getName());
            return true;
        }
    }
}
