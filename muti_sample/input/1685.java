public class ListenerScaleTest {
    private static final int WARMUP_WITH_ONE_MBEAN = 1000;
    private static final int NOTIFS_TO_TIME = 100;
    private static final int EXTRA_MBEANS = 20000;
    private static final ObjectName testObjectName;
    static {
        try {
            testObjectName = new ObjectName("test:type=Sender,number=-1");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
    private static volatile int nnotifs;
    private static volatile long startTime;
    private static volatile long elapsed;
    private static final Semaphore sema = new Semaphore(0);
    private static final NotificationListener timingListener =
        new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
                if (++nnotifs == NOTIFS_TO_TIME) {
                    elapsed = System.nanoTime() - startTime;
                    sema.release();
                }
            }
        };
    private static final long timeNotif(MBeanServer mbs) {
        try {
            startTime = System.nanoTime();
            nnotifs = 0;
            mbs.invoke(testObjectName, "send", null, null);
            sema.acquire();
            return elapsed;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static interface SenderMBean {
        public void send();
    }
    public static class Sender extends NotificationBroadcasterSupport
            implements SenderMBean {
        public void send() {
            for (int i = 0; i < NOTIFS_TO_TIME; i++)
                sendNotification(new Notification("type", this, 1L));
        }
    }
    private static final NotificationListener nullListener =
        new NotificationListener() {
            public void handleNotification(Notification n, Object h) {}
        };
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        Sender sender = new Sender();
        mbs.registerMBean(sender, testObjectName);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        try {
            test(mbs, cs, cc);
        } finally {
            cc.close();
            cs.stop();
        }
    }
    private static void test(MBeanServer mbs, JMXConnectorServer cs,
                             JMXConnector cc) throws Exception {
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        mbsc.addNotificationListener(testObjectName, timingListener, null, null);
        long singleMBeanTime = 0;
        for (int i = 0; i < WARMUP_WITH_ONE_MBEAN; i++)
            singleMBeanTime = timeNotif(mbs);
        if (singleMBeanTime == 0)
            singleMBeanTime = 1;
        System.out.println("Time with a single MBean: " + singleMBeanTime + "ns");
        System.out.println("Now registering " + EXTRA_MBEANS + " MBeans");
        for (int i = 0; i < EXTRA_MBEANS; i++) {
            ObjectName on = new ObjectName("test:type=Sender,number=" + i);
            mbs.registerMBean(new Sender(), on);
            if (i % 1000 == 999) {
                System.out.print("..." + (i+1));
                System.out.flush();
            }
        }
        System.out.println();
        System.out.println("Now registering " + EXTRA_MBEANS + " listeners");
        for (int i = 0; i < EXTRA_MBEANS; i++) {
            ObjectName on = new ObjectName("test:type=Sender,number=" + i);
            mbsc.addNotificationListener(on, nullListener, null, null);
            if (i % 1000 == 999) {
                System.out.print("..." + (i+1));
                System.out.flush();
            }
        }
        System.out.println();
        System.out.println("Timing a notification send now");
        long manyMBeansTime = timeNotif(mbs);
        System.out.println("Time with many MBeans: " + manyMBeansTime + "ns");
        double ratio = (double) manyMBeansTime / singleMBeanTime;
        if (ratio > 100.0)
            throw new Exception("Failed: ratio=" + ratio);
        System.out.println("Test passed: ratio=" + ratio);
    }
}
