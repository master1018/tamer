public class AddressableTest {
    private static final String[] protocols = {"rmi", "iiop"};
    private static final String[] prefixes = {"stub", "ior"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    public static void main(String[] args) throws Exception {
        System.out.println(">>> test the new interface Addressable.");
        boolean ok = true;
        for (int i = 0; i < protocols.length; i++) {
            try {
                test(protocols[i], prefixes[i]);
                System.out.println(">>> Test successed for "+protocols[i]);
            } catch (Exception e) {
                System.out.println(">>> Test failed for "+protocols[i]);
                e.printStackTrace(System.out);
                ok = false;
            }
        }
        if (ok) {
            System.out.println(">>> All Test passed.");
        } else {
            System.out.println(">>> Some TESTs FAILED");
            System.exit(1);
        }
    }
    public static void test(String proto, String prefix) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:" + proto + ":
        JMXConnectorServer server =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        server.start();
        JMXServiceURL serverAddr1 = server.getAddress();
        System.out.println(">>> Created a server with address "+serverAddr1);
        JMXConnector client1 = JMXConnectorFactory.connect(serverAddr1);
        JMXServiceURL clientAddr1 = ((JMXAddressable)client1).getAddress();
        System.out.println(">>> Created a client with address "+clientAddr1);
        if (!serverAddr1.equals(clientAddr1)) {
            throw new RuntimeException("The "+proto+" client does not return correct address.");
        }
        int i = clientAddr1.toString().indexOf(prefix);
        JMXServiceURL clientAddr2 =
            new JMXServiceURL("service:jmx:"+proto+":
        JMXConnector client2 = JMXConnectorFactory.connect(clientAddr2);
        System.out.println(">>> Created a client with address "+clientAddr2);
        if (!clientAddr2.equals(((JMXAddressable)client2).getAddress())) {
            throw new RuntimeException("The "+proto+" client does not return correct address.");
        }
        System.out.println(">>> The new client's host is "+clientAddr2.getHost()
                               +", port is "+clientAddr2.getPort());
        client1.close();
        client2.close();
        server.stop();
    }
}
