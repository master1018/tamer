public class GetConnectionTest {
    public static void main(String[] args) {
        System.out.println("Verify whether getting an IOException when "+
             "calling getMBeanServerConnection to a unconnected connector.");
        boolean ok = true;
        String[] protocols = {"rmi", "iiop", "jmxmp"};
        for (int i = 0; i < protocols.length; i++) {
            final String proto = protocols[i];
            System.out.println("Testing for protocol " + proto);
            try {
                ok &= test(proto);
            } catch (Exception e) {
                System.err.println("Unexpected exception: " + e);
                e.printStackTrace();
                ok = false;
            }
        }
        if (ok)
            System.out.println("All Tests passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto) throws Exception {
        JMXConnector client;
        try {
            JMXServiceURL url = new JMXServiceURL(proto, null, 0);
            client = JMXConnectorFactory.newJMXConnector(url, null);
        } catch (MalformedURLException e) {
            System.out.println("Protocol " + proto +
                               " not supported, ignoring");
            return true;
        }
        try {
            MBeanServerConnection connection =
                client.getMBeanServerConnection();
            System.out.println("FAILED: Expected IOException is not thrown.");
            return false;
        } catch (IOException e) {
            System.out.println("PASSED");
            return true;
        }
    }
}
