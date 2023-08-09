public class AddRemoveTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    public static void main(String[] args) {
        System.out.println(">>> test on add/remove NotificationListener.");
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
    private static boolean test(String proto)
            throws Exception {
        System.out.println(">>> Test for protocol " + proto);
        JMXServiceURL u = new JMXServiceURL(proto, null, 0);
        JMXConnectorServer server;
        JMXServiceURL addr;
        JMXConnector client;
        MBeanServerConnection mserver;
        final ObjectName delegateName =
                    new ObjectName("JMImplementation:type=MBeanServerDelegate");
        final NotificationListener dummyListener = new NotificationListener() {
                public void handleNotification(Notification n, Object o) {
                    return;
                }
            };
        try {
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            mserver = client.getMBeanServerConnection();
            String s1 = "1";
            String s2 = "2";
            String s3 = "3";
            for (int i=0; i<3; i++) {
                mserver.addNotificationListener(delegateName, dummyListener, null, s1);
                mserver.addNotificationListener(delegateName, dummyListener, null, s2);
                mserver.addNotificationListener(delegateName, dummyListener, null, s3);
                mserver.removeNotificationListener(delegateName, dummyListener, null, s3);
                mserver.removeNotificationListener(delegateName, dummyListener, null, s2);
                mserver.removeNotificationListener(delegateName, dummyListener, null, s1);
            }
            for (int i=0; i<3; i++) {
                mserver.addNotificationListener(delegateName, dummyListener, null, s1);
                mserver.addNotificationListener(delegateName, dummyListener, null, s2);
                mserver.addNotificationListener(delegateName, dummyListener, null, s3);
                mserver.removeNotificationListener(delegateName, dummyListener);
                try {
                    mserver.removeNotificationListener(delegateName, dummyListener, null, s1);
                    System.out.println("Failed to remove a listener.");
                    return false;
                } catch (ListenerNotFoundException lne) {
                }
            }
            client.close();
            server.stop();
        } catch (MalformedURLException e) {
            System.out.println(">>> Skipping unsupported URL " + u);
            return true;
        }
        return true;
    }
}
