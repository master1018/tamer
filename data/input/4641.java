public class MultiOpenCloseTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    public static void main(String[] args) {
        System.out.println("Open, connect then close multi-connectors.");
        boolean ok = true;
        for (int i = 0; i < protocols.length; i++) {
            try {
                if (!test(protocols[i])) {
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
    private static boolean test(String proto)
            throws Exception {
        System.out.println("Test for protocol " + proto);
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
        final int MAX_ITERS = 10;
        System.out.println("Looping for " + MAX_ITERS + "iterations...");
        for (int i=0; i<MAX_ITERS; i++) {
            JMXConnector c = JMXConnectorFactory.newJMXConnector(a, null);
            c.connect(null);
            c.close();
        }
        JMXConnector[] cs = new JMXConnector[MAX_ITERS];
        for (int i=0; i<MAX_ITERS; i++) {
            cs[i] = JMXConnectorFactory.newJMXConnector(a, null);
            cs[i].connect(null);
        }
        for (int i=0; i<MAX_ITERS; i++) {
            cs[i].close();
        }
        try {
            Thread.sleep(100);
        } catch (Exception ee) {
        }
        for (int i=0; i<MAX_ITERS; i++) {
            try {
                cs[i].getMBeanServerConnection(null);
                System.out.println("Did not get an IOException as expected, failed to close a client.");
                return false;
            } catch (IOException ioe) {
            }
        }
        s.stop();
        return true;
    }
}
