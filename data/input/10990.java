public class NewMBeanListenerTest {
    public static void main(String[] args) throws Exception {
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        final ObjectName delegateName =
            new ObjectName("JMImplementation:type=MBeanServerDelegate");
        final CountListener countListener = new CountListener();
        final NotificationListener addListener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
                if (!(n instanceof MBeanServerNotification)) {
                    System.out.println("Ignoring delegate notif: " +
                                       n.getClass().getName());
                    return;
                }
                MBeanServerNotification mbsn = (MBeanServerNotification) n;
                if (!(mbsn.getType()
                      .equals(MBeanServerNotification
                              .REGISTRATION_NOTIFICATION))) {
                    System.out.println("Ignoring MBeanServer notif: " +
                                       mbsn.getType());
                    return;
                }
                System.out.println("Got registration notif for " +
                                   mbsn.getMBeanName());
                try {
                    mbs.addNotificationListener(mbsn.getMBeanName(),
                                                countListener, null, null);
                } catch (Exception e) {
                    System.out.println("TEST INCORRECT: addNL failed:");
                    e.printStackTrace(System.out);
                    System.exit(1);
                }
                System.out.println("Added notif listener for " +
                                   mbsn.getMBeanName());
            }
        };
        System.out.println("Adding registration listener");
        mbs.addNotificationListener(delegateName, addListener, null, null);
        final ObjectName broadcasterName = new ObjectName(":type=Broadcaster");
        System.out.println("Creating Broadcaster MBean");
        mbs.createMBean(Broadcaster.class.getName(), broadcasterName);
        if (countListener.getCount() == 1)
            System.out.println("Got notif as expected");
        else {
            System.out.println("TEST FAILED: added listener not called");
            System.exit(1);
        }
        mbs.unregisterMBean(broadcasterName);
        Broadcaster b = new Broadcaster();
        System.out.println("Registering Broadcaster MBean");
        mbs.registerMBean(b, broadcasterName);
        if (countListener.getCount() == 2)
            System.out.println("Got notif as expected");
        else {
            System.out.println("TEST FAILED: added listener not called");
            System.exit(1);
        }
        System.out.println("Test passed");
    }
    public static interface BroadcasterMBean {}
    public static class Broadcaster
            extends NotificationBroadcasterSupport
            implements BroadcasterMBean, MBeanRegistration {
        public ObjectName preRegister(MBeanServer mbs, ObjectName name) {
            return name;
        }
        public void postRegister(Boolean registrationDone) {
            System.out.println("Broadcaster.postRegister: sending notif");
            sendNotification(new Notification("x", this, 0L));
        }
        public void preDeregister() {
        }
        public void postDeregister() {
        }
    }
    private static class CountListener implements NotificationListener {
        private int count;
        public synchronized void handleNotification(Notification n, Object h) {
            if (!n.getType().equals("x")) {
                System.out.println("TEST FAILED: received bogus notif: " + n +
                                   " (type=" + n.getType() + ")");
                System.exit(1);
            }
            count++;
        }
        public synchronized int getCount() {
            return count;
        }
    }
}
