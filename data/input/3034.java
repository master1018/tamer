public class CloseFailedClientTest {
    private static final int port = 999;
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    public static void main(String[] args) throws Exception {
        System.out.println("Test to close a failed client.");
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
        JMXServiceURL url = new JMXServiceURL(proto, null, port);
        JMXConnector connector;
        JMXConnectorServer server;
        for (int i=0; i<20; i++) {
            try {
                connector = JMXConnectorFactory.newJMXConnector(url, null);
            } catch (MalformedURLException e) {
                System.out.println("Skipping unsupported URL " + url);
                return true;
            }
            try {
                connector.connect();
                throw new RuntimeException("Do not get expected IOEeption.");
            } catch (IOException e) {
            }
            connector.close();
            try {
                server = JMXConnectorServerFactory.newJMXConnectorServer(url, null, null);
            } catch (MalformedURLException e) {
                System.out.println("Skipping unsupported URL " + url);
                return true;
            }
            connector = JMXConnectorFactory.newJMXConnector(url, null);
            try {
                connector.connect();
                throw new RuntimeException("Do not get expected IOEeption.");
            } catch (IOException e) {
            }
            connector.close();
        }
        return true;
    }
}
