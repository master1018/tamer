public class NotSerializableNotifTest {
    private static MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
    private static ObjectName emitter;
    private static int port = 2468;
    private static String[] protocols;
    private static final int sentNotifs = 10;
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Test to send a not serializable notification");
        final String v = System.getProperty("java.version");
        float f = Float.parseFloat(v.substring(0, 3));
        if (f<1.5) {
            protocols = new String[] {"rmi", "jmxmp"};
        } else {
            protocols = new String[] {"rmi", "iiop", "jmxmp"};
        }
        emitter = new ObjectName("Default:name=NotificationEmitter");
        mbeanServer.registerMBean(new NotificationEmitter(), emitter);
        boolean ok = true;
        for (int i = 0; i < protocols.length; i++) {
            try {
                if (!test(protocols[i])) {
                    System.out.println(">>> Test failed for " + protocols[i]);
                    ok = false;
                } else {
                    System.out.println(">>> Test successed for " + protocols[i]);
                }
            } catch (Exception e) {
                System.out.println(">>> Test failed for " + protocols[i]);
                e.printStackTrace(System.out);
                ok = false;
            }
        }
        if (ok) {
            System.out.println(">>> Test passed");
        } else {
            System.out.println(">>> TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto) throws Exception {
        System.out.println("\n>>> Test for protocol " + proto);
        JMXServiceURL url = new JMXServiceURL(proto, null, port++);
        System.out.println(">>> Create a server: "+url);
        JMXConnectorServer server = null;
        try {
            server = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
        } catch (MalformedURLException e) {
            System.out.println("System does not recognize URL: " + url +
                               "; ignoring");
            return true;
        }
        server.start();
        url = server.getAddress();
        System.out.println(">>> Creating a client connectint to: "+url);
        JMXConnector conn = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection client = conn.getMBeanServerConnection();
        Listener listener = new Listener();
        client.addNotificationListener(emitter, listener, null, null);
        Object[] params = new Object[] {new Integer(1)};
        String[] signatures = new String[] {"java.lang.Integer"};
        client.invoke(emitter, "sendNotserializableNotifs", params, signatures);
        client.removeNotificationListener(emitter, listener);
        listener = new Listener();
        client.addNotificationListener(emitter, listener, null, null);
        params = new Object[] {new Integer(sentNotifs)};
        client.invoke(emitter, "sendNotifications", params, signatures);
        synchronized (listener) {
            for (int i=0; i<10; i++) {
                if (listener.received() < sentNotifs) {
                    listener.wait(1000);
                } else {
                    break;
                }
            }
        }
        boolean ok = true;
        if (listener.received() != sentNotifs) {
           System.out.println("Failed: received "+listener.received()+
                                   " but should be "+sentNotifs);
           ok = false;
        } else {
           System.out.println("The client received all notifications.");
        }
        client.removeNotificationListener(emitter, listener);
        conn.close();
        server.stop();
        return ok;
    }
    private static class Listener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            synchronized (this) {
                if(++receivedNotifs == sentNotifs) {
                    this.notifyAll();
                }
            }
        }
        public int received() {
            return receivedNotifs;
        }
        private int receivedNotifs = 0;
    }
    public static class NotificationEmitter extends NotificationBroadcasterSupport
        implements NotificationEmitterMBean {
        public MBeanNotificationInfo[] getNotificationInfo() {
            final String[] ntfTypes = {myType};
            final MBeanNotificationInfo[] ntfInfoArray  = {
                new MBeanNotificationInfo(ntfTypes,
                                          "javax.management.Notification",
                                          "Notifications sent by the NotificationEmitter")};
            return ntfInfoArray;
        }
        public void sendNotserializableNotifs(Integer nb) {
            Notification notif;
            for (int i=1; i<=nb.intValue(); i++) {
                notif = new Notification(myType, this, i);
                notif.setUserData(new Object());
                sendNotification(notif);
            }
        }
        public void sendNotifications(Integer nb) {
            Notification notif;
            for (int i=1; i<=nb.intValue(); i++) {
                notif = new Notification(myType, this, i);
                sendNotification(notif);
            }
        }
        private final String myType = "notification.my_notification";
    }
    public interface NotificationEmitterMBean {
        public void sendNotifications(Integer nb);
        public void sendNotserializableNotifs(Integer nb);
    }
}
