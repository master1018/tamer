public class RMISocketFactoriesTest {
    public static void main(String[] args) {
        System.out.println("Test RMI factories : " + args[0]);
        try {
            System.out.println("Start RMI registry...");
            Registry reg = null;
            int port = 5800;
            while (port++ < 6000) {
                try {
                    reg = LocateRegistry.createRegistry(port);
                    System.out.println("RMI registry running on port " + port);
                    break;
                } catch (RemoteException e) {
                    System.out.println("Failed to create RMI registry " +
                                       "on port " + port);
                }
            }
            if (reg == null) {
                System.exit(1);
            }
            System.out.println("Create the MBean server");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            System.out.println("Initialize environment map");
            HashMap env = new HashMap();
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                    new RMIServerFactory(args[0]));
            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                    new RMIClientFactory(args[0]));
            System.out.println("Create an RMI connector server");
            JMXServiceURL url =
                new JMXServiceURL("rmi", null, 0,
                                  "/jndi/rmi:
            JMXConnectorServer rcs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            rcs.start();
            System.out.println("Create an RMI connector client");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, new HashMap());
            System.exit(1);
        } catch (Exception e) {
            if (e.getMessage().equals(args[0])) {
                System.out.println("Expected exception caught = " + e);
                System.out.println("Bye! Bye!");
            } else {
                System.out.println("Unexpected exception caught = " + e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
