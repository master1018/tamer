class TimeChecker extends Thread {
    @Override
    public void run() {
        System.out.println("shutdown hook called");
        long elapsedTime =
            System.currentTimeMillis() - RMIExitTest.exitStartTime;
        if(elapsedTime >= 5000) {
            System.out.println("BUG 4917237 not Fixed.");
            Runtime.getRuntime().halt(1);
        } else {
            System.out.println("BUG 4917237 Fixed");
        }
    }
}
public class RMIExitTest {
    private static final MBeanServer mbs =
        MBeanServerFactory.createMBeanServer();
    public static long exitStartTime = 0;
    public static void main(String[] args) {
        System.out.println("Start test");
        Runtime.getRuntime().addShutdownHook(new TimeChecker());
        test();
        exitStartTime = System.currentTimeMillis();
        System.out.println("End test");
    }
    private static void test() {
        try {
            JMXServiceURL u = new JMXServiceURL("rmi", null, 0);
            JMXConnectorServer server;
            JMXServiceURL addr;
            JMXConnector client;
            MBeanServerConnection mserver;
            final ObjectName delegateName =
                new ObjectName("JMImplementation:type=MBeanServerDelegate");
            final NotificationListener dummyListener =
                new NotificationListener() {
                        public void handleNotification(Notification n,
                                                       Object o) {
                            return;
                        }
                    };
            server = JMXConnectorServerFactory.newJMXConnectorServer(u,
                                                                     null,
                                                                     mbs);
            server.start();
            addr = server.getAddress();
            client = JMXConnectorFactory.newJMXConnector(addr, null);
            client.connect(null);
            mserver = client.getMBeanServerConnection();
            String s1 = "1";
            String s2 = "2";
            String s3 = "3";
            mserver.addNotificationListener(delegateName,
                                            dummyListener, null, s1);
            mserver.addNotificationListener(delegateName,
                                            dummyListener, null, s2);
            mserver.addNotificationListener(delegateName,
                                            dummyListener, null, s3);
            mserver.removeNotificationListener(delegateName,
                                               dummyListener, null, s3);
            mserver.removeNotificationListener(delegateName,
                                               dummyListener, null, s2);
            mserver.removeNotificationListener(delegateName,
                                               dummyListener, null, s1);
            client.close();
            server.stop();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
