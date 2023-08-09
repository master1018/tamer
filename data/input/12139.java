public class CloseServerTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    public static void main(String[] args) {
        System.out.println(">>> Tests for closing a server.");
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
            System.out.println(">>> Open and close a server.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.stop();
            System.out.println(">>> Open, start and close a server.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            server.stop();
            System.out.println(">>> Open, start a server, create a client, close the server then the client.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            server.stop();
            try {
                client.close();
            } catch (Exception ee) {
            }
            System.out.println(">>> Open, start a server, create a client, close the client then server.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            client.close();
            server.stop();
            System.out.println(">>> Open, start a server, create a client, add a listener, close the server then the client.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            mserver = client.getMBeanServerConnection();
            mserver.addNotificationListener(delegateName, dummyListener, null, null);
            server.stop();
            try {
                client.close();
            } catch (Exception e) {
            }
            System.out.println(">>> Open, start a server, create a client, add a listener, close the client then the server.");
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            mserver = client.getMBeanServerConnection();
            mserver.addNotificationListener(delegateName, dummyListener, null, null);
            client.close();
            server.stop();
        } catch (MalformedURLException e) {
            System.out.println(">>> Skipping unsupported URL " + u);
            return true;
        }
        return true;
    }
}
