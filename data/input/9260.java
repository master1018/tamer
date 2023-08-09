public class FailedConnectionTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    public static void main(String[] args) {
        System.out.println(">>> test to get an IOException when calling"+
                          " getConnectionID on a closed connection.");
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
        JMXServiceURL u = null;
        JMXConnectorServer server = null;
        try {
            u = new JMXServiceURL(proto, null, 0);
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
        } catch (MalformedURLException e) {
            System.out.println("Skipping unsupported URL " + proto);
            return true;
        }
        server.start();
        JMXServiceURL addr = server.getAddress();
        HashMap env = new HashMap(1);
        env.put("jmx.remote.x.client.connection.check.period", "0");
        JMXConnector client = JMXConnectorFactory.connect(addr, env);
        server.stop();
        Thread.sleep(1000);
        try {
            client.getConnectionId();
            System.out.println("Do not get expected IOException, failed.");
            return false;
        } catch (IOException ioe) {
            return true;
        }
    }
}
