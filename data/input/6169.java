public class MBeanServerNotificationTest {
    final static String[] names = {
        ":type=Wombat", "wombat:type=Wombat",null,
    };
    public static void main(String[] args) throws Exception {
        System.out.println("Test that MBeanServerNotification.toString " +
                "contains the name of the MBean being registered " +
                "or unregistered.");
        int failures = 0;
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        for (String str:names) {
            try {
                final ObjectName name = (str==null)?null:new ObjectName(str);
                failures+=test(mbs, name, name!=null);
            } catch(Exception x) {
                x.printStackTrace(System.out);
                System.out.println("Test failed for: "+str);
                failures++;
            }
        }
        if (failures == 0)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED: " + failures + " failure(s)");
            System.exit(1);
        }
    }
    private static enum Registration {
        REGISTER(MBeanServerNotification.REGISTRATION_NOTIFICATION),
        UNREGISTER(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        final String type;
        private Registration(String type) {this.type = type;}
        public int test(MBeanServerNotification n, ObjectName name) {
            int failures = 0;
            System.out.println("Testing: "+n);
            if (!n.toString().endsWith("[type="+type+
                "][message="+n.getMessage()+
                "][mbeanName="+name+"]")) {
                System.err.println("Test failed for "+ type+
                   " ["+name+"]: "+n);
                failures++;
            }
            return failures;
        }
        public MBeanServerNotification create(ObjectName name) {
            return new MBeanServerNotification(type,
                MBeanServerDelegate.DELEGATE_NAME, next(), name);
        }
        private static long next = 0;
        private static synchronized long next() {return next++;}
    }
    private static int test(MBeanServer mbs, ObjectName name,
                            boolean register)
            throws Exception {
        System.out.println("--------" + name + "--------");
        int failures = 0;
        for (Registration reg : Registration.values()) {
            failures = reg.test(reg.create(name), name);
        }
        if (!register) return failures;
        final ArrayBlockingQueue<Notification> queue =
                new ArrayBlockingQueue<Notification>(10);
        final NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification,
                    Object handback) {
                try {
                    queue.put(notification);
                } catch(Exception x) {
                    x.printStackTrace(System.out);
                }
            }
        };
        mbs.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                listener, null, name);
        final ObjectInstance oi = mbs.registerMBean(new Wombat(), name);
        try {
            failures+=Registration.REGISTER.test((MBeanServerNotification)
                queue.poll(2, TimeUnit.SECONDS), oi.getObjectName());
        } finally {
            mbs.unregisterMBean(oi.getObjectName());
            failures+=Registration.UNREGISTER.test((MBeanServerNotification)
                queue.poll(2, TimeUnit.SECONDS), oi.getObjectName());
        }
        return failures;
    }
    public static interface WombatMBean {}
    public static class Wombat implements WombatMBean {}
}
