public class NotifDeadlockTest {
    static ObjectName on1, on2, delName;
    static {
        try {
            on1 = new ObjectName("thing:a=b");
            on2 = new ObjectName("thing:c=d");
            delName =
                new ObjectName("JMImplementation:type=MBeanServerDelegate");
        } catch (MalformedObjectNameException e) {
            throw new Error();
        }
    }
    static MBeanServer mbs;
    static boolean timedOut;
    private static class XListener implements NotificationListener {
        private boolean firstTime = true;
        private final boolean register;
        XListener(boolean register) {
            this.register = register;
        }
        public void handleNotification(Notification not, Object handback) {
            if (firstTime) {
                firstTime = false;
                Thread t = new Thread() {
                    public void run() {
                        try {
                            if (register) {
                                mbs.createMBean("javax.management.timer.Timer",
                                                on2);
                                System.out.println("Listener created " + on2);
                            } else {
                                mbs.unregisterMBean(on2);
                                System.out.println("Listener removed " + on2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                try {
                    t.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace(); 
                }
                if (t.isAlive()) {
                    System.out.println("FAILURE: Wait timed out: " +
                                       "probable deadlock");
                    timedOut = true;
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        boolean success = true;
        System.out.println("Test 1: in register notif, unregister an MBean");
        timedOut = false;
        mbs = MBeanServerFactory.createMBeanServer();
        mbs.createMBean("javax.management.timer.Timer", on2);
        mbs.addNotificationListener(delName, new XListener(false), null, null);
        mbs.createMBean("javax.management.timer.Timer", on1);
        MBeanServerFactory.releaseMBeanServer(mbs);
        if (timedOut) {
            success = false;
            Thread.sleep(500);
        }
        System.out.println("Test 1 completed");
        System.out.println("Test 2: in unregister notif, unregister an MBean");
        timedOut = false;
        mbs = MBeanServerFactory.createMBeanServer();
        mbs.createMBean("javax.management.timer.Timer", on1);
        mbs.createMBean("javax.management.timer.Timer", on2);
        mbs.addNotificationListener(delName, new XListener(false), null, null);
        mbs.unregisterMBean(on1);
        MBeanServerFactory.releaseMBeanServer(mbs);
        if (timedOut) {
            success = false;
            Thread.sleep(500);
        }
        System.out.println("Test 2 completed");
        System.out.println("Test 3: in register notif, register an MBean");
        timedOut = false;
        mbs = MBeanServerFactory.createMBeanServer();
        mbs.addNotificationListener(delName, new XListener(true), null, null);
        mbs.createMBean("javax.management.timer.Timer", on1);
        MBeanServerFactory.releaseMBeanServer(mbs);
        if (timedOut) {
            success = false;
            Thread.sleep(500);
        }
        System.out.println("Test 3 completed");
        System.out.println("Test 4: in unregister notif, register an MBean");
        timedOut = false;
        mbs = MBeanServerFactory.createMBeanServer();
        mbs.createMBean("javax.management.timer.Timer", on1);
        mbs.addNotificationListener(delName, new XListener(true), null, null);
        mbs.unregisterMBean(on1);
        MBeanServerFactory.releaseMBeanServer(mbs);
        if (timedOut) {
            success = false;
            Thread.sleep(500);
        }
        System.out.println("Test 4 completed");
        if (success)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED: at least one subcase failed");
            System.exit(1);
        }
    }
}
