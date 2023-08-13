public class NotificationBufferCreationTest {
    private static final MBeanServer mbs =
        MBeanServerFactory.createMBeanServer();
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    public static void main(String[] args) {
        try {
            boolean error = false;
            ObjectName notifierName =
                new ObjectName("TestDomain:type=NotificationSender");
            NotificationSender s = new NotificationSender();
            mbs.registerMBean(s, notifierName);
            for(int i = 0; i < protocols.length; i++) {
                try {
                    System.out.println("dotest for " + protocols[i]);
                    dotest(protocols[i], s, notifierName);
                }catch(Exception e) {
                    e.printStackTrace();
                    error = true;
                }
            }
            if(error)
                System.exit(1);
            System.out.println("Test OK");
        }catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void dotest(String protocol,
                               NotificationSender s,
                               ObjectName notifierName) throws Exception {
        JMXConnector client = null;
        JMXConnectorServer server = null;
        JMXServiceURL u = null;
        try {
            u = new JMXServiceURL(protocol, null, 0);
            server =
                JMXConnectorServerFactory.newJMXConnectorServer(u,
                                                                null,
                                                                mbs);
            checkNotifier(s, 0, "new ConnectorServer");
            server.start();
            checkNotifier(s, 0, "ConnectorServer start");
            JMXServiceURL addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            checkNotifier(s, 0, "new Connector");
            client.connect(null);
            checkNotifier(s, 0, "Connector connect");
            MBeanServerConnection mbsc = client.getMBeanServerConnection();
            final NotificationListener dummyListener =
                new NotificationListener() {
                        public void handleNotification(Notification n,
                                                       Object o) {
                            return;
                        }
                    };
            mbsc.addNotificationListener(notifierName,
                                         dummyListener,
                                         null,
                                         null);
            checkNotifier(s, 1, "addNotificationListener");
            mbsc.removeNotificationListener(notifierName,
                                            dummyListener);
            System.out.println("Test OK for " + protocol);
        }catch(MalformedURLException e) {
            System.out.println("Skipping URL " + u);
        }
        finally {
            if(client != null)
                client.close();
            if(server != null)
                server.stop();
        }
    }
    private static void checkNotifier(NotificationSender s,
                                      int expectedListenerCount,
                                      String msg) throws Exception {
        int count = s.getListenerCount();
        if(count != expectedListenerCount) {
            String errorMsg = "Invalid expected listener count [" +
                expectedListenerCount + "], real [" +  count +"] for " + msg;
            System.out.println(errorMsg);
            throw new Exception(errorMsg);
        }
    }
}
