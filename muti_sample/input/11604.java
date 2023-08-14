public class CloseUnconnectedTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    public static void main(String[] args) {
        System.out.println("Test that a connection can be opened and " +
                           "immediately closed without any operations");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        boolean ok = true;
        for (int i = 0; i < protocols.length; i++) {
            try {
                if (!test(protocols[i], mbs)) {
                    System.out.println("Test failed for " + protocols[i]);
                    ok = false;
                } else {
                    System.out.println("Test successed for " + protocols[i]);
                }
            } catch (Exception e) {
                System.out.println("Test failed for " + protocols[i]);
                e.printStackTrace(System.out);
                ok = false;
            }
        }
        if (ok) {
            System.out.println("Test passed");
            return;
        } else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto, MBeanServer mbs)
            throws Exception {
        System.out.println("Test immediate client close for protocol " +
                           proto);
        JMXServiceURL u = new JMXServiceURL(proto, null, 0);
        JMXConnectorServer s;
        try {
            s = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
        } catch (MalformedURLException e) {
            System.out.println("Skipping unsupported URL " + u);
            return true;
        }
        s.start();
        JMXServiceURL a = s.getAddress();
        JMXConnector c = JMXConnectorFactory.newJMXConnector(a, null);
        c.close();
        s.stop();
        return true;
    }
}
