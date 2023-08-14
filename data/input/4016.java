public class RMIConnectionIdTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that RMI connection id includes " +
                           "client host name");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        String connectionId = cc.getConnectionId();
        System.out.println("Got connection id: " + connectionId);
        if (!connectionId.startsWith("rmi:
            System.out.println("TEST FAILED: does not begin with \"rmi:
            System.exit(1);
        }
        String rest = connectionId.substring("rmi:
        int spaceIndex = rest.indexOf(' ');
        if (spaceIndex < 0) {
            System.out.println("TEST FAILED: no space");
            System.exit(1);
        }
        String clientAddr = rest.substring(0, spaceIndex);
        InetAddress clientInetAddr = InetAddress.getByName(clientAddr);
        InetAddress localAddr = InetAddress.getLocalHost();
        System.out.println("InetAddresses: local=" + localAddr + "; " +
                           "connectionId=" + clientInetAddr);
        if (!localAddr.equals(clientInetAddr)) {
            System.out.println("TEST FAILS: addresses differ");
            System.exit(1);
        }
        cc.close();
        cs.stop();
        System.out.println("Test passed");
    }
}
