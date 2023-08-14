public class UnexpectedNotifTest {
    public static void main(String[] args) throws Exception {
        List<String> protos = new ArrayList<String>();
        protos.add("rmi");
        try {
            Class.forName("javax.management.remote.jmxmp.JMXMPConnectorServer");
            protos.add("jmxmp");
        } catch (ClassNotFoundException e) {
        }
        for (String proto : protos)
            test(proto);
    }
    private static void test(String proto) throws Exception {
        System.out.println("Unexpected notifications test for protocol " +
                           proto);
        MBeanServer mbs = null;
        try {
            mbs = MBeanServerFactory.createMBeanServer();
            mbean = new ObjectName ("Default:name=NotificationEmitter");
            mbs.registerMBean(new NotificationEmitter(), mbean);
            url = new JMXServiceURL("service:jmx:" + proto + ":
            server = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                     null,
                                                                     mbs);
            mbs.registerMBean(
                        server,
                        new ObjectName("Default:name=ConnectorServer"));
            server.start();
            url = server.getAddress();
            for (int j = 0; j < 2; j++) {
                test();
            }
        } finally {
            server.stop();
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }
    private static void test() throws Exception {
        JMXConnector connector = JMXConnectorFactory.connect(url);
        MBeanServerConnection client = connector.getMBeanServerConnection();
        client.addNotificationListener(mbean, listener, null, null);
        receivedNotifs = 0;
        Object[] params = new Object[] {new Integer(nb)};
        String[] signatures = new String[] {"java.lang.Integer"};
        client.invoke(mbean, "sendNotifications", params, signatures);
        synchronized (lock) {
            for (int i = 0; i < 10; i++) {
                if (receivedNotifs < nb) {
                    lock.wait(1000);
                }
            }
        }
        Thread.sleep(3000);
        synchronized (lock) {
            if (receivedNotifs != nb) {
                throw new Exception("The client expected to receive " +
                                    nb + " notifs, but got " + receivedNotifs);
            }
        }
        client.removeNotificationListener(mbean, listener);
        connector.close();
    }
    private static class Listener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            System.out.println("Received: " + notif + " (" +
                               notif.getSequenceNumber() + ")");
            synchronized(lock) {
                if(++receivedNotifs == nb) {
                    lock.notifyAll();
                } else if (receivedNotifs > nb) {
                    System.out.println("The client expected to receive " +
                                       nb + " notifs, but got at least " +
                                       receivedNotifs);
                    System.exit(1);
                }
            }
        }
    }
    public static class NotificationEmitter
        extends NotificationBroadcasterSupport
        implements NotificationEmitterMBean {
        public MBeanNotificationInfo[] getNotificationInfo() {
            MBeanNotificationInfo[] ntfInfoArray = new MBeanNotificationInfo[1];
            String[] ntfTypes = new String[1];
            ntfTypes[0] = myType;
            ntfInfoArray[0] = new MBeanNotificationInfo(
                              ntfTypes,
                              "javax.management.Notification",
                              "Notifications sent by the NotificationEmitter");
            return ntfInfoArray;
        }
        public void sendNotifications(Integer nb) {
            System.out.println("NotificationEmitter: asked to send " +
                               "notifications: " + nb);
            Notification notif;
            for (int i = 1; i <= nb.intValue(); i++) {
                notif = new Notification(myType, this, ++seqno);
                sendNotification(notif);
            }
        }
        private String myType = "notification.my_notification";
    }
    public interface NotificationEmitterMBean {
        public void sendNotifications(Integer nb);
    }
    private static JMXConnectorServer server;
    private static JMXServiceURL url;
    private static ObjectName mbean;
    private static NotificationListener listener = new Listener();
    private static int nb = 10;
    private static int receivedNotifs = 0;
    private static int[] lock = new int[0];
    private static volatile long seqno;
}
